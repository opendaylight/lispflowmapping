package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface ILispSouthboundPlugin extends RpcService {

    Future<RpcResult<java.lang.Void>> handleMapNotify(MapNotify mapNotify, InetAddress targetAddress);

    Future<RpcResult<java.lang.Void>> handleMapReply(MapReply mapReply, InetAddress targetAddress);

}