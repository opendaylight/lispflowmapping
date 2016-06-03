/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.authentication;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Before;
import org.junit.Test;

public class LispMACAuthenticationTest {

    private static final String MAP_REQUEST_PACKET =
            "10 00 00 01 3d 8d 2a cd 39 c8 d6 " +
            "08 00 01 01 02 03 04 00 01 7f 00 " +
            "00 02 00 20 00 01 7f 00 00 01";

    private static LispMACAuthentication lispMACAuthentication;
    private static byte[] expectedAuthData;
    private static final String KEY = "password";
    private static ByteBuffer byteBuffer;
    private static final String ALGORITHM = LispKeyIDEnum.SHA1.getAuthenticationName();

    @Before
    public void init() throws InvalidKeyException, NoSuchAlgorithmException {
        lispMACAuthentication = new LispMACAuthentication(ALGORITHM);
        byteBuffer = ByteBuffer.allocate(100);
        byteBuffer.put(toBytePacket(MAP_REQUEST_PACKET));
        expectedAuthData = getExpectedAuthData();
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method with key = null.
     */
    @Test
    public void validateTest_withNullKey() {
        assertEquals(false, lispMACAuthentication.validate(byteBuffer, expectedAuthData, null));
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method with mapRegisterBuffer = null.
     */
    @Test
    public void validateTest_withNullBuffer() {
        assertEquals(true, lispMACAuthentication.validate(null, expectedAuthData, KEY));
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method.
     */
    @Test
    public void validateTest() {
        //TODO
        assertEquals(false, lispMACAuthentication.validate(byteBuffer, expectedAuthData, KEY));
    }

    private static byte[] toBytePacket(String packetString) {
        final String[] tokens = packetString.split("\\s+");
        ByteBuffer buffer = ByteBuffer.allocate(tokens.length);

        for (String token : tokens) {
            buffer.put((byte) Integer.parseInt(token, 16));
        }

        return buffer.array();
    }

    private static byte[] getExpectedAuthData() throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        final Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(secretKey);
        return mac.doFinal(byteBuffer.array());
    }
}
