package org.opendaylight.lispflowmapping.southbound.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LispMessageEnum;
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
        if (mapNotify.getAuthenticationData() != null) {
            replyBuffer.put(mapNotify.getAuthenticationData());
        }

        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecords()) {
        	EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
        }

        replyBuffer.clear();
        return replyBuffer;
    }
	
	private interface Length {
        int HEADER_SIZE = 16;
        int RES = 2;
    }
	
}
