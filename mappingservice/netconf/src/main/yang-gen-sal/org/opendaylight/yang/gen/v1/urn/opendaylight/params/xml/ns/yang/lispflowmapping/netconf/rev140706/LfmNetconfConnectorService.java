package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.RemoveConnectorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInput;


/**
 * Interface for implementing the following YANG RPCs defined in module <b>lfm-netconf-connector</b>
 * <br />(Source path: <i>META-INF/yang/lfm-netconf-connector.yang</i>):
 * <pre>
 * rpc build-connector {
 *     "Build netconf connector";
 *     input {
 *         leaf instance {
 *             type string;
 *         }
 *         leaf address {
 *             type host;
 *         }
 *         leaf port {
 *             type port-number;
 *         }
 *         leaf username {
 *             type string;
 *         }
 *         leaf password {
 *             type string;
 *         }
 *     }
 *     
 *     status CURRENT;
 * }
 * rpc remove-connector {
 *     "Removes a given netconf connector";
 *     input {
 *         leaf instance {
 *             type string;
 *         }
 *     }
 *     
 *     status CURRENT;
 * }
 * </pre>
 */
public interface LfmNetconfConnectorService
    extends
    RpcService
{




    /**
     * Build netconf connector
     */
    Future<RpcResult<java.lang.Void>> buildConnector(BuildConnectorInput input);
    
    /**
     * Removes a given netconf connector
     */
    Future<RpcResult<java.lang.Void>> removeConnector(RemoveConnectorInput input);

}

