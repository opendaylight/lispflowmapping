/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.transport.address.TransportAddress;

/**
 * An interface for dealing with a map reply message.
 */
public interface IMapRequestResultHandler {
    /**
     * Handle return map-reply message
     *
     * @param mapReply
     *            The map-reply message
     */
    void handleMapReply(MapReply mapReply);

    /**
     * Handle map-request to be forwarded to authoritative ETR
     *
     * @param mapRequest
     *            The map-request message
     * @param transportAddress
     *            The address of the ETR
     */
    void handleNonProxyMapRequest(MapRequest mapRequest, TransportAddress transportAddress);
}