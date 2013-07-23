/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

public enum MapReplyAction {
    /**
     * (0) No-Action: The map-cache is kept alive, and no packet encapsulation
     * occurs.
     */
    NoAction(0), //
    /**
     * (1) Natively-Forward: The packet is not encapsulated or dropped but
     * natively forwarded.
     */
    NativelyForward(1), //
    /**
     * (2) Send-Map-Request: The packet invokes sending a Map-Request.
     */
    SendMapRequest(2), //
    /**
     * (3) Drop: A packet that matches this map-cache entry is dropped. An ICMP
     * Destination Unreachable message SHOULD be sent.
     */
    Drop(3);

    private int code;

    private MapReplyAction(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MapReplyAction valueOf(int code) {
        for (MapReplyAction action : values()) {
            if (action.getCode() == code) {
                return action;
            }
        }
        return null;
    }
}
