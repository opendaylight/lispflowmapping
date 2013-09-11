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
