/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.net.InetAddress;
import java.util.Date;

/**
 * Request source RLOC in the mapping service with it's properties.
 */
public class MappingServiceSubscriberRLOC {
    private InetAddress rloc;
    private Date lastRequestDate;

    public MappingServiceSubscriberRLOC(InetAddress srcRloc) {
        this(srcRloc, new Date(System.currentTimeMillis()));
    }

    public MappingServiceSubscriberRLOC(InetAddress srcRloc, Date lastRequestDate) {
        super();
        this.rloc = srcRloc;
        this.lastRequestDate = lastRequestDate;
    }

    public InetAddress getSrcRloc() {
        return rloc;
    }

    public void setSrcRloc(InetAddress srcRloc) {
        this.rloc = srcRloc;
    }

    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(Date lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    @Override
    public int hashCode() {
        return rloc.hashCode();
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
        return "Src " + rloc.getHostAddress() + ", last request @ " + lastRequestDate.toString();
    }
}
