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
        assertEquals(String.format("0x%04X", expected), String.format("0x%04X", actual));
    }

    public static void assertHexEquals(byte expected, byte actual) {
        assertEquals(String.format("0x%02X", expected), String.format("0x%02X", actual));
    }
}
