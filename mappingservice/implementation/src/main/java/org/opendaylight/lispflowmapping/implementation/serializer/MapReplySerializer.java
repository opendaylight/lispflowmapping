package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;

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
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecords()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);

        replyBuffer.put((byte) ((LispMessageEnum.MapReply.getValue() << 4) | //
                (mapReply.isProbe() ? Flags.PROBE : 0x00) | //
                (mapReply.isEchoNonceEnabled() ? Flags.ECHO_NONCE_ENABLED : 0x00)));

        replyBuffer.position(replyBuffer.position() + Length.RES);
        replyBuffer.put((byte) mapReply.getEidToLocatorRecords().size());
        replyBuffer.putLong(mapReply.getNonce());
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecords()) {
            EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
        }
        return replyBuffer;
    }

    public MapReply deserialize(ByteBuffer replyBuffer) {
        MapReply mapReply = new MapReply();
        byte typeAndFlags = replyBuffer.get();
        mapReply.setProbe(ByteUtil.extractBit(typeAndFlags, Flags.PROBE));
        mapReply.setEchoNonceEnabled(ByteUtil.extractBit(typeAndFlags, Flags.ECHO_NONCE_ENABLED));
        mapReply.setSecurityEnabled(ByteUtil.extractBit(typeAndFlags, Flags.SECURITY_ENABLED));
        replyBuffer.getShort();
        int recordCount = replyBuffer.get();
        mapReply.setNonce(replyBuffer.getLong());
        for (int i = 0; i < recordCount; i++) {
            mapReply.addEidToLocator(EidToLocatorRecordSerializer.getInstance().deserialize(replyBuffer));
        }

        return mapReply;
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
