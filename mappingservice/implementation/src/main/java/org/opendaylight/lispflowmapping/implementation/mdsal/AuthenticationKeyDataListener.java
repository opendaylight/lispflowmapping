/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.Collection;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;

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
    public void onDataTreeChanged(Collection<DataTreeModification<AuthenticationKey>> changes) {
        for (DataTreeModification<AuthenticationKey> change : changes) {
            final DataObjectModification<AuthenticationKey> mod = change.getRootNode();

            if (ModificationType.DELETE == mod.getModificationType()) {
                final AuthenticationKey authKey = mod.getDataBefore();

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", authKey);

                final AuthenticationKey convertedAuthKey = convertToBinaryIfNecessary(authKey);

                mapSystem.removeAuthenticationKey(convertedAuthKey.getEid());
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

                mapSystem.addAuthenticationKey(convertedAuthKey.getEid(), convertedAuthKey.getMappingAuthkey());
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

    void setMappingSystem(IMappingSystem msmr) {
        this.mapSystem = msmr;
    }
}
