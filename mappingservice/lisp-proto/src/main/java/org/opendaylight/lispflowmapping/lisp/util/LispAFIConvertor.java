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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AsNumberAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;

public class LispAFIConvertor {
    public static Class<? extends LispAddressFamily> addressTypeFromSimpleAddress(SimpleAddress address) {
        if (address.getIpAddress() != null) {
            return addressTypeFromIpAddress(address.getIpAddress());
        } else if (address.getIpPrefix() != null) {
            return addressTypeFromIpPrefix(address.getIpPrefix());
        } else if (address.getMacAddress() != null) {
            return MacAfi.class;
        } else if (address.getDistinguishedNameType() != null) {
            return DistinguishedNameAfi.class;
        } else if (address.getAsNumber() != null) {
            return AsNumberAfi.class;
        }
        return null;
    }

    public static Address addressFromSimpleAddress(SimpleAddress address) {
        if (address.getIpAddress() != null) {
            return addressFromIpAddress(address.getIpAddress());
        } else if (address.getIpPrefix() != null) {
            return addressFromIpPrefix(address.getIpPrefix());
        }
        // TODO the rest of the types
        return null;
    }

    public static Class<? extends LispAddressFamily> addressTypeFromInet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return Ipv4Afi.class;
        } else if (address instanceof Inet6Address) {
            return Ipv6Afi.class;
        }
        return null;
    }

    public static Address addressFromInet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder()
            .setIpv4(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address(address.getHostAddress())).build();
        } else if (address instanceof Inet6Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder()
            .setIpv6(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address(address.getHostAddress())).build();
        }
        return null;
    }

    public static Class<? extends LispAddressFamily> addressTypeFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return Ipv4Afi.class;
        } else if (address.getIpv6Address() != null) {
            return Ipv6Afi.class;
        }
        return null;
    }

    public static Address addressFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder().setIpv4(address.getIpv4Address()).build();
        } else if (address.getIpv6Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder().setIpv6(address.getIpv6Address()).build();
        }
        return null;
    }

    public static Class<? extends LispAddressFamily> addressTypeFromIpPrefix(IpPrefix address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Prefix() != null) {
            return Ipv4PrefixAfi.class;
        } else if (address.getIpv6Prefix() != null) {
            return Ipv6PrefixAfi.class;
        }
        return null;
    }

    public static Address addressFromIpPrefix(IpPrefix address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Prefix() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder().setIpv4Prefix(address.getIpv4Prefix()).build();
        } else if (address.getIpv6Prefix() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6PrefixBuilder().setIpv6Prefix(address.getIpv6Prefix()).build();
        }
        return null;
    }

    public static Rloc toRloc(SimpleAddress address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(addressTypeFromSimpleAddress(address));
        builder.setVirtualNetworkId(null);
        builder.setAddress(addressFromSimpleAddress(address));
        return builder.build();
    }

    public static Rloc toRloc(InetAddress address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(addressTypeFromInet(address));
        builder.setVirtualNetworkId(null);
        builder.setAddress(addressFromInet(address));
        return builder.build();
    }

    public static Rloc toRloc(Ipv4Address address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv4Afi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv4Builder().setIpv4(address).build());
        return builder.build();
    }

    public static Rloc toRloc(Ipv6Address address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv6Afi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv6Builder().setIpv6(address).build());
        return builder.build();
    }

    public static Rloc asIpv4(String address) {
        return toRloc(new Ipv4Address(address));
    }

    public static Rloc asIpv6(String address) {
        return toRloc(new Ipv6Address(address));
    }

    public static Eid toEid(Ipv4Prefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixAfi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv4PrefixBuilder().setIpv4Prefix(prefix).build());
        return builder.build();
    }

    public static Eid toEid(Eid eid, IpPrefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress(addressFromIpPrefix(prefix));
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
