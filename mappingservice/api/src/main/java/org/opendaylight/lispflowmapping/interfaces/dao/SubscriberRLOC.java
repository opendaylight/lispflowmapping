/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Date;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

/**
 * Request source RLOC in the mapping service with it's properties.
 */
public class SubscriberRLOC {

    private Rloc rloc;
    private Eid eid;
    private Date lastRequestDate;

    // 86400000L = 1 day (default Cisco IOS mapping TTL)
    private static final long SUBSCRIBER_TIMEOUT = 86400000L;

    public SubscriberRLOC(Rloc srcRloc, Eid srcEid) {
        this(srcRloc, srcEid, new Date(System.currentTimeMillis()));
    }

    public SubscriberRLOC(Rloc srcRloc, Eid srcEid,
            Date lastRequestDate) {
        super();
        this.rloc = srcRloc;
        this.eid = srcEid;
        this.lastRequestDate = lastRequestDate;
    }

    public Rloc getSrcRloc() {
        return rloc;
    }

    public void setSrcRloc(Rloc srcRloc) {
        this.rloc = srcRloc;
    }

    public Eid getSrcEid() {
        return eid;
    }

    public void setSrcEid(Eid srcEid) {
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubscriberRLOC other = (SubscriberRLOC) obj;
        if (!rloc.equals(other.rloc)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "_rloc=" + rloc.toString() + ", _eid=" + eid.toString()
                + ", last request @ " + lastRequestDate.toString();
    }
}
