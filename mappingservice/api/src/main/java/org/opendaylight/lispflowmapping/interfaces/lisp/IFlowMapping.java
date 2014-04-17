/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * A mapping service.
 */
public interface IFlowMapping extends IMapResolver, IMapServer {

    public void clean();

    public void shouldListenOnXtrPort(boolean listenOnXtrPort);

    public void setXtrPort(int port);
}
