/*
 * Copyright (c) 2014, 2017 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.source.dest.key.SourceDestKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MaskUtil.class);
    public static final short IPV4_MAX_MASK = 32;
    public static final short IPV6_MAX_MASK = 128;

    // Utility class, should not be instantiated
    private MaskUtil() {
    }

    public static boolean isMaskable(Address address) {
        if (address instanceof Ipv4Prefix || address instanceof Ipv6Prefix
                || address instanceof Ipv4PrefixBinary || address instanceof Ipv6PrefixBinary
                || address instanceof SourceDestKey) {
            return true;
        } else if (address instanceof InstanceId) {
            return isMaskable(((InstanceId)address).getInstanceId().getAddress());
        }
        return false;
    }

    public static boolean isMaskable(SimpleAddress address) {
        if (address.getIpPrefix() != null) {
            return true;
        }
        return false;
    }

    private static int slashPosition(final String prefix) {
        final int slash = prefix.lastIndexOf('/');
        Preconditions.checkArgument(slash >= 0, "Argument %s does not contain a slash", prefix);
        return slash;
    }

    protected static String getPrefixAddress(final String prefix) {
        return prefix.substring(0, slashPosition(prefix));
    }

    protected static String getPrefixMask(final String prefix) {
        return prefix.substring(slashPosition(prefix) + 1);
    }

    private static String[] splitPrefix(final String prefix) {
        final int slash = slashPosition(prefix);
        return new String[] { prefix.substring(0, slash), prefix.substring(slash + 1) };
    }

    public static Eid normalize(Eid eid, short mask) {
        Address address = eid.getAddress();
        try {
            if (address instanceof Ipv4PrefixBinary) {
                byte[] addr = ((Ipv4PrefixBinary) address).getIpv4AddressBinary().getValue();
                return LispAddressUtil.asIpv4PrefixBinaryEid(eid, normalizeByteArray(addr, mask), mask);
            } else if (address instanceof Ipv6PrefixBinary) {
                byte[] addr = ((Ipv6PrefixBinary) address).getIpv6AddressBinary().getValue();
                return LispAddressUtil.asIpv6PrefixBinaryEid(eid, normalizeByteArray(addr, mask), mask);
            } else if (address instanceof Ipv4Prefix) {
                final String addr = getPrefixAddress(((Ipv4Prefix)address).getIpv4Prefix().getValue());
                InetAddress normalized = normalizeIP(InetAddresses.forString(addr), mask);
                return LispAddressUtil.asIpv4PrefixEid(eid, (Inet4Address)normalized, mask);
            } else if (address instanceof Ipv6Prefix) {
                final String addr = getPrefixAddress(((Ipv6Prefix)address).getIpv6Prefix().getValue());
                InetAddress normalized = normalizeIP(InetAddresses.forString(addr), mask);
                return LispAddressUtil.asIpv6PrefixEid(eid, (Inet6Address)normalized, mask);
            } else if (address instanceof InstanceId) {
                // TODO - not absolutely necessary, but should be implemented
                return eid;
            }
        } catch (UnknownHostException e) {
            LOG.warn("Failed to normalize EID {} with mask {}, returning original EID", eid, mask, e);
        }
        return eid;
    }

    public static Eid normalize(Eid eid) {
        Address address = eid.getAddress();
        try {
            if (address instanceof Ipv4PrefixBinary) {
                byte[] addr = ((Ipv4PrefixBinary) address).getIpv4AddressBinary().getValue();
                short mask = ((Ipv4PrefixBinary) address).getIpv4MaskLength();
                return LispAddressUtil.asIpv4PrefixBinaryEid(eid, normalizeByteArray(addr, mask), mask);
            } else if (address instanceof Ipv6PrefixBinary) {
                byte[] addr = ((Ipv6PrefixBinary) address).getIpv6AddressBinary().getValue();
                short mask = ((Ipv6PrefixBinary) address).getIpv6MaskLength();
                return LispAddressUtil.asIpv6PrefixBinaryEid(eid, normalizeByteArray(addr, mask), mask);
            } else if (address instanceof Ipv4Prefix) {
                String[] v4prefix = splitPrefix(((Ipv4Prefix)address).getIpv4Prefix().getValue());
                short mask = Short.parseShort(v4prefix[1]);
                InetAddress normalized = normalizeIP(InetAddresses.forString(v4prefix[0]), mask);
                return LispAddressUtil.asIpv4PrefixEid(eid, (Inet4Address) normalized, mask);
            } else if (address instanceof Ipv6Prefix) {
                String[] v6prefix = splitPrefix(((Ipv6Prefix)address).getIpv6Prefix().getValue());
                short mask = Short.parseShort(v6prefix[1]);
                InetAddress normalized = normalizeIP(InetAddresses.forString(v6prefix[0]), mask);
                return LispAddressUtil.asIpv6PrefixEid(eid, (Inet6Address) normalized, mask);
            } else if (address instanceof Ipv4) {
                return LispAddressUtil.asIpv4PrefixEid(((Ipv4) address).getIpv4(), eid.getVirtualNetworkId());
            } else if (address instanceof Ipv6) {
                return LispAddressUtil.asIpv6PrefixEid(((Ipv6) address).getIpv6(), eid.getVirtualNetworkId());
            } else if (address instanceof InstanceId) {
                // TODO - not absolutely necessary, but should be implemented
                return eid;
            } else if (address instanceof SourceDestKey) {
                return normalizeSrcDst(eid);
            } else if (address instanceof ServicePath) {
                // Build new Service Path eid with service index set to 0
                long spi = ((ServicePath) address).getServicePath().getServicePathId().getValue();
                long vni = eid.getVirtualNetworkId() != null ? eid.getVirtualNetworkId().getValue() : -1;
                return LispAddressUtil.asServicePathEid(vni, spi, (short)0);
            }
        } catch (UnknownHostException e) {
            LOG.warn("Failed to normalize EID {}, returning original", eid, e);
        }
        return eid;
    }

    private static Eid normalizeSrcDst(Eid eid) throws UnknownHostException {
        final SimpleAddress normalizedSrc = normalizeSimpleAddress(
                ((SourceDestKey) eid.getAddress()).getSourceDestKey().getSource());
        final SimpleAddress normalizedDst = normalizeSimpleAddress(
                ((SourceDestKey) eid.getAddress()).getSourceDestKey().getDest());
        return LispAddressUtil.asSrcDstEid(new SourceDestKeyBuilder()
                .setSource(normalizedSrc).setDest(normalizedDst).build(), eid.getVirtualNetworkId());
    }

    private static SimpleAddress normalizeSimpleAddress(SimpleAddress address) throws UnknownHostException {
        if (address.getIpPrefix() == null) {
            return address;
        }
        return new SimpleAddress(normalizeIpPrefix(address.getIpPrefix()));
    }

    private static IpPrefix normalizeIpPrefix(IpPrefix address) throws UnknownHostException {
        String[] prefix = splitPrefix(String.valueOf(address.getValue()));
        short mask = Short.parseShort(prefix[1]);

        InetAddress normalizedAddress = normalizeIP(InetAddresses.forString(prefix[0]), mask);
        return IetfInetUtil.INSTANCE.ipPrefixFor(normalizedAddress.getAddress(), mask);
    }

    private static InetAddress normalizeIP(InetAddress address, int maskLength) throws UnknownHostException {
        return InetAddress.getByAddress(normalizeByteArray(address.getAddress(), (short) maskLength));
    }

    public static byte[] normalizeByteArray(byte[] address, short maskLength) {
        ByteBuffer byteRepresentation = ByteBuffer.wrap(address);
        byte byteMask = (byte) 0xff;
        int mask = maskLength;
        for (int i = 0; i < byteRepresentation.array().length; i++) {
            if (mask >= 8) {
                byteRepresentation.put(i, (byte) (byteMask & byteRepresentation.get(i)));
            } else if (mask > 0) {
                byteRepresentation.put(i, (byte) ((byte) (byteMask << (8 - mask)) & byteRepresentation.get(i)));
            } else {
                byteRepresentation.put(i, (byte) (0 & byteRepresentation.get(i)));
            }

            mask -= 8;
        }
        return byteRepresentation.array();
    }

    public static int getMaxMask(Address address) {
        if (address instanceof Ipv4 || address instanceof Ipv4Prefix || address instanceof Ipv4Binary
                || address instanceof Ipv4PrefixBinary) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6 || address instanceof Ipv6Prefix || address instanceof Ipv4Binary
                || address instanceof Ipv6PrefixBinary) {
            return IPV6_MAX_MASK;
        } else {
            return -1;
        }
    }

    private static String getIpPrefixString(IpPrefix prefix) {
        if (prefix.getIpv4Prefix() != null) {
            return prefix.getIpv4Prefix().getValue();
        } else if (prefix.getIpv6Prefix() != null) {
            return prefix.getIpv6Prefix().getValue();
        } else {
            throw new IllegalArgumentException("Invalid prefix " + prefix);
        }
    }

    public static short getMaskForIpPrefix(IpPrefix prefix) {
        return Short.parseShort(getPrefixMask(getIpPrefixString(prefix)));
    }

    public static String getAddressStringForIpPrefix(IpPrefix prefix) {
        return getPrefixAddress(getIpPrefixString(prefix));
    }

    public static String getAddressStringForIpv4Prefix(Ipv4Prefix prefix) {
        return getPrefixAddress(prefix.getIpv4Prefix().getValue());
    }

    public static String getAddressStringForIpv6Prefix(Ipv6Prefix prefix) {
        return getPrefixAddress(prefix.getIpv6Prefix().getValue());
    }

    public static short getMaskForAddress(SimpleAddress address) {
        if (address.getIpPrefix() == null) {
            return -1;
        }
        return getMaskForIpPrefix(address.getIpPrefix());
    }

    public static short getMaskForAddress(Address address) {
        if (address instanceof Ipv4) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6) {
            return IPV6_MAX_MASK;
        } else if (address instanceof Ipv4Binary) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6Binary) {
            return IPV6_MAX_MASK;
        } else if (address instanceof Ipv4Prefix) {
            return Short.parseShort(getPrefixMask(((Ipv4Prefix) address).getIpv4Prefix().getValue()));
        } else if (address instanceof Ipv6Prefix) {
            return Short.parseShort(getPrefixMask(((Ipv6Prefix) address).getIpv6Prefix().getValue()));
        } else if (address instanceof InstanceId) {
            return getMaskForAddress(((InstanceId)address).getInstanceId().getAddress());
        } else if (address instanceof Ipv4PrefixBinary) {
            return ((Ipv4PrefixBinary) address).getIpv4MaskLength();
        } else if (address instanceof Ipv6PrefixBinary) {
            return ((Ipv6PrefixBinary) address).getIpv6MaskLength();
        }
        return -1;
    }
}
