/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Date;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;

/**
 * A RLOC in the mapping service with it's properties.
 */
public class MappingServiceRLOC {

    private LocatorRecord record;
    private int ttl;
    private Action action;
    private boolean authoritative;
    private Date registerdDate;

    public MappingServiceRLOC(LocatorRecord record, int ttl, Action action, boolean authoritative) {
        this(record, ttl, action, authoritative, new Date(System.currentTimeMillis()));
    }

    public MappingServiceRLOC(LocatorRecord record, int ttl, Action action, boolean authoritative, Date registerdDate) {
        super();
        this.record = record;
        this.ttl = ttl;
        this.action = action;
        this.authoritative = authoritative;
        this.registerdDate = registerdDate;
    }

    public LocatorRecord getRecord() {
        return record;
    }

    public MappingServiceRLOC setRecord(LocatorRecord record) {
        this.record = record;
        return this;
    }

    public int getTtl() {
        return ttl;
    }

    public MappingServiceRLOC setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public MappingServiceRLOC setAction(Action action) {
        this.action = action;
        return this;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public MappingServiceRLOC setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
        return this;
    }

    public Date getRegisterdDate() {
        return registerdDate;
    }

    public MappingServiceRLOC setRegisterdDate(Date registerdDate) {
        this.registerdDate = registerdDate;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + (authoritative ? 1231 : 1237);
        result = prime * result + ((record == null) ? 0 : record.hashCode());
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
        MappingServiceRLOC other = (MappingServiceRLOC) obj;
        if (action != other.action)
            return false;
        if (authoritative != other.authoritative)
            return false;
        if (record == null) {
            if (other.record != null)
                return false;
        } else if (!record.equals(other.record))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return record.getLispAddressContainer().getAddress().toString();
    }

}
