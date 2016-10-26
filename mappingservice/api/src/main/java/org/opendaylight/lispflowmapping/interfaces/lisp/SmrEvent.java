/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Carries information about received SMR-invoked request.
 */
public class SmrEvent {

    private final List<IpAddressBinary> subscriberAddresses;
    private final Eid eid;
    private final long nonce;

    public SmrEvent(List<IpAddressBinary> subscriberAddresses, Eid eid, long nonce) {
        this.subscriberAddresses = subscriberAddresses;
        this.eid = eid;
        this.nonce = nonce;
    }

    /**
     * Returns the list of subscriber addresses that are subscribed to receive SMR MapRequest for a specific eid.
     *
     * @return the list of subscriber Addresses.
     */
    public List<IpAddressBinary> getSubscriberAddressList() {
        return subscriberAddresses;
    }

    /**
     * Returns the eid which the xTRs are subscribed to.
     *
     * @return the subscribed eid.
     */
    public Eid getEid() {
        return eid;
    }

    /**
     * Returns the nonce associated to a MapRequest.
     *
     * @return the nonce.
     */
    public long getNonce() {
        return nonce;
    }
}
