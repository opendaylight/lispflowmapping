package org.opendaylight.lispflowmapping.type.sbplugin;

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface ILispSouthboundPlugin extends RpcService {

    Future<RpcResult<java.lang.Void>> handleMapNotify(MapNotify mapNotify, InetAddress targetAddress);

    Future<RpcResult<java.lang.Void>> handleMapReply(MapReply mapReply, InetAddress targetAddress);

}