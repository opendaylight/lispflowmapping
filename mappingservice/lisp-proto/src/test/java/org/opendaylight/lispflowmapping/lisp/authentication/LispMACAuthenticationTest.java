/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.authentication;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Before;
import org.junit.Test;

public class LispMACAuthenticationTest {

    private static final String KEY = "pass";
    private static final String ALGORITHM = LispKeyIDEnum.SHA1.getAuthenticationName();
    private static final String MAP_REQUEST_PACKET =
            "10 00 00 01 3d 8d 2a cd 39 c8 d6 "
          + "08 00 01 01 02 03 04 00 01 7f 00 "
          + "00 02 00 20 00 01 7f 00 00 01";

    private static int macLength;
    private static byte[] expectedAuthData;
    private static ByteBuffer byteBuffer;
    private static LispMACAuthentication lispMACAuthentication;

    @Before
    public void init() throws InvalidKeyException, NoSuchAlgorithmException {
        final byte[] bytePacket = toBytePacket(MAP_REQUEST_PACKET);
        macLength = Mac.getInstance(ALGORITHM).getMacLength();
        lispMACAuthentication = new LispMACAuthentication(ALGORITHM);
        byteBuffer = ByteBuffer.allocate(100);
        byteBuffer.put(bytePacket);
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method with key = null.
     */
    @Test
    public void validateTest_withNullKey() throws InvalidKeyException, NoSuchAlgorithmException {
        expectedAuthData = getExpectedAuthData(ByteBuffer.wrap(byteBuffer.array()));
        assertEquals(false, lispMACAuthentication.validate(byteBuffer, expectedAuthData, null));
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method with mapRegisterBuffer = null.
     */
    @Test
    public void validateTest_withNullBuffer() throws InvalidKeyException, NoSuchAlgorithmException {
        expectedAuthData = getExpectedAuthData(ByteBuffer.wrap(byteBuffer.array()));
        assertEquals(true, lispMACAuthentication.validate(null, expectedAuthData, KEY));
    }

    /**
     * Tests {@link LispMACAuthentication#validate} method.
     */
    @Test
    public void validateTest() throws NoSuchAlgorithmException, InvalidKeyException {
        expectedAuthData = getExpectedAuthData(ByteBuffer.wrap(insertEmptyByteArray(byteBuffer).array()));
        assertEquals(true, lispMACAuthentication.validate(byteBuffer, expectedAuthData, KEY));
    }

    /**
     * Tests {@link LispMACAuthentication#getAuthenticationLength} method.
     */
    @Test
    public void getAuthenticationLengthTest() {
        assertEquals(macLength, lispMACAuthentication.getAuthenticationLength());
    }

    /**
     * Tests {@link LispMACAuthentication#getAlgorithm} method.
     */
    @Test
    public void setAlgorithmTest() {
        final String newAlgorithm = LispKeyIDEnum.SHA256.getAuthenticationName();
        lispMACAuthentication.setAlgorithm(newAlgorithm);
        assertEquals(newAlgorithm, lispMACAuthentication.getAlgorithm());
    }

    /**
     * Tests {@link LispMACAuthentication#getAuthenticationData} method.
     */
    @Test
    public void getAuthenticationDataTest() throws InvalidKeyException, NoSuchAlgorithmException {
        final byte[] expectedResutl = getExpectedAuthData(byteBuffer);
        final byte[] result = lispMACAuthentication.getAuthenticationData(ByteBuffer.wrap(byteBuffer.array()), KEY);
        assertArrayEquals(expectedResutl, result);
    }

    /**
     * Tests {@link LispMACAuthentication#getAuthenticationData} method with no array.
     */
    @Test
    public void getAuthenticationDataTest_withNoArray() throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] expectedResutl = getExpectedAuthData(ByteBuffer.allocateDirect(0));
        byte[] result = lispMACAuthentication.getAuthenticationData(ByteBuffer.allocateDirect(0), KEY);
        assertArrayEquals(expectedResutl, result);
    }

    private static byte[] toBytePacket(String packetString) {
        final String[] tokens = packetString.split("\\s+");
        final ByteBuffer buffer = ByteBuffer.allocate(tokens.length);

        for (String token : tokens) {
            buffer.put((byte) Integer.parseInt(token, 16));
        }

        return buffer.array();
    }

    private static ByteBuffer insertEmptyByteArray(ByteBuffer buffer) {
        buffer.position(ILispAuthentication.MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        buffer.put(new byte[macLength]);
        buffer.position(0);

        return buffer;
    }

    private static byte[] getExpectedAuthData(ByteBuffer buffer) throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        final Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(secretKey);

        return mac.doFinal(buffer.hasArray() ? buffer.array() : new byte[0]);
    }
}
