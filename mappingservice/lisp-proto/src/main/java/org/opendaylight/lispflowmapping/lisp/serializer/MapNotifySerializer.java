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

import org.opendaylight.lispflowmapping.lisp.serializer.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.lisp.type.LispMessageEnum;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.lispflowmapping.lisp.util.NumberUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifymessage.MapNotifyBuilder;

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
        int size = Length.HEADER_SIZE;
        if (mapNotify.getAuthenticationData() != null) {
            size += mapNotify.getAuthenticationData().length;
        }
        if (mapNotify.isXtrSiteIdPresent() != null && mapNotify.isXtrSiteIdPresent()) {
            size += org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.XTRID_SIZE +
                    org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.SITEID_SIZE;
        }
        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecord()) {
            size += EidToLocatorRecordSerializer.getInstance().getSerializationSize(eidToLocatorRecord);
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);
        replyBuffer.put((byte) (LispMessageEnum.MapNotify.getValue() << 4));
        replyBuffer.position(replyBuffer.position() + Length.RES);
        if (mapNotify.getEidToLocatorRecord() != null) {
            replyBuffer.put((byte) mapNotify.getEidToLocatorRecord().size());
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

        if (mapNotify.getEidToLocatorRecord() != null) {
            for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecord()) {
                EidToLocatorRecordSerializer.getInstance().serialize(replyBuffer, eidToLocatorRecord);
            }
        }

        if (mapNotify.isXtrSiteIdPresent() != null && mapNotify.isXtrSiteIdPresent()) {
            replyBuffer.put(mapNotify.getXtrId());
            replyBuffer.put(mapNotify.getSiteId());
        }
        replyBuffer.clear();
        return replyBuffer;
    }

    public MapNotify deserialize(ByteBuffer notifyBuffer) {
        try {
            MapNotifyBuilder builder = new MapNotifyBuilder();

            byte typeAndFlags = notifyBuffer.get();
            boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, Flags.XTRSITEID);
            builder.setXtrSiteIdPresent(xtrSiteIdPresent);

            notifyBuffer.position(notifyBuffer.position() + Length.RES);

            byte recordCount = (byte) ByteUtil.getUnsignedByte(notifyBuffer);
            builder.setNonce(notifyBuffer.getLong());
            builder.setKeyId(notifyBuffer.getShort());
            short authenticationLength = notifyBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            notifyBuffer.get(authenticationData);
            builder.setAuthenticationData(authenticationData);

            builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
            for (int i = 0; i < recordCount; i++) {
                builder.getEidToLocatorRecord().add(
                        new EidToLocatorRecordBuilder(EidToLocatorRecordSerializer.getInstance().deserialize(notifyBuffer)).build());
            }

            if (xtrSiteIdPresent) {
                byte[] xtrId  = new byte[org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.XTRID_SIZE];
                notifyBuffer.get(xtrId);
                byte[] siteId = new byte[org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer.Length.SITEID_SIZE];
                notifyBuffer.get(siteId);
                builder.setXtrId(xtrId);
                builder.setSiteId(siteId);
            }
            notifyBuffer.limit(notifyBuffer.position());
            return builder.build();
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Notify (len=" + notifyBuffer.capacity() + ")", re);
        }
    }

    private interface Flags {
        byte XTRSITEID = 0x08;
    }

    private interface Length {
        int HEADER_SIZE = 16;
        int RES = 2;
    }

}
