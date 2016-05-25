/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.authentication;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.interfaces.lisp.ILispAuthentication;

public final class LispAuthenticationFactory {

    private static Map<LispKeyIDEnum, ILispAuthentication> keyIDToAuthenticationMap;

    // Class should not be instantiated
    private LispAuthenticationFactory() {
    }

    private static void initializeMap() {
        keyIDToAuthenticationMap = new HashMap<LispKeyIDEnum, ILispAuthentication>();
        keyIDToAuthenticationMap.put(LispKeyIDEnum.NONE, LispNoAuthentication.getInstance());
        keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA1,
                new LispMACAuthentication(LispKeyIDEnum.SHA1.getAuthenticationName()));
        keyIDToAuthenticationMap.put(LispKeyIDEnum.SHA256,
                new LispMACAuthentication(LispKeyIDEnum.SHA256.getAuthenticationName()));
        keyIDToAuthenticationMap.put(LispKeyIDEnum.UNKNOWN, LispNoAuthentication.getInstance());

    }

    public static ILispAuthentication getAuthentication(LispKeyIDEnum keyID) {
        if (keyIDToAuthenticationMap == null) {
            initializeMap();
        }
        return keyIDToAuthenticationMap.get(keyID);
    }

}
