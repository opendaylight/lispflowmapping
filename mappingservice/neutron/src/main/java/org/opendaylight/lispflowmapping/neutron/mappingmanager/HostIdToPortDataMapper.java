/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 2/5/17.
 */
public class HostIdToPortDataMapper {
    private static final Logger LOG = LoggerFactory.getLogger(HostIdToPortDataMapper.class);

    private ConcurrentHashMap<String, PortUuidToPortDataMapper> mapper;
    private static HostIdToPortDataMapper instance;

    private HostIdToPortDataMapper() {
        mapper = new ConcurrentHashMap();
    }

    public static synchronized HostIdToPortDataMapper getInstance() {
        if (instance == null) {
            instance = new HostIdToPortDataMapper();
        }
        return instance;
    }

    public synchronized void addMapping(String hostId, PortData portData) {
        PortUuidToPortDataMapper uuidToEidMapper = mapper.get(hostId);

        if (uuidToEidMapper == null) {
            uuidToEidMapper = new PortUuidToPortDataMapper();
            mapper.put(hostId, uuidToEidMapper);
        }

        uuidToEidMapper.addUnprocessedUuidToPortDataMapping(portData.getPortUuid(), portData);
        LOG.debug("Adding " + portData.getPortEid().getAddress() + " as EID of "
                + portData.getPortUuid() + " belonging to " + hostId);
    }

    public final PortUuidToPortDataMapper getAllPortData(String hostId) {
        return mapper.get(hostId);
    }

}
