/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.interfaces;

import org.opendatlight.lispflowmapping.util.contianer.MappingAllInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Created by sheikahm on 9/21/16.
 */
public interface ISouthboundMappingTimeoutManager {

    int addMapping(Eid key, String subKey, MappingAllInfo mappingAllInfo);

    int refreshMapping(Eid key, String subKey, MappingAllInfo mappingAllInfo, int containerId);

    void cleanTimeoutMapping();
}
