/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.clustering.api;

/**
 * Interface specify operations necessary to turn on|off module in node cluster
 */
public interface ClusterNodeModuleSwitcher {

    void startModule();

    void stopModule();

}
