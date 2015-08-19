/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;

/**
 * A RLOC in the mapping service with it's properties.
 */
public class RLOCGroup {

    private List<LocatorRecord> records;
    private int ttl;
    private Action action;
    private boolean authoritative;
    private Date registerdDate;

    public RLOCGroup(int ttl, Action action, boolean authoritative) {
        this(ttl, action, authoritative, new Date(System.currentTimeMillis()));
    }

    public RLOCGroup(int ttl, Action action, boolean authoritative, Date registerdDate) {
        super();
        this.records = new ArrayList<>();
        this.ttl = ttl;
        this.action = action;
        this.authoritative = authoritative;
        this.registerdDate = registerdDate;
    }

    public List<LocatorRecord> getRecords() {
        return records;
    }

    public RLOCGroup addRecord(LocatorRecord record) {
        this.records.add(record);
        return this;
    }

    public int getTtl() {
        return ttl;
    }

    public RLOCGroup setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public RLOCGroup setAction(Action action) {
        this.action = action;
        return this;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public RLOCGroup setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
        return this;
    }

    public Date getRegisterdDate() {
        return registerdDate;
    }

    public RLOCGroup setRegisterdDate(Date registerdDate) {
        this.registerdDate = registerdDate;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + (authoritative ? 1231 : 1237);
        result = prime * result + ((records == null) ? 0 : records.hashCode());
        result = prime * result + ((registerdDate == null) ? 0 : registerdDate.hashCode());
        result = prime * result + ttl;
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
        RLOCGroup other = (RLOCGroup) obj;
        if (action != other.action)
            return false;
        if (authoritative != other.authoritative)
            return false;
        if (records == null) {
            if (other.records != null)
                return false;
        } else if (!records.equals(other.records))
            return false;
        if (ttl != other.ttl)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MappingServiceRLOCGroup [records=" + records + ", ttl=" + ttl + ", action=" + action + ", authoritative=" + authoritative
                + ", registerdDate=" + registerdDate + "]";
    }

}
