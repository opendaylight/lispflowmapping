package org.opendaylight.lispflowmapping.type.lisp.address;

public interface IMaskable {

    public int getMaxMask();
    public void normalize(int mask); 
}
