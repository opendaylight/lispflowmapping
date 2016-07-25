/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.clustering.util;

import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

public final class ClusteringUtil {

    public static final String LISPFLOWMAPPING_ENTITY_NAME = "lispflowmapping";
    public static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(
            LISPFLOWMAPPING_ENTITY_NAME);

    private ClusteringUtil() {
        throw new UnsupportedOperationException();
    }

}
