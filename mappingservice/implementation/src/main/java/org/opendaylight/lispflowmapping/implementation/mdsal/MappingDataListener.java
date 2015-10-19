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
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.InstanceId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all Mapping modification events.
 *
 * @author Lorand Jakab
 * @author Florin Coras
 *
 */
public class MappingDataListener extends AbstractDataListener {
    private static final Logger LOG = LoggerFactory.getLogger(MappingDataListener.class);
    private IMappingSystem mapSystem;
    private NotificationPublishService notificationPublishService;

    public MappingDataListener(DataBroker broker, IMappingSystem msmr, NotificationPublishService nps) {
        setBroker(broker);
        setMappingSystem(msmr);
        setNotificationProviderService(nps);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(InstanceId.class)
                .child(Mapping.class));
        LOG.trace("Registering Mapping listener.");
        registerDataChangeListener();
    }

    public void setNotificationProviderService(NotificationPublishService nps) {
        this.notificationPublishService = nps;
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

     // Process newly created mappings
        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            if (entry.getValue() instanceof Mapping) {
                Mapping mapping = (Mapping)entry.getValue();

                LOG.trace("Received created data");
                LOG.trace("Key: {}", entry.getKey());
                LOG.trace("Value: {}", mapping);

                mapSystem.addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(),
                        mapping.getMappingRecord());
            }
        }

        // Process updated mappings
        Map<InstanceIdentifier<?>, DataObject> updatedData = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : updatedData.entrySet()) {
            if (entry.getValue() instanceof Mapping) {
                Mapping mapping = (Mapping)entry.getValue();

                LOG.trace("Received changed data");
                LOG.trace("Key: {}", entry.getKey());
                LOG.trace("Value: {}", entry.getValue());

                mapSystem.addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(),
                        mapping.getMappingRecord());
                try {
                    notificationPublishService.putNotification(MSNotificationInputUtil.toMappingChanged(mapping, MappingChange.Updated));
                } catch (InterruptedException e) {
                    LOG.warn("Notification publication interrupted!");
                }
            }
        }

        // Process deleted mappings
        Set<InstanceIdentifier<?>> removedData = change.getRemovedPaths();
        for (InstanceIdentifier<?> entry : removedData) {
            DataObject dataObject = change.getOriginalData().get(entry);
            if (dataObject instanceof Mapping) {
                Mapping mapping = (Mapping)dataObject;

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", entry);
                LOG.trace("Value: {}", dataObject);

                mapSystem.removeMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid());
                try {
                    notificationPublishService.putNotification(MSNotificationInputUtil.toMappingChanged(mapping, MappingChange.Removed));
                } catch (InterruptedException e) {
                    LOG.warn("Notification publication interrupted!");
                }
            }
        }
    }

    void setMappingSystem(IMappingSystem msmr) {
        this.mapSystem = msmr;
    }
}
