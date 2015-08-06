/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * This interface defines the methods that need to be implemented in order to
 * provide commands for the Karaf shell.
 *
 * @author Lorand Jakab
 *
 */
public interface IFlowMappingShell {
    /**
     * Print the full mapping database.
     *
     * @return the text to be printed on the Karaf console.
     */
    String printMappings();

    /**
     * Add the default key "password" for the IPv4 prefix 0.0.0.0/0.
     */
    void addDefaultKeyIPv4();
}
