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
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapreplymessage.MapReplyBuilder;

/**
 * This class deals with serializing map reply from the java object to udp.
 */
public final class MapReplySerializer {

    private static final MapReplySerializer INSTANCE = new MapReplySerializer();

    // Private constructor prevents instantiation from other classes
    private MapReplySerializer() {
    }

    public static MapReplySerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapReply mapReply) {
        int size = Length.HEADER_SIZE;
        for (MappingRecordItem eidToLocatorRecord : mapReply.getMappingRecordItem()) {
            size += MappingRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord.getMappingRecord());
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);

        replyBuffer.put((byte) ((MessageType.MapReply.getIntValue() << 4) |
                (BooleanUtils.isTrue(mapReply.isProbe()) ? Flags.PROBE : 0x00) |
                (BooleanUtils.isTrue(mapReply.isEchoNonceEnabled()) ? Flags.ECHO_NONCE_ENABLED : 0x00)));

        replyBuffer.position(replyBuffer.position() + Length.RES);
        if (mapReply.getMappingRecordItem() != null) {
            replyBuffer.put((byte) mapReply.getMappingRecordItem().size());
        } else {
            replyBuffer.put((byte) 0);

        }
        replyBuffer.putLong(NumberUtil.asLong(mapReply.getNonce()));
        if (mapReply.getMappingRecordItem() != null) {
            for (MappingRecordItem eidToLocatorRecord : mapReply.getMappingRecordItem()) {
                MappingRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord.getMappingRecord());
            }
        }
        return replyBuffer;
    }

    public MapReply deserialize(ByteBuffer replyBuffer) {
        MapReplyBuilder builder = new MapReplyBuilder();
        byte typeAndFlags = replyBuffer.get();
        builder.setProbe(ByteUtil.extractBit(typeAndFlags, Flags.PROBE));
        builder.setEchoNonceEnabled(ByteUtil.extractBit(typeAndFlags, Flags.ECHO_NONCE_ENABLED));
        builder.setSecurityEnabled(ByteUtil.extractBit(typeAndFlags, Flags.SECURITY_ENABLED));
        replyBuffer.getShort();
        int recordCount = ByteUtil.getUnsignedByte(replyBuffer);
        builder.setNonce(replyBuffer.getLong());
        builder.setMappingRecordItem(new ArrayList<MappingRecordItem>());
        for (int i = 0; i < recordCount; i++) {
            builder.getMappingRecordItem().add(new MappingRecordItemBuilder().setMappingRecord(
                    MappingRecordSerializer.getInstance().deserialize(replyBuffer)).build());
        }

        return builder.build();
    }

    private interface Length {
        int RES = 2;
        int HEADER_SIZE = 12;
    }

    private interface Flags {
        int PROBE = 0x08;
        int ECHO_NONCE_ENABLED = 0x04;
        int SECURITY_ENABLED = 0x02;
    }

}
