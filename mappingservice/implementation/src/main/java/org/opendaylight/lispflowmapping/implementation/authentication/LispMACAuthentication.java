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

public class LispMACAuthentication implements ILispAuthentication {

    protected String algorithem;
    protected Mac mac;
    private byte[] tempAuthenticationData;

    public LispMACAuthentication(String algorithem) {
        this.algorithem = algorithem;
        try {
            mac = Mac.getInstance(algorithem);
            tempAuthenticationData = new byte[mac.getMacLength()];
            Arrays.fill(tempAuthenticationData, (byte) 0);
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public boolean validate(MapRegister mapRegister, String key) {
        if (key == null) {
            return false;
        }
        ByteBuffer mapRegisterBuffer = MapRegisterSerializer.getInstance().serialize(mapRegister);
        if (mapRegisterBuffer == null) {
            return true;
        }

        mapRegisterBuffer.position(MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        mapRegisterBuffer.put(tempAuthenticationData);
        mapRegisterBuffer.position(0);
        return Arrays.equals(getAuthenticationData(mapRegisterBuffer.array(), key), mapRegister.getAuthenticationData());
    }

    protected byte[] getAuthenticationData(byte[] data, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithem);

            mac.init(signingKey);

            return mac.doFinal(data);
        } catch (InvalidKeyException e) {
        }
        return null;
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

    public byte[] getAuthenticationData(MapNotify mapNotify, String key) {
        return getAuthenticationData(MapNotifySerializer.getInstance().serialize(mapNotify).array(), key);
    }

}
