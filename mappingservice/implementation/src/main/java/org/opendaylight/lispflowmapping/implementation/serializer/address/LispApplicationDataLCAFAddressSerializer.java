package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispApplicationDataLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispSegmentLCAFAddress;

public class LispApplicationDataLCAFAddressSerializer extends LispLCAFAddressSerializer {

    private static final LispApplicationDataLCAFAddressSerializer INSTANCE = new LispApplicationDataLCAFAddressSerializer();

    // Private constructor prevents instantiation from other classes
    private LispApplicationDataLCAFAddressSerializer() {
    }

    public static LispApplicationDataLCAFAddressSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public short getLcafLength(LispAddress lispAddress) {
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(((LispApplicationDataLCAFAddress) lispAddress).getAddress()
                .getAfi());
        return (short) (Length.ALL_FIELDS + serializer.getAddressSize(lispAddress));
    }

    @Override
    public void serialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        LispApplicationDataLCAFAddress applicationDataAddress = ((LispApplicationDataLCAFAddress) lispAddress);
        buffer.put(ByteUtil.partialIntToByteArray(applicationDataAddress.getIPTos(), Length.TOC));
        buffer.put(applicationDataAddress.getProtocol());
        buffer.putShort(applicationDataAddress.getLocalPort());
        buffer.putShort(applicationDataAddress.getRemotePort());
        LispAddressSerializer serializer = LispAddressSerializerFactory.getSerializer(applicationDataAddress.getAddress().getAfi());
        if (serializer == null) {
            throw new LispSerializationException("Unknown AFI type=" + ((LispSegmentLCAFAddress) lispAddress).getAddress().getAfi());
        }
        serializer.serialize(buffer, ((LispApplicationDataLCAFAddress) lispAddress).getAddress());
    }

    @Override
    public LispApplicationDataLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {

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
