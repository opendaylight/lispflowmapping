/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/12/17.
 */
public class HostIdToRlocMapper {
    private static final Logger LOG = LoggerFactory.getLogger(HostIdToRlocMapper.class);

    private ConcurrentHashMap<String, List<Rloc>> mapper;
    private static HostIdToRlocMapper instance;

    private HostIdToRlocMapper() {
        mapper = new ConcurrentHashMap();
    }

    public static synchronized HostIdToRlocMapper getInstance() {
        if (instance == null) {
            instance = new HostIdToRlocMapper();
        }
        return instance;
    }

    public synchronized void addMapping(String hostId, Rloc hostRloc) {
        List<Rloc> hostIdSpeceficRlocs = mapper.get(hostId);

        if (hostIdSpeceficRlocs == null) {
            hostIdSpeceficRlocs = new ArrayList<>();
        }

        hostIdSpeceficRlocs.add(hostRloc);
        mapper.put(hostId, hostIdSpeceficRlocs);
        LOG.debug("Adding " + hostRloc.getAddress() + " as Rloc of " + hostId);
    }

    public List<Rloc> getRlocs(String hostId) {
        return mapper.get(hostId);
    }

    public synchronized void deleteMapping(String hostId) {
        //for now, delete all rlocs
        mapper.remove(hostId);
    }
}