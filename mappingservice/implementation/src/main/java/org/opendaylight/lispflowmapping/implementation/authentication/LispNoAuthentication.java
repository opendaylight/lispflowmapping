/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;

public class LispNoAuthentication implements ILispAuthentication {

    private static final LispNoAuthentication INSTANCE = new LispNoAuthentication();

    private static byte[] authenticationData;

    public static LispNoAuthentication getInstance() {
        return INSTANCE;
    }

    private LispNoAuthentication() {
        authenticationData = new byte[0];
    }

    public int getAuthenticationLength() {
        return 0;
    }

    public byte[] getAuthenticationData(MapNotify mapNotify, String key) {
        return authenticationData;
    }

    public boolean validate(MapRegister mapRegister, String key) {
        return true;
    }

}
