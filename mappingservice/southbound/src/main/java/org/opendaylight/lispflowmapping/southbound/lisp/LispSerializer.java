/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LispMessageEnum;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.util.ByteUtil;

/**
 * This class deals with serializing and deserializing all of the lisp messages from udp to java objects
 */
public class LispSerializer {
	
	
	public static MapRegister deserializeMapRegister(ByteBuffer registerBuffer) {
        try {
            MapRegister mapRegister = new MapRegister();

            mapRegister.setProxyMapReply(ByteUtil.extractBit(registerBuffer.get(), MapRegisterFlags.PROXY));

            registerBuffer.position(registerBuffer.position() + MapRegisterLength.RES);
            mapRegister.setWantMapNotify(ByteUtil.extractBit(registerBuffer.get(), MapRegisterFlags.WANT_MAP_REPLY));
            byte recordCount = registerBuffer.get();
            mapRegister.setNonce(registerBuffer.getLong());
            mapRegister.setKeyId(registerBuffer.getShort());

            short authenticationLength = registerBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            registerBuffer.get(authenticationData);
            mapRegister.setAuthenticationData(authenticationData);

            for (int i = 0; i < recordCount; i++) {
                mapRegister.addEidToLocator(EidToLocatorRecord.deserialize(registerBuffer));
            }
            return mapRegister;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Register (len=" + registerBuffer.capacity() + ")", re);
        }

    }
	
	public static MapRequest deserializeMapRequest(ByteBuffer requestBuffer) {
        try {
            MapRequest mapRequest = new MapRequest();

            byte typeAndFlags = requestBuffer.get();
            mapRequest.setAuthoritative(ByteUtil.extractBit(typeAndFlags, MapRequestFlags.AUTHORITATIVE));
            mapRequest.setMapDataPresent(ByteUtil.extractBit(typeAndFlags, MapRequestFlags.MAP_DATA_PRESENT));
            mapRequest.setProbe(ByteUtil.extractBit(typeAndFlags, MapRequestFlags.PROBE));
            mapRequest.setSmr(ByteUtil.extractBit(typeAndFlags, MapRequestFlags.SMR));

            byte moreFlags = requestBuffer.get();
            mapRequest.setPitr(ByteUtil.extractBit(moreFlags, MapRequestFlags.PITR));
            mapRequest.setSmrInvoked(ByteUtil.extractBit(moreFlags, MapRequestFlags.SMR_INVOKED));

            int itrCount = requestBuffer.get() + 1;
            int recordCount = requestBuffer.get();
            mapRequest.setNonce(requestBuffer.getLong());
            mapRequest.setSourceEid(LispAddress.valueOf(requestBuffer));

            for (int i = 0; i < itrCount; i++) {
                mapRequest.addItrRloc(LispAddress.valueOf(requestBuffer));
            }

            for (int i = 0; i < recordCount; i++) {
                mapRequest.addEidRecord(EidRecord.deserialize(requestBuffer));
            }
            return mapRequest;
        } catch (RuntimeException re) {
            throw new LispMalformedPacketException("Couldn't deserialize Map-Request (len=" + requestBuffer.capacity() + ")", re);
        }
    }
	
	public static ByteBuffer serializeMapNotify(MapNotify mapNotify) {
        int size = MapNotifyLength.HEADER_SIZE + mapNotify.getAuthenticationData().length;
        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecords()) {
            size += eidToLocatorRecord.getSerializationSize();
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);
        replyBuffer.put((byte) (LispMessageEnum.MapNotify.getValue() << 4));
        replyBuffer.position(replyBuffer.position() + MapNotifyLength.RES);
        replyBuffer.put((byte) mapNotify.getEidToLocatorRecords().size());
        replyBuffer.putLong(mapNotify.getNonce());
        replyBuffer.putShort(mapNotify.getKeyId());
        replyBuffer.putShort((short) mapNotify.getAuthenticationData().length);
        if (mapNotify.getAuthenticationData() != null) {
            replyBuffer.put(mapNotify.getAuthenticationData());
        }

        for (EidToLocatorRecord eidToLocatorRecord : mapNotify.getEidToLocatorRecords()) {
            eidToLocatorRecord.serialize(replyBuffer);
        }

        replyBuffer.clear();
        return replyBuffer;
    }
	
	public static ByteBuffer serializeMapReply(MapReply mapReply) {
        int size = MapReplyLength.HEADER_SIZE;
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecords()) {
            size += eidToLocatorRecord.getSerializationSize();
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(size);

        replyBuffer.put((byte) ((LispMessageEnum.MapReply.getValue() << 4) | //
                (mapReply.isProbe() ? MapReplyFlags.PROBE : 0x00) | //
                (mapReply.isEchoNonceEnabled() ? MapReplyFlags.ECHO_NONCE_ENABLED : 0x00)));

        replyBuffer.position(replyBuffer.position() + MapReplyLength.RES);
        replyBuffer.put((byte) mapReply.getEidToLocatorRecords().size());
        replyBuffer.putLong(mapReply.getNonce());
        for (EidToLocatorRecord eidToLocatorRecord : mapReply.getEidToLocatorRecords()) {
            eidToLocatorRecord.serialize(replyBuffer);
        }
        return replyBuffer;
    }

    
	
	private interface MapRegisterFlags {
        byte PROXY = 0x08;
        byte WANT_MAP_REPLY = 0x01;
    }

	private interface MapRegisterLength {
        int RES = 1;
    }
	
	public interface MapRequestFlags {
        byte AUTHORITATIVE = 0x08;
        byte MAP_DATA_PRESENT = 0x04;
        byte PROBE = 0x02;
        byte SMR = 0x01;

        byte PITR = (byte) 0x80;
        byte SMR_INVOKED = 0x40;
    }
	
	private interface MapNotifyLength {
        int HEADER_SIZE = 16;
        int RES = 2;
    }
	


    private interface MapReplyLength {
        int RES = 2;
        int HEADER_SIZE = 12;
    }

    private interface MapReplyFlags {
        int PROBE = 0x08;
        int ECHO_NONCE_ENABLED = 0x04;
    }

}
