package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispMacAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class LispMACAddressSerializer extends LispAddressSerializer {

    private static final LispMACAddressSerializer INSTANCE = new LispMACAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispMACAddressSerializer() {
    }

    public static LispMACAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public int getAddressSize(LispAFIAddress lispAddress) {
        return Length.MAC;
    }

    @Override
    protected LispMacAddress deserializeData(ByteBuffer buffer) {
        byte[] macBuffer = new byte[6];
        buffer.get(macBuffer);
        StringBuilder sb = new StringBuilder(17);
        for (byte b : macBuffer) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return new MacBuilder().setMacAddress(new MacAddress(sb.toString())).setAfi((short) 16389).build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispMacAddress lispMACAddress = (LispMacAddress) lispAddress;
        String macString = lispMACAddress.getMacAddress().getValue();
        macString = macString.replaceAll(":", "");
        buffer.put(DatatypeConverter.parseHexBinary(macString));
    }

    private interface Length {
        int MAC = 6;
    }

}
