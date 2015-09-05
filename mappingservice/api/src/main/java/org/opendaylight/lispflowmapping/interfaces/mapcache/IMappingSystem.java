/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;

/**
 * Mapping System interface
 *
 * @author Florin Coras
 *
 */

public interface IMappingSystem {
    void addMapping(MappingOrigin origin, LispAddressContainer key, Object value);
    Object getMapping(LispAddressContainer src, LispAddressContainer dst);
    Object getMapping(LispAddressContainer dst);
    Object getMapping(MappingOrigin origin, LispAddressContainer key);
    void updateMappingRegistration(MappingOrigin origin, LispAddressContainer key);
    void removeMapping(MappingOrigin origin, LispAddressContainer key);

    void addAuthenticationKey(LispAddressContainer key, String authKey);
    String getAuthenticationKey(LispAddressContainer key);
    void removeAuthenticationKey(LispAddressContainer key);

    void addData(MappingOrigin origin, LispAddressContainer key, String subKey, Object data);
    Object getData(MappingOrigin origin, LispAddressContainer key, String subKey);
    void removeData(MappingOrigin origin, LispAddressContainer key, String subKey);

    void setIterateMask(boolean iterate);
    public void setOverwritePolicy(boolean overwrite);
    String printMappings();
}
