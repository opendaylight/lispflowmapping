/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;

/**
 * The async map resolver interface for dealing with async map request calls.
 */
public interface IMapResolverAsync extends IGeneralMapResolver {
    public void handleMapRequest(MapRequest request, IMapRequestResultHandlerHandler callback);
}
