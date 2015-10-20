/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.authentication;

//import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.opendaylight.lispflowmapping.interfaces.lisp.ILispAuthentication;
//import org.opendaylight.lispflowmapping.lisp.serializer.MapNotifySerializer;
//import org.opendaylight.lispflowmapping.lisp.serializer.MapRegisterSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapNotify;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LispMACAuthentication implements ILispAuthentication {

    protected static final Logger LOG = LoggerFactory.getLogger(LispMACAuthentication.class);

    protected String algorithm;
    private byte[] tempAuthenticationData;
    private int authenticationLength;

    public LispMACAuthentication(String algorithm) {
        this.algorithm = algorithm;
        try {
            authenticationLength = Mac.getInstance(algorithm).getMacLength();
            tempAuthenticationData = new byte[authenticationLength];
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("No such MAC algorithm" + algorithm);
        }
    }

    public boolean validate(MapRegister mapRegister, String key) {
        return true;
        /*
        if (key == null) {
            LOG.warn("The authentication key is null!");
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
        */
    }

    protected byte[] getAuthenticationData(byte[] data, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);

            return mac.doFinal(data);
        } catch (InvalidKeyException e) {
            LOG.warn("Invalid password" + key);
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("No such MAC algorithm" + algorithm);
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
        return null;
        //return getAuthenticationData(MapNotifySerializer.getInstance().serialize(mapNotify).array(), key);
    }

}
