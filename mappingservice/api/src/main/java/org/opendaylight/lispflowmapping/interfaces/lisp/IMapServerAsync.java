package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

public interface IMapServerAsync extends IGeneralMapServer {
    public void handleMapRegister(MapRegister request, IMapNotifyHandler callback);
}
