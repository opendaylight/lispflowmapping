package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;

/**
 * This class deals with serializing map notify from the java object to udp.
 */
public class MapNotifySerializer {

    private static final MapNotifySerializer INSTANCE = new MapNotifySerializer();

    // Private constructor prevents instantiation from other classes
    private MapNotifySerializer() {
    }

    public static MapNotifySerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapNotify mapNotify) {
        int size = Length.HEADER_SIZE + mapNotify.getAuthenticationData().length;
        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecord()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);
        replyBuffer.put((byte) (LispMessageEnum.MapNotify.getValue() << 4));
        replyBuffer.position(replyBuffer.position() + Length.RES);
        replyBuffer.put((byte) mapNotify.getEidToLocatorRecord().size());
        replyBuffer.putLong(mapNotify.getNounce());
        replyBuffer.putShort(mapNotify.getKeyId());
        replyBuffer.putShort((short) mapNotify.getAuthenticationData().length);
        replyBuffer.put(mapNotify.getAuthenticationData());

        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecord()) {
            EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
        }
        replyBuffer.clear();
        return replyBuffer;
    }

    public MapNotify deserialize(ByteBuffer notifyBuffer) {
        try {
            MapNotifyBuilder builder = new MapNotifyBuilder();
            builder.setProxyMapReply(ByteUtil.extractBit(notifyBuffer.get(), Flags.PROXY));

            notifyBuffer.position(notifyBuffer.position() + Length.RES);

            byte recordCount = notifyBuffer.get();
            builder.setNounce(notifyBuffer.getLong());
            builder.setKeyId(notifyBuffer.getShort());
            short authenticationLength = notifyBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            notifyBuffer.get(authenticationData);
            builder.setAuthenticationData(authenticationData);

            for (int i = 0; i < recordCount; i++) {
                builder.getEidToLocatorRecord().add(EidToLocatorRecordSerializer.getInstance().deserialize(notifyBuffer));
            }
            notifyBuffer.limit(notifyBuffer.position());
            return builder.build();
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Notify (len=" + notifyBuffer.capacity() + ")", re);
        }
    }

    private interface Flags {
        byte PROXY = 0x08;
    }

    private interface Length {
        int HEADER_SIZE = 16;
        int RES = 2;
    }

}
