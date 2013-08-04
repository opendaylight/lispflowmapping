/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp.exception;

public class LispAuthenticationException extends RuntimeException {

	private static final long serialVersionUID = -6262815671761189838L;

	public LispAuthenticationException(String message) {
        super(message);
    }

    public LispAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
