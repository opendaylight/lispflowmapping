package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.LfmNetconfConnectorData;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * Top-level container for all connector database objects.
 * <p>This class represents the following YANG schema fragment defined in module <b>lfm-netconf-connector</b>
 * <br />(Source path: <i>META-INF/yang/lfm-netconf-connector.yang</i>):
 * <pre>
 * container nc-connector {
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>lfm-netconf-connector/nc-connector</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.NcConnectorBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.NcConnectorBuilder
 */
public interface NcConnector
    extends
    ChildOf<LfmNetconfConnectorData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.NcConnector>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:lispflowmapping:netconf","2014-07-06","nc-connector");;


}

