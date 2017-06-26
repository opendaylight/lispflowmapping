/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentLispSouthboundStatsTest {

    private static ConcurrentLispSouthboundStats lispSouthboundStats;

    @Before
    public void init() {
        lispSouthboundStats = new ConcurrentLispSouthboundStats();
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#resetStats} method.
     */

    @Test
    public void resetStatsTest() throws NoSuchFieldException, IllegalAccessException {
        setRxField(new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8});

        lispSouthboundStats.resetStats();
        for (long value : lispSouthboundStats.getRx()) {
            assertEquals(0, value);
        }
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementRx} method.
     */
    @Test
    public void incrementRxTest() {
        lispSouthboundStats.incrementRx(4);
        long[] rx = lispSouthboundStats.getRx();

        for (int i = 0; i < rx.length; i++) {
            if (i == 4) {
                assertEquals(1, rx[i]);
            } else {
                assertEquals(0, rx[i]);
            }
        }
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementRx} method.
     */
    @Test
    public void incrementRxTest_withMaxValue() throws NoSuchFieldException, IllegalAccessException {
        setRxField(new long []{Long.MAX_VALUE, 1, 1, 1, 1, 1, 1, 1, 1});
        lispSouthboundStats.incrementRx(0);
        long[] rx = lispSouthboundStats.getRx();

        for (int i = 0; i < rx.length; i++) {
            if (i == 0) {
                assertEquals(0, rx[i]);
            } else {
                assertEquals(1, rx[i]);
            }
        }
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementTx} method.
     */
    @Test
    public void incrementTxTest() {
        lispSouthboundStats.incrementTx(4);
        long[] tx = lispSouthboundStats.getTx();

        for (int i = 0; i < tx.length; i++) {
            if (i == 4) {
                assertEquals(1, tx[i]);
            } else {
                assertEquals(0, tx[i]);
            }
        }
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementRxUnknown} method.
     */
    @Test
    public void incrementRxUnknownTest() throws NoSuchFieldException, IllegalAccessException {
        setRxUnkownField(100L);
        lispSouthboundStats.incrementRxUnknown();

        assertEquals(101L, lispSouthboundStats.getRxUnknown());
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementRxUnknown} method with Long.MAX_VALUE.
     */
    @Test
    public void incrementRxUnknownTest_withMaxValue() throws NoSuchFieldException, IllegalAccessException {
        setRxUnkownField(Long.MAX_VALUE);
        lispSouthboundStats.incrementRxUnknown();

        assertEquals(0, lispSouthboundStats.getRxUnknown());
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementTxErrors} method.
     */
    @Test
    public void incrementTxErrorsTest() throws NoSuchFieldException, IllegalAccessException {
        setTxErrorsField(100L);
        lispSouthboundStats.incrementTxErrors();

        assertEquals(101L, lispSouthboundStats.getTxErrors());
    }

    /**
     * Tests {@link ConcurrentLispSouthboundStats#incrementTxErrors} method with Long.MAX_VALUE.
     */
    @Test
    public void incrementTxErrorsTest_withMaxValue() throws NoSuchFieldException, IllegalAccessException {
        setTxErrorsField(Long.MAX_VALUE);
        lispSouthboundStats.incrementTxErrors();

        assertEquals(0, lispSouthboundStats.getTxErrors());
    }

    private static void setRxField(long[] array) throws NoSuchFieldException, IllegalAccessException {
        Field rx = ConcurrentLispSouthboundStats.class.getDeclaredField("rx");
        rx.setAccessible(true);
        rx.set(lispSouthboundStats, array);
    }

    private static void setRxUnkownField(long value) throws NoSuchFieldException, IllegalAccessException {
        Field rxUnknown = ConcurrentLispSouthboundStats.class.getDeclaredField("rxUnknown");
        rxUnknown.setAccessible(true);
        rxUnknown.set(lispSouthboundStats, value);
    }

    private static void setTxErrorsField(long value) throws NoSuchFieldException, IllegalAccessException {
        Field txErrors = ConcurrentLispSouthboundStats.class.getDeclaredField("txErrors");
        txErrors.setAccessible(true);
        txErrors.set(lispSouthboundStats, value);
    }
}
