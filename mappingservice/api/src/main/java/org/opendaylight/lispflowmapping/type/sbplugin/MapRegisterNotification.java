/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class MapRegisterNotification implements LispNotification {
    private MapRegister mapRegister;
    private InetAddress address;

    public MapRegisterNotification(MapRegister mapRegister, InetAddress sourceAddress) {
        this.mapRegister = mapRegister;
        this.address = sourceAddress;
    }

    public Class<? extends DataObject> getImplementedInterface() {
        return LispNotification.class;
    }

    public MapRegister getMapRegister() {
        return mapRegister;
    }

    public InetAddress getSourceAddress() {
        return address;
    }
}
