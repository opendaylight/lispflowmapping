/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache.timebucket.manager.interfaces;

import org.opendaylight.lispflowmapping.mapcache.timebucket.TimeBucketLFMKey;

/**
 * Created by sheikahm on 9/21/16.
 */
public interface ISouthboundMappingTimeoutManager {

    int addOrRefreshMapping(TimeBucketLFMKey key, Runnable removalMethod, Integer containerId);

    void cleanTimeoutMapping();
}
