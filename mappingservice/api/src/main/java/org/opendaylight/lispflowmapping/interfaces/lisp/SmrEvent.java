/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import java.util.List;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Carries information about received SMR-invoked request.
 */
public class SmrEvent {

    private final List<Subscriber> subscribers;
    private final Eid eid;
    private final long nonce;

    public SmrEvent(List<Subscriber> subscribers, Eid eid, long nonce) {
        this.subscribers = subscribers;
        this.eid = eid;
        this.nonce = nonce;
    }

    /**
     * Returns the list of subscriber addresses that are subscribed to receive SMR MapRequest for a specific EID.
     *
     * @return the list of subscriber addresses.
     */
    public List<Subscriber> getSubscriberList() {
        return subscribers;
    }

    /**
     * Returns the EID which the xTRs are subscribed to.
     *
     * @return the subscribed EID.
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
