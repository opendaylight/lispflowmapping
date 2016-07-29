/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.util;

import java.nio.ByteBuffer;

public final class ByteUtil {

    protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    // Utility class, should not be instantiated
    private ByteUtil() {
    }

    public static int getUnsignedByte(byte[] inBuffer, int pos) {
        return inBuffer[pos] & 0xFF;
    }

    public static int getUnsignedByte(ByteBuffer inBuffer, int pos) {
        return inBuffer.get(pos) & 0xFF;
    }

    public static int getUnsignedByte(ByteBuffer inBuffer) {
        return inBuffer.get() & 0xFF;
    }

    public static int getUnsignedShort(byte[] inBuffer, int pos) {
        return asUnsignedShort(getShort(inBuffer, pos));
    }

    public static int getUnsignedShort(ByteBuffer inBuffer, int pos) {
        return asUnsignedShort(inBuffer.getShort(pos));
    }

    public static short getShort(byte[] inBuffer, int pos) {
        return ByteBuffer.wrap(inBuffer, pos, 2).getShort();
    }

    public static int getInt(byte[] inBuffer, int pos) {
        return ByteBuffer.wrap(inBuffer, pos, 4).getInt();
    }

    public static int getPartialInt(byte[] inBuffer) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.position(4 - inBuffer.length);
        buffer.put(inBuffer);
        buffer.position(0);
        return buffer.getInt();
    }

    public static short asUnsignedByte(byte byteArg) {
        return (short) ((short) byteArg & 0xFF);
    }

    public static int asUnsignedShort(short shortArg) {
        return shortArg & 0xFFFF;
    }

    public static long asUnsignedInteger(int intArg) {
        return intArg & 0xFFFFFFFF;
    }

    public static byte[] partialIntToByteArray(int number, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number);
        byte[] result = new byte[length];
        buffer.position(4 - length);
        buffer.get(result);
        return result;
    }

    public static long getLong(byte[] inBuffer, int pos) {
        return ByteBuffer.wrap(inBuffer, pos, 8).getLong();
    }

    public static boolean extractBit(byte byteValue, int bitMask) {
        return (byteValue & bitMask) == bitMask;
    }

    public static byte boolToBit(boolean flag, int bit) {
        return (byte) (flag ? bit : 0x00);
    }

    public static String bytesToHex(byte[] bytes, int length) {
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int vv = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[vv >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[vv & 0x0F];
        }
        return new String(hexChars);
    }
}
