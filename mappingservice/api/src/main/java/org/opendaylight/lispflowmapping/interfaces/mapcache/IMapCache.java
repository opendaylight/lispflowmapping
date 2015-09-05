/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

/**
 * Map-cache interface
 *
 * @author Florin Coras
 *
 */

public interface IMapCache {
    void addMapping(LispAddressContainer key, Object data, boolean shouldOverwrite);
    Object getMapping(LispAddressContainer srcKey, LispAddressContainer dstKey);
    void removeMapping(LispAddressContainer eid, boolean overwrite);
    void addAuthenticationKey(LispAddressContainer key, String authKey);
    String getAuthenticationKey(LispAddressContainer key);
    void removeAuthenticationKey(LispAddressContainer key);
    void updateMappingRegistration(LispAddressContainer key);
    void addData(LispAddressContainer key, String subKey, Object data);
    Object getData(LispAddressContainer key, String subKey);
    void removeData(LispAddressContainer key, String subKey);
    String printMappings();
}
