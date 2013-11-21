/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

/**
 * A map request notification
 */
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class MapRequestNotification implements LispNotification {
    private MapRequest mapRequest;
    private InetAddress address;

    public MapRequestNotification(MapRequest mapRequest, InetAddress sourceAddress) {
        this.mapRequest = mapRequest;
        this.address = sourceAddress;
    }

    public Class<? extends DataObject> getImplementedInterface() {
        return LispNotification.class;
    }

    public MapRequest getMapRequest() {
        return mapRequest;
    }

    public InetAddress getSourceAddress() {
        return address;
    }
}
