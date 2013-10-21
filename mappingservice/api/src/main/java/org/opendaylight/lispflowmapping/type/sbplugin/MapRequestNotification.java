package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class MapRequestNotification implements LispNotification {
    private MapRequest mapRequest;
    private InetAddress address;

    public MapRequestNotification(MapRequest mapRequest, InetAddress sourceAddress) {
        this.mapRequest = mapRequest;
        this.address = sourceAddress;
    }

    public Class<? extends DataContainer> getImplementedInterface() {
        return LispNotification.class;
    }

    public MapRequest getMapRequest() {
        return mapRequest;
    }

    public InetAddress getSourceAddress() {
        return address;
    }
}
