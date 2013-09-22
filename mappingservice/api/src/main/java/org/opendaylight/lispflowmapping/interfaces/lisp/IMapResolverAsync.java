package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.MapRequest;

public interface IMapResolverAsync extends IGeneralMapResolver {
    public void handleMapRequest(MapRequest request, IMapReplyHandler callback);
}
