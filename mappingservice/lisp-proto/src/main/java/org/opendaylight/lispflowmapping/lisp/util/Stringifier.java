/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

/**
 * Utility class with static methods returning user friendly string
 * representations of certain model based auto-generated classes.
 *
 * @author Lorand Jakab
 *
 */
public class Stringifier {
    private static final String NEW_LINE = System.lineSeparator();

    public static String getSpacesAsString(int length) {
        return new String(new char[length]).replace("\0", " ");
    }

    public static String getString(MappingAuthkey key) {
        return getString(key, 0);
    }

    public static String getString(MappingAuthkey key, int indentation) {
        final String indent = getSpacesAsString(indentation);

        StringBuilder masb = new StringBuilder(indent);
        masb.append(key.getKeyString());
        masb.append("   ");
        masb.append(key.getKeyType());
        return masb.toString();
    }

    public static String getString(MappingRecord mapping) {
        return getString(mapping, 0);
    }

    public static String getString(MappingRecord mapping, int indentation) {
        final String indent = getSpacesAsString(indentation);

        StringBuilder mrsb = new StringBuilder(indent);

        // Main information, EID prefix and TTL (for now)
        mrsb.append(LispAddressStringifier.getString(mapping.getEid()));
        mrsb.append(", TTL: ");
        mrsb.append(mapping.getRecordTtl().toString());
        mrsb.append(NEW_LINE);

        // Locator records
        // Regular indentation for the mapping record
        mrsb.append(indent);
        // Extra indentation for locator records
        mrsb.append(indent);
        if (mapping.getLocatorRecord() == null || mapping.getLocatorRecord().isEmpty()) {
            // We only print the action for negative mappings (0 locator records)
            mrsb.append("-> Negative entry, action: ");
            mrsb.append(mapping.getAction().getName());
        } else {
            mrsb.append("-> Locator                                         State     Pri/Wgt");
            mrsb.append(NEW_LINE);
            mrsb.append(indent);
            boolean first = true;
            for (LocatorRecord record : mapping.getLocatorRecord()) {
                if (first) {
                    first = false;
                } else {
                    mrsb.append(NEW_LINE);
                    mrsb.append(indent);
                }
                mrsb.append(getString(record, indentation + 3));
            }
        }

        return mrsb.toString();
    }

    public static String getString(LocatorRecord locator) {
        return getString(locator, 0);
    }

    public static String getString(LocatorRecord locator, int indentation) {
        final String indent = getSpacesAsString(indentation);

        StringBuilder lrsb = new StringBuilder(indent);

        String rloc = LispAddressStringifier.getString(locator.getRloc());
        int padLen = Math.max(2, Constants.INET6_ADDRSTRLEN + 2 - rloc.length());
        lrsb.append(rloc);
        lrsb.append(getSpacesAsString(padLen));
        lrsb.append(locator.isRouted() ? "up        " : "no-route  ");
        lrsb.append(locator.getPriority().toString());
        lrsb.append('/');
        lrsb.append(locator.getWeight().toString());

        return lrsb.toString();
    }
}
