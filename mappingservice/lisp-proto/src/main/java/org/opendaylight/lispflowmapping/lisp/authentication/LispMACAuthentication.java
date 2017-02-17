/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.authentication;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
            LOG.warn("No such MAC algorithm {}", algorithm, e);
        }
    }

    @Override
    public boolean validate(ByteBuffer mapRegisterBuffer, byte[] expectedAuthData, String key) {
        if (key == null) {
            LOG.warn("Authentication failed: mapping authentication password is null!");
            return false;
        }
        if (mapRegisterBuffer == null) {
            return true;
        }

        mapRegisterBuffer.position(ILispAuthentication.MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        mapRegisterBuffer.put(tempAuthenticationData);
        mapRegisterBuffer.position(0);
        byte[] mapRegisterArray;
        if (mapRegisterBuffer.hasArray()) {
            mapRegisterArray = mapRegisterBuffer.array();
        } else {
            mapRegisterArray = new byte[mapRegisterBuffer.remaining()];
            mapRegisterBuffer.get(mapRegisterArray);
        }
        return Arrays.equals(getAuthenticationData(mapRegisterArray, key), expectedAuthData);
    }

    protected byte[] getAuthenticationData(byte[] data, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);

            return mac.doFinal(data);
        } catch (InvalidKeyException e) {
            LOG.warn("Invalid password {}", key, e);
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("No such MAC algorithm {}", algorithm, e);
        }
        return null;
    }

    public byte[] getAuthenticationData(final ByteBuffer buffer, final String key) {
        byte[] bufferAsArray;
        if (buffer.hasArray()) {
            bufferAsArray = buffer.array();
        } else {
            buffer.position(0);
            bufferAsArray = new byte[buffer.limit()];
            buffer.get(bufferAsArray);
        }

        return getAuthenticationData(bufferAsArray, key);
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
}