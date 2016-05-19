/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.southbound.lisp.cache;

import com.google.common.base.Optional;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.lisp.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.afn.safi.rev130704.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.map.register.cache.key.container.MapRegisterCacheKeyBuilder;

public final class MapRegisterPartialDeserializer {

    /**
     * Consists of:
     * - 4bits Type,
     * - 1bit  P,
     * - 1bit  Reserved,
     * - 1bit  I,
     * - 1bit  R,
     * - 15bit Reserved,
     * - 1bit  M,
     * - 8bits RecordCount
     */
    private static final int PAYLOAD1_LEN = 4;

    /**
     * Consists of:
     * - 64bits  Nonce
     */
    private static final int SKIP1_LEN = 8;

    /**
     * Consists of:
     * - 16bits - Key ID
     */
    private static final int PAYLOAD2_LEN = 2;

    private static final int NUM_OF_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS = PAYLOAD1_LEN + SKIP1_LEN + PAYLOAD2_LEN;
    private static final int NUM_OF_BYTES_AUTHENTICATION_DATA_LENGTH = 2;

    /**
     * Consists of:
     * - 32bits  Record TTL
     * -  8bits  Locator Count
     * -  8bits  EID mask-len
     * -  3bits  ACT
     * -  1bit   A
     * - 12bits  Reserved
     * -  4bits  Reserved
     * - 12bits  Map-Version Number
     */
    private static final int NUM_OF_BYTES_FROM_AUTH_DATA_TO_EID_PREFIX_AFI = 10;
    private static final int NUM_OF_BYTES_EID_PREFIX_AFI = 2;
    private static final int NUM_OF_BYTES_IPV4 = 4;
    private static final int NUM_OF_BYTES_IPV6 = 16;
    private static final int NUM_OF_BYTES_MAC48 = 6;
    private static final byte XTR_SITE_ID = 0x02;
    private static final byte MERGE_ENABLED_BIT_POSITION = 0x04;
    private static final int NUM_OF_BYTES_FROM_EID_PREFIX_AFI_TO_LCAF_LENGTH = 6;
    private static final int NUM_OF_BYTES_FROM_LCAF_RSVD1_TO_LCAF_LENGTH = 6;


    private MapRegisterPartialDeserializer() {
        throw new UnsupportedOperationException("Trying to instantiate util class.");
    }

    public static Map.Entry<MapRegisterCacheKey, byte[]> deserializePartially(final ByteBuffer buffer,
                                                                                      final InetAddress sourceRloc) {

        final byte thirdByte = buffer.get(2);
        //if merge bit is set to 1, not store to cache.
        final boolean isMergeBitSet = ByteUtil.extractBit(thirdByte, MERGE_ENABLED_BIT_POSITION);
        if (isMergeBitSet) {
            return null;
        }

        final int authDataLength = ByteUtil.getUnsignedShort(buffer, NUM_OF_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS);

        MapRegisterCacheKey mapRegisterCacheKey = createKeyFromBytes(buffer, authDataLength);
        if (mapRegisterCacheKey == null) {
            return null;
        }
        final byte[] relevantPayloadData = createValueFromBytes(buffer, authDataLength);

        buffer.position(0);
        return new HashMap.SimpleEntry<>(mapRegisterCacheKey, relevantPayloadData);
    }

    /**
     * Extract from buffer the data which doesn't change.
     *
     * It means that nonce and authentication items are ommited (skipped).
     *
     * payload1 - <Type;RecordCount>
     * skip1    - Nonce
     * payload2 - KeyId
     * skip2    - <Authentication Data Length; Authentication Data>
     * payload3 - <Record TTL; end of message>
     *
     * @param buffer
     * @return
     */
    private static byte[] createValueFromBytes(ByteBuffer buffer, final int authDataLength) {
        buffer.position(0);
        final int bufferLength = buffer.remaining();

        final int skip2Length = authDataLength + NUM_OF_BYTES_AUTHENTICATION_DATA_LENGTH;
        final int totalNewPayloadLength = bufferLength - SKIP1_LEN - skip2Length;
        final byte[] newPayload = new byte[totalNewPayloadLength];

        buffer.get(newPayload, 0, PAYLOAD1_LEN);
        buffer.position(PAYLOAD1_LEN + SKIP1_LEN);
        buffer.get(newPayload, PAYLOAD1_LEN, PAYLOAD2_LEN);
        buffer.position(PAYLOAD1_LEN + SKIP1_LEN + PAYLOAD2_LEN + skip2Length);
        int payload3Len = totalNewPayloadLength - PAYLOAD1_LEN - PAYLOAD2_LEN;
        buffer.get(newPayload, PAYLOAD1_LEN + PAYLOAD2_LEN, payload3Len);

        return newPayload;
    }

