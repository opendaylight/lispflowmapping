package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispSourceDestLCAFAddress extends LispLCAFAddress{

	private LispAddress srcAddress;
	private LispAddress dstAddress;
    private byte srcMaskLength;
    private byte dstMaskLength;
    private short reserved;

    public LispSourceDestLCAFAddress(byte res2) {
    	super(LispCanonicalAddressFormatEnum.SOURCE_DEST, res2);
	}

    public LispSourceDestLCAFAddress(
			byte res2, short reserved,
			byte srcMaskLength, byte dstMaskLength, 
			LispAddress srcAddress, LispAddress dstAddress) {
		super(LispCanonicalAddressFormatEnum.SOURCE_DEST, res2);
		this.srcAddress = srcAddress;
		this.dstAddress = dstAddress;
		this.srcMaskLength = srcMaskLength;
		this.dstMaskLength = dstMaskLength;
		this.reserved = reserved;
	}
    
	public LispAddress getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(LispAddress srcAddress) {
		this.srcAddress = srcAddress;
	}


	public LispAddress getDstAddress() {
		return dstAddress;
	}


	public void setDstAddress(LispAddress dstAddress) {
		this.dstAddress = dstAddress;
	}


	public byte getSrcMaskLength() {
		return srcMaskLength;
	}


	public void setSrcMaskLength(byte srcMaskLength) {
		this.srcMaskLength = srcMaskLength;
	}


	public byte getDstMaskLength() {
		return dstMaskLength;
	}


	public void setDstMaskLength(byte dstMaskLength) {
		this.dstMaskLength = dstMaskLength;
	}

	public short getReserved() {
		return reserved;
	}


	public void setReserved(short reserved) {
		this.reserved = reserved;
	}


	@Override
    public String toString() {
        return "LispSourceDestLCAFAddress#[srcAddress=" + srcAddress + ", dstAddress=" + dstAddress + "]" + super.toString();
    }
}
