package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;

public interface IMapNotifyHandler {
    public void handleMapNotify(MapNotify mapNotify);
}
