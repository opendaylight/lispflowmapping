/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util;

import java.nio.ByteBuffer;

public class ByteUtil {

    public static int getUnsignedByte(byte[] inBuffer, int pos) {
        return inBuffer[pos] & 0xFF;
    }

    public static int getUnsignedByte(ByteBuffer inBuffer, int pos) {
        return inBuffer.get(pos) & 0xFF;
    }

    public static int getUnsignedShort(byte[] inBuffer, int pos) {
        return getShort(inBuffer, pos) & 0xFFFF;
    }

    public static int getUnsignedShort(ByteBuffer inBuffer, int pos) {
        return inBuffer.getShort(pos) & 0xFFFF;
    }

    public static short getShort(byte[] inBuffer, int pos) {
        return ByteBuffer.wrap(inBuffer, pos, 2).getShort();
    }

    public static int getInt(byte[] inBuffer, int pos) {
        return ByteBuffer.wrap(inBuffer, pos, 4).getInt();
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
}
