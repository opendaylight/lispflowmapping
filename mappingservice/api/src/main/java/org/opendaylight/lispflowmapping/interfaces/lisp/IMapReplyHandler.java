package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;

/**
 * An interface for dealing with a map reply message.
 */
public interface IMapReplyHandler {
    public void handleMapReply(MapReply mapReply);
}
