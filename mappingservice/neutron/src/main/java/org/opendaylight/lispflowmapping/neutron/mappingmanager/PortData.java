/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.mappingmanager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by Shakib Ahmed on 2/6/17.
 */
public class PortData {
    private String portUuid;

    private Eid portEid;

    public PortData(String portUuid, Eid portEid) {
        this.portUuid = portUuid;
        this.portEid = portEid;
    }

    public String getPortUuid() {
        return portUuid;
    }

    public void setPortUuid(String portUuid) {
        this.portUuid = portUuid;
    }

    public Eid getPortEid() {
        return portEid;
    }

    public void setPortEid(Eid portEid) {
        this.portEid = portEid;
    }
}
