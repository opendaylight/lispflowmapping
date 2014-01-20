/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * The southbound plugin interface.
 */
public interface ILispSouthboundPlugin extends RpcService {

    /**
     * Handle a map notify by sending it back to the xTR
     * 
     * @param mapNotify
     * @param targetAddress
     * @return
     */
    Future<RpcResult<java.lang.Void>> handleMapNotify(MapNotify mapNotify, InetAddress targetAddress);

    /**
     * Handle a map reply by sending it back to the xTR.
     * 
     * @param mapReply
     * @param targetAddress
     * @return
     */
    Future<RpcResult<java.lang.Void>> handleMapReply(MapReply mapReply, InetAddress targetAddress);

}