/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.inventory;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.inventory.IPluginInInventoryService;
import org.opendaylight.controller.sal.inventory.IPluginOutInventoryService;
import org.opendaylight.controller.sal.utils.HexEncode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdSalLispInventoryService implements IAdSalLispInventoryService, IPluginInInventoryService {
    private static final Logger logger = LoggerFactory.getLogger(AdSalLispInventoryService.class);

    private ConcurrentMap<Node, Map<String, Property>> nodeProps;
    private ConcurrentMap<NodeConnector, Map<String, Property>> nodeConnectorProps;
    private final Set<IPluginOutInventoryService> pluginOutInventoryServices =
            new CopyOnWriteArraySet<IPluginOutInventoryService>();

    /**
     * Function called by the dependency manager when all the required
     * dependencies are satisfied
     *
     */
    public void init() {
        nodeProps = new ConcurrentHashMap<Node, Map<String, Property>>();
        nodeConnectorProps = new ConcurrentHashMap<NodeConnector, Map<String, Property>>();
        logger.debug("Registering \"LISP\" NodeID and NodeConnectorID types");
        Node.NodeIDType.registerIDType("LISP", String.class);
        NodeConnector.NodeConnectorIDType.registerIDType("LISP", String.class, "LISP");
    }

    /**
     * Function called by the dependency manager when at least one dependency
     * become unsatisfied or when the component is shutting down because for
     * example bundle is being stopped.
     *
     */
    public void destroy() {
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */
    public void start() {
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    public void stop() {
    }

    public String getXtrNodeId(InetAddress address) {
        return address.getHostAddress();
    }

    public String getXtrNodeId(InetAddress address, byte[] xtrId) {
        return address.getHostAddress() + "|" + HexEncode.bytesToHexString(xtrId);
    }

    public Set<Property> getXtrNodeProps(InetAddress address) {
        IPAddressNodeProperty addressProp = new IPAddressNodeProperty(address);
        Set<Property> props = new HashSet<Property>();
        props.add(addressProp);
        return props;
    }

    public Set<Property> getXtrNodeProps(InetAddress address, byte[] xtrId) {
        IPAddressNodeProperty addressProp = new IPAddressNodeProperty(address);
        XtrIdNodeProperty xtrIdProp = new XtrIdNodeProperty(xtrId);
        Set<Property> props = new HashSet<Property>();
        props.add(addressProp);
        props.add(xtrIdProp);
        return props;
    }

    public Node createLispNode(String nodeId) {
        Node node = null;
        try {
            node = new Node("LISP", nodeId);
        } catch (ConstructionException e) {
            logger.error("Error creating LISP node with identifier " + nodeId, e);
        }
        return node;
    }

    @Override
    public void addNode(Node node, Set<Property> props) {
        Map<String, Property> nProp = nodeProps.get(node);
        if (nProp == null) {
            nProp = new HashMap<String, Property>();
        } else {
            logger.debug("Not adding LISP node with identifier {} to inventory: node already exists", node);
            return;
        }
        for (Property prop : props) {
            nProp.put(prop.getName(), prop);
        }
        nodeProps.put(node, nProp);
        for (IPluginOutInventoryService service : pluginOutInventoryServices) {
            service.updateNode(node, UpdateType.ADDED, props);
        }
        logger.info("Added LISP node with identifier {} to inventory", node);
        logger.debug("nodeProps " + nodeProps);
    }

    @Override
    public void removeNode(Node node) {
        nodeProps.remove(node);
        for (IPluginOutInventoryService service : pluginOutInventoryServices) {
            service.updateNode(node, UpdateType.REMOVED, null);
        }
        logger.info("Removed LISP node with identifier {} from inventory", node.getNodeIDString());
    }

    @Override
    public void addNode(InetAddress address, byte[] xtrId) {
        if (xtrId != null) {
            Node node = createLispNode(getXtrNodeId(address, xtrId));
            Set<Property> props = getXtrNodeProps(address, xtrId);
            addNode(node, props);
        } else {
            Node node = createLispNode(getXtrNodeId(address));
            Set<Property> props = getXtrNodeProps(address);
            addNode(node, props);
        }
    }

    public void setPluginOutInventoryServices(IPluginOutInventoryService service) {
        logger.trace("Added inventory service " + service);
        this.pluginOutInventoryServices.add(service);
    }

    public void unsetPluginOutInventoryServices(IPluginOutInventoryService service) {
        logger.trace("Removed inventory service " + service);
        this.pluginOutInventoryServices.remove(service);
    }

    @Override
    public ConcurrentMap<Node, Map<String, Property>> getNodeProps() {
        return nodeProps;
    }

    @Override
    public ConcurrentMap<NodeConnector, Map<String, Property>> getNodeConnectorProps(
            Boolean refresh) {
        return nodeConnectorProps;
    }

    @Override
    public Set<Node> getConfiguredNotConnectedNodes() {
        return Collections.emptySet();
    }
}
