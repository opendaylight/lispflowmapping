/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.subscriber.data.grouping.SubscriberData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.subscriber.data.grouping.SubscriberDataBuilder;

/**
 * Request source RLOC in the mapping service with it's properties.
 */
public class Subscriber {
    // 1 day is default Cisco IOS mapping TTL
    public static final int DEFAULT_SUBSCRIBER_TIMEOUT = (int) TimeUnit.DAYS.toMinutes(1);
    // Subscriber timeout should be slightly higher than the mapping TTL. Arbitrary value is set to 10 minutes
    private static final int SUBSCRIBER_TIMEOUT_CONSTANT = 10;

    private SubscriberData data;
    private Date lastRequestDate;

    /**
     * Constructor.
     *
     * @param srcRloc A source RLOC.
     * @param srcEid  A source EID.
     * @param subscriberTimeout Subscriber timeout in min(s).
     */
    public Subscriber(Rloc srcRloc, Eid srcEid, int subscriberTimeout) {
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
    public Subscriber(Rloc srcRloc, Eid srcEid, int subscriberTimeout, Date lastRequestDate) {
        super();
        this.data = new SubscriberDataBuilder().setRloc(srcRloc).setEid(srcEid).setTimeout(subscriberTimeout).build();
        this.lastRequestDate = lastRequestDate;
    }

    public SubscriberData getSubscriberData() {
        return data;
    }

    public Rloc getSrcRloc() {
        return data.getRloc();
    }

    public Eid getSrcEid() {
        return data.getEid();
    }

    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(Date lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    public int getSubscriberTimeout() {
        return data.getTimeout();
    }

    // Only used in MapResolverTest
    public void setSubscriberTimeoutByRecordTtl(Integer recordTtl) {
        SubscriberDataBuilder sdb = new SubscriberDataBuilder(this.data);
        sdb.setTimeout(recordTtlToSubscriberTime(recordTtl));
        this.data = sdb.build();
    }

    /**
     * Static method to calculate the subscriber timeout from a mapping record TTL. If a mapping record TTL is not
     * provided, use the default 1 day TTL. The subscriber timeout is the TTL plus a constant value.
     *
     * @param recordTtl The time to live (TTL) value
     * @return the subscriber timeout
     */
    public static int recordTtlToSubscriberTime(Integer recordTtl) {
        if (recordTtl != null) {
            return (recordTtl + SUBSCRIBER_TIMEOUT_CONSTANT);
        }
        return DEFAULT_SUBSCRIBER_TIMEOUT;
    }

    public boolean timedOut() {
        return TimeUnit.MILLISECONDS
                .toMinutes(System.currentTimeMillis() - lastRequestDate.getTime()) > data.getTimeout();
    }

    @Override
    public int hashCode() {
        return data.hashCode();
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
        Subscriber other = (Subscriber) obj;
        if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "_rloc=" + data.getRloc().toString() + ", _eid=" + data.getEid().toString()
                + ", _timeout=" + data.getTimeout().toString() + ", last request @ " + lastRequestDate.toString();
    }
}
