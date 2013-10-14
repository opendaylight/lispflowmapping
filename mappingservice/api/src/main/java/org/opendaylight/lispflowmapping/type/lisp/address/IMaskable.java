package org.opendaylight.lispflowmapping.type.lisp.address;

public interface IMaskable extends Cloneable {

    public int getMaxMask();

    public void normalize(int mask);

    public LispAddress clone();
}
