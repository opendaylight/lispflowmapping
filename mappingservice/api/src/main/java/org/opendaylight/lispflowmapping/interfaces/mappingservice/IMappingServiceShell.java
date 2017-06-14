/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.mappingservice;

/**
 * This interface defines the methods that need to be implemented in order to
 * provide commands for the Karaf shell.
 *
 * @author Lorand Jakab
 *
 */
public interface IMappingServiceShell {
    /**
     * Print the full mapping database.
     *
     * @return the text to be printed on the Karaf console.
     */
    String printMappings();

    /**
     * Print the full mapping database in human readable form.
     *
     * @return the text to be printed on the Karaf console.
     */
    String prettyPrintMappings();

    /**
     * Print the full authentication key database.
     *
     * @return the text to be printed on the Karaf console.
     */
    String printKeys();

    /**
     * Print the full authentication key database in human readable form.
     *
     * @return the text to be printed on the Karaf console.
     */
    String prettyPrintKeys();

    /**
     * Add the default key "password" for the IPv4 prefix 0.0.0.0/0.
     */
    void addDefaultKeyIPv4();

    /**
     * Add the default key "password" for the IPv6 prefix ::0/0.
     */
    void addDefaultKeyIPv6();
}
