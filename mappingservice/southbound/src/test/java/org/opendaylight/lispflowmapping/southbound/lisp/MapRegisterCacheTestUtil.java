/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.opendaylight.lispflowmapping.southbound.lisp.cache.MapRegisterCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.AddMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MappingKeepAlive;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.value.grouping.MapRegisterCacheValue;

final class MapRegisterCacheTestUtil {
    final static byte[] data1 = new byte[] {
            0x33, 0x00, 0x01, 0x01     //Type, P, I, R, Reserved, M, Record Count
    };

    final static byte[] nonce = new byte[] {
            0x00, 0x00, 0x00, 0x00     //nonce
            ,0x00, 0x00, 0x00, 0x00    //nonce
    };

    final static byte[] keyId = new byte[] {
            0x00, 0x01                 //key ID
    };

    final static byte[] authenticationData = new byte[]{
            0x00, 0x14                 //authentication data length
            ,(byte)0xfa, 0x50, 0x1d, (byte)0xd6    //auth data
            ,0x63, 0x53, 0x67, 0x6c    //auth data
            ,0x00, 0x3c, 0x61, 0x67    //auth data
            ,0x35, (byte) 0xb8, (byte) 0xcf, 0x16    //auth data
            ,(byte) 0x91, (byte) 0xcd, 0x6b, (byte) 0x95    //auth data
    };

    final static byte[] data2 = new byte[]{
            0x00, 0x00, 0x00, 0x6f    //TTL
            ,0x01, 0x08, 0x00, 0x00
            ,0x00, 0x01                //Rsvd, Map-Version Number
    };

    final static byte[] data3 = new byte[] {
            0x01, 0x0c, 0x0d, 0x0b     //Priority, Weight, M Priority, M Weight
            ,0x00, 0x00, 0x00, 0x01    //Unused Flags, L, p, R, Loc-Afi
            ,0x0c, 0x0c, 0x0c, 0x0c    //Locator
    };

    final static byte[] xTRId = new byte[]{
            0x53, 0x61, 0x6e, 0x20     //xTR id
            ,0x4a, 0x6f, 0x73, 0x65    //xTR id
            ,0x2c, 0x20, 0x32, 0x66    //xTR id
            ,0x33, 0x72, 0x32, 0x64    //xTR id
    };

    final static byte[] siteId = new byte[]{
            0x33, 0x2c, 0x31, 0x34     //site id
            ,0x31, 0x35, 0x39, 0x32    //site id
    };


    private MapRegisterCacheTestUtil() {
        throw new UnsupportedOperationException();
    }

    static byte[] joinArrays(byte[] firstArray, byte[] ... arrays) {
        byte[] result = firstArray;
        for(byte[] array : arrays) {
            result = ArrayUtils.addAll(result, array);
        }
        return result;
    }

    static NotificationPublishService resetMockForNotificationProvider(final LispSouthboundHandler testedLispService) {
        NotificationPublishService mockedNotificationProvider = mock(NotificationPublishService.class);
        testedLispService.setNotificationProvider(mockedNotificationProvider);
        return mockedNotificationProvider;
    }

    static void afterSecondMapRegisterInvocationValidation(final NotificationPublishService
                                                                    mockedNotificationProvider,
                                                            final LispSouthboundPlugin mockLispSouthboundPlugin,
                                                            byte[] eidPrefixAfi, byte[] eidPrefix) throws
            InterruptedException, UnknownHostException {
        verify(mockedNotificationProvider).putNotification(Mockito.any(MappingKeepAlive.class));

        final byte[] resetForMapNotify = new byte[]{
                0x04, 0x00, 0x00, 0x01
        };

        final byte[] newAuthenticationData = new byte []{
                0x00, 0x14,
                (byte)0xA6,(byte)0xF6,0x7F,(byte)0xC9
                ,0x31,(byte)0x83,0x28,0x5F
                ,(byte)0xDA,0x49,(byte)0xC7,0x3C
                ,0x3E,0x46,0x13,0x27
                ,(byte)0xA7,0x35,0x3D,0x3A
        };

        final ByteBuffer byteBuffer = ByteBuffer.wrap(joinArrays(resetForMapNotify, nonce, keyId, newAuthenticationData,
                data2, eidPrefixAfi, eidPrefix, data3, xTRId, siteId));

        verify(mockLispSouthboundPlugin, Mockito.times(2)).handleSerializedLispBuffer(
                Mockito.eq(Inet4Address.getByAddress("0.0.0.0", new byte[]{0, 0, 0, 0}))
                , Mockito.eq(byteBuffer)
                , Mockito.eq(MessageType.MapNotify)
                , Mockito.eq(0));
    }

    static void afterMapRegisterInvocationValidation(final NotificationPublishService mockedNotificationProvider, final
    MapRegisterCacheKey mapRegisterCacheKey, final MapRegisterCache mapRegisterCache, final byte[] eidPrefixAfi,
                                                     byte[] eidPrefix) throws InterruptedException {
        Mockito.verify(mockedNotificationProvider).putNotification(Mockito.any(AddMapping.class));

        Assert.assertEquals(1, mapRegisterCache.cacheSize());
        final MapRegisterCacheValue currentMapRegisterCacheValue = mapRegisterCache.getEntry(mapRegisterCacheKey);
        Assert.assertNotNull(currentMapRegisterCacheValue);
        final byte[] currentMapRegisterMsg = currentMapRegisterCacheValue.getPacketData();
        final byte[] expectedMapRegisterMsg = joinArrays(data1, keyId, data2, eidPrefixAfi, eidPrefix, data3, xTRId,
                siteId);
        Assert.assertArrayEquals(expectedMapRegisterMsg, currentMapRegisterMsg);
    }

    static MapRegisterCacheKey createMapRegisterCacheKey(final byte[] eidPrefix) {
        final MapRegisterCacheKeyBuilder mapRegisterCacheKeyBuilder = new MapRegisterCacheKeyBuilder();
        mapRegisterCacheKeyBuilder.setXtrId(xTRId);
        mapRegisterCacheKeyBuilder.setSiteId(siteId);
        mapRegisterCacheKeyBuilder.setEidPrefix(eidPrefix);
        return mapRegisterCacheKeyBuilder.build();
    }

    static void beforeMapRegisterInvocationValidation(final MapRegisterCacheKey mapRegisterCacheKey,
                                                       final MapRegisterCache mapRegisterCache) {
        Assert.assertEquals(0, mapRegisterCache.cacheSize());
        Assert.assertNull(mapRegisterCache.getEntry(mapRegisterCacheKey));
    }




}
