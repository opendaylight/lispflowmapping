/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.InstanceId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all AuthenticationKey modification events.
 *
 * @author Lorand Jakab
 *
 */
public class AuthenticationKeyDataListener extends AbstractDataListener {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationKeyDataListener.class);
    private IMappingSystem mapSystem;

    public AuthenticationKeyDataListener(DataBroker broker, IMappingSystem mapSystem) {
        setBroker(broker);
        setMappingSystem(mapSystem);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(InstanceId.class)
                .child(AuthenticationKey.class));
        LOG.trace("Registering AuthenticationKey listener.");
        registerDataChangeListener();
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        // Process newly created authentication keys
        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            if (entry.getValue() instanceof AuthenticationKey) {
                AuthenticationKey authkey = (AuthenticationKey)entry.getValue();

                LOG.trace("Received created data");
                LOG.trace("Key: {}", entry.getKey());
                LOG.trace("Value: {}", authkey);

                mapSystem.addAuthenticationKey(authkey.getLispAddressContainer(), authkey.getAuthkey());
            }
        }

        // Process updated authentication keys
        Map<InstanceIdentifier<?>, DataObject> updatedData = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : updatedData.entrySet()) {
            if (entry.getValue() instanceof AuthenticationKey) {
                AuthenticationKey authkey = (AuthenticationKey)entry.getValue();

                LOG.trace("Received changed data");
                LOG.trace("Key: {}", entry.getKey());
                LOG.trace("Value: {}", authkey);

                mapSystem.addAuthenticationKey(authkey.getLispAddressContainer(), authkey.getAuthkey());
            }
        }

        // Process deleted authentication keys
        Set<InstanceIdentifier<?>> removedData = change.getRemovedPaths();
        for (InstanceIdentifier<?> entry : removedData) {
            DataObject dataObject = change.getOriginalData().get(entry);
            if (dataObject instanceof AuthenticationKey) {
                AuthenticationKey authkey = (AuthenticationKey)dataObject;

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", entry);
                LOG.trace("Value: {}", authkey);

                mapSystem.removeAuthenticationKey(authkey.getLispAddressContainer());
            }
        }
    }

    void setMappingSystem(IMappingSystem msmr) {
        this.mapSystem = msmr;
    }
}
