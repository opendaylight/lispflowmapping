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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;

import com.google.common.collect.Range;

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
public class LocatorRecord {
    /**
     * Priority: Each RLOC is assigned a unicast Priority. Lower values are more
     * preferable. When multiple RLOCs have the same Priority, they MAY be used
     * in a load-split fashion. A value of 255 means the RLOC MUST NOT be used
     * for unicast forwarding.
     */
    @XmlElement
    private short priority;
    /**
     * Weight: When priorities are the same for multiple RLOCs, the Weight
     * indicates how to balance unicast traffic between them. Weight is encoded
     * as a relative weight of total unicast packets that match the mapping
     * entry. For example, if there are 4 Locators in a Locator-Set, where the
     * Weights assigned are 30, 20, 20, and 10, the first Locator will get 37.5%
     * of the traffic, the 2nd and 3rd Locators will get 25% of the traffic, and
     * the 4th Locator will get 12.5% of the traffic. If all Weights for a
     * Locator-Set are equal, the receiver of the Map-Reply will decide how to
     * load-split the traffic. See Section 6.5 for a suggested hash algorithm to
     * distribute the load across Locators with the same Priority and equal
     * Weight values.
     */
    @XmlElement
    private short weight;
    /**
     * M Priority: Each RLOC is assigned a multicast Priority used by an ETR in
     * a receiver multicast site to select an ITR in a source multicast site for
     * building multicast distribution trees. A value of 255 means the RLOC MUST
     * NOT be used for joining a multicast distribution tree. For more details,
     * see [RFC6831].
     */
    @XmlElement
    private short multicastPriority;
    /**
     * M Weight: When priorities are the same for multiple RLOCs, the Weight
     * indicates how to balance building multicast distribution trees across
     * multiple ITRs. The Weight is encoded as a relative weight (similar to the
     * unicast Weights) of the total number of trees built to the source site
     * identified by the EID-Prefix. If all Weights for a Locator-Set are equal,
     * the receiver of the Map-Reply will decide how to distribute multicast
     * state across ITRs. For more details, see [RFC6831].
     */
    @XmlElement
    private short multicastWeight;
    /**
     * L: When this bit is set, the Locator is flagged as a local Locator to the
     * ETR that is sending the Map-Reply. When a Map-Server is doing proxy
     * Map-Replying [RFC6833] for a LISP site, the L-bit is set to 0 for all
     * Locators in this Locator-Set.
     */
    @XmlElement
    private boolean localLocator;
    /**
     * p: When this bit is set, an ETR informs the RLOC-Probing ITR that the
     * locator address for which this bit is set is the one being RLOC-probed
     * and MAY be different from the source address of the Map-Reply. An ITR
     * that RLOC-probes a particular Locator MUST use this Locator for
     * retrieving the data structure used to store the fact that the Locator is
     * reachable. The p-bit is set for a single Locator in the same Locator-Set.
     * If an implementation sets more than one p-bit erroneously, the receiver
     * of the Map-Reply MUST select the first Locator. The p-bit MUST NOT be set
     * for Locator-Set records sent in Map-Request and Map-Register messages.
     */
    @XmlElement
    private boolean rlocProbed;
    /**
     * R: This is set when the sender of a Map-Reply has a route to the Locator
     * in the Locator data record. This receiver may find this useful to know if
     * the Locator is up but not necessarily reachable from the receiver's point
     * of view. See also Section 6.4 for another way the R-bit may be used.
     */
    @XmlElement
    private boolean routed;
    /**
     * Locator: This is an IPv4 or IPv6 address (as encoded by the 'Loc-AFI'
     * field) assigned to an ETR. Note that the destination RLOC address MAY be
     * an anycast address. A source RLOC can be an anycast address as well. The
     * source or destination RLOC MUST NOT be the broadcast address
     * (255.255.255.255 or any subnet broadcast address known to the router) and
     * MUST NOT be a link-local multicast address. The source RLOC MUST NOT be a
     * multicast address. The destination RLOC SHOULD be a multicast address if
     * it is being mapped from a multicast destination EID.
     */

    private LispAddress locator;

    /**
     * To be used on the NB interface, prior to parse and convert it into a
     * specific LISP address type
     */
    @XmlElement
    private LispAddressGeneric locatorGeneric;

    public void setLocatorGeneric(LispAddressGeneric locatorGeneric) {
        this.locatorGeneric = locatorGeneric;
    }

    public LispAddressGeneric getLocatorGeneric() {
        return locatorGeneric;
    }

