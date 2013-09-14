package org.opendaylight.lispflowmapping.type.lisp.address;

public class LispASCILCAFAddress extends LispBaseOneLCAFAddress {

    private LispDistinguishedNameAddress distinguishedName;

    public LispASCILCAFAddress(byte res2, LispDistinguishedNameAddress distinguishedName) {
        super(res2);
        this.distinguishedName = distinguishedName;
    }

    public LispDistinguishedNameAddress getdistinguishedName() {
        return distinguishedName;
    }

    public byte getIdMaskLen() {
        return getRes2();
    }

    @Override
    public String toString() {
        return "LispASCILCAFAddress#[distinguishedName=" + distinguishedName + "]" + super.toString();
    }
}
