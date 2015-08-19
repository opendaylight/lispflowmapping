/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.LispMessageEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class deals with deserializing map request from udp to the java object.
 */
public class MapRequestSerializer {

    private static final MapRequestSerializer INSTANCE = new MapRequestSerializer();
    protected static final Logger LOG = LoggerFactory.getLogger(MapRequestSerializer.class);

    // Private constructor prevents instantiation from other classes
    private MapRequestSerializer() {
    }

    public static MapRequestSerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapRequest mapRequest) {
        int size = Length.HEADER_SIZE;
        if (mapRequest.getSourceEid() != null && mapRequest.getSourceEid().getLispAddressContainer() != null) {
            size += LispAddressSerializer.getInstance().getAddressSize(
                    LispAFIConvertor.toAFI(mapRequest.getSourceEid().getLispAddressContainer()));
        } else {
            size += 2;
        }
        if (mapRequest.getItrRloc() != null) {
            for (ItrRloc address : mapRequest.getItrRloc()) {
                size += LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFI(address.getLispAddressContainer()));
            }
        }
        if (mapRequest.getEidRecord() != null) {
            for (EidRecord record : mapRequest.getEidRecord()) {
                size += 2 + LispAddressSerializer.getInstance().getAddressSize(LispAFIConvertor.toAFI(record.getLispAddressContainer()));
            }
        }
        ByteBuffer requestBuffer = ByteBuffer.allocate(size);
        requestBuffer.put((byte) ((byte) (LispMessageEnum.MapRequest.getValue() << 4)
                | ByteUtil.boolToBit(BooleanUtils.isTrue(mapRequest.isAuthoritative()), Flags.AUTHORITATIVE)
                | ByteUtil.boolToBit(BooleanUtils.isTrue(mapRequest.isMapDataPresent()), Flags.MAP_DATA_PRESENT)
                | ByteUtil.boolToBit(BooleanUtils.isTrue(mapRequest.isProbe()), Flags.PROBE) | ByteUtil.boolToBit(
                BooleanUtils.isTrue(mapRequest.isSmr()), Flags.SMR)));
        requestBuffer.put((byte) (ByteUtil.boolToBit(BooleanUtils.isTrue(mapRequest.isPitr()), Flags.PITR) | ByteUtil.boolToBit(
                BooleanUtils.isTrue(mapRequest.isSmrInvoked()), Flags.SMR_INVOKED)));
        if (mapRequest.getItrRloc() != null) {
            int IRC = mapRequest.getItrRloc().size();
            if (IRC > 0) {
                IRC--;
            }
            requestBuffer.put((byte) (IRC));
        } else {
            requestBuffer.put((byte) 0);

        }
        if (mapRequest.getEidRecord() != null) {
            requestBuffer.put((byte) mapRequest.getEidRecord().size());
        } else {
            requestBuffer.put((byte) 0);

        }
        requestBuffer.putLong(NumberUtil.asLong(mapRequest.getNonce()));
        if (mapRequest.getSourceEid() != null && mapRequest.getSourceEid().getLispAddressContainer() != null) {
            LispAddressSerializer.getInstance().serialize(requestBuffer,
                    LispAFIConvertor.toAFI(mapRequest.getSourceEid().getLispAddressContainer()));
        } else {
            requestBuffer.putShort((short) 0);
        }
        if (mapRequest.getItrRloc() != null) {
            for (ItrRloc address : mapRequest.getItrRloc()) {
                LispAddressSerializer.getInstance().serialize(requestBuffer, LispAFIConvertor.toAFI(address.getLispAddressContainer()));
            }
        }
        if (mapRequest.getEidRecord() != null) {
            for (EidRecord record : mapRequest.getEidRecord()) {
                requestBuffer.put((byte) 0);
                requestBuffer.put((byte) record.getMask().byteValue());
                LispAddressSerializer.getInstance().serialize(requestBuffer, LispAFIConvertor.toAFI(record.getLispAddressContainer()));
            }
        }
        if (mapRequest.getMapReply() != null) {
            ByteBuffer replyBuffer = ByteBuffer.allocate(EidToLocatorRecordSerializer.getInstance().getSerializationSize(mapRequest.getMapReply()));
            EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, mapRequest.getMapReply());
            ByteBuffer combinedBuffer = ByteBuffer.allocate(requestBuffer.capacity() + replyBuffer.capacity());
            combinedBuffer.put(requestBuffer.array());
            combinedBuffer.put(replyBuffer.array());
            return combinedBuffer;
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

            int itrCount = ByteUtil.getUnsignedByte(requestBuffer) + 1;
            int recordCount = ByteUtil.getUnsignedByte(requestBuffer);
            builder.setNonce(requestBuffer.getLong());
            builder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                    LispAFIConvertor.toContainer(LispAddressSerializer.getInstance().deserialize(requestBuffer)))
                    .build());

            if (builder.getItrRloc() == null) {
                builder.setItrRloc(new ArrayList<ItrRloc>());
            }
            for (int i = 0; i < itrCount; i++) {
                builder.getItrRloc().add(
                        new ItrRlocBuilder().setLispAddressContainer(
                                LispAFIConvertor.toContainer(LispAddressSerializer.getInstance().deserialize(requestBuffer)))
                                .build());
            }

            if (builder.getEidRecord() == null) {
                builder.setEidRecord(new ArrayList<EidRecord>());
            }
            for (int i = 0; i < recordCount; i++) {
                builder.getEidRecord().add(EidRecordSerializer.getInstance().deserialize(requestBuffer));
            }
            if (builder.isMapDataPresent() && requestBuffer.hasRemaining()) {
                try {
                    builder.setMapReply(new org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.MapReplyBuilder(
                            new EidToLocatorRecordBuilder(EidToLocatorRecordSerializer.getInstance().deserialize(requestBuffer)).build()).build());
                } catch (RuntimeException re) {
                    LOG.warn("couldn't deserialize map reply encapsulated in map request. {}", re.getMessage());
                }
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
