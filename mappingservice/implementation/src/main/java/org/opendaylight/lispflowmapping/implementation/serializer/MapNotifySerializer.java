package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;

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
        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecords()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);
        replyBuffer.put((byte) (LispMessageEnum.MapNotify.getValue() << 4));
        replyBuffer.position(replyBuffer.position() + Length.RES);
        replyBuffer.put((byte) mapNotify.getEidToLocatorRecords().size());
        replyBuffer.putLong(mapNotify.getNonce());
        replyBuffer.putShort(mapNotify.getKeyId());
        replyBuffer.putShort((short) mapNotify.getAuthenticationData().length);
        replyBuffer.put(mapNotify.getAuthenticationData());

        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecords()) {
            EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
        }
        replyBuffer.clear();
        return replyBuffer;
    }

    public MapNotify deserialize(ByteBuffer notifyBuffer) {
        try {
            MapNotify mapNotify = new MapNotify();
            mapNotify.setProxyMapReply(ByteUtil.extractBit(notifyBuffer.get(), Flags.PROXY));

            notifyBuffer.position(notifyBuffer.position() + Length.RES);

            byte recordCount = notifyBuffer.get();
            mapNotify.setNonce(notifyBuffer.getLong());
            mapNotify.setKeyId(notifyBuffer.getShort());
            short authenticationLength = notifyBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            notifyBuffer.get(authenticationData);
            mapNotify.setAuthenticationData(authenticationData);

            for (int i = 0; i < recordCount; i++) {
                mapNotify.addEidToLocator(EidToLocatorRecordSerializer.getInstance().deserialize(notifyBuffer));
            }
            notifyBuffer.limit(notifyBuffer.position());
            return mapNotify;
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
