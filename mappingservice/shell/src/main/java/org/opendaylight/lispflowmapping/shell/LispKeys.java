/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingServiceShell;

/**
 * This class implements the "mappingservice:keys" Karaf shell command.
 *
 * @author Lorand Jakab
 *
 */
@Command(scope = "mappingservice", name = "keys", description = "Print LISP authentication keys")
public class LispKeys  extends OsgiCommandSupport {
    @Option(name = "-d", aliases = "--debug", description = "Debug output", required = false, multiValued = false)
    private boolean debug;

    private IMappingServiceShell mappingServiceShell;

    /*
     * TODO  Eventually it would be best to merge the addkey command into this one, using optional arguments.
     */

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    protected Object doExecute() throws Exception {
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
