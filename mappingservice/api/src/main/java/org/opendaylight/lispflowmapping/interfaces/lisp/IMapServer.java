/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public interface IMapServer {
    MapNotify handleMapRegister(MapRegister mapRegister);

    boolean shouldAuthenticate();

    boolean shouldIterateMask();

    void setShouldIterateMask(boolean shouldIterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);

    String getAuthenticationKey(LispAddress address, int maskLen);

    boolean removeAuthenticationKey(LispAddress address, int maskLen);

    boolean addAuthenticationKey(LispAddress address, int maskLen, String key);

}
