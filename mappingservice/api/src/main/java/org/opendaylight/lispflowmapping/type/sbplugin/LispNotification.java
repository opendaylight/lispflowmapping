package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;

import org.opendaylight.yangtools.yang.binding.Notification;

public interface LispNotification extends Notification {
    public Object getLispMessage();

    public InetAddress getSourceAddress();
}
