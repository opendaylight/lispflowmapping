package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

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
