/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import java.util.List;

import org.opendaylight.lispflowmapping.interfaces.dao.SmrNonce;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;

/**
 * An interface for dealing with a map notify message.
 */
public interface IMapNotifyHandler {
    /**
     * Handle map-notify message.
     *
     * @param mapNotify
     *            The map-notify message
     *
     * @param rlocs
     *            A list of RLOCs which need to be notified
     */
    void handleMapNotify(MapNotify mapNotify, List<TransportAddress> rlocs);

    /**
     * Handle Solicit Map Request message.
     *
     * @param mapRequest
     *            the SMR message
     *
     * @param subscriber
     *            The address of the subscriber that should be SMRed
     */
    void handleSMR(MapRequest mapRequest, Rloc subscriber);

    SmrNonce getSmrNonce();
}
