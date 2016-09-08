/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
