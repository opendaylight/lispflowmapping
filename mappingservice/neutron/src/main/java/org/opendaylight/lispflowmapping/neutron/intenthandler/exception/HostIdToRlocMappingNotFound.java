/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.exception;

/**
 * Created by Shakib Ahmed on 1/12/17.
 */
public class HostIdToRlocMappingNotFound extends RuntimeException {

    public HostIdToRlocMappingNotFound(String hostId) {
        super("No Host Id to Rloc Mapping found for " + hostId);
    }
}
