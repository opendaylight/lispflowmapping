package org.opendaylight.lispflowmapping.type.lisp.address;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

public class LispApplicationDataLCAFAddress extends LispLCAFAddress {

	private ApplicationData applicationData;

	public LispApplicationDataLCAFAddress(byte res2, ApplicationData applicationData) {
		super(LispCanonicalAddressFormatEnum.APPLICATION_DATA, res2);
		this.applicationData = applicationData;
	}
	
	public ApplicationData getApplicationData() {
		return applicationData;
	}
	
	@Override
    public String toString() {
        return "LispApplicationDataLCAFAddress#[" + applicationData + "]" + super.toString();
    }

}
