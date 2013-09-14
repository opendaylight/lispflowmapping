package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

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

    public MapNotify deserialize(ByteBuffer buf) {
        MapNotify notify = new MapNotify();

        buf.position(buf.position() + 3);

        byte recordCount = buf.get();
        notify.setNonce(buf.getLong());
        notify.setKeyId(buf.getShort());
        short authenticationLength = buf.getShort();
        byte[] authenticationData = new byte[authenticationLength];
        buf.get(authenticationData);
        notify.setAuthenticationData(authenticationData);

        for (int i = 0; i < recordCount; i++) {
            notify.addEidToLocator(EidToLocatorRecordSerializer.getInstance().deserialize(buf));
        }
        buf.limit(buf.position());
        byte[] mapRegisterBytes = new byte[buf.position()];
        buf.position(0);
        buf.get(mapRegisterBytes);
        return notify;
    }

    private interface Length {
        int HEADER_SIZE = 16;
        int RES = 2;
    }

}
