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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((srcRloc == null) ? 0 : srcRloc.hashCode());
        result = prime * result + ((lastRequestDate == null) ? 0 : lastRequestDate.hashCode());
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
        MappingServiceRequestRLOC other = (MappingServiceRequestRLOC) obj;
        if (!srcRloc.equals(other.srcRloc))
            return false;
        if (!lastRequestDate.equals(other.lastRequestDate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RLOC " + srcRloc.toString() + "last requested @ " + lastRequestDate.toString();
    }    
}
