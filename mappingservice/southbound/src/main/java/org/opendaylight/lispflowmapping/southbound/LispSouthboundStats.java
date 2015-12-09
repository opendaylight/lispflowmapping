/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;

/**
 * Object to hold statistics about LISP southbound events
 *
 * @author Lorand Jakab
 *
 */
public class LispSouthboundStats {
    public final static int MAX_LISP_TYPES = getMaxMessageTypeValue();

    private long rx[] = new long[MAX_LISP_TYPES + 1];
    private long tx[] = new long[MAX_LISP_TYPES + 1];
    private long rxUnknown = 0;
    private long txErrors = 0;

    public LispSouthboundStats() {
        resetStats();
    }

    public void resetStats() {
        for (int i = 0; i <= MAX_LISP_TYPES; i++) {
            rx[i] = 0;
            tx[i] = 0;
        }
    }

    public long[] getRx() {
        return rx;
    }

    public void incrementRx(int type) {
        this.rx[type] = incrementWithWrap(rx[type]);
    }

    public long[] getTx() {
        return tx;
    }

    public void incrementTx(int type) {
        this.tx[type] = incrementWithWrap(tx[type]);
    }

    public long getRxUnknown() {
        return rxUnknown;
    }

    public void incrementRxUnknown() {
        this.rxUnknown = incrementWithWrap(rxUnknown);
    }

    public long getTxErrors() {
        return txErrors;
    }

    public void incrementTxErrors() {
        this.txErrors = incrementWithWrap(txErrors);
    }

    private static long incrementWithWrap(long value) {
        if (value == Long.MAX_VALUE) {
            return 0;
        } else {
            return value + 1;
        }
    }

    // TODO move this method to the appropriate helper class if we start using MessageType in other places
    public static int getMaxMessageTypeValue() {
        int max = 0;
        for (MessageType mt : MessageType.values()) {
            if (mt.getIntValue() > max) {
                max = mt.getIntValue();
            }
        }
        return max;
    }
}
