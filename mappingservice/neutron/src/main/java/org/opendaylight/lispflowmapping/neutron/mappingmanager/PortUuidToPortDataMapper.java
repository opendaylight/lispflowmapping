/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Shakib Ahmed on 2/6/17.
 */
public class PortUuidToPortDataMapper {
    private HashMap<String, PortData> unprocessedPortUuidToEidMapper;
    private HashMap<String, PortData> processedPortUuidToEidMapper;

    public PortUuidToPortDataMapper() {
        unprocessedPortUuidToEidMapper = new HashMap<>();
        processedPortUuidToEidMapper = new HashMap<>();
    }

    public synchronized void addUnprocessedUuidToPortDataMapping(String uuid, PortData portData) {
        unprocessedPortUuidToEidMapper.put(uuid, portData);
    }

    public synchronized void addPortDataToProcessed(PortData portData) {
        processedPortUuidToEidMapper.put(portData.getPortUuid(), portData);
    }

    public synchronized Collection<PortData> getAllUnprocessedPorts() {
        return unprocessedPortUuidToEidMapper.values();
    }

    public synchronized void clearAllUnprocessedData() {
        unprocessedPortUuidToEidMapper.clear();
    }

    public synchronized void clearAllProcessedData() {
        processedPortUuidToEidMapper.clear();
    }

    public synchronized void clearAllData() {
        clearAllProcessedData();
        clearAllUnprocessedData();
    }

    public synchronized void transferAllProcessedPortDataToUnprocessed() {
        processedPortUuidToEidMapper.forEach(unprocessedPortUuidToEidMapper::putIfAbsent);
        processedPortUuidToEidMapper.clear();
    }
}
