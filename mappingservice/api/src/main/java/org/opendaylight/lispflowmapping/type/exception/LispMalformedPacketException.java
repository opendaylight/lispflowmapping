/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.exception;

public class LispMalformedPacketException extends RuntimeException {

    private static final long serialVersionUID = 7873237471831750080L;

    public LispMalformedPacketException(String message) {
        super(message);
    }

    public LispMalformedPacketException(String message, Throwable cause) {
        super(message, cause);
    }
}
