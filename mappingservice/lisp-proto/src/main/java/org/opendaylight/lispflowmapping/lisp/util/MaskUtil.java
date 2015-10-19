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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.SourceDestKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.instance.id.InstanceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MaskUtil.class);
    private static final short IPV4_MAX_MASK = 32;
    private static final short IPV6_MAX_MASK = 128;

    public static boolean isMaskable(Address address) {
        if (address instanceof Ipv4Prefix || address instanceof Ipv6Prefix || address instanceof SourceDestKey) {
            return true;
        } else if (address instanceof InstanceId) {
            return isMaskable(((InstanceId)address).getAddress());
        }
        return false;
    }

    public static boolean isMaskable(SimpleAddress address) {
        if (address.getIpPrefix() != null) {
            return true;
        }
        return false;
    }

    public static Eid normalize(Eid eid) {
        Address address = eid.getAddress();
        try {
            if (address instanceof Ipv4Prefix) {
                String v4 = ((Ipv4Prefix)address).getIpv4Prefix().getValue();
                short mask = Short.parseShort(v4.split("/")[1]);
                InetAddress normalized = normalizeIP(Inet4Address.getByName(v4), mask);
                return LispAFIConvertor.asIpv4Prefix(eid, (Inet4Address)normalized, mask);
            } else if (address instanceof Ipv6Prefix) {
                String v6 = ((Ipv6Prefix)address).getIpv6Prefix().getValue();
                short mask = Short.parseShort(v6.split("/")[1]);
                InetAddress normalized = normalizeIP(Inet6Address.getByName(v6), mask);
                return LispAFIConvertor.asIpv6Prefix(eid, (Inet6Address)normalized, mask);
            } else if (address instanceof InstanceId) {
                // TODO - not absolutely necessary, but should be implemented
                return eid;
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize " + eid + ": " + ExceptionUtils.getStackTrace(e));
        }
        return eid;
    }

    private static InetAddress normalizeIP(InetAddress address, int mask) throws UnknownHostException {
        ByteBuffer byteRepresentation = ByteBuffer.wrap(address.getAddress());
        byte b = (byte) 0xff;
        for (int i = 0; i < byteRepresentation.array().length; i++) {
            if (mask >= 8)
                byteRepresentation.put(i, (byte) (b & byteRepresentation.get(i)));

            else if (mask > 0) {
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
        if (address.getIpPrefix() != null) {
            String prefix = new String(address.getIpPrefix().getValue());
            return Short.parseShort(prefix.split("/")[1]);
        }
        return -1;
    }

    public static short getMaskForAddress(Address address) {
        if (address instanceof Ipv4) {
            return IPV4_MAX_MASK;
        } else if (address instanceof Ipv6) {
            return IPV6_MAX_MASK;
        } else if (address instanceof Ipv4Prefix) {
            String[] prefix = ((Ipv4Prefix)address).getIpv4Prefix().getValue().split("/");  // TODO use something more efficient
            return Short.parseShort(prefix[1]);
        } else if (address instanceof Ipv6Prefix) {
            String[] prefix = ((Ipv6Prefix)address).getIpv6Prefix().getValue().split("/");  // TODO use something more efficient
            return Short.parseShort(prefix[1]);
        } else if (address instanceof InstanceId) {
            return getMaskForAddress(((InstanceId)address).getAddress());
        }
        return -1;
    }
}
