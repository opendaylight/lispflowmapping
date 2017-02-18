/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.Collection;
import java.util.List;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.LispUtil;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.HostIdToPortDataMapper;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.HostIdToRlocMapper;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.NeutronTenantToVniMapper;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.PortUuidToPortDataMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 2/5/17.
 */
public class HostInformationManager {
    private static final Logger LOG = LoggerFactory.getLogger(HostInformationManager.class);

    private OdlMappingserviceService lfmDbService;
    private HostIdToPortDataMapper hostIdToPortDataMapper;
    private HostIdToRlocMapper hostIdToRlocMapper;
    private NeutronTenantToVniMapper neutronTenantToVniMapper;

    private static HostInformationManager instance;

    private HostInformationManager() {
        hostIdToPortDataMapper = new HostIdToPortDataMapper();
        hostIdToRlocMapper = new HostIdToRlocMapper();
        neutronTenantToVniMapper = new NeutronTenantToVniMapper();
    }

    public static synchronized HostInformationManager getInstance() {
        if (instance == null) {
            instance = new HostInformationManager();
        }
        return instance;
    }

    public long getInstanceId(String tenantUuid) {
        return neutronTenantToVniMapper.getVni(tenantUuid);
    }

    public void setOdlMappingserviceService(OdlMappingserviceService lfmDbService) {
        this.lfmDbService = lfmDbService;
    }

    public synchronized void addHostRelatedInfo(String hostId, Object data) {
        if (data instanceof PortData) {
            attemptToDeleteMappingRecord(hostId, ((PortData) data).getPortUuid());
            hostIdToPortDataMapper.addMapping(hostId, (PortData) data);
        } else if (data instanceof Rloc) {
            hostIdToRlocMapper.addMapping(hostId, (Rloc) data);
        } else {
            LOG.warn("Unwanted information type " + data.getClass().getSimpleName()
                    + " for Host Id " + hostId);
        }

        attemptToCreateMappingRecord(hostId);
    }

    private synchronized void attemptToCreateMappingRecord(String hostId) {
        List<Rloc> rlocForMappingRecord = hostIdToRlocMapper.getRlocs(hostId);
        PortUuidToPortDataMapper uuidToEidMapper = hostIdToPortDataMapper.getAllPortData(hostId);

        if (rlocForMappingRecord == null || rlocForMappingRecord.isEmpty()
                || uuidToEidMapper == null) {
            return;
        }

        Collection<PortData> allUnprocessedPorts = uuidToEidMapper.getAllUnprocessedPorts();
        List<LocatorRecord> locators = LispAddressUtil.asLocatorRecords(rlocForMappingRecord);

        allUnprocessedPorts.forEach(portData -> {
            lfmDbService.addMapping(LispUtil.buildAddMappingInput(portData.getPortEid(), locators));
            uuidToEidMapper.addPortDataToProcessed(portData);
        });
        uuidToEidMapper.clearAllUnprocessedData();
    }

    private synchronized void attemptToDeleteMappingRecord(String hostId, String portUuid) {
        PortUuidToPortDataMapper uuidToEidMapper = hostIdToPortDataMapper.getAllPortData(hostId);

        if (uuidToEidMapper == null) {
            return;
        }

        PortData oldPortData = uuidToEidMapper.getProcessedPortData(portUuid);

        if (oldPortData == null) {
            return;
        }

        RemoveMappingInput removeMappingInput = LispUtil.buildRemoveMappingInput(oldPortData.getPortEid());
        lfmDbService.removeMapping(removeMappingInput);
        uuidToEidMapper.deleteProcessedPortData(portUuid);
    }
}