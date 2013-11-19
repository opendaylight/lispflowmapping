package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv6Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;

public class LispIpv6AddressSerializer extends LispAddressSerializer {

    private static final LispIpv6AddressSerializer INSTANCE = new LispIpv6AddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispIpv6AddressSerializer() {
    }

    public static LispIpv6AddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.IPV6;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispIpv6Address lispIpvAddress = (LispIpv6Address) lispAddress;
        try {
            buffer.put(Inet6Address.getByName(lispIpvAddress.getIpv6Address().getValue()).getAddress());
        } catch (UnknownHostException e) {
        }
    }

    @Override
    protected LispIpv6Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[16];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        return new Ipv6Builder().setIpv6Address(new Ipv6Address(address.getHostAddress())).setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode())
                .build();
    }

    private interface Length {
        int IPV6 = 16;
    }

}
