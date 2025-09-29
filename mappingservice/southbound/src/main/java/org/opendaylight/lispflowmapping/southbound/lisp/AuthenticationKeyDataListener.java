/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.map.register.cache.metadata.EidLispAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.metadata.container.map.register.cache.metadata.EidLispAddressKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all AuthenticationKey modification events.
 *
 */
public class AuthenticationKeyDataListener implements DataTreeChangeListener<AuthenticationKey> {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationKeyDataListener.class);

    private final AuthKeyDb akdb;
    private final InstanceIdentifier<AuthenticationKey> path;
    private final Registration registration;
    private final ConcurrentHashMap<Eid, Long> updatedEntries;

    public AuthenticationKeyDataListener(final DataBroker broker, final AuthKeyDb akdb) {
        this.akdb = akdb;
        path = InstanceIdentifier.create(MappingDatabase.class).child(VirtualNetworkIdentifier.class)
                .child(AuthenticationKey.class);
        LOG.trace("Registering AuthenticationKey listener.");
        registration = broker.registerTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, path), this);
        updatedEntries = new ConcurrentHashMap<>();
    }

    public void closeDataChangeListener() {
        registration.close();
    }

    @Override
    public synchronized void onDataTreeChanged(final List<DataTreeModification<AuthenticationKey>> changes) {
        for (DataTreeModification<AuthenticationKey> change : changes) {
            final DataObjectModification<AuthenticationKey> mod = change.getRootNode();

            switch (mod.modificationType()) {
                case DELETE -> {
                    final AuthenticationKey authKey = mod.dataBefore();

                    LOG.trace("Received deleted data");
                    LOG.trace("Key: {}", change.getRootPath().path());
                    LOG.trace("Value: {}", authKey);

                    final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                    akdb.removeAuthenticationKey(convertedAuthKey.getEid());
                    updatedEntries.put(convertedAuthKey.getEid(), System.currentTimeMillis());
                }
                case SUBTREE_MODIFIED, WRITE -> {
                    // Process newly created or updated authentication keys
                    final AuthenticationKey authKey = mod.dataAfter();

                    LOG.trace("Key: {}", change.getRootPath().path());
                    LOG.trace("Value: {}", authKey);

                    final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                    akdb.addAuthenticationKey(convertedAuthKey.getEid(), convertedAuthKey.getMappingAuthkey());
                    updatedEntries.put(convertedAuthKey.getEid(), System.currentTimeMillis());
                }
                default -> LOG.warn("Ignoring unhandled modification type {}", mod.modificationType());
            }
        }
    }

    /**
     * We maintain a HashMap with the update times of AuthenticationKey objects in the updatedEntries field. We keep
     * entries in the HashMap for the Map-Register cache timeout interval, and lazy remove them afterwards. As a result
     * the same EID will be considered updated during that interval, even on subsequent queries. This is necessary
     * because more than one xTR may register the same EID, and to avoid complexity we don't store origin information.
     * The performance trade-off is not significant, because during a typical cache timeout the same xTR will send only
     * a few registration packets (2 for the default value of 90s, when UDP Map-Registers are sent at 1 minute
     * intervals).
     *
     * @param eids List of EIDs to check
     * @param timeout MapRegister cache timeout value
     * @return false if any of the EIDs in the eids list was updated in the last timout period, true otherwise
     */
    public synchronized boolean authKeysForEidsUnchanged(final Map<EidLispAddressKey, EidLispAddress> eids,
            final long timeout) {
        boolean result = true;
        Long currentTime = System.currentTimeMillis();
        for (EidLispAddress eidLispAddress : eids.values()) {
            Long updateTime = updatedEntries.get(eidLispAddress.getEid());
            if (updateTime != null) {
                result = false;
                if (currentTime - updateTime > timeout) {
                    updatedEntries.remove(eidLispAddress.getEid());
                }
            }
        }
        return result;
    }

    private static AuthenticationKey convertToBinaryIfNecessary(final AuthenticationKey authKey) {
        Eid originalEid = authKey.getEid();
        if (LispAddressUtil.addressNeedsConversionToBinary(originalEid.getAddress())) {
            AuthenticationKeyBuilder akb = new AuthenticationKeyBuilder(authKey);
            akb.setEid(LispAddressUtil.convertToBinary(originalEid));
            return akb.build();
        }
        return authKey;
    }

}
