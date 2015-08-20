/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address.factory;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.lispflowmapping.lisp.serializer.address.LispApplicationDataLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispKeyValueLCAFSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispListLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispSegmentLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispSourceDestLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispTrafficEngineeringLCAFAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.type.LispCanonicalAddressFormatEnum;

public class LispLCAFAddressSerializerFactory {

    private static Map<LispCanonicalAddressFormatEnum, LispLCAFAddressSerializer> lcafToSearializerMap;

    private static void initializeMap() {

        lcafToSearializerMap = new HashMap<LispCanonicalAddressFormatEnum, LispLCAFAddressSerializer>();
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.LIST, LispListLCAFAddressSerializer.getInstance());
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.SEGMENT, LispSegmentLCAFAddressSerializer.getInstance());
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.APPLICATION_DATA, LispApplicationDataLCAFAddressSerializer.getInstance());
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING, LispTrafficEngineeringLCAFAddressSerializer.getInstance());
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.SOURCE_DEST, LispSourceDestLCAFAddressSerializer.getInstance());
        lcafToSearializerMap.put(LispCanonicalAddressFormatEnum.KEY_VALUE, LispKeyValueLCAFSerializer.getInstance());
    }

    public static LispLCAFAddressSerializer getLCAFSerializer(LispCanonicalAddressFormatEnum lcaf) {
        if (lcafToSearializerMap == null) {
            initializeMap();
        }
        return lcafToSearializerMap.get(lcaf);
    }

}
