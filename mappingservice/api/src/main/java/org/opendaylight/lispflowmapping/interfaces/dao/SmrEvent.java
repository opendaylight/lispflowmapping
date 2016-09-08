package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;

/**
 * Carries information about received SMR-invoked request.
 */
public class SmrEvent {

    private final long smrNonce;
    private final Address subscriberAddress;

    public SmrEvent(long smrNonce, Address subscriberAddress) {
        this.smrNonce = smrNonce;
        this.subscriberAddress = subscriberAddress;
    }

    public long getSmrNonce() {
        return smrNonce;
    }

    public Address  getSubscriberAddress() {
        return subscriberAddress;
    }
}
