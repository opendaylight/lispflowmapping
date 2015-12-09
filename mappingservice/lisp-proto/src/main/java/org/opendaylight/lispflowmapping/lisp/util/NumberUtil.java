/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

public final class NumberUtil {
    // Utility class, should not be instantiated
    private NumberUtil() {
    }

    public static int asInt(Integer number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static byte asByte(Byte number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static short asShort(Short number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static long asLong(Long number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

}
