package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.MapNotify;

public interface IMapNotifyHandler {
    public void handleMapNotify(MapNotify notify);
}
