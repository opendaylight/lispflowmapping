/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util;

/**
 * Defines DAO Subkeys
 *
 * @author Florin Coras
 *
 */
public enum DAOSubKeys {
    PASSWORD_SUBKEY("password"),
    ADDRESS_SUBKEY("address"),
    SUBSCRIBERS_SUBKEY("subscribers"),
    LCAF_SRCDST_SUBKEY("lcaf_srcdst"),
    UNKOWN("-1");

    private String key;

    private DAOSubKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

//    public static DAOSubKeys valueOf(String key) {
//        for (DAOSubKeys val : values()) {
//            if (val.getKey() == key) {
//                return val;
//            }
//        }
//        return UNKOWN;
//    }
}
