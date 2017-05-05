/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.authentication;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LispAuthenticationUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(LispAuthenticationUtil.class);

    private static final short MAP_REGISTER_AND_MAP_NOTIFY_KEY_ID_POS = 12;

    // Utility class, should not be instantiated
    private LispAuthenticationUtil() {
    }

    private static ILispAuthentication resolveAuthentication(final MapRegister mapRegister, final Eid eid, final
            MappingAuthkey key) {
        if (key == null) {
            LOG.warn("Authentication failed: mapping authentication for EID {} key is null",
                    LispAddressStringifier.getString(eid));
            return null;
        }
        short keyId = 0;
        if (mapRegister.getKeyId() != null) {
            keyId = mapRegister.getKeyId();
        }
        if (keyId != key.getKeyType().shortValue()) {
            LOG.warn("Authentication failed: key-ID in Map-Register ({}) is different from the one on file ({}) for {}",
                    keyId, key.getKeyType().shortValue(),
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

    public static byte[] createAuthenticationData(final MapNotify mapNotify, String authKey) {
        return createAuthenticationData(MapNotifySerializer.getInstance().serialize(mapNotify), authKey);
    }

    public static byte[] createAuthenticationData(final ByteBuffer buffer, String authKey) {
        final short keyId = buffer.getShort(MAP_REGISTER_AND_MAP_NOTIFY_KEY_ID_POS);
        final ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(
                LispKeyIDEnum.valueOf(keyId));
        final int authenticationLength = authentication.getAuthenticationLength();
        buffer.position(ILispAuthentication.MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        buffer.put(new byte[authenticationLength]);
        return authentication.getAuthenticationData(buffer, authKey);
    }
}
