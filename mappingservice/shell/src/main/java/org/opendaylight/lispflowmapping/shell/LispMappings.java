/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMappingShell;

/**
 * This class implements the "lisp:mappings" Karaf shell command
 *
 * @author Lorand Jakab
 *
 */
@Command(scope = "lisp", name = "mappings", description="Print LISP mapping database")
public class LispMappings  extends OsgiCommandSupport {
    private IFlowMappingShell lispShellService;

    @Override
    protected Object doExecute() throws Exception {
        System.out.print(lispShellService.printMappings());
        return null;
    }

    public void setLispShellService(IFlowMappingShell lispShellService) {
        this.lispShellService = lispShellService;
    }
}
