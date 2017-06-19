/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 7/12/17.
 */
public class HostIdToEidMapper {
    private static final Logger LOG = LoggerFactory.getLogger(HostIdToEidMapper.class);

    private SetMultimap<String, Eid> processedHostIdToEidMapper;
    private SetMultimap<String, Eid> unprocessedHostIdToEidMapper;

    public HostIdToEidMapper() {
        processedHostIdToEidMapper = HashMultimap.create();
        unprocessedHostIdToEidMapper = HashMultimap.create();
    }

    public synchronized void addUnprocessedEidInHost(String hostId, Eid eid) {
        unprocessedHostIdToEidMapper.put(hostId, eid);
    }

    public synchronized void addProcessedEidInHost(String hostId, Eid eid) {
        processedHostIdToEidMapper.put(hostId, eid);
    }

    public synchronized boolean isProcessedEid(String hostId, Eid eid) {
        return processedHostIdToEidMapper.get(hostId).contains(eid);
    }

    public synchronized List<Eid> getAllUnprocessedEidsInHost(String hostId) {
        return new ArrayList<>(unprocessedHostIdToEidMapper.get(hostId));
    }

    public synchronized List<Eid> getAllProcessedEidsInHost(String hostId) {
        return new ArrayList<>(processedHostIdToEidMapper.get(hostId));
    }

    public synchronized void deleteAllProcessedEidFormHost(String hostId) {
        processedHostIdToEidMapper.removeAll(hostId);
    }

    public synchronized void deleteAllUnprocessedEidFormHost(String hostId) {
        unprocessedHostIdToEidMapper.removeAll(hostId);
    }

    public synchronized void deleteSpecificProcessedEidFromHost(String hostId, Eid eid) {
        processedHostIdToEidMapper.remove(hostId, eid);
    }

    public synchronized void deleteSpecificUnprocessedEidFromHost(String hostId, Eid eid) {
        unprocessedHostIdToEidMapper.remove(hostId, eid);
    }

    public synchronized void transferAllProcessedEidsToUnprocessed(String hostId) {
        processedHostIdToEidMapper.get(hostId).forEach(eid -> unprocessedHostIdToEidMapper.put(hostId, eid));
        deleteAllProcessedEidFormHost(hostId);
    }
}
