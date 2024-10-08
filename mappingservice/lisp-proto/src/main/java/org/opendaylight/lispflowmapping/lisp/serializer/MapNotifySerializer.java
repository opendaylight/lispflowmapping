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
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MessageType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.list.MappingRecordItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.list.MappingRecordItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping._record.list.MappingRecordItemKey;

/**
 * This class deals with serializing map notify from the java object to udp.
 */
public final class MapNotifySerializer {

    private static final MapNotifySerializer INSTANCE = new MapNotifySerializer();

    // Private constructor prevents instantiation from other classes
    private MapNotifySerializer() {
    }

    public static MapNotifySerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapNotify mapNotify) {
        int size = Length.HEADER_SIZE;
        if (mapNotify.getAuthenticationData() != null) {
            size += mapNotify.getAuthenticationData().length;
        }
        if (mapNotify.getXtrSiteIdPresent() != null && mapNotify.getXtrSiteIdPresent()) {
            size += org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.XTRID_SIZE
                  + org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.SITEID_SIZE;
        }
        for (MappingRecordItem mappingRecord : mapNotify.getMappingRecordItem()) {
            size += MappingRecordSerializer.getInstance().getSerializationSize(mappingRecord.getMappingRecord());
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);
        replyBuffer.put((byte) ((byte) (MessageType.MapNotify.getIntValue() << 4)
                | ByteUtil.boolToBit(BooleanUtils.isTrue(mapNotify.getXtrSiteIdPresent()), Flags.XTRSITEID)));
        replyBuffer.position(replyBuffer.position() + Length.RES);
        replyBuffer.put(ByteUtil.boolToBit(BooleanUtils.isTrue(mapNotify.getMergeEnabled()), Flags.MERGE_ENABLED));
        if (mapNotify.getMappingRecordItem() != null) {
            replyBuffer.put((byte) mapNotify.getMappingRecordItem().size());
        } else {
            replyBuffer.put((byte) 0);
        }
        replyBuffer.putLong(NumberUtil.asLong(mapNotify.getNonce()));
        replyBuffer.putShort(NumberUtil.asShort(mapNotify.getKeyId()));
        if (mapNotify.getAuthenticationData() != null) {
            replyBuffer.putShort((short) mapNotify.getAuthenticationData().length);
            replyBuffer.put(mapNotify.getAuthenticationData());
        } else {
            replyBuffer.putShort((short) 0);
        }

        if (mapNotify.getMappingRecordItem() != null) {
            for (MappingRecordItem mappingRecord : mapNotify.getMappingRecordItem()) {
                MappingRecordSerializer.getInstance().serialize(replyBuffer, mappingRecord.getMappingRecord());
            }
        }

        if (mapNotify.getXtrSiteIdPresent() != null && mapNotify.getXtrSiteIdPresent()) {
            replyBuffer.put(mapNotify.getXtrId().getValue());
            replyBuffer.put(mapNotify.getSiteId().getValue());
        }
        replyBuffer.clear();
        return replyBuffer;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public MapNotify deserialize(ByteBuffer notifyBuffer) {
        try {
            final byte typeAndFlags = notifyBuffer.get();
            final int type = typeAndFlags >> 4;
            if (MessageType.forValue(type) != MessageType.MapNotify) {
                throw new LispSerializationException("Expected Map-Notify packet (type 4), but was type " + type);
            }

            MapNotifyBuilder builder = new MapNotifyBuilder();
            builder.setMappingRecordItem(new ArrayList<MappingRecordItem>());

            boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, Flags.XTRSITEID);
            builder.setXtrSiteIdPresent(xtrSiteIdPresent);

            notifyBuffer.position(notifyBuffer.position() + Length.RES);
            builder.setMergeEnabled(ByteUtil.extractBit(notifyBuffer.get(), Flags.MERGE_ENABLED));

            final byte recordCount = (byte) ByteUtil.getUnsignedByte(notifyBuffer);
            builder.setNonce(notifyBuffer.getLong());
            builder.setKeyId(notifyBuffer.getShort());
            short authenticationLength = notifyBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            notifyBuffer.get(authenticationData);
            builder.setAuthenticationData(authenticationData);

            if (xtrSiteIdPresent) {
                List<MappingRecordBuilder> mrbs = new ArrayList<>();
                for (int i = 0; i < recordCount; i++) {
                    mrbs.add(MappingRecordSerializer.getInstance().deserializeToBuilder(notifyBuffer));
                }
                byte[] xtrIdBuf  = new byte[MapRegisterSerializer.Length.XTRID_SIZE];
                notifyBuffer.get(xtrIdBuf);
                XtrId xtrId = new XtrId(xtrIdBuf);
                byte[] siteIdBuf = new byte[MapRegisterSerializer.Length.SITEID_SIZE];
                notifyBuffer.get(siteIdBuf);
                SiteId siteId = new SiteId(siteIdBuf);
                builder.setXtrId(xtrId);
                builder.setSiteId(siteId);
                int idx = 0;
                for (MappingRecordBuilder mrb : mrbs) {
                    mrb.setXtrId(xtrId);
                    mrb.setSiteId(siteId);
                    builder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                            .withKey(new MappingRecordItemKey(Integer.toString(idx)))
                            .setMappingRecord(mrb.build()).build());
                    idx++;
                }
            } else {
                for (int i = 0; i < recordCount; i++) {
                    builder.getMappingRecordItem().add(new MappingRecordItemBuilder()
                            .withKey(new MappingRecordItemKey(Integer.toString(i)))
                            .setMappingRecord(MappingRecordSerializer.getInstance().deserialize(notifyBuffer))
                            .build());
                }
            }

            notifyBuffer.limit(notifyBuffer.position());
            return builder.build();
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Notify (len="
                    + notifyBuffer.capacity() + ")", re);
        }
    }

    private interface Flags {
        byte XTRSITEID = 0x08;
        byte MERGE_ENABLED = 0x04;
    }

    private interface Length {
        int HEADER_SIZE = 16;
        int RES = 1;
    }

}
