/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.util.List;
import org.opendaylight.lispflowmapping.lisp.type.LispMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for MappingRecord objects.
 *
 * @author Lorand Jakab
 *
 */
public final class MappingRecordUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingRecordUtil.class);

    // Utility class, should not be instantiated
    private MappingRecordUtil() {
    }

    public static boolean isNegativeMapping(MappingRecord mapping) {
        List<LocatorRecord> rlocs = mapping.getLocatorRecord();
        if (mapping.getAction() == LispMessage.NEGATIVE_MAPPING_ACTION && (rlocs == null || rlocs.isEmpty())) {
            return true;
        }
        return false;
    }

    public static boolean isPositiveMapping(MappingRecord mapping) {
        return !isNegativeMapping(mapping);
    }
}
