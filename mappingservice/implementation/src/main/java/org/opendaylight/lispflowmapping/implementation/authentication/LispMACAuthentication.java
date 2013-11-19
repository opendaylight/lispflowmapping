package org.opendaylight.lispflowmapping.implementation.authentication;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.buf.HexUtils;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMACAuthentication implements ILispAuthentication {

    protected static final Logger logger = LoggerFactory.getLogger(LispMACAuthentication.class);

    protected String algorithm;
    private byte[] tempAuthenticationData;
    private int authenticationLength;

    public LispMACAuthentication(String algorithm) {
        this.algorithm = algorithm;
        try {
            authenticationLength = Mac.getInstance(algorithm).getMacLength();
            tempAuthenticationData = new byte[authenticationLength];
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such MAC algorithm" + algorithm);
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
        System.out.println("Expected Authentication:");
        System.out.println(HexUtils.toHexString(getAuthenticationData(mapRegisterBuffer.array(), key)));
        System.out.println("Got Authentication:");
        System.out.println(HexUtils.toHexString(mapRegister.getAuthenticationData()));
        return Arrays.equals(getAuthenticationData(mapRegisterBuffer.array(), key), mapRegister.getAuthenticationData());
    }

    protected byte[] getAuthenticationData(byte[] data, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);

            return mac.doFinal(data);
        } catch (InvalidKeyException e) {
            logger.error("Invalid password" + key);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such MAC algorithm" + algorithm);
        }
        return null;
    }

    public int getAuthenticationLength() {
        return authenticationLength;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getAuthenticationData(MapNotify mapNotify, String key) {
        return getAuthenticationData(MapNotifySerializer.getInstance().serialize(mapNotify).array(), key);
    }

}
