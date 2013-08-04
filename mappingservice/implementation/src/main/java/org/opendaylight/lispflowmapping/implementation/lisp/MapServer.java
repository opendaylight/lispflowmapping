/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tomcat.util.buf.HexUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.ILispAuthentication;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationFactory;
import org.opendaylight.lispflowmapping.implementation.authentication.LispKeyIDEnum;
import org.opendaylight.lispflowmapping.implementation.lisp.exception.LispAuthenticationException;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServer;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer implements IMapServer {
    private ILispDAO dao;
    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);
    private static final int MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION = 16;

    public MapServer(ILispDAO dao) {
        this.dao = dao;
    }

    public MapNotify handleMapRegister(MapRegister mapRegister) {
        if (dao == null) {
            logger.warn("handleMapRegister called while dao is uninitialized");
            return null;
        }
        validateMapRegisterAuthentication(mapRegister);
        EidToLocatorRecord eidRecord = mapRegister.getEidToLocatorRecords().get(0);
        List<MappingEntry<?>> rlocs = new ArrayList<ILispDAO.MappingEntry<?>>();
        rlocs.add(new MappingEntry<Integer>("NumRLOCs", eidRecord.getLocators().size()));
        int i = 0;
        for (LocatorRecord locatorRecord : eidRecord.getLocators()) {
            rlocs.add(new MappingEntry<LispAddress>("RLOC" + (i++), locatorRecord.getLocator()));
        }
        dao.put(eidRecord.getPrefix(), rlocs.toArray(new MappingEntry[rlocs.size()]));
        MapNotify mapNotify = null;
        if (mapRegister.isWantMapNotify()) {
            mapNotify = new MapNotify();
            mapNotify.setFromMapRegister(mapRegister);
            addMapNotifyAuthentication(mapNotify);
        }
        return mapNotify;
    }
    
    private void addMapNotifyAuthentication(MapNotify mapNotify) {
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapNotify.getKeyId()));
        ByteBuffer mapNotifyBytes = MapNotifySerializer.getInstance().serialize(mapNotify);
        byte[] authenticationBytes = new byte[authentication.getAuthenticationLength()];
        Arrays.fill(authenticationBytes, (byte)0);
        mapNotifyBytes.position(MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        mapNotifyBytes.put(authenticationBytes);
        mapNotifyBytes.position(0);
        mapNotify.setAuthenticationData(authentication.getAuthenticationData(mapNotifyBytes.array()));
    }
    
    private void validateMapRegisterAuthentication(MapRegister mapRegister) {
        if (mapRegister.getMapRegisterBytes() == null) {
            return;
        }
        ILispAuthentication authentication = LispAuthenticationFactory.getAuthentication(LispKeyIDEnum.valueOf(mapRegister.getKeyId()));
        
        ByteBuffer mapRegisterBuffer = ByteBuffer.wrap(mapRegister.getMapRegisterBytes());
        mapRegisterBuffer.position(MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION);
        byte[] tempAuthenticationData = new byte[authentication.getAuthenticationLength()];
        Arrays.fill(tempAuthenticationData,(byte)0x00);
        mapRegisterBuffer.put(tempAuthenticationData);
        mapRegisterBuffer.position(0);
        if (!Arrays.equals(mapRegister.getAuthenticationData(),authentication.getAuthenticationData(mapRegisterBuffer.array()))) {
            throw new LispAuthenticationException(String.format("Authentication isn't correct. Expected %s got %s",HexUtils.toHexString(authentication.getAuthenticationData(mapRegister.getMapRegisterBytes())),HexUtils.toHexString(mapRegister.getAuthenticationData())));
        }
    }
}
