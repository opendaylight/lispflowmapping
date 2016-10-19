/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface ILispSouthboundService {

    void handlePacket(DatagramPacket packet);

    void handlePacket(final InetSocketAddress sender, final ByteBuffer inBuffer);


}
