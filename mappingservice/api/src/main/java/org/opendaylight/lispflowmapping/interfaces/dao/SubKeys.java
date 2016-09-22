/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.dao;

/**
 * Defines DAO Subkeys.
 *
 * @author Florin Coras
 *
 */

public interface SubKeys {
    String AUTH_KEY = "password";
    String RECORD = "address";
    String EXT_RECORD = "ext_address";
    String XTRID_RECORDS = "xtrid";
    String SUBSCRIBERS = "subscribers";
    String SRC_RLOCS = "src_rlocs";
    String REGDATE = "date";
    String VNI = "vni";
    String LCAF_SRCDST = "lcaf_srcdst";
    String BUCKET_INDEX = "bucket_index";

    String UNKOWN = "-1";
}
