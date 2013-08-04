package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.tomcat.util.buf.HexUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.ILispAuthentication;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationFactory;
import org.opendaylight.lispflowmapping.implementation.authentication.LispKeyIDEnum;
import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispAuthenticationException;
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

	public MapRegister deserialize(ByteBuffer registerBuffer) {
        try {
        	MapRegister mapRegister = new MapRegister();

            mapRegister.setProxyMapReply(ByteUtil.extractBit(registerBuffer.get(), Flags.PROXY));

            registerBuffer.position(registerBuffer.position() + Length.RES);
            mapRegister.setWantMapNotify(ByteUtil.extractBit(registerBuffer.get(), Flags.WANT_MAP_REPLY));
            byte recordCount = registerBuffer.get();
            mapRegister.setNonce(registerBuffer.getLong());
            mapRegister.setKeyId(registerBuffer.getShort());
            ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapRegister.getKeyId()));
            short authenticationLength = registerBuffer.getShort();
            if (authenticationLength != authentication.getAuthenticationLength()) {
            	throw new LispAuthenticationException(String.format("Authentication length isn't correct. Expected:%d, got %d",authentication.getAuthenticationLength(), authenticationLength));
            }
            int authenticationPosition = registerBuffer.position();
            byte[] authenticationData = new byte[authenticationLength];
            registerBuffer.get(authenticationData);
            mapRegister.setAuthenticationData(authenticationData);
            byte[] tempAuthenticationData = new byte[authenticationLength];
            Arrays.fill(tempAuthenticationData,(byte)0x00);
            registerBuffer.position(authenticationPosition);
            registerBuffer.put(tempAuthenticationData);

            for (int i = 0; i < recordCount; i++) {
                mapRegister.addEidToLocator(EidToLocatorRecordSerializer.getInstance().deserialize(registerBuffer));
            }
            registerBuffer.limit(registerBuffer.position());
            byte[] mapRegisterBytes = new byte[registerBuffer.position()];
            registerBuffer.position(0);
            registerBuffer.get(mapRegisterBytes);
            if (!Arrays.equals(mapRegister.getAuthenticationData(),authentication.getAuthenticationData(mapRegisterBytes))) {
            	throw new LispAuthenticationException(String.format("Authentication isn't correct. Expected %s got %s",HexUtils.toHexString(authentication.getAuthenticationData(registerBuffer.array())),HexUtils.toHexString(mapRegister.getAuthenticationData())));
            }
            return mapRegister;
        } catch (LispAuthenticationException lue) {
            throw lue;
        }  
        catch (RuntimeException re) {
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
