/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.DstSubscriberItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.DstSubscriberItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.SubscriberItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.SubscriberItemBuilder;

/**
 * Utility class to convert a mapping change into a notification.
 *
 * @author Florin Coras
 * @author Lorand Jakab
 *
 */
public final class MSNotificationInputUtil {
    // Utility class, should not be instantiated
    private MSNotificationInputUtil() {
    }

    public static MappingChanged toMappingChanged(MappingRecord mapping, Eid eid, Set<Subscriber> subscribers,
                                                  Set<Subscriber> dstSubscribers, MappingChange change) {
        return new MappingChangedBuilder()
                .setMappingRecord(mapping)
                .setEid(eid)
                .setSubscriberItem(toSubscriberList(subscribers))
                .setDstSubscriberItem(toDstSubscriberList(dstSubscribers))
                .setChangeType(change).build();
    }

    public static List<SubscriberItem> toSubscriberList(Set<Subscriber> subscribers) {
        if (subscribers == null) {
            return null;
        }
        List<SubscriberItem> subscriberList = new ArrayList<SubscriberItem>();
        for (Subscriber subscriber : subscribers) {
            subscriberList.add(new SubscriberItemBuilder().setSubscriberData(
                    subscriber.getSubscriberData()).build());
        }
        return subscriberList;
    }

    public static List<DstSubscriberItem> toDstSubscriberList(Set<Subscriber> subscribers) {
        if (subscribers == null) {
            return null;
        }
        List<DstSubscriberItem> subscriberList = new ArrayList<DstSubscriberItem>();
        for (Subscriber subscriber : subscribers) {
            subscriberList.add(new DstSubscriberItemBuilder().setSubscriberData(
                    subscriber.getSubscriberData()).build());
        }
        return subscriberList;
    }

    public static Set<Subscriber> toSubscriberSet(List<SubscriberItem> subscribers) {
        if (subscribers == null) {
            return null;
        }
        Set<Subscriber> subscriberSet = Sets.newConcurrentHashSet();
        for (SubscriberItem subscriber : subscribers) {
            subscriberSet.add(new Subscriber(subscriber.getSubscriberData()));
        }
        return subscriberSet;
    }

    public static Set<Subscriber> toSubscriberSetFromDst(List<DstSubscriberItem> subscribers) {
        if (subscribers == null) {
            return null;
        }
        Set<Subscriber> subscriberSet = Sets.newConcurrentHashSet();
        for (DstSubscriberItem subscriber : subscribers) {
            subscriberSet.add(new Subscriber(subscriber.getSubscriberData()));
        }
        return subscriberSet;
    }
}
