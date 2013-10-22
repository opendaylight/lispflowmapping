package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
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
