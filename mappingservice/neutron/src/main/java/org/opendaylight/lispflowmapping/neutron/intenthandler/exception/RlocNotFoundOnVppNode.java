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
public class RlocNotFoundOnVppNode extends RuntimeException {
    private static final String MESSAGE = "No available interface found on node ";

    public RlocNotFoundOnVppNode(String hostId) {
        super(MESSAGE + hostId);
    }

    public RlocNotFoundOnVppNode(String hostId, Throwable cause) {
        super(MESSAGE + hostId, cause);
    }
}
