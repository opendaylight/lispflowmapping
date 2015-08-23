/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;

/**
 * The async map server interface for dealing with async map register calls.
 */
public interface IMapServerAsync extends IGeneralMapServer {
    public void handleMapRegister(MapRegister request, boolean smr, IMapNotifyHandler callback);
    public void removeMapping(LispAddressContainer address, boolean smr, IMapNotifyHandler callback);

}
