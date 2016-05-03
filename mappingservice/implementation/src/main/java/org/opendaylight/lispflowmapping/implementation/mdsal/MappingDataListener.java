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
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.implementation.util.MSNotificationInputUtil;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
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
public class MappingDataListener extends NewAbstractDataListener<Mapping> {
    private static final Logger LOG = LoggerFactory.getLogger(MappingDataListener.class);
    private IMappingSystem mapSystem;
    private NotificationPublishService notificationPublishService;

    public MappingDataListener(DataBroker broker, IMappingSystem msmr, NotificationPublishService nps) {
        setBroker(broker);
        setMappingSystem(msmr);
        setNotificationProviderService(nps);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(VirtualNetworkIdentifier.class)
                .child(Mapping.class));
        LOG.trace("Registering Mapping listener.");
        registerDataChangeListener();
    }

    public void setNotificationProviderService(NotificationPublishService nps) {
        this.notificationPublishService = nps;
    }

    void setMappingSystem(IMappingSystem msmr) {
        this.mapSystem = msmr;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Mapping>> changes) {
        for (DataTreeModification<Mapping> change : changes) {
            final DataObjectModification<Mapping> mod = change.getRootNode();

            if (ModificationType.DELETE == mod.getModificationType()) {
                // Process deleted mappings

                final Mapping mapping = mod.getDataBefore();

                // Only treat mapping changes caused by Northbound, since Southbound changes are already handled
                // before being persisted.
                if (mapping.getOrigin() == MappingOrigin.Southbound) {
                    continue;
                }

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", mapping);

                mapSystem.removeMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid());
                try {
                    notificationPublishService.putNotification(MSNotificationInputUtil.toMappingChanged(
                            mapping, MappingChange.Removed));
                } catch (InterruptedException e) {
                    LOG.warn("Notification publication interrupted!");
                }

            } else if (ModificationType.SUBTREE_MODIFIED == mod.getModificationType() || ModificationType.WRITE == mod
                    .getModificationType()) {
                final Mapping mapping = mod.getDataAfter();

                // Only treat mapping changes caused by Northbound, since Southbound changes are already handled
                // before being persisted. XXX separate NB and SB to avoid ignoring SB notifications
                if (mapping.getOrigin() == MappingOrigin.Southbound) {
                    continue;
                }

                MappingChange mappingChange;

                if (ModificationType.SUBTREE_MODIFIED == mod.getModificationType()) {
                    LOG.trace("Received update data");
                    mappingChange = MappingChange.Updated;
                } else {
                    LOG.trace("Received write data");
                    mappingChange = MappingChange.Created;
                }
                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", mapping);

                mapSystem.addMapping(mapping.getOrigin(), mapping.getMappingRecord().getEid(),
                        mapping.getMappingRecord(), false);

                try {
                    // The notifications are used for sending SMR.
                    notificationPublishService.putNotification(MSNotificationInputUtil.toMappingChanged(mapping,
                            mappingChange));
                } catch (InterruptedException e) {
                    LOG.warn("Notification publication interrupted!");
                }

            } else {
                LOG.warn("Ignoring unhandled modification type {}", mod.getModificationType());
            }
        }
    }
}
