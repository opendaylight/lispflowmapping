/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lcafsegmentaddress.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MaskUtil.class);
    public static boolean isMaskable(LispAFIAddress address) {
        if (address instanceof Ipv4Address || address instanceof Ipv6Address
                || address instanceof LcafSegmentAddr) {
            return true;
        }
        return false;
    }

    public static LispAFIAddress normalize(LispAFIAddress address, int mask) {
        try {
            if (address instanceof Ipv4Address) {
                return LispAFIConvertor.asIPAfiAddress(normalizeIP(Inet4Address.getByName(((Ipv4Address) address)
                        .getIpv4Address().getValue()), mask).getHostAddress());
            } else if (address instanceof Ipv6Address) {
                return  LispAFIConvertor.asIPv6AfiAddress(normalizeIP(Inet6Address.getByName(((Ipv6Address) address)
                        .getIpv6Address().getValue()), mask).getHostAddress());
            } else if (address instanceof LcafSegmentAddr) {
                LcafSegmentAddr segAddr = (LcafSegmentAddr) address;
                Address normalizedAddr = new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(
                        normalizeIpAddress(LispAFIConvertor.toAFIfromPrimitive(segAddr.getAddress()
                        .getPrimitiveAddress()), mask))).build();
                return new LcafSegmentAddrBuilder(segAddr).setAddress(normalizedAddr).build();
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize " + address + ": " + ExceptionUtils.getStackTrace(e));
            return null;
        }
        return null;
    }

    private static LispAFIAddress normalizeIpAddress(LispAFIAddress address, int mask) throws UnknownHostException {
        if (address instanceof Ipv4Address) {
            return LispAFIConvertor.asIPAfiAddress(normalizeIP(Inet4Address.getByName(((Ipv4Address) address)
                    .getIpv4Address().getValue()), mask).getHostAddress());
        } else if (address instanceof Ipv6Address) {
            return  LispAFIConvertor.asIPv6AfiAddress(normalizeIP(Inet6Address.getByName(((Ipv6Address) address)
                    .getIpv6Address().getValue()), mask).getHostAddress());
        }
        throw new UnknownHostException("Address + " + address + " not an IPv4 or IPv6 address");
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

    public static int getMaxMask(LispAFIAddress address) {
        if (address instanceof Ipv4Address) {
            return 32;
        }
        if (address instanceof Ipv6Address) {
            return 128;
        }
        return -1;
    }

    public static byte getMaxMaskForAfi(int afi) {
        if (afi == 1) {
            return (byte) 32;
        } else if (afi == 2){
            return (byte) 128;
        } else {
            return (byte) -1;
        }
    }
}
