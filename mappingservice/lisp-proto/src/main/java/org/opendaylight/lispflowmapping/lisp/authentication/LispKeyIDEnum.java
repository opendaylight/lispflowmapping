/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.authentication;

public enum LispKeyIDEnum {
    NONE(0, null),
    SHA1(1, "HmacSHA1"),
    SHA256(2, "HmacSHA256"),
    UNKNOWN(-1, null);

    private short keyID;
    private String authenticationName;

    LispKeyIDEnum(int keyID, String authenticationName) {
        this.keyID = (short) keyID;
        this.authenticationName = authenticationName;
    }

    public String getAuthenticationName() {
        return authenticationName;
    }

    public short getKeyID() {
        return keyID;
    }

    public static LispKeyIDEnum valueOf(short keyID) {
        for (LispKeyIDEnum val : values()) {
            if (val.getKeyID() == keyID) {
                return val;
            }
        }
        return UNKNOWN;
    }
}
