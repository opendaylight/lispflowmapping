/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.exception;

/**
 * Created by sheikahm on 1/12/17.
 */
public class RlocNotFoundOnVppNode extends RuntimeException {
    public RlocNotFoundOnVppNode(String hostId) {
        super("No available interface found on node " + hostId);
    }
}
