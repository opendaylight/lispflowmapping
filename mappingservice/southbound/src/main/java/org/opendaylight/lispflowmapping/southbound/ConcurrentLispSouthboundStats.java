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
 * Object to hold statistics about LISP southbound events.
 *
 * @author Lorand Jakab
 *
 */
public class ConcurrentLispSouthboundStats {
    public static final int MAX_LISP_TYPES = getMaxMessageTypeValue();

    private long[] rx = new long[MAX_LISP_TYPES + 1];
    private long[] tx = new long[MAX_LISP_TYPES + 1];
    private long rxUnknown = 0;
    private long txErrors = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    public ConcurrentLispSouthboundStats() {
        resetStats();
    }

    public synchronized void resetStats() {
        for (int i = 0; i <= MAX_LISP_TYPES; i++) {
            rx[i] = 0;
            tx[i] = 0;
        }
    }

    public synchronized long[] getRx() {
        return rx;
    }

    public synchronized void incrementRx(int type) {
        this.rx[type] = incrementWithWrap(rx[type]);
    }

    public synchronized long[] getTx() {
        return tx;
    }

    public synchronized void incrementTx(int type) {
        this.tx[type] = incrementWithWrap(tx[type]);
    }

    public synchronized long getRxUnknown() {
        return rxUnknown;
    }

    public synchronized void incrementRxUnknown() {
        this.rxUnknown = incrementWithWrap(rxUnknown);
    }

    public synchronized long getTxErrors() {
        return txErrors;
    }

    public synchronized void incrementTxErrors() {
        this.txErrors = incrementWithWrap(txErrors);
    }

    public synchronized long getCacheHits() {
        return cacheHits;
    }

    public synchronized void incrementCacheHits() {
        this.cacheHits = incrementWithWrap(cacheHits);
    }

    public synchronized long getCacheMisses() {
        return cacheMisses;
    }

    public synchronized void incrementCacheMisses() {
        this.cacheMisses = incrementWithWrap(cacheMisses);
    }

    private static synchronized long incrementWithWrap(long value) {
        if (value == Long.MAX_VALUE) {
            return 0;
        } else {
            return value + 1;
        }
    }

    // TODO move this method to the appropriate helper class if we start using MessageType in other places
    public static synchronized int getMaxMessageTypeValue() {
        int max = 0;
        for (MessageType mt : MessageType.values()) {
            if (mt.getIntValue() > max) {
                max = mt.getIntValue();
            }
        }
        return max;
    }
}
