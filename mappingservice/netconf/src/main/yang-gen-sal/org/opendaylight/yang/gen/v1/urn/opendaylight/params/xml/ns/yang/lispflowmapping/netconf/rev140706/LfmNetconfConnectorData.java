package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.NcConnector;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>lfm-netconf-connector</b>
 * <br />Source path: <i>META-INF/yang/lfm-netconf-connector.yang</i>):
 * <pre>
 * module lfm-netconf-connector {
 *     yang-version 1;
 *     namespace "urn:opendaylight:params:xml:ns:yang:lispflowmapping:netconf";
 *     prefix "lnc";
 *     import ietf-inet-types { prefix "inet"; }
 *     
 *     import config { prefix "config"; }
 *     revision 2014-00-06 {
 *         description "";
 *     }
 *     container nc-connector {
 *     }
 *     rpc build-connector {
 *         "Build netconf connector";
 *         input {
 *             leaf instance {
 *                 type string;
 *             }
 *             leaf address {
 *                 type host;
 *             }
 *             leaf port {
 *                 type port-number;
 *             }
 *             leaf username {
 *                 type string;
 *             }
 *             leaf password {
 *                 type string;
 *             }
 *         }
 *         
 *         status CURRENT;
 *     }
 *     rpc remove-connector {
 *         "Removes a given netconf connector";
 *         input {
 *             leaf instance {
 *                 type string;
 *             }
 *         }
 *         
 *         status CURRENT;
 *     }
 * }
 * </pre>
 */
public interface LfmNetconfConnectorData
    extends
    DataRoot
{




    /**
     * Top-level container for all connector database objects.
     */
    NcConnector getNcConnector();

}

