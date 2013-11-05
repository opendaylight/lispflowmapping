package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;

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
        size += LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) mapRequest.getSourceEid().getLispAddressContainer().getAddress());
        for (ItrRloc address : mapRequest.getItrRloc()) {
            size += LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) address.getLispAddressContainer().getAddress());
        }
        for (EidRecord record : mapRequest.getEidRecord()) {
            size += 2 + LispAddressSerializer.getInstance().getAddressSize((LispAFIAddress) record.getLispAddressContainer().getAddress());
        }
        ByteBuffer requestBuffer = ByteBuffer.allocate(size);
        requestBuffer
                .put((byte) ((byte) (LispMessageEnum.MapRequest.getValue() << 4)
                        | ByteUtil.boolToBit(mapRequest.isAuthoritative(), Flags.AUTHORITATIVE)
                        | ByteUtil.boolToBit(mapRequest.isMapDataPresent(), Flags.MAP_DATA_PRESENT)
                        | ByteUtil.boolToBit(mapRequest.isProbe(), Flags.PROBE) | ByteUtil.boolToBit(mapRequest.isSmr(), Flags.SMR)));
        requestBuffer.put((byte) (ByteUtil.boolToBit(mapRequest.isPitr(), Flags.PITR) | ByteUtil.boolToBit(mapRequest.isSmrInvoked(),
                Flags.SMR_INVOKED)));
        int IRC = mapRequest.getItrRloc().size();
        if (IRC > 0) {
            IRC--;
        }
        requestBuffer.put((byte) (IRC));
        requestBuffer.put((byte) mapRequest.getEidRecord().size());
        requestBuffer.putLong(mapRequest.getNonce());
        LispAddressSerializer.getInstance().serialize(requestBuffer,
                (LispAFIAddress) mapRequest.getSourceEid().getLispAddressContainer().getAddress());
        for (ItrRloc address : mapRequest.getItrRloc()) {
            LispAddressSerializer.getInstance().serialize(requestBuffer, (LispAFIAddress) address.getLispAddressContainer().getAddress());
        }
        for (EidRecord record : mapRequest.getEidRecord()) {
            requestBuffer.put((byte) 0);
            requestBuffer.put((byte) record.getMask());
            LispAddressSerializer.getInstance().serialize(requestBuffer, (LispAFIAddress) record.getLispAddressContainer().getAddress());
        }
        return requestBuffer;
    }

    public MapRequest deserialize(ByteBuffer requestBuffer) {
        try {
            MapRequestBuilder builder = new MapRequestBuilder();

            byte typeAndFlags = requestBuffer.get();
            builder.setAuthoritative(ByteUtil.extractBit(typeAndFlags, Flags.AUTHORITATIVE));
            builder.setMapDataPresent(ByteUtil.extractBit(typeAndFlags, Flags.MAP_DATA_PRESENT));
            builder.setProbe(ByteUtil.extractBit(typeAndFlags, Flags.PROBE));
            builder.setSmr(ByteUtil.extractBit(typeAndFlags, Flags.SMR));

            byte moreFlags = requestBuffer.get();
            builder.setPitr(ByteUtil.extractBit(moreFlags, Flags.PITR));
            builder.setSmrInvoked(ByteUtil.extractBit(moreFlags, Flags.SMR_INVOKED));

            int itrCount = requestBuffer.get() + 1;
            int recordCount = requestBuffer.get();
            builder.setNonce(requestBuffer.getLong());
            builder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                    new LispAddressContainerBuilder().setAddress((Address) LispAddressSerializer.getInstance().deserialize(requestBuffer)).build())
                    .build());

            if (builder.getItrRloc() == null) {
                builder.setItrRloc(new ArrayList<ItrRloc>());
            }
            for (int i = 0; i < itrCount; i++) {
                builder.getItrRloc().add(
                        new ItrRlocBuilder().setLispAddressContainer(
                                new LispAddressContainerBuilder()
                                        .setAddress((Address) LispAddressSerializer.getInstance().deserialize(requestBuffer)).build()).build());
            }

            if (builder.getEidRecord() == null) {
                builder.setEidRecord(new ArrayList<EidRecord>());
            }
            for (int i = 0; i < recordCount; i++) {
                builder.getEidRecord().add(EidRecordSerializer.getInstance().deserialize(requestBuffer));
            }
            return builder.build();
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
