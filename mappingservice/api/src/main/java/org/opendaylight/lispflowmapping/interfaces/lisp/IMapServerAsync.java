package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;

/**
 * The async map server interface for dealing with async map register calls.
 */
public interface IMapServerAsync extends IGeneralMapServer {
    public void handleMapRegister(MapRegister request, IMapNotifyHandler callback);
}
