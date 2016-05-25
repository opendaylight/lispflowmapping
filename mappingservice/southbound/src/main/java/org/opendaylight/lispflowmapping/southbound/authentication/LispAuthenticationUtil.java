/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.authentication;

import java.nio.ByteBuffer;
import org.opendaylight.lispflowmapping.interfaces.lisp.ILispAuthentication;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LispAuthenticationUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(LispAuthenticationUtil.class);

    // Utility class, should not be instantiated
    private LispAuthenticationUtil() {
    }

    private static ILispAuthentication resolveAuthentication(final MapRegister mapRegister, final Eid eid, final
            MappingAuthkey key) {
        if (key == null) {
            LOG.warn("Authentication failed: mapping authentication key is null");
            return null;
        }
        short keyId = 0;
        if (mapRegister.getKeyId() != null) {
            keyId = mapRegister.getKeyId();
        }
        if (keyId != key.getKeyType().shortValue()) {
            LOG.warn("Authentication failed: key-ID in Map-Register is different from the one on file for {}",
                    LispAddressStringifier.getString(eid));
            return null;
        }
        return LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(keyId));
    }


    public static boolean validate(MapRegister mapRegister, ByteBuffer byteBuffer, Eid eid, MappingAuthkey key) {
        final ILispAuthentication authentication = resolveAuthentication(mapRegister, eid, key);
        return authentication == null ? false : authentication.validate(byteBuffer, mapRegister.getAuthenticationData(),
                key.getKeyString());
    }

    public static byte[] createAuthenticationData(MapNotify mapNotify, String key) {
        // We assume that the key-ID is correctly set in the Map-Notify message
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(
                LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        return authentication.getAuthenticationData(mapNotify, key);
    }
}
