/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

/**
 * An interface for dealing with a map notify message.
 */
public interface IMapNotifyHandler {
    /**
     * Handle map-notify message
     *
     * @param mapNotify
     *            The map-notify message
     */
    void handleMapNotify(MapNotify mapNotify);

    /**
     * Handle Solicit Map Request message
     *
     * @param mapRequest
     *            the SMR message
     *
     * @param subscriber
     *            The address of the subscriber that should be SMRed
     */
    void handleSMR(MapRequest mapRequest, LispAddressContainer subscriber);
}
