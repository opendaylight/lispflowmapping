/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;

/**
 * Request source RLOC in the mapping service with it's properties.
 */
public class SubscriberRLOC {

    private Rloc rloc;
    private Eid eid;
    private Date lastRequestDate;
    private int subscriberTimeout = DEFAULT_SUBSCRIBER_TIMEOUT;

    private static final int SUBSCRIBER_TIMEOUT_CONSTANT = 10;

    //1 day is default Cisco IOS mapping TTL
    public static final int DEFAULT_SUBSCRIBER_TIMEOUT = (int) TimeUnit.DAYS.toMinutes(1);

    /**
     * Constructor.
     *
     * @param srcRloc A source RLOC.
     * @param srcEid  A source EID.
     * @param subscriberTimeout Subscriber timeout in min(s).
     */
    public SubscriberRLOC(Rloc srcRloc, Eid srcEid, int subscriberTimeout) {
        this(srcRloc, srcEid, subscriberTimeout, new Date(System.currentTimeMillis()));
    }

    /**
     * Constructor.
     *
     * @param srcRloc A source RLOC.
     * @param srcEid  A source EID.
     * @param subscriberTimeout Subscriber timeout in min(s).
     * @param lastRequestDate Last request date for this subscriber.
     */
    public SubscriberRLOC(Rloc srcRloc, Eid srcEid, int subscriberTimeout,
                          Date lastRequestDate) {
        super();
        this.rloc = srcRloc;
        this.eid = srcEid;
        this.lastRequestDate = lastRequestDate;
        this.subscriberTimeout = subscriberTimeout;
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

    public int getSubscriberTimeout() {
        return subscriberTimeout;
    }

    public void setSubscriberTimeout(int subscriberTimeout) {
        this.subscriberTimeout = subscriberTimeout;
    }

    /**
     * Set Subscriber Timeout from a record's ttl.
     *
     * @param recordTtl A mapping record's ttl in Minutes. If null, the value is
     *                  set to default value of 1440 mins (1 days).
     */
    public void setSubscriberTimeoutByRecordTtl(Integer recordTtl) {
        this.subscriberTimeout = recordTtlToSubscriberTime(recordTtl);
    }

    public static int recordTtlToSubscriberTime(Integer recordTtl) {
        if (recordTtl != null) {
            return ( recordTtl + SUBSCRIBER_TIMEOUT_CONSTANT );
        }
        return DEFAULT_SUBSCRIBER_TIMEOUT;
    }

    public boolean timedOut() {
        return TimeUnit.MILLISECONDS
                .toMinutes(System.currentTimeMillis() - lastRequestDate.getTime()) > subscriberTimeout;
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
