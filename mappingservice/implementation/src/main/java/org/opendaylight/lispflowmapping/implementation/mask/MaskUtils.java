package org.opendaylight.lispflowmapping.implementation.mask;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address;

public class MaskUtils {

    public static boolean isMaskable(LispAFIAddress address, int mask) {
        if (address instanceof LispIpv4Address || address instanceof LispIpv6Address) {
            if (mask > 0 && mask < getMaxMask(address)) {
                return true;
            }
        }
        return false;
    }

    public static LispAFIAddress normalize(LispAFIAddress address, int mask) {
        try {
            if (address instanceof LispIpv4Address) {
                return LispAFIConvertor.asIPAfiAddress(normalizeIP(Inet4Address.getByName(((LispIpv4Address) address).getIpv4Address().getValue()),
                        mask).getHostAddress());
            }
            if (address instanceof LispIpv6Address) {
                return LispAFIConvertor.asIPv6AfiAddress(normalizeIP(Inet6Address.getByName(((LispIpv6Address) address).getIpv6Address().getValue()),
                        mask).getHostAddress());
            }

        } catch (UnknownHostException e) {
            return null;
        }
        return null;
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

    private static int getMaxMask(LispAFIAddress address) {
        if (address instanceof LispIpv4Address) {
            return 32;
        }
        if (address instanceof LispIpv6Address) {
            return 128;
        }
        return -1;
    }

}
