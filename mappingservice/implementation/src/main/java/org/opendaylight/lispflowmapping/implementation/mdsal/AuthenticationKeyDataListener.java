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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
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
public class AuthenticationKeyDataListener extends NewAbstractDataListener<AuthenticationKey> {
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

            switch (mod.getModificationType()) {
                case DELETE:
                    AuthenticationKey authKey = mod.getDataBefore();

                    LOG.trace("Received deleted data");
                    LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                    LOG.trace("Value: {}", authKey);

                    mapSystem.removeAuthenticationKey(authKey.getEid());
                    break;
                case WRITE:
                    LOG.trace("Received created data");
                case SUBTREE_MODIFIED:
                    // Process newly created or updated authentication keys
                    authKey = mod.getDataAfter();

                    LOG.trace("Received updated data");
                    LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                    LOG.trace("Value: {}", authKey);

                    mapSystem.addAuthenticationKey(authKey.getEid(), authKey.getMappingAuthkey());

                    break;
                default:
                    LOG.warn("Ignoring unhandled modification type {}", mod.getModificationType());
                    break;

            }
        }
    }

    void setMappingSystem(IMappingSystem msmr) {
        this.mapSystem = msmr;
    }
}
