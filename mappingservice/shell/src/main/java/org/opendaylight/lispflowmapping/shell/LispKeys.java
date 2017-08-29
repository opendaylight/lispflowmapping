/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc.  All rights reserved.
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
 * This class implements the "mappingservice:keys" Karaf shell command.
 *
 * @author Lorand Jakab
 *
 */
@Command(scope = "mappingservice", name = "keys", description = "Print LISP authentication keys")
@Service
public class LispKeys implements Action {
    @Option(name = "-d", aliases = "--debug", description = "Debug output", required = false, multiValued = false)
    private boolean debug;

    private IMappingServiceShell mappingServiceShell;

    /*
     * TODO  Eventually it would be best to merge the addkey command into this one, using optional arguments.
     */

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() throws Exception {
        if (debug) {
            System.out.print(mappingServiceShell.printKeys());
        } else {
            System.out.print(mappingServiceShell.prettyPrintKeys());
        }
        return null;
    }

    public void setMappingServiceShell(IMappingServiceShell mappingServiceShell) {
        this.mappingServiceShell = mappingServiceShell;
    }
}
