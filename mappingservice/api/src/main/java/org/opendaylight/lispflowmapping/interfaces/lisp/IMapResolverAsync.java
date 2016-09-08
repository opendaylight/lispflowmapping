/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;

/**
 * Map Resolver interface for dealing with async map request calls.
 */
public interface IMapResolverAsync extends IGenericMapResolver {
    /**
     * Handle Map-Request.
     *
     * @param request
     *            The map-request message
     */
    void handleMapRequest(MapRequest request);

    /**
     * Set SMR message notification listener.
     *
     * @param listener The SMR notification listener
     */
    void setSmrNotificationListener(ISmrNotificationListener listener);
}
