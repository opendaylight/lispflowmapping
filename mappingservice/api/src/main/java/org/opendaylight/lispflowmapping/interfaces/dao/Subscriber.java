/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
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
    // Subscriber TTL should be slightly higher than the mapping TTL. Arbitrary value is set to 10 minutes
    private static final int SUBSCRIBER_TIMEOUT_CONSTANT = 10;

    private SubscriberData data;
    private Date lastRequestDate;

    /**
     * Constructor.
     *
     * @param subscriberData YANG modeled SubscriberData object.
     */
    public Subscriber(SubscriberData subscriberData) {
        super();
        this.data = subscriberData;
        this.lastRequestDate = new Date(System.currentTimeMillis());
    }

    /**
     * Constructor.
     *
     * @param srcRloc A source RLOC.
     * @param srcEid  A source EID.
     * @param subscriberTtl Subscriber TTL in min(s).
     */
    public Subscriber(Rloc srcRloc, Eid srcEid, int subscriberTtl) {
        this(srcRloc, srcEid, subscriberTtl, new Date(System.currentTimeMillis()));
    }

    /**
     * Constructor.
     *
     * @param srcRloc A source RLOC.
     * @param srcEid  A source EID.
     * @param subscriberTtl Subscriber TTL in min(s).
     * @param lastRequestDate Last request date for this subscriber.
     */
    public Subscriber(Rloc srcRloc, Eid srcEid, int subscriberTtl, Date lastRequestDate) {
        super();
        this.data = new SubscriberDataBuilder().setRloc(srcRloc).setEid(srcEid).setTtl(subscriberTtl).build();
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

    public int getSubscriberTtl() {
        return data.getTtl();
    }

    // Only used in MapResolverTest
    public void setSubscriberTtlByRecordTtl(Integer recordTtl) {
        SubscriberDataBuilder sdb = new SubscriberDataBuilder(this.data);
        sdb.setTtl(recordTtlToSubscriberTime(recordTtl));
        this.data = sdb.build();
    }

    /**
     * Static method to calculate the subscriber TTL from a mapping record TTL. If a mapping record TTL is not
     * provided, use the default 1 day TTL. The subscriber TTL is the TTL plus a constant value.
     *
     * @param recordTtl The time to live (TTL) value
     * @return the subscriber TTL
     */
    public static int recordTtlToSubscriberTime(Integer recordTtl) {
        if (recordTtl != null) {
            return (recordTtl + SUBSCRIBER_TIMEOUT_CONSTANT);
        }
        return DEFAULT_SUBSCRIBER_TIMEOUT;
    }

    public boolean timedOut() {
        return TimeUnit.MILLISECONDS
                .toMinutes(System.currentTimeMillis() - lastRequestDate.getTime()) > data.getTtl();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(data.getRloc());
        result = prime * result + Objects.hashCode(data.getEid());
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
        Subscriber other = (Subscriber) obj;
        if (!Objects.equals(data.getEid(), other.getSrcEid())) {
            return false;
        }
        if (!Objects.equals(data.getRloc(), other.getSrcRloc())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "_rloc=" + data.getRloc().toString() + ", _eid=" + data.getEid().toString()
                + ", _ttl=" + data.getTtl().toString() + ", last request @ " + lastRequestDate.toString();
    }

    public String getString() {
        return "[" + LispAddressStringifier.getString(data.getRloc())
                + ", " + LispAddressStringifier.getString(data.getEid()) + "]";
    }
}
