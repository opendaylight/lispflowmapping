/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class LispAFIConvertor {
    private static final InstanceIdType DEFAULT_VNI = new InstanceIdType(0L);

    public static Address addressFromInet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder()
            .setIpv4(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address(address.getHostAddress()));
        } else if (address instanceof Inet6Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder()
            .setIpv6(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address(address.getHostAddress()));
        }
        return null;
    }

    public static Address addressFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder().setIpv4(address.getIpv4Address());
        } else if (address.getIpv6Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder().setIpv6(address.getIpv6Address());
        }
        return null;
    }

    public static Rloc toRloc(SimpleAddress address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setVirtualNetworkId(DEFAULT_VNI);
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress(addressFromIpAddress(address.getIpAddress()));
        return builder.build();
    }

    public static Rloc toRloc(InetAddress address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setVirtualNetworkId(DEFAULT_VNI);
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress(addressFromInet(address));
        return builder.build();
    }

    public static Eid toEid(Ipv4Prefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixAfi.class);
        builder.setVirtualNetworkId(DEFAULT_VNI);
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress((Address) new Ipv4PrefixBuilder().setIpv4Prefix(prefix).build());
        return builder.build();
    }

    public static Eid toEid(Eid eid, IpPrefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address)prefix);
        return builder.build();
    }

    public static Eid asIpv4Prefix(String prefix) {
        return toEid(new Ipv4Prefix(prefix));
    }

    public static Eid asIpv4Prefix(Eid eid, Inet4Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(new Ipv4Prefix(getStringPrefix(address, mask))).build());
        return builder.build();
    }

    public static Eid asIpv6Prefix(Eid eid, Inet6Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv6PrefixBuilder().setIpv6Prefix(new Ipv6Prefix(getStringPrefix(address, mask))).build());
        return builder.build();
    }

    private static String getStringPrefix(InetAddress address, short mask) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getHostAddress());
        sb.append("/");
        sb.append(mask);
        return sb.toString();
    }
}
