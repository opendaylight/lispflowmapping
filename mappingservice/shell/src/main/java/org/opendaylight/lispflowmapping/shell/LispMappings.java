/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingServiceShell;

/**
 * This class implements the "mappingservice:mappings" Karaf shell command.
 *
 * @author Lorand Jakab
 *
 */
@Command(scope = "mappingservice", name = "mappings", description = "Print mapping database")
@Service
public class LispMappings implements Action {
    @Option(name = "-d", aliases = "--debug", description = "Debug output", required = false, multiValued = false)
    private boolean debug;

    private IMappingServiceShell mappingServiceShell;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() throws Exception {
        if (debug) {
            System.out.print(mappingServiceShell.printMappings());
        } else {
            System.out.print(mappingServiceShell.prettyPrintMappings());
        }
        return null;
    }

    public void setMappingServiceShell(IMappingServiceShell mappingServiceShell) {
        this.mappingServiceShell = mappingServiceShell;
    }
}
