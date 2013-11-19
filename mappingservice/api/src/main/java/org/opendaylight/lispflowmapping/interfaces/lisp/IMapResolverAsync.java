package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;

public interface IMapResolverAsync extends IGeneralMapResolver {
    public void handleMapRequest(MapRequest request, IMapReplyHandler callback);
}
