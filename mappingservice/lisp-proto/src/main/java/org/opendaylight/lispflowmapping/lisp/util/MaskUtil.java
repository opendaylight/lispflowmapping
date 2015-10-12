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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.SimpleAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lcaf.address.address.instance.id.InstanceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispIpv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsegmentaddress.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.DstAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.SrcAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSegmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv4.Ipv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.ipv6.Ipv6AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsegment.LcafSegmentAddrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.lcafsourcedest.LcafSourceDestAddrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MaskUtil.class);
    public static boolean isMaskable(LispAddress address) {
        if (address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4 ||
            address instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6 ||
            address instanceof InstanceId) {
            return true;
        }
        return false;
    }

    public static boolean isMaskable(LispAFIAddress address) {
        if (address instanceof Ipv4Address || address instanceof Ipv6Address || address instanceof LcafSegmentAddr) {
            return true;
        }
        return false;
    }

    public static boolean isMaskable(LispAddressContainer address) {
        if (address.getAddress() instanceof Ipv4  || address.getAddress() instanceof Ipv6) {
            return true;
        } else if (address.getAddress() instanceof LcafSegment) {
            return isMaskable(LispAFIConvertor.toAFIfromPrimitive(((LcafSegment) address.getAddress())
                    .getLcafSegmentAddr().getAddress().getPrimitiveAddress()));
        } else if (address.getAddress() instanceof LcafSourceDestAddress) {
            LcafSourceDestAddr sd = ((LcafSourceDest) address.getAddress()).getLcafSourceDestAddr();
            return isMaskable(LispAFIConvertor.toAFIfromPrimitive(sd.getSrcAddress().getPrimitiveAddress()))
                    && isMaskable(LispAFIConvertor.toAFIfromPrimitive(sd.getDstAddress().getPrimitiveAddress()));
        }
        return false;
    }

    public static LispAFIAddress normalize(LispAFIAddress address, short mask) {
        try {
            if (address instanceof Ipv4Address) {
                return LispAFIConvertor.asIPAfiAddress(normalizeIP(
                        Inet4Address.getByName(((Ipv4Address) address).getIpv4Address().getValue()),
                        mask).getHostAddress());
            } else if (address instanceof Ipv6Address) {
                return LispAFIConvertor.asIPv6AfiAddress(normalizeIP(
                        Inet6Address.getByName(((Ipv6Address) address).getIpv6Address().getValue()),
                        mask).getHostAddress());
            } else if (address instanceof LcafSegmentAddr) {
                LcafSegmentAddr segAddr = (LcafSegmentAddr) address;
                LispAFIAddress afiAddr = LispAFIConvertor
                        .toAFIfromPrimitive(segAddr.getAddress().getPrimitiveAddress());
                Address normalizedAddr = new AddressBuilder().setPrimitiveAddress(
                        LispAFIConvertor.toPrimitive(normalize(afiAddr, mask))).build();
                return new LcafSegmentAddrBuilder(segAddr).setAddress(normalizedAddr).build();
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize " + address + ": " + ExceptionUtils.getStackTrace(e));
        }
        return address;
    }

    public static LispAFIAddress normalize(LispAFIAddress address) {
        if (address instanceof Ipv4Address) {
            return normalize(address, ((Ipv4Address) address).getMask());
        } else if (address instanceof Ipv6Address) {
            return normalize(address, ((Ipv6Address) address).getMask());
        } else if (address instanceof LcafSegmentAddr) {
            LcafSegmentAddr segAddr = (LcafSegmentAddr) address;
            LispAFIAddress afiAddr = LispAFIConvertor.toAFIfromPrimitive(segAddr.getAddress().getPrimitiveAddress());
            short mask = getMaskForAfiAddress(afiAddr);
            if (mask == 0) {
                return address;
            }
            Address normalizedAddr = new AddressBuilder().setPrimitiveAddress(
                    LispAFIConvertor.toPrimitive(normalize(afiAddr, mask))).build();
            return new LcafSegmentAddrBuilder(segAddr).setAddress(normalizedAddr).build();
        }
        return address;
    }

    public static LispAddressContainer normalize(LispAddressContainer address, short mask) {
        try {
            if (address.getAddress() instanceof Ipv4) {
                return LispAFIConvertor.asIPv4Prefix(normalizeIP(
                        Inet4Address.getByName(((Ipv4) address.getAddress()).getIpv4Address().getIpv4Address().getValue()),
                        mask).getHostAddress(), mask);
            } else if (address.getAddress() instanceof Ipv6) {
                return LispAFIConvertor.asIPv6Prefix(normalizeIP(
                        Inet6Address.getByName(((Ipv6) address.getAddress()).getIpv6Address().getIpv6Address().getValue()),
                        mask).getHostAddress(), mask);
            } else if (address.getAddress() instanceof LcafSegment) {
                LcafSegmentAddress segAddr = ((LcafSegment) address.getAddress()).getLcafSegmentAddr();
                LispAFIAddress afiAddr = LispAFIConvertor
                        .toAFIfromPrimitive(segAddr.getAddress().getPrimitiveAddress());
                Address normalizedAddr = new AddressBuilder().setPrimitiveAddress(
                        LispAFIConvertor.toPrimitive(normalize(afiAddr, mask))).build();
                return new LispAddressContainerBuilder().setAddress(
                        new LcafSegmentBuilder().setLcafSegmentAddr(
                                new LcafSegmentAddrBuilder(segAddr).setAddress(normalizedAddr).build()).build())
                        .build();
            }
        } catch (UnknownHostException e) {
            LOG.trace("Failed to normalize " + address + ": " + ExceptionUtils.getStackTrace(e));
        }
        return address;
    }

    public static LispAddressContainer normalize(LispAddressContainer address) {
        if (address.getAddress() instanceof Ipv4) {
            return normalize(address, ((Ipv4)address.getAddress()).getIpv4Address().getMask());
        } else if (address.getAddress() instanceof Ipv6) {
            return normalize(address, ((Ipv6)address.getAddress()).getIpv6Address().getMask());
        } else if (address.getAddress() instanceof LcafSegment) {
            LcafSegmentAddress segAddr = ((LcafSegment) address.getAddress()).getLcafSegmentAddr();
            LispAFIAddress afiAddr = LispAFIConvertor
                    .toAFIfromPrimitive(segAddr.getAddress().getPrimitiveAddress());
            short mask = getMaskForAfiAddress(afiAddr);
            if (mask == 0) {
                return address;
            }
            Address normalizedAddr = new AddressBuilder().setPrimitiveAddress(
                    LispAFIConvertor.toPrimitive(normalize(afiAddr, mask))).build();
            return new LispAddressContainerBuilder().setAddress(
                    new LcafSegmentBuilder().setLcafSegmentAddr(
                            new LcafSegmentAddrBuilder(segAddr).setAddress(normalizedAddr).build()).build())
                    .build();
        }
        return address;
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

    public static String normalizeIPString(String addr, int mask) {
        short afi = getIpAfiForString(addr);
        try {
            if (afi == 1) {
                return normalizeIP(Inet4Address.getByName(addr), mask).getHostAddress();
            } else if (afi == 2) {
                return normalizeIP(Inet6Address.getByName(addr), mask).getHostAddress();
            } else {
                LOG.debug("The string {} is not a valid IP address!", addr);
                return null;
            }
        } catch (Exception e){
            LOG.trace("Failed to normalize " + addr + ": " + ExceptionUtils.getStackTrace(e));
        }
        return null;
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
        } else if (afi == 2) {
            return (byte) 128;
        } else {
            return (byte) -1;
        }
    }

    private static short getMaskForAfiAddress(LispAFIAddress addr) {
        if (addr instanceof Ipv4Address) {
            Short res = ((Ipv4Address) addr).getMask();
            return (res != null) ? res.shortValue() : 32;
        } else if (addr instanceof Ipv6Address) {
            Short res = ((Ipv6Address) addr).getMask();
            return (res != null) ? res.shortValue() : 128;
        } else {
            return 0;
        }
    }

    public static short getMaskForAddress(SimpleAddress address) {
        return 0;
    }

    public static short getMaskForAddress(LispAddress address) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.Address addr = address.getAddress();
        if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4) {
            return 32;
        } else if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6) {
            return 128;
        } else if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4Prefix) {
            String[] prefix = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv4Prefix)addr).getIpv4Prefix().getValue().split("/");
            return Short.parseShort(prefix[1]);
        } else if (addr instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6Prefix) {
            String[] prefix = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.lisp.address.types.rev150309.lisp.address.address.Ipv6Prefix)addr).getIpv6Prefix().getValue().split("/");
            return Short.parseShort(prefix[1]);
        } else if (addr instanceof InstanceId) {
            return getMaskForAddress(((InstanceId)addr).getAddress());
        }
        return 0;
    }

    public static short getMaskForAddress(LispAddressContainer addr) {
        if (addr.getAddress() instanceof Ipv4) {
            Short res = ((Ipv4)addr.getAddress()).getIpv4Address().getMask();
            return (res != null) ? res.shortValue() : 32;
        } else if (addr.getAddress() instanceof Ipv6) {
            Short res = ((Ipv6)addr.getAddress()).getIpv6Address().getMask();
            return (res != null) ? res.shortValue() : 128;
        } else if (addr.getAddress() instanceof LcafSegment) {
            return getMaskForAfiAddress(LispAFIConvertor.toAFIfromPrimitive((((LcafSegment) addr.getAddress())
                    .getLcafSegmentAddr()).getAddress().getPrimitiveAddress()));
        }
        return 0;
    }

    private static short getIpAfiForString(String addr) {
        if(addr.matches("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") == true){
            return 1;
        }
        else if(addr.matches("([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)") == true){
            return 2;
        }
        return -1;
    }

    public static LispAddressContainer setMask(LispAddressContainer addr, int mask) {
        if (addr.getAddress() instanceof Ipv4) {
            return new LispAddressContainerBuilder().setAddress(
                    new Ipv4Builder().setIpv4Address(
                            new Ipv4AddressBuilder(((Ipv4) addr.getAddress()).getIpv4Address()).setMask((short) mask)
                                    .build()).build()).build();

        } else if (addr.getAddress() instanceof Ipv6) {
            return new LispAddressContainerBuilder().setAddress(
                    new Ipv6Builder().setIpv6Address(
                            new Ipv6AddressBuilder(((Ipv6) addr.getAddress()).getIpv6Address()).setMask((short) mask)
                                    .build()).build()).build();
        } else if (addr.getAddress() instanceof LcafSegment) {
            setMask(LispAFIConvertor.toAFIfromPrimitive(((LcafSegment) addr.getAddress()).getLcafSegmentAddr()
                    .getAddress().getPrimitiveAddress()), mask);
        }
        return addr;
    }

    public static LispAFIAddress setMask(LispAFIAddress addr, int mask) {
        if (addr instanceof LispIpv4Address) {
            return new Ipv4AddressBuilder().setIpv4Address(((LispIpv4Address) addr).getIpv4Address())
                    .setAfi(addr.getAfi()).setMask((short)mask).build();
        } else if (addr instanceof LispIpv6Address) {
            return new Ipv6AddressBuilder().setIpv6Address(((LispIpv6Address) addr).getIpv6Address())
                    .setAfi(addr.getAfi()).setMask((short)mask).build();
        } else if (addr instanceof LcafSegmentAddress) {
            LispAFIAddress afiAddr = LispAFIConvertor.toAFIfromPrimitive(((LcafSegmentAddress) addr).getAddress()
                    .getPrimitiveAddress());
            afiAddr = setMask(afiAddr, mask);
            return new LcafSegmentAddrBuilder((LcafSegmentAddress) addr).setAddress(
                    new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(afiAddr)).build()).build();
        }
        return addr;
    }

    public static LispAFIAddress setMaskSourceDest(LispAFIAddress addr, int srcMask, int dstMask) {
        DstAddress dst = LcafSourceDestHelper.getDstAddress(addr);
        LispAFIAddress dstAfi = LispAFIConvertor.toAFIfromPrimitive(dst.getPrimitiveAddress());
        DstAddress newDst = new DstAddressBuilder(dst).setPrimitiveAddress(LispAFIConvertor.toPrimitive(setMask(dstAfi, dstMask))).build();
        SrcAddress src = LcafSourceDestHelper.getSrcAddress(addr);
        LispAFIAddress srcAfi = LispAFIConvertor.toAFIfromPrimitive(src.getPrimitiveAddress());
        SrcAddress newSrc = new SrcAddressBuilder(src).setPrimitiveAddress(LispAFIConvertor.toPrimitive(setMask(srcAfi, srcMask))).build();
        return new LcafSourceDestAddrBuilder((LcafSourceDestAddress)addr).setDstAddress(newDst).setSrcAddress(newSrc).build();
    }

    public static LispAddressContainer setMaskSourceDest(LispAddressContainer addr, int srcMask, int dstMask) {
        return new LispAddressContainerBuilder().setAddress(
                new LcafSourceDestBuilder((LcafSourceDest) addr.getAddress()).setLcafSourceDestAddr(
                        (LcafSourceDestAddr) setMaskSourceDest(
                                ((LcafSourceDest) addr.getAddress()).getLcafSourceDestAddr(), srcMask, dstMask))
                        .build()).build();
    }

    /**
     * RFC6830 defines masks and EIDs as separate fields of larger, encompassing blocks of LISP messages. However within
     * LispFlowMapping masks are internal fields of EIDs. The fixMask methods should be used in deserializers and RPC
     * handlers to ensure that masks for EIDs reflect accordingly the mask field values carried in LISP messages or RPC
     * input objects respectively.
     */
    public static LispAFIAddress fixMask(LispAFIAddress addr, short mask) {
        if (addr instanceof LispIpv4Address || addr instanceof LispIpv6Address || addr instanceof LcafSegmentAddress) {
           return MaskUtil.setMask(addr, mask);
        } else if (addr instanceof LcafSourceDestAddress) {
           return MaskUtil.setMaskSourceDest(addr, LcafSourceDestHelper.getSrcMask(addr),
                    LcafSourceDestHelper.getDstMask(addr));
        }
        return addr;
    }

    public static LispAddressContainer fixMask(LispAddressContainer addr, short mask) {
        if (addr.getAddress() instanceof Ipv4 || addr.getAddress() instanceof Ipv6
                || addr.getAddress() instanceof LcafSegment) {
            return MaskUtil.setMask(addr, mask);
        } else if (addr.getAddress() instanceof LcafSourceDest) {
            return MaskUtil.setMaskSourceDest(addr, LcafSourceDestHelper.getSrcMask(addr),
                    LcafSourceDestHelper.getDstMask(addr));
        }
        return addr;
    }
}
