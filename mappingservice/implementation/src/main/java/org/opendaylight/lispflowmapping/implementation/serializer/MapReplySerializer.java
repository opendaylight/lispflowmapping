package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapreplymessage.MapReplyBuilder;

/**
 * This class deals with serializing map reply from the java object to udp.
 */
public class MapReplySerializer {

    private static final MapReplySerializer INSTANCE = new MapReplySerializer();

    // Private constructor prevents instantiation from other classes
    private MapReplySerializer() {
    }

    public static MapReplySerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapReply mapReply) {
        int size = Length.HEADER_SIZE;
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecord()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);

        replyBuffer.put((byte) ((LispMessageEnum.MapReply.getValue() << 4) | //
                (mapReply.isProbe() ? Flags.PROBE : 0x00) | //
                (mapReply.isEchoNonceEnabled() ? Flags.ECHO_NONCE_ENABLED : 0x00)));

        replyBuffer.position(replyBuffer.position() + Length.RES);
        replyBuffer.put((byte) mapReply.getEidToLocatorRecord().size());
        replyBuffer.putLong(mapReply.getNonce());
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecord()) {
            EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
        }
        return replyBuffer;
    }

    public MapReply deserialize(ByteBuffer replyBuffer) {
        MapReplyBuilder builder = new MapReplyBuilder();
        byte typeAndFlags = replyBuffer.get();
        builder.setProbe(ByteUtil.extractBit(typeAndFlags, Flags.PROBE));
        builder.setEchoNonceEnabled(ByteUtil.extractBit(typeAndFlags, Flags.ECHO_NONCE_ENABLED));
        builder.setSecurityEnabled(ByteUtil.extractBit(typeAndFlags, Flags.SECURITY_ENABLED));
        replyBuffer.getShort();
        int recordCount = replyBuffer.get();
        builder.setNonce(replyBuffer.getLong());
        for (int i = 0; i < recordCount; i++) {
            builder.getEidToLocatorRecord().add(EidToLocatorRecordSerializer.getInstance().deserialize(replyBuffer));
        }

        return builder.build();
    }

    private interface Length {
        int RES = 2;
        int HEADER_SIZE = 12;
    }

    private interface Flags {
        int PROBE = 0x08;
        int ECHO_NONCE_ENABLED = 0x04;
        int SECURITY_ENABLED = 0x02;
    }

}
