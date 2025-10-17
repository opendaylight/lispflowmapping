/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.List;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModification.WithDataAfter;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all AuthenticationKey modification events.
 *
 * @author Lorand Jakab
 *
 */
public class AuthenticationKeyDataListener extends AbstractDataListener<AuthenticationKey> {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationKeyDataListener.class);
    private IMappingSystem mapSystem;

    public AuthenticationKeyDataListener(DataBroker broker, IMappingSystem mapSystem) {
        setBroker(broker);
        setMappingSystem(mapSystem);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(VirtualNetworkIdentifier.class)
                .child(AuthenticationKey.class));
        LOG.trace("Registering AuthenticationKey listener.");
        registerDataChangeListener();
    }

    @Override
    public void onDataTreeChanged(List<DataTreeModification<AuthenticationKey>> changes) {
        for (var change : changes) {
            switch (change.getRootNode()) {
                case DataObjectDeleted<AuthenticationKey> deleted -> {
                    final AuthenticationKey authKey = deleted.dataBefore();

                    LOG.trace("Received deleted data");
                    LOG.trace("Key: {}", change.path());
                    LOG.trace("Value: {}", authKey);

                    final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                    mapSystem.removeAuthenticationKey(convertedAuthKey.getEid());
                }
                case WithDataAfter<AuthenticationKey> present -> {
                    // Process newly created or updated authentication keys
                    final AuthenticationKey authKey = present.dataAfter();

                    LOG.trace("Key: {}", change.path());
                    LOG.trace("Value: {}", authKey);

                    final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                    mapSystem.addAuthenticationKey(convertedAuthKey.getEid(), convertedAuthKey.getMappingAuthkey());
                }
            }
        }
    }

    private static AuthenticationKey convertToBinaryIfNecessary(AuthenticationKey authKey) {
        Eid originalEid = authKey.getEid();
        if (LispAddressUtil.addressNeedsConversionToBinary(originalEid.getAddress())) {
            return new AuthenticationKeyBuilder(authKey)
                .setEid(LispAddressUtil.convertToBinary(originalEid))
                .build();
        }
        return authKey;
    }

    void setMappingSystem(IMappingSystem msmr) {
        mapSystem = msmr;
    }
}
