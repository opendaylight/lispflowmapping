package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import org.apache.tomcat.util.buf.HexUtils;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispMacAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.MacBuilder;
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
        StringBuilder sb = new StringBuilder();
        for (byte b : macBuffer) {
            sb.append(HexUtils.getHex(b) + ":");
        }
        sb.deleteCharAt(17);
        return new MacBuilder().setMacAddress(new MacAddress(sb.toString())).build();
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAFIAddress lispAddress) {
        LispMacAddress lispMACAddress = (LispMacAddress) lispAddress;
        String macString = lispMACAddress.getMacAddress().getValue();
        macString.replace(':', ' ');
        buffer.put(DatatypeConverter.parseHexBinary(macString));
    }

    protected static ByteBuffer hexToByteBuffer(String hex) {
        String[] hexBytes = hex.split(" ");
        ByteBuffer bb = ByteBuffer.allocate(hexBytes.length);
        for (String hexByte : hexBytes) {
            bb.put((byte) Integer.parseInt(hexByte, 16));
        }
        bb.clear();
        return bb;
    }

    private interface Length {
        int MAC = 6;
    }

}
