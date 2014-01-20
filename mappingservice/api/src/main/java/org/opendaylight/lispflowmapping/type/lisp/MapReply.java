/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *        0                   1                   2                   3
 *        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *       |Type=2 |P|E|S|          Reserved               | Record Count  |
 *       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *       |                         Nonce . . .                           |
 *       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *       |                         . . . Nonce                           |
 *   +-> +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |   |                          Record TTL                           |
 *   |   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   R   | Locator Count | EID mask-len  | ACT |A|      Reserved         |
 *   e   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   c   | Rsvd  |  Map-Version Number   |       EID-Prefix-AFI          |
 *   o   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   r   |                          EID-Prefix                           |
 *   d   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |  /|    Priority   |    Weight     |  M Priority   |   M Weight    |
 *   | L +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   | o |        Unused Flags     |L|p|R|           Loc-AFI             |
 *   | c +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |  \|                             Locator                           |
 *   +-> +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author gmainzer
 * 
 */
public class MapReply {
    /**
     * P: This is the probe-bit, which indicates that the Map-Reply is in
     * response to a Locator reachability probe Map-Request. The 'Nonce' field
     * MUST contain a copy of the nonce value from the original Map-Request. See
     * Section 6.3.2 for more details.
     */
    private boolean probe;
    /**
     * E: This bit indicates that the ETR that sends this Map-Reply message is
     * advertising that the site is enabled for the Echo-Nonce Locator
     * reachability algorithm. See Section 6.3.1 for more details.
     */
    private boolean echoNonceEnabled;
    /**
     * S: This is the Security bit. When set to 1, the following authentication
     * information will be appended to the end of the Map-Reply. The detailed
     * format of the Authentication Data Content is for further study.
     * 
     * <pre>
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |    AD Type    |       Authentication Data Content . . .       |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     */
    private boolean securityEnabled;

    /**
     * Nonce: This is a 24-bit value set in a Data-Probe packet, or a 64-bit
     * value from the Map-Request is echoed in this 'Nonce' field of the
     * Map-Reply. When a 24-bit value is supplied, it resides in the low-order
     * 64 bits of the 'Nonce' field.
     */
    private long nonce;

    /**
     * Record Count: This is the number of records in this reply message. A
     * record is comprised of that portion of the packet labeled 'Record' above
     * and occurs the number of times equal to Record Count.
     * 
     * private byte recordCount;
     */
    private List<EidToLocatorRecord> eidToLocatorRecords;

    public MapReply() {
        eidToLocatorRecords = new ArrayList<EidToLocatorRecord>();
    }

    public void addEidToLocator(EidToLocatorRecord record) {
        eidToLocatorRecords.add(record);
    }

    public long getNonce() {
        return nonce;
    }

    public MapReply setNonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public List<EidToLocatorRecord> getEidToLocatorRecords() {
        return eidToLocatorRecords;
    }

    public boolean isProbe() {
        return probe;
    }

    public MapReply setProbe(boolean probe) {
        this.probe = probe;
        return this;
    }

    public boolean isEchoNonceEnabled() {
        return echoNonceEnabled;
    }

    public MapReply setEchoNonceEnabled(boolean echoNonceEnabled) {
        this.echoNonceEnabled = echoNonceEnabled;
        return this;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public MapReply setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
        return this;
    }

    public void setEidToLocatorRecords(List<EidToLocatorRecord> eidToLocatorRecords) {
        this.eidToLocatorRecords = eidToLocatorRecords;
    }

    
}
