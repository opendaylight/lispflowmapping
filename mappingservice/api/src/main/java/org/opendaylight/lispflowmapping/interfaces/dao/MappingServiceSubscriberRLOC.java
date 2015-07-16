/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Date;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;

/**
 * Request source RLOC in the mapping service with it's properties.
 */
public class MappingServiceSubscriberRLOC {
    private LispAddressContainer rloc;
    private LispAddressContainer eid;
    private Date lastRequestDate;
    private static final long SUBSCRIBER_TIMEOUT = 600000;    /* milliseconds */

    public MappingServiceSubscriberRLOC(LispAddressContainer srcRloc, LispAddressContainer srcEid) {
        this(srcRloc, srcEid, new Date(System.currentTimeMillis()));
    }

    public MappingServiceSubscriberRLOC(LispAddressContainer srcRloc, LispAddressContainer srcEid,
            Date lastRequestDate) {
        super();
        this.rloc = srcRloc;
        this.eid = srcEid;
        this.lastRequestDate = lastRequestDate;
    }

    public LispAddressContainer getSrcRloc() {
        return rloc;
    }

    public void setSrcRloc(LispAddressContainer srcRloc) {
        this.rloc = srcRloc;
    }

    public LispAddressContainer getSrcEid() {
        return eid;
    }

    public void setSrcEid(LispAddressContainer srcEid) {
        this.eid = srcEid;
    }

    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(Date lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    public boolean timedOut() {
        return System.currentTimeMillis() - lastRequestDate.getTime() > SUBSCRIBER_TIMEOUT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rloc == null) ? 0 : rloc.hashCode());
        result = prime * result + ((eid == null) ? 0 : eid.hashCode());
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
        MappingServiceSubscriberRLOC other = (MappingServiceSubscriberRLOC) obj;
        if (!rloc.equals(other.rloc))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "_rloc=" + rloc.toString() + ", _eid=" + eid.toString()
                + ", last request @ " + lastRequestDate.toString();
    }
}
