package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

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

    public ByteBuffer serialize(MapRequest mapRequest) {
        int size = Length.HEADER_SIZE;
        size += LispAddressSerializer.getInstance().getAddressSize(mapRequest.getSourceEid());
        for (LispAddress address : mapRequest.getItrRlocs()) {
            size += LispAddressSerializer.getInstance().getAddressSize(address);
        }
        for (EidRecord record : mapRequest.getEids()) {
            size += 2 + LispAddressSerializer.getInstance().getAddressSize(record.getPrefix());
        }
        ByteBuffer requestBuffer = ByteBuffer.allocate(size);
        requestBuffer
                .put((byte) ((byte) (LispMessageEnum.MapRequest.getValue() << 4)
                        | ByteUtil.boolToBit(mapRequest.isAuthoritative(), Flags.AUTHORITATIVE)
                        | ByteUtil.boolToBit(mapRequest.isMapDataPresent(), Flags.MAP_DATA_PRESENT)
                        | ByteUtil.boolToBit(mapRequest.isProbe(), Flags.PROBE) | ByteUtil.boolToBit(mapRequest.isSmr(), Flags.SMR)));
        requestBuffer.put((byte) (ByteUtil.boolToBit(mapRequest.isPitr(), Flags.PITR) | ByteUtil.boolToBit(mapRequest.isSmrInvoked(),
                Flags.SMR_INVOKED)));
        int IRC = mapRequest.getItrRlocs().size();
        if (IRC > 0) {
            IRC--;
        }
        requestBuffer.put((byte) (IRC));
        requestBuffer.put((byte) mapRequest.getEids().size());
        requestBuffer.putLong(mapRequest.getNonce());
        LispAddressSerializer.getInstance().serialize(requestBuffer, mapRequest.getSourceEid());
        for (LispAddress address : mapRequest.getItrRlocs()) {
            LispAddressSerializer.getInstance().serialize(requestBuffer, address);
        }
        for (EidRecord record : mapRequest.getEids()) {
            requestBuffer.put((byte) 0);
            requestBuffer.put((byte) record.getMaskLength());
            LispAddressSerializer.getInstance().serialize(requestBuffer, record.getPrefix());
        }
        return requestBuffer;
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

    private interface Length {
        int HEADER_SIZE = 12;
    }

}
