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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.ServicePath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MaskUtil.class);
    private static final short IPV4_MAX_MASK = 32;
    private static final short IPV6_MAX_MASK = 128;

    // Utility class, should not be instantiated
    private MaskUtil() {
    }

    public static boolean isMaskable(Address address) {
        if (address instanceof Ipv4Prefix || address instanceof Ipv6Prefix || address instanceof SourceDestKey) {
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

    public static Eid normalize(Eid eid, short mask) {
        Address address = eid.getAddress();
        try {
            if (address instanceof Ipv4Prefix) {
                String[] v4prefix = String.valueOf(((Ipv4Prefix)address).getIpv4Prefix().getValue()).split("/");
                InetAddress normalized = normalizeIP(Inet4Address.getByName(v4prefix[0]), mask);
                return LispAddressUtil.asIpv4PrefixEid(eid, (Inet4Address)normalized, mask);
            } else if (address instanceof Ipv6Prefix) {
                String[] v6prefix = String.valueOf(((Ipv6Prefix)address).getIpv6Prefix().getValue()).split("/");
                InetAddress normalized = normalizeIP(Inet6Address.getByName(v6prefix[0]), mask);
                return LispAddressUtil.asIpv6PrefixEid(eid, (Inet6Address)normalized, mask);
            } else if (address instanceof InstanceId) {
                // TODO - not absolutely necessary, but should be implemented
                return eid;
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize eid {} with mask {}: {}", eid, mask, ExceptionUtils.getStackTrace(e));
        }
        return eid;
    }

    public static Eid normalize(Eid eid) {
        Address address = eid.getAddress();
        try {
            if (address instanceof Ipv4Prefix) {
                String[] v4prefix = String.valueOf(((Ipv4Prefix)address).getIpv4Prefix().getValue()).split("/");
                short mask = Short.parseShort(v4prefix[1]);
                InetAddress normalized = normalizeIP(Inet4Address.getByName(v4prefix[0]), mask);
                return LispAddressUtil.asIpv4PrefixEid(eid, (Inet4Address)normalized, mask);
            } else if (address instanceof Ipv6Prefix) {
                String[] v6prefix = String.valueOf(((Ipv6Prefix)address).getIpv6Prefix().getValue()).split("/");
                short mask = Short.parseShort(v6prefix[1]);
                InetAddress normalized = normalizeIP(Inet6Address.getByName(v6prefix[0]), mask);
                return LispAddressUtil.asIpv6PrefixEid(eid, (Inet6Address)normalized, mask);
            } else if (address instanceof InstanceId) {
                // TODO - not absolutely necessary, but should be implemented
                return eid;
            } else if (address instanceof ServicePath) {
                // Build new Service Path eid with service index set to 0
                long spi = ((ServicePath) address).getServicePath().getServicePathId().getValue();
                long vni = eid.getVirtualNetworkId() != null ? eid.getVirtualNetworkId().getValue() : -1;
                return LispAddressUtil.asServicePathEid(vni, spi, (short)0);
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize eid {}: {}", eid, ExceptionUtils.getStackTrace(e));
        }
        return eid;
    }

    private static InetAddress normalizeIP(InetAddress address, int maskLength) throws UnknownHostException {
        ByteBuffer byteRepresentation = ByteBuffer.wrap(address.getAddress());
        byte b = (byte) 0xff;
        int mask = maskLength;
        for (int i = 0; i < byteRepresentation.array().length; i++) {
            if (mask >= 8) {
                byteRepresentation.put(i, (byte) (b & byteRepresentation.get(i)));
            } else if (mask > 0) {
                byteRepresentation.put(i, (byte) ((byte) (b << (8 - mask)) & byteRepresentation.get(i)));
            } else {
                byteRepresentation.put(i, (byte) (0 & byteRepresentation.get(i)));
            }

            mask -= 8;
        }
        return InetAddress.getByAddress(byteRepresentation.array());
    }

    public static int getMaxMask(Address address) {
        if (address instanceof Ipv4 || address instanceof Ipv4Prefix) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6 || address instanceof Ipv6Prefix) {
            return IPV6_MAX_MASK;
        } else {
            return -1;
        }
    }

    public static short getMaskForAddress(SimpleAddress address) {
        if (address.getIpPrefix() == null) {
            return -1;
        }
        return getMaskForIpPrefix(address.getIpPrefix());
    }

    public static short getMaskForIpPrefix(IpPrefix prefix) {
        return Short.parseShort(String.valueOf(prefix.getValue()).split("/")[1]);
    }

    public static String getAddressStringForIpPrefix(IpPrefix prefix) {
        return String.valueOf(prefix.getValue()).split("/")[0];
    }

    public static String getAddressStringForIpv4Prefix(Ipv4Prefix prefix) {
        return String.valueOf(prefix.getIpv4Prefix().getValue()).split("/")[0];
    }

    public static String getAddressStringForIpv6Prefix(Ipv6Prefix prefix) {
        return String.valueOf(prefix.getIpv6Prefix().getValue()).split("/")[0];
    }

    public static short getMaskForAddress(Address address) {
        if (address instanceof Ipv4) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6) {
            return IPV6_MAX_MASK;
        } else if (address instanceof Ipv4Prefix) {
            String[] prefix = String.valueOf(((Ipv4Prefix)address).getIpv4Prefix().getValue()).split("/");  // TODO use something more efficient
            return Short.parseShort(prefix[1]);
        } else if (address instanceof Ipv6Prefix) {
            String[] prefix = String.valueOf(((Ipv6Prefix)address).getIpv6Prefix().getValue()).split("/");  // TODO use something more efficient
            return Short.parseShort(prefix[1]);
        } else if (address instanceof InstanceId) {
            return getMaskForAddress(((InstanceId)address).getInstanceId().getAddress());
        }
        return -1;
    }
}
