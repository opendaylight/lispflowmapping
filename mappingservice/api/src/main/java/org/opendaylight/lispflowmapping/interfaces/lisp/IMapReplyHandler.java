package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.type.lisp.MapReply;

public interface IMapReplyHandler {
	public void handleMapReply(MapReply reply);
}
