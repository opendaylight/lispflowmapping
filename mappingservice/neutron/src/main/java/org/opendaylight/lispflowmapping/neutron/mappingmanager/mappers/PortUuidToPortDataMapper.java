/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager.mappers;

import java.util.Collection;
import java.util.HashMap;

import org.opendaylight.lispflowmapping.neutron.mappingmanager.PortData;

/**
 * Created by Shakib Ahmed on 2/6/17.
 */
public class PortUuidToPortDataMapper {
    private HashMap<String, PortData> unprocessedPortUuidToPortDataMapper;
    private HashMap<String, PortData> processedPortUuidToPortDataMapper;

    public PortUuidToPortDataMapper() {
        unprocessedPortUuidToPortDataMapper = new HashMap<>();
        processedPortUuidToPortDataMapper = new HashMap<>();
    }

    public synchronized void addUnprocessedUuidToPortDataMapping(String uuid, PortData portData) {
        unprocessedPortUuidToPortDataMapper.put(uuid, portData);
    }

    public synchronized void addPortDataToProcessed(PortData portData) {
        processedPortUuidToPortDataMapper.put(portData.getPortUuid(), portData);
    }

    public synchronized PortData getProcessedPortData(String uuid) {
        return processedPortUuidToPortDataMapper.get(uuid);
    }

    public synchronized void deleteProcessedPortData(String portUuid) {
        processedPortUuidToPortDataMapper.remove(portUuid);
    }

    public synchronized void delereUnprocessedPortData(String portUuid) {
        unprocessedPortUuidToPortDataMapper.remove(portUuid);
    }

    public synchronized Collection<PortData> getAllUnprocessedPorts() {
        return unprocessedPortUuidToPortDataMapper.values();
    }

    public synchronized void clearAllUnprocessedData() {
        unprocessedPortUuidToPortDataMapper.clear();
    }

    public synchronized void clearAllProcessedData() {
        processedPortUuidToPortDataMapper.clear();
    }

    public synchronized void clearAllData() {
        clearAllProcessedData();
        clearAllUnprocessedData();
    }

    public synchronized void transferAllProcessedPortDataToUnprocessed() {
        processedPortUuidToPortDataMapper.forEach(unprocessedPortUuidToPortDataMapper::putIfAbsent);
        processedPortUuidToPortDataMapper.clear();
    }

    public synchronized Collection<PortData> getAllProcessedPortData() {
        return processedPortUuidToPortDataMapper.values();
    }
}
