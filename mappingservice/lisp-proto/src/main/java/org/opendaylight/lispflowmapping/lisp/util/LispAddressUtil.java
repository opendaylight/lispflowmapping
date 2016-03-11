/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.AsNumberAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.DistinguishedNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ExplicitLocatorPathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv4PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6Afi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.Ipv6PrefixAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.KeyValueAddressLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.MacAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.NoAddressAfi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathIdType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.ServicePathLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SourceDestKeyLcaf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.AsNumberBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.DistinguishedNameBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6PrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.NoAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop.LrsBits;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IetfInetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LispAddressUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(LispAddressUtil.class);

    public static final short STARTING_SERVICE_INDEX = 255;
    private static final Pattern IP4_PATTERN =
            Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])", Pattern.CASE_INSENSITIVE);
    private static final Pattern IP6_PATTERN =
            Pattern.compile("([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}", Pattern.CASE_INSENSITIVE);

    // Utility class, should not be instantiated
    private LispAddressUtil() {
    }

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
        } else if (address.getMacAddress() != null) {
            return addressFromMacAddress(address.getMacAddress());
        } else if (address.getDistinguishedNameType() != null) {
            return addressFromDistinguishedName(address.getDistinguishedNameType());
        } else if (address.getAsNumber() != null) {
            return addressFromAsNumber(address.getAsNumber());
        }
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
            .setIpv4(IetfInetUtil.INSTANCE.ipv4AddressFor(address)).build();
        } else if (address instanceof Inet6Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder()
            .setIpv6(IetfInetUtil.INSTANCE.ipv6AddressFor(address)).build();
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

    public static Address addressFromMacAddress(MacAddress address) {
        if (address == null) {
            return null;
        } else {
            return (Address) new MacBuilder().setMac(address).build();
        }
    }

    public static Address addressFromServicePath(ServicePath address) {
        if (address == null) {
            return null;
        } else {
            return (Address) new ServicePathBuilder().setServicePath(address).build();
        }
    }

    public static Address addressFromDistinguishedName(DistinguishedNameType address) {
        if (address == null) {
            return null;
        } else {
            return (Address) new DistinguishedNameBuilder().setDistinguishedName(address).build();
        }
    }

    public static Address addressFromAsNumber(AsNumber address) {
        if (address == null) {
            return null;
        } else {
            return (Address) new AsNumberBuilder().setAsNumber(address).build();
        }
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

    public static Rloc asIpv4Rloc(String address) {
        return toRloc(new Ipv4Address(address));
    }

    public static Rloc asIpv6Rloc(String address) {
        return toRloc(new Ipv6Address(address));
    }

    public static Eid toEid(Ipv4Prefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv4PrefixBuilder().setIpv4Prefix(prefix).build());
        return builder.build();
    }

    public static Eid toEid(Ipv4Address prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4Afi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv4Builder().setIpv4(prefix).build());
        return builder.build();
    }

    // XXX getMapping rcp fails if VNI set to 0
    public static Eid toEidNoVni(IpPrefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddress(addressFromIpPrefix(prefix));
        builder.setAddressType(addressTypeFromIpPrefix(prefix));
        return builder.build();
    }

    public static Eid toEid(IpPrefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddress(addressFromIpPrefix(prefix));
        builder.setAddressType(addressTypeFromIpPrefix(prefix));
        builder.setVirtualNetworkId(vni);
        return builder.build();
    }

    public static Eid toIpPrefixEid(IpAddress addr, int vni) {
        // If you touch this, be sure that sfclisp compiles!
        int mask = addressTypeFromIpAddress(addr) == Ipv4Afi.class ? 32 : 128;
        IpPrefix prefix = asIpPrefix(String.valueOf(addr.getValue()), mask);
        // XXX getMapping rcp fails if VNI set to 0
        return toEidNoVni(prefix);
    }

    public static Eid asEid(SimpleAddress address, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(addressTypeFromSimpleAddress(address));
        builder.setVirtualNetworkId(vni);
        // XXX Not sure if the below actually works as expected... also, what happens to AFI?
        builder.setAddress(addressFromSimpleAddress(address));
        return builder.build();
    }

    public static Eid asIpv4PrefixEid(String prefix) {
        return toEid(new Ipv4Prefix(prefix), null);
    }

    public static Eid asIpv4Eid(String address) {
        return toEid(new Ipv4Address(address), null);
    }

    public static Eid toEid(Ipv6Prefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6PrefixAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv6PrefixBuilder().setIpv6Prefix(prefix).build());
        return builder.build();
    }

    public static Eid toEid(Ipv6Address prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6Afi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv6Builder().setIpv6(prefix).build());
        return builder.build();
    }

    public static Eid asIpv6Eid(String address, long vni) {
        return toEid(new Ipv6Address(address), new InstanceIdType(vni));
    }

    public static Eid asIpv6PrefixEid(String prefix) {
        return toEid(new Ipv6Prefix(prefix), null);
    }

    public static Eid asIpv6Eid(String address) {
        return toEid(new Ipv6Address(address), null);
    }

    public static Eid asIpv4Eid(String address, long vni) {
        return toEid(new Ipv4Address(address), new InstanceIdType(vni));
    }

    public static int ipVersionFromString(String ip) {
        if (IP4_PATTERN.matcher(ip).matches()) {
            return 4;
        } else if (IP6_PATTERN.matcher(ip).matches()) {
            return 6;
        } else {
            return 0;
        }
    }

    public static IpPrefix asIpPrefix(String addr, int mask) {
        int version = ipVersionFromString(addr);
        if (version == 4 && (mask >= 0 && mask <= 32)) {
            return new IpPrefix(new Ipv4Prefix(addr + "/" + mask));
        } else if (version == 6 && (mask >=0 && mask <= 128)) {
            return new IpPrefix(new Ipv6Prefix(addr + "/" + mask));
        } else {
            return null;
        }
    }

    public static Eid asIpv4PrefixEid(Ipv4Address addr, InstanceIdType vni) {
        return toEid(new IpPrefix(IetfInetUtil.INSTANCE.ipv4PrefixFor(addr)), vni);
    }

    public static Eid asIpv6PrefixEid(Ipv6Address addr, InstanceIdType vni) {
        return toEid(new IpPrefix(IetfInetUtil.INSTANCE.ipv6PrefixFor(addr)), vni);
    }

    public static Eid asIpv4PrefixEid(Eid eid, Inet4Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(IetfInetUtil.INSTANCE.ipv4PrefixFor(address, mask)).build());
        return builder.build();
    }

    public static Eid asIpv6PrefixEid(Eid eid, Inet6Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(eid.getAddressType());
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv6PrefixBuilder().setIpv6Prefix(IetfInetUtil.INSTANCE.ipv6PrefixFor(address, mask)).build());
        return builder.build();
    }

    public static Eid asServicePathEid(long vni, long servicePathId, short serviceIndex) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ServicePathLcaf.class);
        if (vni >= 0) {
            eb.setVirtualNetworkId(new InstanceIdType(vni));
        }
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder spb = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePathBuilder();
        spb.setServicePathId(new ServicePathIdType(servicePathId));
        spb.setServiceIndex(serviceIndex);
        return eb.setAddress(addressFromServicePath(spb.build())).build();
    }

    public static Eid toEid(MacAddress mac, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(MacAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new MacBuilder().setMac(mac).build());
        return builder.build();
    }

    public static Eid asMacEid(String address, long vni) {
        return toEid(new MacAddress(address), new InstanceIdType(vni));
    }

    public static Eid asMacEid(String address) {
        return toEid(new MacAddress(address), null);
    }

    public static Eid toEid(DistinguishedNameType dn, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(DistinguishedNameAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new DistinguishedNameBuilder().setDistinguishedName(dn).build());
        return builder.build();
    }

    public static Eid asDistinguishedNameEid(String address, long vni) {
        return toEid(new MacAddress(address), new InstanceIdType(vni));
    }

    public static Eid asDistinguishedNameEid(String address) {
        return toEid(new DistinguishedNameType(address), null);
    }

    public static Eid asKeyValueAddressEid(SimpleAddress key, SimpleAddress value) {
        KeyValueAddressBuilder kvab = new KeyValueAddressBuilder();
        kvab.setKey(key);
        kvab.setValue(value);
        KeyValueAddress address = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddressBuilder()
                .setKeyValueAddress(kvab.build()).build();
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(KeyValueAddressLcaf.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) address);
        return builder.build();
    }

    public static Rloc asKeyValueAddressRloc(SimpleAddress key, SimpleAddress value) {
        KeyValueAddressBuilder kvab = new KeyValueAddressBuilder();
        kvab.setKey(key);
        kvab.setValue(value);
        KeyValueAddress address = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.KeyValueAddressBuilder()
                .setKeyValueAddress(kvab.build()).build();
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(KeyValueAddressLcaf.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) address);
        return builder.build();
    }

    public static Rloc asKeyValueAddress(String key, SimpleAddress value) {
        return asKeyValueAddressRloc(new SimpleAddress(new DistinguishedNameType(key)), value);
    }

    public static SourceDestKey asSrcDst(String src, String dst, int smask, int dmask) {
        SourceDestKeyBuilder builder = new SourceDestKeyBuilder();
        builder.setSource(new SimpleAddress(asIpPrefix(src, smask)));
        builder.setDest(new SimpleAddress(asIpPrefix(dst, dmask)));
        return builder.build();
    }

    public static Eid asSrcDstEid(String src, String dst, int smask, int dmask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(SourceDestKeyLcaf.class);
        builder.setAddress(
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                        .setSourceDestKey(asSrcDst(src, dst, smask, dmask)).build());

        return builder.build();
    }

    public static Eid asSrcDstEid(String src, String dst, int smask, int dmask, int vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(SourceDestKeyLcaf.class);
        builder.setVirtualNetworkId(new InstanceIdType(Long.valueOf(vni)));
        builder.setAddress(
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKeyBuilder()
                        .setSourceDestKey(asSrcDst(src, dst, smask, dmask)).build());

        return builder.build();
    }

    public static Eid asSrcDstEid(SourceDestKey sd, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(SourceDestKeyLcaf.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                .rev151105.lisp.address.address.SourceDestKeyBuilder().setSourceDestKey(sd).build());
        return builder.build();
    }

    public static Rloc asTeLcafRloc(List<IpAddress> hopList) {
        RlocBuilder teBuilder = new RlocBuilder();
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPathBuilder elpBuilder =
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ExplicitLocatorPathBuilder();
        ExplicitLocatorPathBuilder teAddrBuilder = new ExplicitLocatorPathBuilder();
        teAddrBuilder.setHop(new ArrayList<Hop>());
        for (IpAddress hop : hopList) {
            HopBuilder hopBuilder = new HopBuilder();
            hopBuilder.setAddress(new SimpleAddress(hop));
            hopBuilder.setHopId("Hop " + teAddrBuilder.getHop().size());
            hopBuilder.setLrsBits(new LrsBits(false, false, false));
            teAddrBuilder.getHop().add(hopBuilder.build());
        }

        elpBuilder.setExplicitLocatorPath(teAddrBuilder.build());
        teBuilder.setAddress(elpBuilder.build());
        teBuilder.setAddressType(ExplicitLocatorPathLcaf.class);
        return teBuilder.build();
    }

    public static List<LocatorRecord> asLocatorRecords(List<Rloc> locators) {
        List<LocatorRecord> locatorRecords = new ArrayList<LocatorRecord>();
        for (Rloc locator : locators) {
            LocatorRecordBuilder locatorBuilder = new LocatorRecordBuilder();
            locatorBuilder.setLocalLocator(false).setRlocProbed(false).setWeight((short) 1).setPriority((short) 1)
                    .setMulticastWeight((short) 1).setMulticastPriority((short) 1).setRouted(true)
                    .setRloc(locator).setLocatorId("SFC_LISP").build();
            locatorRecords.add(locatorBuilder.build());
        }
        return locatorRecords;
    }

    public static Eid getNoAddressEid() {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(NoAddressAfi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress(new NoAddressBuilder().setNoAddress(true).build());
        return builder.build();
    }

    public static InetAddress ipAddressToInet(Address addr) {
        if (addr instanceof Ipv4) {
            return InetAddresses.forString(((Ipv4) addr).getIpv4().getValue());
        } else if (addr instanceof Ipv6) {
            return InetAddresses.forString(((Ipv6) addr).getIpv6().getValue());
        } else {
            return null;
        }
    }

    public static int compareInetAddresses(InetAddress a, InetAddress b) {
        int i;
        if (a instanceof Inet4Address && b instanceof Inet6Address) {
            return -1;
        } else if (a instanceof Inet6Address && b instanceof Inet4Address) {
            return 1;
        } else if (a instanceof Inet4Address && b instanceof Inet4Address) {
            byte[] aBytes = ((Inet4Address) a).getAddress();
            byte[] bBytes = ((Inet4Address) b).getAddress();
            for (i = 0; i < 4; i++) {
                if (aBytes[i] < bBytes[i]) {
                    return -1;
                } else if (aBytes[i] > bBytes[i]) {
                    return 1;
                }
            }
            return 0;
        } else if (a instanceof Inet6Address && b instanceof Inet6Address) {
            byte[] aBytes = ((Inet6Address) a).getAddress();
            byte[] bBytes = ((Inet6Address) b).getAddress();
            for (i = 0; i < 16; i++) {
                if (aBytes[i] < bBytes[i]) {
                    return -1;
                } else if (aBytes[i] > bBytes[i]) {
                    return 1;
                }
            }
            return 0;
        }
        return 0;
    }
}
