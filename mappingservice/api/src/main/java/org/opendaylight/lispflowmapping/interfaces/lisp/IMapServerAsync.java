package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;

public interface IMapServerAsync extends IGeneralMapServer {
    public void handleMapRegister(MapRegister request, IMapNotifyHandler callback);
}
