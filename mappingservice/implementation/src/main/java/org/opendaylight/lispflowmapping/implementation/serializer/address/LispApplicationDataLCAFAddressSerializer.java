package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;

public class LispApplicationDataLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispApplicationDataLCAFAddressSerializer INSTANCE = new LispApplicationDataLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispApplicationDataLCAFAddressSerializer() {
    }

    public static LispApplicationDataLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected short getLcafLength(LispAddress lispAddress) {
        return (short) (Length.ALL_FIELDS + LispAddressSerializer.getInstance().getAddressSize(((LispApplicationDataLCAFAddress)lispAddress).getAddress()));
    }

    @Override
    protected void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        LispApplicationDataLCAFAddress applicationDataAddress = ((LispApplicationDataLCAFAddress) lispAddress);
        buffer.put(ByteUtil.partialIntToByteArray(applicationDataAddress.getIPTos(), Length.TOC));
        buffer.put(applicationDataAddress.getProtocol());
        buffer.putShort(applicationDataAddress.getLocalPort());
        buffer.putShort(applicationDataAddress.getRemotePort());
        LispAddressSerializer.getInstance().serialize(buffer, ((LispApplicationDataLCAFAddress) lispAddress).getAddress());
    }

    @Override
    protected LispApplicationDataLCAFAddress deserializeData(ByteBuffer buffer, byte res2, short length) {

        LispApplicationDataLCAFAddress applicationDataAddress = new LispApplicationDataLCAFAddress(res2);
        byte[] rawIPTos = new byte[3];
        buffer.get(rawIPTos);
        applicationDataAddress.setIPTos(ByteUtil.getPartialInt(rawIPTos));
        applicationDataAddress.setProtocol(buffer.get());
        applicationDataAddress.setLocalPort(buffer.getShort());
        applicationDataAddress.setRemotePort(buffer.getShort());
        LispAddress address = LispAddressSerializer.getInstance().deserialize(buffer);
        applicationDataAddress.setAddress(address);

        return applicationDataAddress;
    }

    private interface Length {
        int LOCAL_PORT = 2;
        int REMOTE_PORT = 2;
        int TOC = 3;
        int PROTOCOL = 1;
        int ALL_FIELDS = LOCAL_PORT + REMOTE_PORT + TOC + PROTOCOL;
    }

}
