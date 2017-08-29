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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingServiceShell;

/**
 * This class implements the "mappingservice:addkey" Karaf shell command.
 *
 * @author Lorand Jakab
 *
 */
@Command(scope = "mappingservice", name = "addkey", description = "Add an authentication key")
@Service
public class LispAddKey implements Action {
    private IMappingServiceShell mappingServiceShell;

    @Override
    public Object execute() throws Exception {
        mappingServiceShell.addDefaultKeyIPv4();
        mappingServiceShell.addDefaultKeyIPv6();
        return null;
    }

    public void setMappingServiceShell(IMappingServiceShell mappingServiceShell) {
        this.mappingServiceShell = mappingServiceShell;
    }
}
