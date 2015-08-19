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
import org.opendaylight.lispflowmapping.implementation.LispMappingService;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.lisp.util.MapServerMapResolverUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.mapping.database.InstanceId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataListener for all Mapping modification events.
 *
 * @author Lorand Jakab
 *
 */
public class MappingDataListener extends AbstractDataListener {
    private static final Logger LOG = LoggerFactory.getLogger(MappingDataListener.class);
    private LispMappingService msmr;
    private static final ConfigIni configIni = new ConfigIni();
    private volatile boolean smr = configIni.smrIsSet();

    public MappingDataListener(DataBroker broker, LispMappingService msmr) {
        setBroker(broker);
        setMsmr(msmr);
        setPath(InstanceIdentifier.create(MappingDatabase.class).child(InstanceId.class)
                .child(Mapping.class));
        LOG.trace("Registering Mapping listener.");
        registerDataChangeListener();
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

                if (mapping.getOrigin() != MappingOrigin.Southbound) {
                    MapRegister register = MapServerMapResolverUtil.getMapRegister(mapping);
                    msmr.handleMapRegister(register, smr);
                } else {
                    LOG.trace("Mapping is coming from the southbound plugin, already handled");
                }
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

                if (mapping.getOrigin() != MappingOrigin.Southbound) {
                    MapRegister register = MapServerMapResolverUtil.getMapRegister(mapping);
                    msmr.handleMapRegister(register, smr);
                } else {
                    LOG.trace("Mapping is coming from the southbound plugin, already handled");
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

                msmr.removeMapping(mapping.getLispAddressContainer(), mapping.getMaskLength());
            }
        }
    }

    void setMsmr(LispMappingService msmr) {
        this.msmr = msmr;
    }
}
