/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.List;

import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.HostIdToEidMapper;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.HostIdToRlocMapper;
import org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers.NeutronTenantToVniMapper;
import org.opendaylight.lispflowmapping.neutron.util.LispUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 2/5/17.
 */
public class OpenstackNodeInformationManager {
    private static final Logger LOG = LoggerFactory.getLogger(OpenstackNodeInformationManager.class);

    private OdlMappingserviceService lfmDbService;
    private HostIdToEidMapper hostIdToEidMapper;
    private HostIdToRlocMapper hostIdToRlocMapper;
    private NeutronTenantToVniMapper neutronTenantToVniMapper;

    private static OpenstackNodeInformationManager instance;

    private OpenstackNodeInformationManager() {
        hostIdToEidMapper = new HostIdToEidMapper();
        hostIdToRlocMapper = new HostIdToRlocMapper();
        neutronTenantToVniMapper = new NeutronTenantToVniMapper();
    }

    public static synchronized OpenstackNodeInformationManager getInstance() {
        if (instance == null) {
            instance = new OpenstackNodeInformationManager();
        }
        return instance;
    }

    public long getInstanceId(String tenantUuid) {
        return neutronTenantToVniMapper.getVni(tenantUuid);
    }

    public void setOdlMappingserviceService(OdlMappingserviceService lfmDbService) {
        this.lfmDbService = lfmDbService;
    }

    public synchronized void addRlocOfHost(String hostId, Rloc rloc) {
        hostIdToRlocMapper.addRlocToHost(hostId, rloc);
        attemptToResolveUnprocessedEids(hostId);
    }

    public synchronized void addEidInHost(String hostId, Eid eid) {
        hostIdToEidMapper.addUnprocessedEidInHost(hostId, eid);
        attemptToResolveUnprocessedEids(hostId);
    }

    private synchronized void attemptToResolveUnprocessedEids(String hostId) {
        List<Rloc> rlocForMappingRecord = hostIdToRlocMapper.getRlocs(hostId);
        List<Eid> unprocessedEidList = hostIdToEidMapper.getAllUnprocessedEidsInHost(hostId);

        if (rlocForMappingRecord == null || rlocForMappingRecord.isEmpty()
                || unprocessedEidList == null || unprocessedEidList.isEmpty()) {
            return;
        }

        List<LocatorRecord> locators = LispAddressUtil.asLocatorRecords(rlocForMappingRecord);

        unprocessedEidList.forEach(eid -> {
            lfmDbService.addMapping(LispUtil.buildAddMappingInput(eid, locators));
            hostIdToEidMapper.addProcessedEidInHost(hostId, eid);
        });

        hostIdToEidMapper.deleteAllUnprocessedEidFormHost(hostId);
    }

    public synchronized void attemptToDeleteExistingMappingRecord(String hostId, Eid eid) {
        if (hostIdToEidMapper.isProcessedEid(hostId, eid)) {
            removeMappingFromLfmDb(eid);
            hostIdToEidMapper.deleteSpecificProcessedEidFromHost(hostId, eid);
        }
    }

    private synchronized void removeMappingFromLfmDb(Eid oldEid) {
        RemoveMappingInput removeMappingInput = LispUtil.buildRemoveMappingInput(oldEid);
        lfmDbService.removeMapping(removeMappingInput);
    }

    public synchronized void deleteAllEidsOfHostAndMarkUnprocessed(String hostId) {
        List<Eid> processedEidList = hostIdToEidMapper.getAllProcessedEidsInHost(hostId);

        processedEidList.forEach(this::removeMappingFromLfmDb);

        hostIdToEidMapper.transferAllProcessedEidsToUnprocessed(hostId);
    }
}