    public short getPriority() {
        return priority;
    }

    public LocatorRecord setPriority(short priority) {
    	 if (priority != 0) {
             boolean isValidRange = false;
             List<Range<Short>> rangeConstraints = new ArrayList<>(); 
             rangeConstraints.add(Range.closed(new Short("0"), new Short("255")));
             for (Range<Short> r : rangeConstraints) {
                 if (r.contains(priority)) {
                 isValidRange = true;
                 }
             }
             if (!isValidRange) {
                 throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", priority, rangeConstraints));
             }
         }    
         this.priority = priority;
         return this;
    }

    public short getWeight() {
        return weight;
    }

    public LocatorRecord setWeight(short weight) {
    	if (weight != 0) {
            boolean isValidRange = false;
            List<Range<Short>> rangeConstraints = new ArrayList<>(); 
            rangeConstraints.add(Range.closed(new Short("0"), new Short("255")));
            for (Range<Short> r : rangeConstraints) {
                if (r.contains(weight)) {
                isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", weight, rangeConstraints));
            }
        }    
        this.weight = weight;
        return this;
    }

    public short getMulticastPriority() {
        return multicastPriority;
    }

    public LocatorRecord setMulticastPriority(short value) {
    	if (value != 0) {
            boolean isValidRange = false;
            List<Range<Short>> rangeConstraints = new ArrayList<>(); 
            rangeConstraints.add(Range.closed(new Short("0"), new Short("255")));
            for (Range<Short> r : rangeConstraints) {
                if (r.contains(value)) {
                isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, rangeConstraints));
            }
        }    
        this.multicastPriority = value;
        return this;
    }

    public short getMulticastWeight() {
        return multicastWeight;
    }

    public LocatorRecord setMulticastWeight(short multicastWeight) {
    	if (multicastWeight != 0) {
            boolean isValidRange = false;
            List<Range<Short>> rangeConstraints = new ArrayList<>(); 
            rangeConstraints.add(Range.closed(new Short("0"), new Short("255")));
            for (Range<Short> r : rangeConstraints) {
                if (r.contains(multicastWeight)) {
                isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", multicastWeight, rangeConstraints));
            }
        }    
        this.multicastWeight = multicastWeight;
        return this;
    }

    public boolean isLocalLocator() {
        return localLocator;
    }

    public LocatorRecord setLocalLocator(boolean localLocator) {
        this.localLocator = localLocator;
        return this;
    }

    public boolean isRlocProbed() {
        return rlocProbed;
    }

    public LocatorRecord setRlocProbed(boolean rlocProbed) {
        this.rlocProbed = rlocProbed;
        return this;
    }

    public LocatorRecord setLocator(LispAddress locator) {
        this.locator = locator;
        return this;
    }

    public LispAddress getLocator() {
        return locator;
    }

    public boolean isRouted() {
        return routed;
    }

    public LocatorRecord setRouted(boolean routed) {
        this.routed = routed;
        return this;
    }

    public LocatorRecord clone() {
        LocatorRecord cloned = new LocatorRecord();
        cloned.setLocalLocator(isLocalLocator());
        cloned.setLocator(getLocator());
        cloned.setMulticastPriority(getMulticastPriority());
        cloned.setMulticastWeight(getMulticastWeight());
        cloned.setPriority(getPriority());
        cloned.setRlocProbed(isRlocProbed());
        cloned.setRouted(isRouted());
        cloned.setWeight(getWeight());
        return cloned;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (localLocator ? 1231 : 1237);
        result = prime * result + ((locator == null) ? 0 : locator.hashCode());
        result = prime * result + multicastPriority;
        result = prime * result + multicastWeight;
        result = prime * result + priority;
        result = prime * result + (rlocProbed ? 1231 : 1237);
        result = prime * result + (routed ? 1231 : 1237);
        result = prime * result + weight;
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
        LocatorRecord other = (LocatorRecord) obj;
        if (localLocator != other.localLocator)
            return false;
        if (locator == null) {
            if (other.locator != null)
                return false;
        } else if (!locator.equals(other.locator))
            return false;
        if (multicastPriority != other.multicastPriority)
            return false;
        if (multicastWeight != other.multicastWeight)
            return false;
        if (priority != other.priority)
            return false;
        if (rlocProbed != other.rlocProbed)
            return false;
        if (routed != other.routed)
            return false;
        if (weight != other.weight)
            return false;
        return true;
    }
}
