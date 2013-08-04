package org.opendaylight.lispflowmapping.implementation.authentication;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

public class LispMACAuthentication extends LispAuthentication {

    private static String KEY = "password";
    protected String algorithem;
    protected Mac mac;
    private byte[] tempAuthenticationData;
    
    public LispMACAuthentication(String algorithem) {
        this.algorithem = algorithem;
        try {
            byte[] keyBytes = KEY.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithem);

            mac = Mac.getInstance(algorithem);
            mac.init(signingKey);
            tempAuthenticationData = new byte[mac.getMacLength()];
            Arrays.fill(tempAuthenticationData, (byte) 0);

        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeyException e) {
        }
    }

    public boolean validate(MapRegister mapRegister) {
        ByteBuffer mapRegisterBuffer = MapRegisterSerializer.getInstance().serialize(mapRegister);
        if (mapRegisterBuffer == null) {
            return true;
        }
        
        mapRegisterBuffer.position(MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        mapRegisterBuffer.put(tempAuthenticationData);
        mapRegisterBuffer.position(0);
        return Arrays.equals(getAuthenticationData(mapRegisterBuffer.array()),mapRegister.getAuthenticationData());
    }

    protected byte[] getAuthenticationData(byte[] data) {
        return mac.doFinal(data);
    }

    public int getAuthenticationLength() {
        return mac.getMacLength();
    }

    public String getAlgorithem() {
        return algorithem;
    }

    public void setAlgorithem(String algorithem) {
        this.algorithem = algorithem;
    }

    public byte[] getAuthenticationData(MapNotify mapNotify) {
        return getAuthenticationData(MapNotifySerializer.getInstance().serialize(mapNotify).array());
    }

}
