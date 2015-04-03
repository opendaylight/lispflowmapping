/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;

public class MapRequestUtil {
    public static InetAddress selectItrRloc(MapRequest request) {
        if (request.getItrRloc() == null) {
            return null;
        }
        InetAddress selectedItrRloc = null;
        for (ItrRloc itr : request.getItrRloc()) {
            Address addr = itr.getLispAddressContainer().getAddress();
            if (addr instanceof Ipv4) {
                try {
                    selectedItrRloc = InetAddress.getByName(((Ipv4) addr).getIpv4Address().getIpv4Address().getValue());
                } catch (UnknownHostException e) {
                }
                break;
            }
            if (addr instanceof Ipv6) {
                try {
                    selectedItrRloc = InetAddress.getByName((((Ipv6) addr).getIpv6Address().getIpv6Address().getValue()));
                } catch (UnknownHostException e) {
                }
                break;
            }
        }
        return selectedItrRloc;
    }
}
