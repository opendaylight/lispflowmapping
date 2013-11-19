package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

public class LispIpv4AddressSerializer extends LispAddressSerializer {

    private static final LispIpv4AddressSerializer INSTANCE = new LispIpv4AddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispIpv4AddressSerializer() {
    }

    public static LispIpv4AddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.IPV4;
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispIpv4Address lispIpvAddress = (LispIpv4Address) lispAddress;
        try {
            buffer.put(Inet4Address.getByName(lispIpvAddress.getIpv4Address().getValue()).getAddress());
        } catch (UnknownHostException e) {
        }
    }

    @Override
    protected LispIpv4Address deserializeData(ByteBuffer buffer) {
        byte[] ipBuffer = new byte[4];
        InetAddress address = null;
        buffer.get(ipBuffer);
        try {
            address = InetAddress.getByAddress(ipBuffer);
        } catch (UnknownHostException e) {
        }
        return new Ipv4Builder().setIpv4Address(new Ipv4Address(address.getHostAddress())).setAfi(AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    private interface Length {
        int IPV4 = 4;
    }

}
