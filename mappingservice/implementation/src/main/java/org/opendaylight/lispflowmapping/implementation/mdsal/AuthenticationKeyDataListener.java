/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.mapping.database.InstanceId;
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

    public AuthenticationKeyDataListener(DataBroker broker) {
        setBroker(broker);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(InstanceId.class)
                .child(AuthenticationKey.class));
        LOG.info("Registering AuthenticationKey listener.");
        registerDataChangeListener();
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            if (entry.getValue() instanceof AuthenticationKey) {
                LOG.info("Received changed data");
                LOG.info("Key: {}", entry.getKey());
                LOG.info("Value: {}", entry.getValue());
            }
        }
    }
}
