package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Host;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>lfm-netconf-connector</b>
 * <br />(Source path: <i>META-INF/yang/lfm-netconf-connector.yang</i>):
 * <pre>
 * container input {
 *     leaf instance {
 *         type string;
 *     }
 *     leaf address {
 *         type host;
 *     }
 *     leaf port {
 *         type port-number;
 *     }
 *     leaf username {
 *         type string;
 *     }
 *     leaf password {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>lfm-netconf-connector/build-connector/input</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInputBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInputBuilder
 */
public interface BuildConnectorInput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInput>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:lispflowmapping:netconf","2014-07-06","input");;

    /**
     * instance name
     */
    java.lang.String getInstance();
    
    /**
     * Device address
     */
    Host getAddress();
    
    /**
     * Device port
     */
    PortNumber getPort();
    
    /**
     * Username for netconf connection
     */
    java.lang.String getUsername();
    
    /**
     * Password for netconf connection
     */
    java.lang.String getPassword();

}

