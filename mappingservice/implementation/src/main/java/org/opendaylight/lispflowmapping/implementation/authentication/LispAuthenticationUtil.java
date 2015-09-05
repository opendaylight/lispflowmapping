/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.authentication;

import org.opendaylight.lispflowmapping.interfaces.lisp.ILispAuthentication;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;

public class LispAuthenticationUtil {

    public static boolean validate(MapRegister mapRegister, String key) {
        short keyId = 0;
        if (mapRegister.getKeyId() != null) {
            keyId = mapRegister.getKeyId();
        }
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(keyId));
        return authentication.validate(mapRegister, key);
    }

    public static byte[] createAuthenticationData(MapNotify mapNotify, String key) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        return authentication.getAuthenticationData(mapNotify, key);
    }

}
