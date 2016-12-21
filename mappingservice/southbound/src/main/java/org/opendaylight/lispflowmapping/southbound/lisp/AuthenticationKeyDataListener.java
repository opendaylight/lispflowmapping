/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.mapcache.AuthKeyDb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all AuthenticationKey modification events.
 *
 */
public class AuthenticationKeyDataListener implements ClusteredDataTreeChangeListener<AuthenticationKey> {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationKeyDataListener.class);

    private final AuthKeyDb akdb;
    private final DataBroker broker;
    private final InstanceIdentifier<AuthenticationKey> path;
    private ListenerRegistration<ClusteredDataTreeChangeListener<AuthenticationKey>> registration;
    private boolean authKeyRefreshing = false;
    private long authKeyRefreshingDate;


    public AuthenticationKeyDataListener(final DataBroker broker, final AuthKeyDb akdb) {
        this.broker = broker;
        this.akdb = akdb;
        this.path = InstanceIdentifier.create(MappingDatabase.class).child(VirtualNetworkIdentifier.class)
                .child(AuthenticationKey.class);
        LOG.trace("Registering AuthenticationKey listener.");
        final DataTreeIdentifier<AuthenticationKey> dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, path);
        registration = broker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    public void closeDataChangeListener() {
        registration.close();
    }

    public boolean isAuthKeyRefreshing() {
        return authKeyRefreshing;
    }

    public void setAuthKeyRefreshing(boolean authKeyRefreshing) {
        this.authKeyRefreshing = authKeyRefreshing;
    }

    public long getAuthKeyRefreshingDate() {
        return authKeyRefreshingDate;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<AuthenticationKey>> changes) {
        authKeyRefreshing = true;
        authKeyRefreshingDate = System.currentTimeMillis();
        for (DataTreeModification<AuthenticationKey> change : changes) {
            final DataObjectModification<AuthenticationKey> mod = change.getRootNode();

            if (ModificationType.DELETE == mod.getModificationType()) {
                final AuthenticationKey authKey = mod.getDataBefore();

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", authKey);

                final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                akdb.removeAuthenticationKey(convertedAuthKey.getEid());
            } else if (ModificationType.WRITE == mod.getModificationType() || ModificationType.SUBTREE_MODIFIED == mod
                    .getModificationType()) {
                if (ModificationType.WRITE == mod.getModificationType()) {
                    LOG.trace("Received created data");
                } else {
                    LOG.trace("Received updated data");
                }
                // Process newly created or updated authentication keys
                final AuthenticationKey authKey = mod.getDataAfter();

                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", authKey);

                final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                akdb.addAuthenticationKey(convertedAuthKey.getEid(), convertedAuthKey.getMappingAuthkey());
            } else {
                LOG.warn("Ignoring unhandled modification type {}", mod.getModificationType());
            }
        }
    }

    private static AuthenticationKey convertToBinaryIfNecessary(AuthenticationKey authKey) {
        Eid originalEid = authKey.getEid();
        if (LispAddressUtil.addressNeedsConversionToBinary(originalEid.getAddress())) {
            AuthenticationKeyBuilder akb = new AuthenticationKeyBuilder(authKey);
            akb.setEid(LispAddressUtil.convertToBinary(originalEid));
            return akb.build();
        }
        return authKey;
    }

}
