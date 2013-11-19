package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;

public interface IMapReplyHandler {
    public void handleMapReply(MapReply mapReply);
}
