/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import static org.mockito.Mockito.verify;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValue;

final class MapRegisterCacheTestUtil {
    static final byte[] DATA1 = new byte[] {
        //Type, P, I, R, Reserved, M, Record Count
        0x33, 0x00, 0x01, 0x01
    };

    static final byte[] NONCE = new byte[] {
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00
    };

    static final byte[] KEY_ID = new byte[] {
        0x00, 0x01
    };

    static final byte[] AUTH_DATA = new byte[] {
        //authentication data length
        0x00, 0x14,
        //auth data
        (byte)0xfa, 0x50, 0x1d, (byte)0xd6,
        //auth data
        0x63, 0x53, 0x67, 0x6c,
        //auth data
        0x00, 0x3c, 0x61, 0x67,
        //auth data
        0x35, (byte) 0xb8, (byte) 0xcf, 0x16,
        //auth data
        (byte) 0x91, (byte) 0xcd, 0x6b, (byte) 0x95
    };

    static final byte[] DATA2 = new byte[] {
        //TTL
        0x00, 0x00, 0x00, 0x6f,
        //Rsvd, Map-Version Number
        0x01, 0x08, 0x00, 0x00,
        0x00, 0x01
    };

    static final byte[] DATA3 = new byte[] {
        //Priority, Weight, M Priority, M Weight
        0x01, 0x0c, 0x0d, 0x0b,
        //Unused Flags, L, p, R, Loc-Afi
        0x00, 0x00, 0x00, 0x01,
        //Locator
        0x0c, 0x0c, 0x0c, 0x0c
    };

    static final byte[] XTR_ID = new byte[] {
        0x53, 0x61, 0x6e, 0x20,
        0x4a, 0x6f, 0x73, 0x65,
        0x2c, 0x20, 0x32, 0x66,
        0x33, 0x72, 0x32, 0x64
    };

    static final byte[] SITE_ID = new byte[] {
        0x33, 0x2c, 0x31, 0x34,
        0x31, 0x35, 0x39, 0x32
    };


    private MapRegisterCacheTestUtil() {
        throw new UnsupportedOperationException();
    }

    static byte[] joinArrays(byte[] firstArray, byte[] ... arrays) {
        byte[] result = firstArray;
        for (byte[] array : arrays) {
            result = ArrayUtils.addAll(result, array);
        }
        return result;
    }

    static void afterSecondMapRegisterInvocationValidation(final LispSouthboundPlugin mockLispSouthboundPlugin,
                                                            byte[] eidPrefixAfi, byte[] eidPrefix) throws
            InterruptedException, UnknownHostException {
        final byte[] resetForMapNotify = new byte[]{
            0x4C, 0x00, 0x00, 0x01
        };

        final byte[] newAuthenticationData = new byte [] {
            // authentication data length
            (byte) 0x00, (byte) 0x14,
            (byte) 0x7A, (byte) 0x53, (byte) 0x7F, (byte) 0xF6,
            (byte) 0xB8, (byte) 0x91, (byte) 0x33, (byte) 0x7F,
            (byte) 0xB1, (byte) 0x41, (byte) 0xC3, (byte) 0x51,
            (byte) 0x2B, (byte) 0xF8, (byte) 0x9D, (byte) 0x87,
            (byte) 0x30, (byte) 0x6E, (byte) 0xEE, (byte) 0x08
        };

        final ByteBuffer byteBuffer = ByteBuffer.wrap(joinArrays(resetForMapNotify, NONCE, KEY_ID,
                newAuthenticationData, DATA2, eidPrefixAfi, eidPrefix, DATA3, XTR_ID, SITE_ID));

        verify(mockLispSouthboundPlugin).handleSerializedLispBuffer(
                Mockito.eq(Inet4Address.getByAddress("0.0.0.0", new byte[]{0, 0, 0, 0})),
                Mockito.eq(byteBuffer),
                Mockito.eq(MessageType.MapNotify),
                Mockito.eq(0),
                Mockito.eq(null));
    }

    static void afterMapRegisterInvocationValidation(final MapRegisterCacheKey mapRegisterCacheKey,
            final MapRegisterCache mapRegisterCache, final byte[] eidPrefixAfi, byte[] eidPrefix)
                    throws InterruptedException {
        Assert.assertEquals(1, mapRegisterCache.cacheSize());
        final MapRegisterCacheValue currentMapRegisterCacheValue = mapRegisterCache.getEntry(mapRegisterCacheKey);
        Assert.assertNotNull(currentMapRegisterCacheValue);
        final byte[] currentMapRegisterMsg = currentMapRegisterCacheValue.getPacketData();
        final byte[] expectedMapRegisterMsg = joinArrays(DATA1, KEY_ID, DATA2, eidPrefixAfi, eidPrefix, DATA3, XTR_ID,
                SITE_ID);
        Assert.assertArrayEquals(expectedMapRegisterMsg, currentMapRegisterMsg);
    }

    static MapRegisterCacheKey createMapRegisterCacheKey(final byte[] eidPrefix) {
        final MapRegisterCacheKeyBuilder mapRegisterCacheKeyBuilder = new MapRegisterCacheKeyBuilder();
        mapRegisterCacheKeyBuilder.setXtrId(XTR_ID);
        mapRegisterCacheKeyBuilder.setSiteId(SITE_ID);
        mapRegisterCacheKeyBuilder.setEidPrefix(eidPrefix);
        return mapRegisterCacheKeyBuilder.build();
    }

    static void beforeMapRegisterInvocationValidation(final MapRegisterCacheKey mapRegisterCacheKey,
                                                       final MapRegisterCache mapRegisterCache) {
        Assert.assertEquals(0, mapRegisterCache.cacheSize());
        Assert.assertNull(mapRegisterCache.getEntry(mapRegisterCacheKey));
    }
}
