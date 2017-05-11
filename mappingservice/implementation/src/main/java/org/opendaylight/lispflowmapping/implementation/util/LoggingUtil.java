/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.mapcache.lisp.LispMapCacheStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;

/**
 * Static utility methods for common logging patterns.
 *
 * @author Lorand Jakab
 *
 */
public class LoggingUtil {
    // Utility class, should not be instantiated
    private LoggingUtil() {
    }

    public static void logSubscribers(Logger log, Eid eid, Set<Subscriber> subscribers) {
        if (log.isTraceEnabled()) {
            log.trace("Subscribers for EID {}\n{}",
                    LispAddressStringifier.getString(eid),
                    LispMapCacheStringifier.prettyPrintSubscriberSet(subscribers, 0));
        }
    }
}
