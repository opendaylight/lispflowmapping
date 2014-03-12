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
public class MappingServiceRequestRLOC {
    private InetAddress srcRloc;
    private Date lastRequestDate;

    public MappingServiceRequestRLOC(InetAddress srcRloc) {
        this(srcRloc, new Date(System.currentTimeMillis()));
    }

    public MappingServiceRequestRLOC(InetAddress srcRloc, Date lastRequestDate) {
        super();
        this.srcRloc = srcRloc;
        this.lastRequestDate = lastRequestDate;
    }

    public InetAddress getSrcRloc() {
        return srcRloc;
    }

    public void setSrcRloc(InetAddress srcRloc) {
        this.srcRloc = srcRloc;
    }

    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(Date lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    @Override
    public int hashCode() {
        return srcRloc.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MappingServiceRequestRLOC other = (MappingServiceRequestRLOC) obj;
        if (!srcRloc.equals(other.srcRloc))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Src " + srcRloc.toString() + ", last request @ " + lastRequestDate.toString();
    }
}
