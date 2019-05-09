/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.lispflowmapping.interfaces.mapcache.IMappingSystem;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.MappingBuilder;
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
public class MappingDataListener extends AbstractDataListener<Mapping> {
    private static final Logger LOG = LoggerFactory.getLogger(MappingDataListener.class);
    private IMappingSystem mapSystem;
    private NotificationPublishService notificationPublishService;
    private final boolean isMaster = false;

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
                // before being persisted, except for cluster slaves
                if (mapping.getOrigin() == MappingOrigin.Southbound && mapSystem.isMaster()) {
                    continue;
                }

                LOG.trace("Received deleted data");
                LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                LOG.trace("Value: {}", mapping);

                final Mapping convertedMapping = convertToBinaryIfNecessary(mapping);

                mapSystem.removeMapping(convertedMapping.getOrigin(), convertedMapping.getMappingRecord().getEid());

            } else if (ModificationType.SUBTREE_MODIFIED == mod.getModificationType() || ModificationType.WRITE == mod
                    .getModificationType()) {
                final Mapping mapping = mod.getDataAfter();

                // Only treat mapping changes caused by Northbound, since Southbound changes are already handled
                // before being persisted, except for cluster slaves XXX separate NB and SB to avoid ignoring
                // SB notifications
                if (mapping.getOrigin() == MappingOrigin.Southbound && mapSystem.isMaster()) {
                    continue;
                }

                final Mapping convertedMapping = convertToBinaryIfNecessary(mapping);
                Eid convertedEid = convertedMapping.getMappingRecord().getEid();

                if (ModificationType.SUBTREE_MODIFIED == mod.getModificationType()) {
                    LOG.trace("Received update data");
                    LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                    LOG.trace("Value: {}", mapping);
                    mapSystem.updateMapping(convertedMapping.getOrigin(), convertedEid,
                            new MappingData(convertedMapping.getMappingRecord()));
                } else {
                    LOG.trace("Received write data");
                    LOG.trace("Key: {}", change.getRootPath().getRootIdentifier());
                    LOG.trace("Value: {}", mapping);
                    mapSystem.addMapping(convertedMapping.getOrigin(), convertedEid,
                            new MappingData(convertedMapping.getMappingRecord()));
                }
            } else {
                LOG.warn("Ignoring unhandled modification type {}", mod.getModificationType());
            }
        }
    }

    private static Mapping convertToBinaryIfNecessary(Mapping mapping) {
        MappingRecord originalRecord = mapping.getMappingRecord();
        List<LocatorRecord> originalLocators = originalRecord.getLocatorRecord();

        List<LocatorRecord> convertedLocators = null;
        if (originalLocators != null) {
            // If convertedLocators is non-null, while originalLocators is also non-null, conversion has been made
            convertedLocators = convertToBinaryIfNecessary(originalLocators);
        }

        if (LispAddressUtil.addressNeedsConversionToBinary(originalRecord.getEid().getAddress())
                || originalLocators != null && convertedLocators != null) {
            MappingRecordBuilder mrb = new MappingRecordBuilder(originalRecord);
            mrb.setEid(LispAddressUtil.convertToBinary(originalRecord.getEid()));
            if (convertedLocators != null) {
                mrb.setLocatorRecord(convertedLocators);
            }
            return new MappingBuilder(mapping).setMappingRecord(mrb.build()).build();
        }
        return mapping;
    }

    private static List<LocatorRecord> convertToBinaryIfNecessary(List<LocatorRecord> originalLocators) {
        List<LocatorRecord> convertedLocators = null;
        for (LocatorRecord record : originalLocators) {
            if (LispAddressUtil.addressNeedsConversionToBinary(record.getRloc().getAddress())) {
                LocatorRecordBuilder lrb = new LocatorRecordBuilder(record);
                lrb.setRloc(LispAddressUtil.convertToBinary(record.getRloc()));
                if (convertedLocators == null) {
                    convertedLocators = new ArrayList<>();
                }
                convertedLocators.add(lrb.build());
            }
        }
        if (convertedLocators != null) {
            return convertedLocators;
        }
        return originalLocators;
    }
}
