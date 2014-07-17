/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.inventory;

import java.net.InetAddress;
import java.util.Set;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.inventory.IPluginInInventoryService;

public interface IAdSalLispInventoryService extends IPluginInInventoryService {
    public void addNode(InetAddress address, byte[] xtrId);
    public void addNode(Node node, Set<Property> props);
    public void removeNode(Node node);
}
