/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <pre>
 *         0                   1                   2                   3
 *         0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |Type=3 |P|            Reserved               |M| Record Count  |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |                         Nonce . . .                           |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |                         . . . Nonce                           |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |            Key ID             |  Authentication Data Length   |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        ~                     Authentication Data                       ~
 *    +-  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |   |                          Record TTL                           |
 *    |   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    R   | Locator Count | EID mask-len  | ACT |A|      Reserved         |
 *    e   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    c   | Rsvd  |  Map-Version Number   |        EID-Prefix-AFI         |
 *    o   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    r   |                          EID-Prefix                           |
 *    d   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |  /|    Priority   |    Weight     |  M Priority   |   M Weight    |
 *    | L +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | o |        Unused Flags     |L|p|R|           Loc-AFI             |
 *    | c +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |  \|                             Locator                           |
 *    +-  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * @author gmainzer
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MapRegister {
    /**
     * P: This is the proxy Map-Reply bit. When set to 1, an ETR sends a
     * Map-Register message requesting the Map-Server to proxy a Map-Reply. The
     * Map-Server will send non-authoritative Map-Replies on behalf of the ETR.
     * Details on this usage can be found in [RFC6833].
     */
    @XmlElement
    private boolean proxyMapReply;
    /**
     * M: This is the want-map-notify bit. When set to 1, an ETR is requesting a
     * Map-Notify message to be returned in response to sending a Map-Register
     * message. The Map-Notify message sent by a Map-Server is used to
     * acknowledge receipt of a Map-Register message.
     */
    private boolean wantMapNotify;

    /**
     * Nonce: This 8-octet 'Nonce' field is set to 0 in Map-Register messages.
     * Since the Map-Register message is authenticated, the 'Nonce' field is not
     * currently used for any security function but may be in the future as part
     * of an anti-replay solution.
     */
    @XmlElement
    private long nonce;

    /**
     * Key ID: This is a configured ID to find the configured Message
     * Authentication Code (MAC) algorithm and key value used for the
     * authentication function. See Section 14.4 for codepoint assignments.
     */
    @XmlElement
    private short keyId;

    /**
     * Authentication Data Length: This is the length in octets of the
     * 'Authentication Data' field that follows this field. The length of the
     * 'Authentication Data' field is dependent on the MAC algorithm used. The
     * length field allows a device that doesn't know the MAC algorithm to
     * correctly parse the packet. private short authenticationLength;
     */

    /**
     * Authentication Data: This is the message digest used from the output of
     * the MAC algorithm. The entire Map-Register payload is authenticated with
     * this field preset to 0. After the MAC is computed, it is placed in this
     * field. Implementations of this specification MUST include support for
     * HMAC-SHA-1-96 [RFC2404], and support for HMAC-SHA-256-128 [RFC4868] is
     * RECOMMENDED.
     */

    private byte[] authenticationData;

    /**
     * The representation in bytes of the map register.
     */
    private byte[] mapRegisterBytes;

    /**
     * Record Count: This is the number of records in this Map-Register message.
     * A record is comprised of that portion of the packet labeled 'Record'
     * above and occurs the number of times equal to Record Count.
     *
     * private byte recordCount;
     */
    @XmlElement
    private List<EidToLocatorRecord> eidToLocatorRecords;

    public MapRegister() {
        eidToLocatorRecords = new ArrayList<EidToLocatorRecord>();
        setAuthenticationData(null);
    }

    public void addEidToLocator(EidToLocatorRecord record) {
        eidToLocatorRecords.add(record);
    }

    public List<EidToLocatorRecord> getEidToLocatorRecords() {
        return eidToLocatorRecords;
    }

    public boolean isProxyMapReply() {
        return proxyMapReply;
    }

    public void setProxyMapReply(boolean proxyMapReply) {
        this.proxyMapReply = proxyMapReply;
    }

    public byte[] getAuthenticationData() {
        return authenticationData;
    }

    public MapRegister setAuthenticationData(byte[] authenticationData) {
        this.authenticationData = (authenticationData != null) ? authenticationData : NO_AUTHENTICATION_DATA;
        return this;
    }

    public boolean isWantMapNotify() {
        return wantMapNotify;
    }

    public MapRegister setWantMapNotify(boolean wantMapNotify) {
        this.wantMapNotify = wantMapNotify;
        return this;
    }

    public long getNonce() {
        return nonce;
    }

    public MapRegister setNonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public short getKeyId() {
        return keyId;
    }

    public MapRegister setKeyId(short keyId) {
        this.keyId = keyId;
        return this;
    }

    public byte[] getMapRegisterBytes() {
        return mapRegisterBytes;
    }

    public MapRegister setMapRegisterBytes(byte[] mapRegisterBytes) {
        this.mapRegisterBytes = Arrays.copyOf(mapRegisterBytes, mapRegisterBytes.length);
        return this;
    }

    private static byte[] NO_AUTHENTICATION_DATA = new byte[] {};
}
