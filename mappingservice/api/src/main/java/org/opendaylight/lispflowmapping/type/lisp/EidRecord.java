/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

public class EidRecord {
    /**
     * EID mask-len: This is the mask length for the EID-Prefix.
     */
    private byte maskLength;
    /**
     * EID-Prefix-AFI: This is the address family of the EID-Prefix according to
     * [AFI].
     * 
     * EID-Prefix: This prefix is 4 octets for an IPv4 address family and 16
     * octets for an IPv6 address family. When a Map-Request is sent by an ITR
     * because a data packet is received for a destination where there is no
     * mapping entry, the EID-Prefix is set to the destination IP address of the
     * data packet, and the 'EID mask-len' is set to 32 or 128 for IPv4 or IPv6,
     * respectively. When an xTR wants to query a site about the status of a
     * mapping it already has cached, the EID-Prefix used in the Map-Request has
     * the same mask length as the EID-Prefix returned from the site when it
     * sent a Map-Reply message.
     */
    private LispAddress prefix;

    public EidRecord(byte maskLength, LispAddress prefix) {
        setMaskLength(maskLength);
        setPrefix(prefix);
    }

    public int getMaskLength() {
        return maskLength & 0xFF;
    }

    public void setMaskLength(int maskLength) {
        this.maskLength = (byte) maskLength;
    }

    public LispAddress getPrefix() {
        return prefix;
    }

    public void setPrefix(LispAddress prefix) {
        this.prefix = (prefix != null) ? prefix : NO_PREFIX;
    }

    private static LispAddress NO_PREFIX = new LispNoAddress();
}
