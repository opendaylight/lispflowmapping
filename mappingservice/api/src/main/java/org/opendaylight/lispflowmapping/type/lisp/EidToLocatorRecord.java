/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

/**
 * <pre>
 *        0                   1                   2                   3
 *        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
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

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EidToLocatorRecord {
    /**
     * Record TTL: This is the time in minutes the recipient of the Map-Reply
     * will store the mapping. If the TTL is 0, the entry SHOULD be removed from
     * the cache immediately. If the value is 0xffffffff, the recipient can
     * decide locally how long to store the mapping.
     */
    @XmlElement
    private int recordTtl;

    /**
     * EID mask-len: This is the mask length for the EID-Prefix.
     */
    @XmlElement
    private byte maskLength;

    /**
     * ACT: This 3-bit field describes Negative Map-Reply actions. In any other
     * message type, these bits are set to 0 and ignored on receipt. These bits
     * are used only when the 'Locator Count' field is set to 0. The action bits
     * are encoded only in Map-Reply messages. The actions defined are used by
     * an ITR or PITR when a destination EID matches a negative Map-Cache entry.
     * Unassigned values should cause a Map-Cache entry to be created, and when
     * packets match this negative cache entry, they will be dropped. The
     * current assigned values are:
     */
    @XmlElement
    private MapReplyAction action;
    /**
     * A: The Authoritative bit, when sent, is always set to 1 by an ETR. When a
     * Map-Server is proxy Map-Replying [RFC6833] for a LISP site, the
     * Authoritative bit is set to 0. This indicates to requesting ITRs that the
     * Map-Reply was not originated by a LISP node managed at the site that owns
     * the EID-Prefix.
     */
    @XmlElement
    private boolean authoritative;
    /**
     * Map-Version Number: When this 12-bit value is non-zero, the Map-Reply
     * sender is informing the ITR what the version number is for the EID record
     * contained in the Map-Reply. The ETR can allocate this number internally
     * but MUST coordinate this value with other ETRs for the site. When this
     * value is 0, there is no versioning information conveyed. The Map-Version
     * Number can be included in Map-Request and Map-Register messages. See
     * Section 6.6.3 for more details.
     */
    @XmlElement
    private short mapVersion;

    /**
     * EID-Prefix-AFI: Address family of the EID-Prefix according to [AFI].
     * 
     * EID-Prefix: This prefix is 4 octets for an IPv4 address family and 16
     * octets for an IPv6 address family.
     */

    private LispAddress prefix;

    /**
     * To be used on the NB interface, prior to parse and convert it into a
     * specific LISP address type
     */
    @XmlElement
    private LispAddressGeneric prefixGeneric;

    public void setPrefixGeneric(LispAddressGeneric prefixGeneric) {
        this.prefixGeneric = prefixGeneric;
    }

    public LispAddressGeneric getPrefixGeneric() {
        return prefixGeneric;
    }

    /**
     * Locator Count: This is the number of Locator entries. A Locator entry
     * comprises what is labeled above as 'Loc'. The Locator count can be 0,
     * indicating that there are no Locators for the EID-Prefix.
     * 
     * private byte locatorCount;
     */
    @XmlElement
    private List<LocatorRecord> locators;

    public EidToLocatorRecord() {
        locators = new ArrayList<LocatorRecord>();
        setAction(null);
        setPrefix(null);
    }

    public EidToLocatorRecord setMaskLength(int maskLength) {
        this.maskLength = (byte) maskLength;
        return this;
    }

    public int getMaskLength() {
        return maskLength;
    }

    public LispAddress getPrefix() {
        return prefix;
    }

    public EidToLocatorRecord setPrefix(LispAddress prefix) {
        this.prefix = (prefix != null) ? prefix : NO_PREFIX;
        return this;
    }

    public List<LocatorRecord> getLocators() {
        return locators;
    }

    public void addLocator(LocatorRecord record) {
        locators.add(record);
    }

    public int getRecordTtl() {
        return recordTtl;
    }

    public EidToLocatorRecord setRecordTtl(int recordTtl) {
        this.recordTtl = recordTtl;
        return this;
    }

    public MapReplyAction getAction() {
        return action;
    }

    public EidToLocatorRecord setAction(MapReplyAction action) {
        this.action = (action != null) ? action : MapReplyAction.NoAction;
        return this;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public EidToLocatorRecord setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
        return this;
    }

    public short getMapVersion() {
        return mapVersion;
    }

    public EidToLocatorRecord setMapVersion(short mapVersion) {
        this.mapVersion = mapVersion;
        return this;
    }

    public EidToLocatorRecord clone() {
        EidToLocatorRecord cloned = new EidToLocatorRecord();
        cloned.setAction(getAction());
        cloned.setAuthoritative(isAuthoritative());
        cloned.setMapVersion(getMapVersion());
        cloned.setMaskLength(getMaskLength());
        cloned.setPrefix(getPrefix());
        cloned.setRecordTtl(getRecordTtl());
        for (LocatorRecord record : getLocators()) {
            cloned.addLocator(record.clone());
        }
        return cloned;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + (authoritative ? 1231 : 1237);
        result = prime * result + ((locators == null) ? 0 : locators.hashCode());
        result = prime * result + mapVersion;
        result = prime * result + maskLength;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + recordTtl;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EidToLocatorRecord other = (EidToLocatorRecord) obj;
        if (action != other.action)
            return false;
        if (authoritative != other.authoritative)
            return false;
        if (locators == null) {
            if (other.locators != null)
                return false;
        } else if (!locators.equals(other.locators))
            return false;
        if (mapVersion != other.mapVersion)
            return false;
        if (maskLength != other.maskLength)
            return false;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        if (recordTtl != other.recordTtl)
            return false;
        return true;
    }

    private static LispNoAddress NO_PREFIX = new LispNoAddress();
}
