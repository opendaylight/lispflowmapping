/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;

/**
 * An interface for dealing with a map reply message.
 */
public interface IMapRequestResultHandlerHandler {
    public void handleMapReply(MapReply mapReply);

    public void handleNonProxyMapRequest(MapRequest mapRequest, InetAddress targetAddress);
}
