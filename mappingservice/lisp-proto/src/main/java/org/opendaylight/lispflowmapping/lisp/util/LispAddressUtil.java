/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.ExplicitLocatorPathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.Hop.LrsBits;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.explicit.locator.path.explicit.locator.path.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.key.value.address.KeyValueAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.service.path.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv4AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.Ipv6AddressBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv4PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6BinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.Ipv6PrefixBinaryAfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6BinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinaryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.EidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.RlocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LispAddressUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(LispAddressUtil.class);

    public static final short STARTING_SERVICE_INDEX = 255;
    private static final Pattern IP4_PATTERN =
            Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern IP6_PATTERN =
            Pattern.compile("([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}", Pattern.CASE_INSENSITIVE);

    // Utility class, should not be instantiated
    private LispAddressUtil() {
    }

    public static Class<? extends LispAddressFamily> addressTypeFromSimpleAddress(SimpleAddress address) {
        if (address.getIpAddress() != null) {
            return binaryAddressTypeFromIpAddress(address.getIpAddress());
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
            return binaryAddressFromIpAddress(address.getIpAddress());
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
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv4Builder()
                    .setIpv4(IetfInetUtil.INSTANCE.ipv4AddressFor(address)).build();
        } else if (address instanceof Inet6Address) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv6Builder()
                    .setIpv6(IetfInetUtil.INSTANCE.ipv6AddressFor(address)).build();
        }
        return null;
    }

    public static Class<? extends LispAddressFamily> binaryAddressTypeFromInet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return Ipv4BinaryAfi.class;
        } else if (address instanceof Inet6Address) {
            return Ipv6BinaryAfi.class;
        }
        return null;
    }

    public static Address binaryAddressFromInet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return (Address) new Ipv4BinaryBuilder().setIpv4Binary(new Ipv4AddressBinary(address.getAddress())).build();
        } else if (address instanceof Inet6Address) {
            return (Address) new Ipv6BinaryBuilder().setIpv6Binary(new Ipv6AddressBinary(address.getAddress())).build();
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

    public static Class<? extends LispAddressFamily> binaryAddressTypeFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return Ipv4BinaryAfi.class;
        } else if (address.getIpv6Address() != null) {
            return Ipv6BinaryAfi.class;
        }
        return null;
    }

    public static Address addressFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv4Builder().setIpv4(address.getIpv4Address()).build();
        } else if (address.getIpv6Address() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv6Builder().setIpv6(address.getIpv6Address()).build();
        }
        return null;
    }


    public static Address binaryAddressFromIpAddress(IpAddress address) {
        if (address == null) {
            return null;
        } else if (address.getIpv4Address() != null) {
            return (Address) new Ipv4BinaryBuilder().setIpv4Binary(new Ipv4AddressBinary(
                    InetAddresses.forString(address.getIpv4Address().getValue()).getAddress())).build();
        } else if (address.getIpv6Address() != null) {
            return (Address) new Ipv6BinaryBuilder().setIpv6Binary(new Ipv6AddressBinary(
                    InetAddresses.forString(address.getIpv6Address().getValue()).getAddress())).build();
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
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv4PrefixBuilder().setIpv4Prefix(address.getIpv4Prefix()).build();
        } else if (address.getIpv6Prefix() != null) {
            return (Address) new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                    .rev151105.lisp.address.address.Ipv6PrefixBuilder().setIpv6Prefix(address.getIpv6Prefix()).build();
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
        builder.setAddressType(binaryAddressTypeFromInet(address));
        builder.setVirtualNetworkId(null);
        builder.setAddress(binaryAddressFromInet(address));
        return builder.build();
    }

    public static Rloc toRloc(Ipv4Address address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv4Afi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv4Builder().setIpv4(address).build());
        return builder.build();
    }

    public static Rloc toRloc(Ipv4AddressBinary address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv4BinaryAfi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv4BinaryBuilder().setIpv4Binary(address).build());
        return builder.build();
    }

    public static Rloc toRloc(Ipv6Address address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv6Afi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv6Builder().setIpv6(address).build());
        return builder.build();
    }

    public static Rloc toRloc(Ipv6AddressBinary address) {
        RlocBuilder builder = new RlocBuilder();
        builder.setAddressType(Ipv6BinaryAfi.class);
        builder.setVirtualNetworkId(null);
        builder.setAddress((Address) new Ipv6BinaryBuilder().setIpv6Binary(address).build());
        return builder.build();
    }

    public static Rloc asIpv4Rloc(String address) {
        return toRloc(new Ipv4AddressBinary(InetAddresses.forString(address).getAddress()));
    }

    public static Rloc asIpv6Rloc(String address) {
        return toRloc(new Ipv6AddressBinary(InetAddresses.forString(address).getAddress()));
    }

    public static Eid toEid(Ipv4Prefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv4PrefixBuilder().setIpv4Prefix(prefix).build());
        return builder.build();
    }

    public static Eid toEid(Ipv4PrefixBinary prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixBinaryAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) prefix);
        return builder.build();
    }

    public static Eid toEid(Ipv6PrefixBinary prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6PrefixBinaryAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) prefix);
        return builder.build();
    }

    public static Eid toEid(Ipv4Address address, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4Afi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv4Builder().setIpv4(address).build());
        return builder.build();
    }

    public static Eid toEid(Ipv4AddressBinary address, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4BinaryAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv4BinaryBuilder().setIpv4Binary(address).build());
        return builder.build();
    }

    public static Eid toEid(IpPrefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddress(addressFromIpPrefix(prefix));
        builder.setAddressType(addressTypeFromIpPrefix(prefix));
        builder.setVirtualNetworkId(vni);
        return builder.build();
    }

    public static Eid toEid(Ipv6Address address, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6Afi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv6Builder().setIpv6(address).build());
        return builder.build();
    }

    public static Eid toEid(Ipv6AddressBinary address, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6BinaryAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv6BinaryBuilder().setIpv6Binary(address).build());
        return builder.build();
    }

    public static Eid toEid(DistinguishedNameType dn, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(DistinguishedNameAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new DistinguishedNameBuilder().setDistinguishedName(dn).build());
        return builder.build();
    }

    public static Eid toEid(MacAddress mac, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(MacAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new MacBuilder().setMac(mac).build());
        return builder.build();
    }

    public static Eid toEid(Ipv6Prefix prefix, InstanceIdType vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6PrefixAfi.class);
        builder.setVirtualNetworkId(vni);
        builder.setAddress((Address) new Ipv6PrefixBuilder().setIpv6Prefix(prefix).build());
        return builder.build();
    }

    // XXX getMapping rcp fails if VNI set to 0
    public static Eid toEidNoVni(IpPrefix prefix) {
        EidBuilder builder = new EidBuilder();
        builder.setAddress(addressFromIpPrefix(prefix));
        builder.setAddressType(addressTypeFromIpPrefix(prefix));
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
        return asIpv4PrefixEid(prefix, null);
    }

    public static Eid asIpv4PrefixEid(Ipv4Address addr, InstanceIdType vni) {
        return toEid(new IpPrefix(IetfInetUtil.INSTANCE.ipv4PrefixFor(addr)), vni);
    }

    public static Eid asIpv4PrefixEid(final String prefix, final InstanceIdType iiType) {
        return toEid(new Ipv4Prefix(prefix), iiType);
    }

    public static Eid asIpv4PrefixEid(Eid eid, Inet4Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixAfi.class);
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv4PrefixBuilder().setIpv4Prefix(
                IetfInetUtil.INSTANCE.ipv4PrefixFor(address, mask)).build());
        return builder.build();
    }

    public static Eid asIpv4PrefixBinaryEid(final String prefix) {
        return asIpv4PrefixBinaryEid(prefix, null);
    }

    public static Eid asIpv4PrefixBinaryEid(final String prefix, final InstanceIdType iiType) {
        String address = MaskUtil.getPrefixAddress(prefix);
        short mask = Short.valueOf(MaskUtil.getPrefixMask(prefix));
        byte[] ipv4 = InetAddresses.forString(address).getAddress();
        return toEid(new Ipv4PrefixBinaryBuilder().setIpv4AddressBinary(new Ipv4AddressBinary(ipv4))
                .setIpv4MaskLength(mask).build(), iiType);
    }

    public static Eid asIpv4PrefixBinaryEid(Eid eid, byte[] address, short mask) {
        Preconditions.checkArgument(address.length == 4,
                "asIpv4PrefixBinaryEid called with incorrect length byte array ({})", address.length);
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv4PrefixBinaryAfi.class);
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv4PrefixBinaryBuilder().setIpv4AddressBinary(new Ipv4AddressBinary(address))
                .setIpv4MaskLength(mask).build());
        return builder.build();
    }

    public static Eid asIpv4Eid(String address) {
        return toEid(new Ipv4AddressBinary(InetAddresses.forString(address).getAddress()), null);
    }

    public static Eid asIpv4Eid(String address, long vni) {
        return toEid(new Ipv4AddressBinary(InetAddresses.forString(address).getAddress()), new InstanceIdType(vni));
    }

    public static Eid asIpv6Eid(String address) {
        return toEid(new Ipv6AddressBinary(InetAddresses.forString(address).getAddress()), null);
    }

    public static Eid asIpv6Eid(String address, long vni) {
        return toEid(new Ipv6AddressBinary(InetAddresses.forString(address).getAddress()), new InstanceIdType(vni));
    }

    public static Eid asIpv6PrefixEid(String prefix) {
        return toEid(new Ipv6Prefix(prefix), null);
    }

    public static Eid asIpv6PrefixEid(Ipv6Address addr, InstanceIdType vni) {
        return toEid(new IpPrefix(IetfInetUtil.INSTANCE.ipv6PrefixFor(addr)), vni);
    }

    public static Eid asIpv6PrefixEid(Eid eid, Inet6Address address, short mask) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6PrefixAfi.class);
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv6PrefixBuilder().setIpv6Prefix(
                IetfInetUtil.INSTANCE.ipv6PrefixFor(address, mask)).build());
        return builder.build();
    }

    public static Eid asIpv6PrefixBinaryEid(final String prefix) {
        return asIpv6PrefixBinaryEid(prefix, null);
    }

    public static Eid asIpv6PrefixBinaryEid(final String prefix, final InstanceIdType iiType) {
        String address = MaskUtil.getPrefixAddress(prefix);
        short mask = Short.valueOf(MaskUtil.getPrefixMask(prefix));
        byte[] ipv6 = InetAddresses.forString(address).getAddress();
        return toEid(new Ipv6PrefixBinaryBuilder().setIpv6AddressBinary(new Ipv6AddressBinary(ipv6))
                .setIpv6MaskLength(mask).build(), iiType);
    }

    public static Eid asIpv6PrefixBinaryEid(Eid eid, byte[] address, short mask) {
        Preconditions.checkArgument(address.length == 16,
                "asIpv6PrefixBinaryEid called with incorrect length byte array ({})", address.length);
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(Ipv6PrefixBinaryAfi.class);
        builder.setVirtualNetworkId(eid.getVirtualNetworkId());
        builder.setAddress(new Ipv6PrefixBinaryBuilder().setIpv6AddressBinary(new Ipv6AddressBinary(address))
                .setIpv6MaskLength(mask).build());
        return builder.build();
    }

    public static Eid asBinaryEid(SimpleAddress address, InstanceIdType iid) {
        if (address.getIpPrefix() != null) {
            if (address.getIpPrefix().getIpv4Prefix() != null) {
                return LispAddressUtil.asIpv4PrefixBinaryEid(address.getIpPrefix().getIpv4Prefix().getValue(), iid);
            } else if (address.getIpPrefix().getIpv6Prefix() != null) {
                return LispAddressUtil.asIpv6PrefixBinaryEid(address.getIpPrefix().getIpv6Prefix().getValue(), iid);
            }
        } else if (address.getIpAddress() != null) {
            if (address.getIpAddress().getIpv4Address() != null) {
                LispAddressUtil.toEid(new Ipv4AddressBinary(InetAddresses.forString(
                        address.getIpAddress().getIpv4Address().getValue()).getAddress()), iid);
            } else if (address.getIpAddress().getIpv6Address() != null) {
                LispAddressUtil.toEid(new Ipv6AddressBinary(InetAddresses.forString(
                        address.getIpAddress().getIpv6Address().getValue()).getAddress()), iid);
            }
        }
        return LispAddressUtil.asEid(address, iid);
    }

    public static Eid asIpPrefixBinaryEid(Eid eid) {
        Address address = eid.getAddress();
        if (address instanceof Ipv4Binary) {
            return LispAddressUtil.asIpv4PrefixBinaryEid(eid, ((Ipv4Binary) address).getIpv4Binary().getValue(),
                    (short) 32);
        } else if (address instanceof Ipv6Binary) {
            return LispAddressUtil.asIpv6PrefixBinaryEid(eid, ((Ipv6Binary) address).getIpv6Binary().getValue(),
                    (short) 128);
        } else if (address instanceof Ipv4PrefixBinary || address instanceof Ipv6PrefixBinary) {
            return eid;
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.Ipv4) {
            return LispAddressUtil.asIpv4PrefixBinaryEid(((Ipv4) address).getIpv4().getValue() + "/32",
                    eid.getVirtualNetworkId());
        } else if (address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address
                .types.rev151105.lisp.address.address.Ipv6) {
            return LispAddressUtil.asIpv6PrefixBinaryEid(((Ipv6) address).getIpv6().getValue() + "/128",
                    eid.getVirtualNetworkId());
        }
        return convertToBinary(eid);
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
        } else if (version == 6 && (mask >= 0 && mask <= 128)) {
            return new IpPrefix(new Ipv6Prefix(addr + "/" + mask));
        } else {
            return null;
        }
    }

    public static Eid asServicePathEid(long vni, long servicePathId, short serviceIndex) {
        EidBuilder eb = new EidBuilder();
        eb.setAddressType(ServicePathLcaf.class);
        if (vni >= 0) {
            eb.setVirtualNetworkId(new InstanceIdType(vni));
        }
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.service.path.ServicePathBuilder spb =
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                .lisp.address.address.service.path.ServicePathBuilder();
        spb.setServicePathId(new ServicePathIdType(servicePathId));
        spb.setServiceIndex(serviceIndex);
        return eb.setAddress(addressFromServicePath(spb.build())).build();
    }

    public static Eid asMacEid(String address, long vni) {
        return toEid(new MacAddress(address), new InstanceIdType(vni));
    }

    public static Eid asMacEid(String address) {
        return toEid(new MacAddress(address), null);
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
        KeyValueAddress address = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                .rev151105.lisp.address.address.KeyValueAddressBuilder()
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
        KeyValueAddress address = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types
                .rev151105.lisp.address.address.KeyValueAddressBuilder()
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

    public static Eid asSrcDstEid(String src, String dst, int smask, int dmask, int vni) {
        EidBuilder builder = new EidBuilder();
        builder.setAddressType(SourceDestKeyLcaf.class);
        builder.setVirtualNetworkId(new InstanceIdType(Long.valueOf(vni)));
        builder.setAddress(
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
                        .lisp.address.address.SourceDestKeyBuilder()
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
        ExplicitLocatorPathBuilder teAddrBuilder = new ExplicitLocatorPathBuilder();
        teAddrBuilder.setHop(new ArrayList<Hop>());
        for (IpAddress hop : hopList) {
            HopBuilder hopBuilder = new HopBuilder();
            hopBuilder.setAddress(new SimpleAddress(hop));
            hopBuilder.setHopId("Hop " + teAddrBuilder.getHop().size());
            hopBuilder.setLrsBits(new LrsBits(false, false, false));
            teAddrBuilder.getHop().add(hopBuilder.build());
        }

        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.ExplicitLocatorPathBuilder elpBuilder =
            new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.ExplicitLocatorPathBuilder();
        elpBuilder.setExplicitLocatorPath(teAddrBuilder.build());

        RlocBuilder teBuilder = new RlocBuilder();
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

    public static byte[] ipAddressToByteArray(Address addr) {
        if (addr instanceof Ipv4) {
            return InetAddresses.forString(((Ipv4) addr).getIpv4().getValue()).getAddress();
        } else if (addr instanceof Ipv6) {
            return InetAddresses.forString(((Ipv6) addr).getIpv6().getValue()).getAddress();
        } else if (addr instanceof Ipv4Binary) {
            return ((Ipv4Binary) addr).getIpv4Binary().getValue();
        } else if (addr instanceof Ipv6Binary) {
            return ((Ipv6Binary) addr).getIpv6Binary().getValue();
        } else {
            return null;
        }
    }

    public static Address byteArrayToIpAddress(byte[] addr, Class<? extends LispAddressFamily> addrType)
            throws UnknownHostException {
        if (addrType.isAssignableFrom(Ipv4.class)) {
            return new Ipv4Builder().setIpv4(new Ipv4Address(InetAddress.getByAddress(addr).toString())).build();
        } else if (addrType.isAssignableFrom(Ipv6.class)) {
            return new Ipv6Builder().setIpv6(new Ipv6Address(InetAddress.getByAddress(addr).toString())).build();
        } else if (addrType.isAssignableFrom(Ipv4Binary.class)) {
            return new Ipv4BinaryBuilder().setIpv4Binary(new Ipv4AddressBinary(addr)).build();
        } else if (addrType.isAssignableFrom(Ipv6Binary.class)) {
            return new Ipv6BinaryBuilder().setIpv6Binary(new Ipv6AddressBinary(addr)).build();
        } else {
            return null;
        }
    }

    private static Ipv4PrefixBinary convertToBinary(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
            .lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix prefix) {
        Ipv4PrefixBinaryBuilder pb = new Ipv4PrefixBinaryBuilder();
        byte[] address = InetAddresses.forString(MaskUtil.getAddressStringForIpv4Prefix(prefix)).getAddress();
        pb.setIpv4AddressBinary(new Ipv4AddressBinary(address));
        pb.setIpv4MaskLength(MaskUtil.getMaskForAddress(prefix));
        return pb.build();
    }

    private static Ipv6PrefixBinary convertToBinary(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
            .lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix prefix) {
        Ipv6PrefixBinaryBuilder pb = new Ipv6PrefixBinaryBuilder();
        byte[] address = InetAddresses.forString(MaskUtil.getAddressStringForIpv6Prefix(prefix)).getAddress();
        pb.setIpv6AddressBinary(new Ipv6AddressBinary(address));
        pb.setIpv6MaskLength(MaskUtil.getMaskForAddress(prefix));
        return pb.build();
    }

    private static Ipv4Binary convertToBinary(Ipv4 address) {
        byte[] addr = InetAddresses.forString(address.getIpv4().getValue()).getAddress();
        return new Ipv4BinaryBuilder().setIpv4Binary(new Ipv4AddressBinary(addr)).build();
    }

    private static Ipv6Binary convertToBinary(Ipv6 address) {
        byte[] addr = InetAddresses.forString(address.getIpv6().getValue()).getAddress();
        return new Ipv6BinaryBuilder().setIpv6Binary(new Ipv6AddressBinary(addr)).build();
    }

    private static Pair<Class<? extends LispAddressFamily>, Address> convertToBinary(Address addr) {
        Address convAddr = null;
        Class<? extends LispAddressFamily> convType = null;
        if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix) {
            convAddr = convertToBinary((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                    .lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix) addr);
            convType = Ipv4PrefixBinaryAfi.class;
        } else if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) {
            convAddr = convertToBinary((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                    .lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) addr);
            convType = Ipv6PrefixBinaryAfi.class;
        } else if (addr instanceof Ipv4) {
            convAddr = convertToBinary((Ipv4) addr);
            convType = Ipv4BinaryAfi.class;
        } else if (addr instanceof Ipv6) {
            convAddr = convertToBinary((Ipv6) addr);
            convType = Ipv6BinaryAfi.class;
        }
        return new ImmutablePair<Class<? extends LispAddressFamily>, Address>(convType, convAddr);
    }

    public static Eid convertToBinary(Eid eid) {
        Pair<Class<? extends LispAddressFamily>, Address> converted = convertToBinary(eid.getAddress());
        if (converted.getRight() == null) {
            return eid;
        }
        EidBuilder eb = new EidBuilder(eid);
        eb.setAddressType(converted.getLeft());
        eb.setAddress(converted.getRight());
        return eb.build();
    }

    public static Rloc convertToBinary(Rloc rloc) {
        Pair<Class<? extends LispAddressFamily>, Address> converted = convertToBinary(rloc.getAddress());
        if (converted.getRight() == null) {
            return rloc;
        }
        RlocBuilder rb = new RlocBuilder(rloc);
        rb.setAddressType(converted.getLeft());
        rb.setAddress(converted.getRight());
        return rb.build();
    }

    /**
     * Converts the {@link InetAddress} into Ipv4 or Ipv6 {@link IpAddressBinary}. If null parameter is passed, method
     * returns the Ipv4 loopback address (127.0.0.1).
     *
     * @param inetAddress Any Ipv4 or Ipv6 InetAddress.
     * @return The converted Ipv4 or Ipv6 IpAddressBinary, or Ipv4 loopback address (127.0.0.1) if null is passed.
     */
    public static IpAddressBinary addressBinaryFromInet(InetAddress inetAddress) {
        if (inetAddress == null) {
            inetAddress = Inet4Address.getLoopbackAddress();
        }

        if (inetAddress instanceof Inet4Address) {
            return new IpAddressBinary(new Ipv4AddressBinary(inetAddress.getAddress()));
        } else if (inetAddress instanceof Inet6Address) {
            return new IpAddressBinary(new Ipv6AddressBinary(inetAddress.getAddress()));
        }
        return null;
    }

    public static IpAddressBinary addressBinaryFromAddress(Address address) {
        if (address instanceof Ipv4Binary) {
            return new IpAddressBinary(((Ipv4Binary) address).getIpv4Binary());
        } else if (address instanceof Ipv6Binary) {
            return new IpAddressBinary(((Ipv6Binary) address).getIpv6Binary());
        }
        return null;
    }

    public static List<IpAddressBinary> addressBinariesFromItrRlocs(List<ItrRloc> itrRlocs) {
        List<IpAddressBinary> ipAddressBinaryList = Lists.newArrayList();
        for (ItrRloc itrRloc : itrRlocs) {
            ipAddressBinaryList.add(addressBinaryFromAddress(itrRloc.getRloc().getAddress()));
        }
        return ipAddressBinaryList;
    }

    private static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv4Prefix convertFromBinary(Ipv4PrefixBinary prefix) {
        return new Ipv4PrefixBuilder().setIpv4Prefix(IetfInetUtil.INSTANCE.ipv4PrefixFor(
                prefix.getIpv4AddressBinary().getValue(),
                prefix.getIpv4MaskLength())).build();
    }

    private static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105
            .lisp.address.address.Ipv6Prefix convertFromBinary(Ipv6PrefixBinary prefix) {
        return new Ipv6PrefixBuilder().setIpv6Prefix(IetfInetUtil.INSTANCE.ipv6PrefixFor(
                prefix.getIpv6AddressBinary().getValue(),
                prefix.getIpv6MaskLength())).build();
    }

    private static Ipv4 convertFromBinary(Ipv4Binary address) {
        return new Ipv4Builder().setIpv4(IetfInetUtil.INSTANCE.ipv4AddressFor(address.getIpv4Binary().getValue()))
                .build();
    }

    private static Ipv6 convertFromBinary(Ipv6Binary address) {
        return new Ipv6Builder().setIpv6(IetfInetUtil.INSTANCE.ipv6AddressFor(address.getIpv6Binary().getValue()))
                .build();
    }

    private static Pair<Class<? extends LispAddressFamily>, Address> convertFromBinary(Address addr) {
        Address convAddr = null;
        Class<? extends LispAddressFamily> convType = null;
        if (addr instanceof Ipv4PrefixBinary) {
            convAddr = convertFromBinary((Ipv4PrefixBinary) addr);
            convType = Ipv4PrefixAfi.class;
        } else if (addr instanceof Ipv6PrefixBinary) {
            convAddr = convertFromBinary((Ipv6PrefixBinary) addr);
            convType = Ipv6PrefixAfi.class;
        } else if (addr instanceof Ipv4Binary) {
            convAddr = convertFromBinary((Ipv4Binary) addr);
            convType = Ipv4Afi.class;
        } else if (addr instanceof Ipv6Binary) {
            convAddr = convertFromBinary((Ipv6Binary) addr);
            convType = Ipv6Afi.class;
        }
        return new ImmutablePair<Class<? extends LispAddressFamily>, Address>(convType, convAddr);
    }

    public static Eid convertFromBinary(Eid eid) {
        Pair<Class<? extends LispAddressFamily>, Address> converted = convertFromBinary(eid.getAddress());
        if (converted.getRight() == null) {
            return eid;
        }
        EidBuilder eb = new EidBuilder(eid);
        eb.setAddressType(converted.getLeft());
        eb.setAddress(converted.getRight());
        return eb.build();
    }

    public static Rloc convertFromBinary(Rloc rloc) {
        Pair<Class<? extends LispAddressFamily>, Address> converted = convertFromBinary(rloc.getAddress());
        if (converted.getRight() == null) {
            return rloc;
        }
        RlocBuilder rb = new RlocBuilder(rloc);
        rb.setAddressType(converted.getLeft());
        rb.setAddress(converted.getRight());
        return rb.build();
    }

    public static boolean addressNeedsConversionToBinary(Address address) {
        if (address instanceof Ipv4 || address instanceof Ipv6
                || address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                        .lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix
                || address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                        .lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix) {
            return true;
        }
        return false;
    }

    public static boolean addressNeedsConversionFromBinary(Address address) {
        if (address instanceof Ipv4Binary || address instanceof Ipv6Binary
                || address instanceof Ipv4PrefixBinary || address instanceof Ipv6PrefixBinary) {
            return true;
        }
        return false;
    }

    public static int compareIpAddressByteArrays(byte[] one, byte[] two) {
        if (one.length < two.length) {
            return -1;
        } else if (one.length > two.length) {
            return 1;
        } else if (one.length == 4 && two.length == 4) {
            for (int i = 0; i < 4; i++) {
                if (one[i] < two[i]) {
                    return -1;
                } else if (one[i] > two[i]) {
                    return 1;
                }
            }
            return 0;
        } else if (one.length == 16 && two.length == 16) {
            for (int i = 0; i < 16; i++) {
                if (one[i] < two[i]) {
                    return -1;
                } else if (one[i] > two[i]) {
                    return 1;
                }
            }
            return 0;
        }
        return 0;
    }

    public static short getIpPrefixMask(Eid eid) {
        Address addr = eid.getAddress();
        if (addr instanceof Ipv4PrefixBinary) {
            return ((Ipv4PrefixBinary) addr).getIpv4MaskLength();
        } else if (addr instanceof Ipv6PrefixBinary) {
            return ((Ipv6PrefixBinary) addr).getIpv6MaskLength();
        }
        return 0;
    }
}
