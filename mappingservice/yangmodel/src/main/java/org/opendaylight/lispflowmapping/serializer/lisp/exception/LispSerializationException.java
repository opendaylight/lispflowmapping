/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.serializer.lisp.exception;

public class LispSerializationException extends RuntimeException {

    private static final long serialVersionUID = 73639582991861261L;

    public LispSerializationException(String message) {
        super(message);
    }

    public LispSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
