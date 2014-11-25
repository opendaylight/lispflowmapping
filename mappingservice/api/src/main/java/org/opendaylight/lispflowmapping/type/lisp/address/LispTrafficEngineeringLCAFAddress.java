/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.List;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispTrafficEngineeringLCAFAddress extends LispLCAFAddress {

    private List<ReencapHop> hops;

    public LispTrafficEngineeringLCAFAddress(byte res2, List<ReencapHop> hops) {
        super(LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING, res2);
        this.hops = hops;
    }

    @Override
    public String toString() {
        return "LispTrafficEngineeringLCAFAddress#[hops=" + hops + "]" + super.toString();
    }

    public List<ReencapHop> getHops() {
        return hops;
    }

    public void setHops(List<ReencapHop> hops) {
        this.hops = hops;
    }

}
