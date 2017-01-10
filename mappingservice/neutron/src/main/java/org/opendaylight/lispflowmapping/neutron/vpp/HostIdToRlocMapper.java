/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.vpp;

import java.util.HashMap;

import org.opendaylight.lispflowmapping.neutron.vpp.exception.HostIdToRlocMappingNotFound;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 1/12/17.
 */
public class HostIdToRlocMapper {
    private static final Logger LOG = LoggerFactory.getLogger(HostIdToRlocMapper.class);

    private HashMap<String, Rloc> mapper;
    private static HostIdToRlocMapper instance;

    private HostIdToRlocMapper() {
        mapper = new HashMap<>();
    }

    public static synchronized HostIdToRlocMapper getInstance() {
        if (instance == null) {
            instance = new HostIdToRlocMapper();
        }
        return instance;
    }

    public synchronized void addMapping(String hostId, Rloc hostRloc) {
        mapper.put(hostId, hostRloc);
    }

    public synchronized Rloc getRloc(String hostId) {
        if ( !mapper.containsKey(hostId) ) {
            throw new HostIdToRlocMappingNotFound(hostId);
        }

        return mapper.get(hostId);
    }

    public synchronized void deleteRloc(String hostId) {
        mapper.remove(hostId);
    }
}
