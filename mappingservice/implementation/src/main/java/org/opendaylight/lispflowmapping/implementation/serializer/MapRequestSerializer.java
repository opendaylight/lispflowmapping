package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;


/**
 * This class deals with deserializing map request from udp to the java object.
 */
public class MapRequestSerializer {
	
	private static final MapRequestSerializer INSTANCE = new MapRequestSerializer();

	// Private constructor prevents instantiation from other classes
	private MapRequestSerializer() {
	}

	public static MapRequestSerializer getInstance() {
		return INSTANCE;
	}

	public MapRequest deserialize(ByteBuffer requestBuffer) {
        try {
            MapRequest mapRequest = new MapRequest();

            byte typeAndFlags = requestBuffer.get();
            mapRequest.setAuthoritative(ByteUtil.extractBit(typeAndFlags, Flags.AUTHORITATIVE));
            mapRequest.setMapDataPresent(ByteUtil.extractBit(typeAndFlags, Flags.MAP_DATA_PRESENT));
            mapRequest.setProbe(ByteUtil.extractBit(typeAndFlags, Flags.PROBE));
            mapRequest.setSmr(ByteUtil.extractBit(typeAndFlags, Flags.SMR));

            byte moreFlags = requestBuffer.get();
            mapRequest.setPitr(ByteUtil.extractBit(moreFlags, Flags.PITR));
            mapRequest.setSmrInvoked(ByteUtil.extractBit(moreFlags, Flags.SMR_INVOKED));

            int itrCount = requestBuffer.get() + 1;
            int recordCount = requestBuffer.get();
            mapRequest.setNonce(requestBuffer.getLong());
            mapRequest.setSourceEid(LispAddressSerializer.getInstance().deserialize(requestBuffer));

            for (int i = 0; i < itrCount; i++) {
                mapRequest.addItrRloc(LispAddressSerializer.getInstance().deserialize(requestBuffer));
            }

            for (int i = 0; i < recordCount; i++) {
                mapRequest.addEidRecord(EidRecordSerializer.getInstance().deserialize(requestBuffer));
            }
            return mapRequest;
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Request (len=" + requestBuffer.capacity() + ")", re);
        }
    }
	
	public interface Flags {
        byte AUTHORITATIVE = 0x08;
        byte MAP_DATA_PRESENT = 0x04;
        byte PROBE = 0x02;
        byte SMR = 0x01;

        byte PITR = (byte) 0x80;
        byte SMR_INVOKED = 0x40;
    }
	
	
}
