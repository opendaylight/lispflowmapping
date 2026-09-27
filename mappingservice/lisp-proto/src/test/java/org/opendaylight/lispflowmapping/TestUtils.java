/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

public final class TestUtils {

    private TestUtils() {
        // utility class
    }

    public static ByteBuffer hexToByteBuffer(String hex) {
        String[] hexBytes = hex.split(" ");
        ByteBuffer bb = ByteBuffer.allocate(hexBytes.length);
        for (String hexByte : hexBytes) {
            bb.put((byte) Integer.parseInt(hexByte, 16));
        }
        bb.clear();
        return bb;
    }

    public static void assertHexEquals(short expected, short actual) {
        assertEquals("0x%04X".formatted(expected), "0x%04X".formatted(actual));
    }

    public static void assertHexEquals(byte expected, byte actual) {
        assertEquals("0x%02X".formatted(expected), "0x%02X".formatted(actual));
    }
}
