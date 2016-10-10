/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.mapcache;

import com.google.common.base.Preconditions;
import java.util.Date;
import org.opendaylight.lispflowmapping.lisp.type.ExtendedMappingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to implement merging of locator sets.
 *
 * @author Lorand Jakab
 *
 */
public final class MappingMergeUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingMergeUtil.class);

    // SB Map Register validity period in milliseconds. Default is 3.3 minutes.
    // TODO Added temporarily, until we get a proper configuration infra
    public static final long MIN_REGISTRATION_VALIDITY_SB = 200000L;

    // Utility class, should not be instantiated
    private MappingMergeUtil() {
    }

    public static boolean mappingIsExpired(ExtendedMappingRecord mapping) {
        Preconditions.checkNotNull(mapping, "mapping should not be null!");
        if (mapping.getTimestamp() != null) {
            return timestampIsExpired(mapping.getTimestamp());
        }
        return false;
    }

    public static boolean timestampIsExpired(Date timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        return timestampIsExpired(timestamp.getTime());
    }

    public static boolean timestampIsExpired(Long timestamp) {
        Preconditions.checkNotNull(timestamp, "timestamp should not be null!");
        if ((System.currentTimeMillis() - timestamp) > MIN_REGISTRATION_VALIDITY_SB ) {
            return true;
        }
        return false;
    }
}
