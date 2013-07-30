package org.opendaylight.lispflowmapping.southbound.serializer;

import java.nio.ByteBuffer;

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
	
	private interface Length {
        int RES = 2;
        int HEADER_SIZE = 12;
    }

    private interface Flags {
        int PROBE = 0x08;
        int ECHO_NONCE_ENABLED = 0x04;
    }
	
}