    private static MapRegisterCacheKey createKeyFromBytes(final ByteBuffer buffer, int authDataLength) {

        byte typeAndFlags = buffer.get();
        buffer.position(0);
        boolean xtrSiteIdPresent = ByteUtil.extractBit(typeAndFlags, XTR_SITE_ID);
        if (!xtrSiteIdPresent) {
            return null;
        }
        final MapRegisterCacheKeyBuilder mapRegisterCacheKeyBuilder = new MapRegisterCacheKeyBuilder();
        final byte[] eidPrefix = extractEidPrefix(buffer, authDataLength);
        if (eidPrefix == null) {
            return null;
        }
        mapRegisterCacheKeyBuilder.setEidPrefix(eidPrefix);
        if (xtrSiteIdPresent) {
            buffer.position(0);
            final int bufferLength = buffer.remaining();
            buffer.position(bufferLength - MapRegisterSerializer.Length.XTRID_SIZE - MapRegisterSerializer.Length
                    .SITEID_SIZE);

            byte[] xtrId  = new byte[MapRegisterSerializer.Length.XTRID_SIZE];
            buffer.get(xtrId);
            mapRegisterCacheKeyBuilder.setXtrId(xtrId);

            byte[] siteId = new byte[MapRegisterSerializer.Length.SITEID_SIZE];
            buffer.get(siteId);
            mapRegisterCacheKeyBuilder.setSiteId(siteId);
        }
        return mapRegisterCacheKeyBuilder.build();
    }

    private static byte[] extractEidPrefix(final ByteBuffer buffer, int authDataLength) {
        final int startPositionOfEidPrefixAFI = NUM_OF_BYTES_FROM_START_TO_AUTH_DATA_LENGTH_POS
                + NUM_OF_BYTES_AUTHENTICATION_DATA_LENGTH + authDataLength +
                NUM_OF_BYTES_FROM_AUTH_DATA_TO_EID_PREFIX_AFI;
        buffer.position(startPositionOfEidPrefixAFI);
        final int eidPrefixAfi = ByteUtil.getUnsignedShort(buffer, startPositionOfEidPrefixAFI);
        Optional<Integer> eidPrefixLengthOpt = resolveEidPrefixAfi(eidPrefixAfi, buffer);
        if (eidPrefixLengthOpt.isPresent()) {
            final byte[] eidPrefix = new byte[eidPrefixLengthOpt.get()];
            final int startPositionOfEidPrefix = startPositionOfEidPrefixAFI + NUM_OF_BYTES_EID_PREFIX_AFI;
            buffer.position(startPositionOfEidPrefix);
            buffer.get(eidPrefix);
            return eidPrefix;
        }
        return null;
    }

    private static Optional<Integer> resolveEidPrefixAfi(final int eidPrefixAfi, final ByteBuffer buffer) {
        switch (AddressFamily.forValue(eidPrefixAfi)) {
            case IpV4:
                return Optional.of(NUM_OF_BYTES_IPV4);
            case IpV6:
                return Optional.of(NUM_OF_BYTES_IPV6);
            case _48BitMac:
                return Optional.of(NUM_OF_BYTES_MAC48);
            case LispCanonicalAddressFormat:
                buffer.position(buffer.position() + NUM_OF_BYTES_FROM_EID_PREFIX_AFI_TO_LCAF_LENGTH);
                final short lengthRawValue = buffer.getShort();
                final int length = (int)(lengthRawValue & 0xFFFF);
                buffer.position(0);
                return Optional.of(NUM_OF_BYTES_FROM_LCAF_RSVD1_TO_LCAF_LENGTH + length);
            default:
                return Optional.absent();
        }
    }


}
