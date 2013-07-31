package org.opendaylight.lispflowmapping.southbound.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.southbound.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public class EidRecordSerializer {
	
	private static final EidRecordSerializer INSTANCE = new EidRecordSerializer();

	// Private constructor prevents instantiation from other classes
	private EidRecordSerializer() {
	}

	public static EidRecordSerializer getInstance() {
		return INSTANCE;
	}
	
	public EidRecord deserialize(ByteBuffer requestBuffer) {
        /* byte reserved = */requestBuffer.get();
        byte maskLength = requestBuffer.get();
        LispAddress prefix = LispAddressSerializer.valueOf(requestBuffer);
        return new EidRecord(maskLength, prefix);
    }
}
