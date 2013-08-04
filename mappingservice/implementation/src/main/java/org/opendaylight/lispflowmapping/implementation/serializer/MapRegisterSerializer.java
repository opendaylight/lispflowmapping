package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispSerializationException;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

/**
 * This class deals with deserializing map register from udp to the java object.
 */
public class MapRegisterSerializer {

    private static final MapRegisterSerializer INSTANCE = new MapRegisterSerializer();

    // Private constructor prevents instantiation from other classes
    private MapRegisterSerializer() {
    }

    public static MapRegisterSerializer getInstance() {
        return INSTANCE;
    }

    public ByteBuffer serialize(MapRegister mapRegister) {
        if (mapRegister.getMapRegisterBytes() != null)
            return ByteBuffer.wrap(mapRegister.getMapRegisterBytes());
        else {
            return null;
        }
    }

    public MapRegister deserialize(ByteBuffer registerBuffer) {
        try {
            MapRegister mapRegister = new MapRegister();

            mapRegister.setProxyMapReply(ByteUtil.extractBit(registerBuffer.get(), Flags.PROXY));

            registerBuffer.position(registerBuffer.position() + Length.RES);
            mapRegister.setWantMapNotify(ByteUtil.extractBit(registerBuffer.get(), Flags.WANT_MAP_REPLY));
            byte recordCount = registerBuffer.get();
            mapRegister.setNonce(registerBuffer.getLong());
            mapRegister.setKeyId(registerBuffer.getShort());
            short authenticationLength = registerBuffer.getShort();
            byte[] authenticationData = new byte[authenticationLength];
            registerBuffer.get(authenticationData);
            mapRegister.setAuthenticationData(authenticationData);

            for (int i = 0; i < recordCount; i++) {
                mapRegister.addEidToLocator(EidToLocatorRecordSerializer.getInstance().deserialize(registerBuffer));
            }
            registerBuffer.limit(registerBuffer.position());
            byte[] mapRegisterBytes = new byte[registerBuffer.position()];
            registerBuffer.position(0);
            registerBuffer.get(mapRegisterBytes);
            mapRegister.setMapRegisterBytes(mapRegisterBytes);
            return mapRegister;
        } catch (RuntimeException re) {
            throw new LispSerializationException("Couldn't deserialize Map-Register (len=" + registerBuffer.capacity() + ")", re);
        }

    }

    private interface Flags {
        byte PROXY = 0x08;
        byte WANT_MAP_REPLY = 0x01;
    }

    private interface Length {
        int RES = 1;
    }
}
