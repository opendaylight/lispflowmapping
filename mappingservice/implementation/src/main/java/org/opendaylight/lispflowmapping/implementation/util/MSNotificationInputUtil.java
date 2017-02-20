/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.SubscriberItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.changed.SubscriberItemBuilder;

/**
 * Utility class to convert a mapping change into a notification.
 *
 * @author Florin Coras
 *
 */
public final class MSNotificationInputUtil {
    // Utility class, should not be instantiated
    private MSNotificationInputUtil() {
    }

    public static MappingChanged toMappingChanged(Mapping input, MappingChange change) {
        return new MappingChangedBuilder().setMappingRecord(input.getMappingRecord())
                .setChangeType(change).build();
    }

    public static MappingChanged toMappingChanged(MappingData mapping, Set<Subscriber> subscribers,
            MappingChange change) {
        List<SubscriberItem> subscriberList = new ArrayList<SubscriberItem>();
        for (Subscriber subscriber : subscribers) {
            subscriberList.add(new SubscriberItemBuilder().setSubscriberData(
                    subscriber.getSubscriberData()).build());
        }
        return new MappingChangedBuilder().setMappingRecord(mapping.getRecord()).setSubscriberItem(subscriberList)
                .setChangeType(change).build();
    }
}
