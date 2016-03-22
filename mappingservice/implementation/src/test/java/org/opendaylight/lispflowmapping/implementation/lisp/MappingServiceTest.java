/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.MappingService;
import org.opendaylight.lispflowmapping.implementation.MappingSystem;
import org.opendaylight.lispflowmapping.implementation.mdsal.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.authkey.container.MappingAuthkeyBuilder;

/**
 *
 * @author Florin Coras
 *
 */
public class MappingServiceTest extends BaseTestCase {

    private ILispDAO dao;
    private MappingService mapService;
    private DataStoreBackEnd dsbe;
    private MappingSystem mapSystem;

    private Eid eid;
    private String eidIpv4String = "10.31.0.5";

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        dao = context.mock(ILispDAO.class);
        dsbe = context.mock(DataStoreBackEnd.class);

        // map-caches init and table creation
        allowing(dao).putTable(with(MappingOrigin.Northbound.toString()));will(returnValue(dao));
        allowing(dao).putTable(with(MappingOrigin.Southbound.toString()));will(returnValue(dao));

        mapSystem = new MappingSystem(dao, true, true, true);

        mapService = new MappingService();
        mapService.setDaoService(dao);
        inject(mapService, "dsbe", dsbe);
        inject(mapService, "mappingSystem", mapSystem);

        eid = LispAddressUtil.asIpv4PrefixEid(eidIpv4String + "/32");

    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void handleAddAuthenticationKey() throws Exception {
        MappingAuthkey authKey = new MappingAuthkeyBuilder().setKeyType(1).setKeyString("pass").build();
        MappingEntry<MappingAuthkey> keyMappingEntry = new MappingEntry<>(SubKeys.AUTH_KEY, authKey);
        Eid key = getDefaultKey();
        MappingEntry<MappingAuthkey>[] authKeys =(MappingEntry<MappingAuthkey>[]) (Arrays.asList(keyMappingEntry).toArray());
        addDsbeAddKeyExpectation();
        oneOf(dao).put(weq(key), weq(authKeys));
        mapService.addAuthenticationKey(eid, authKey);
    }

    @Test
    @Ignore
    public void handleGetAuthenticationKey() throws Exception {
        Eid key = getDefaultKey();
        oneOf(dao).getSpecific(weq(key), with(SubKeys.AUTH_KEY));
        ret("password");
        assertEquals("password", mapService.getAuthenticationKey(eid));
    }

    @Test
    public void handleGetAuthenticationKeyNoIteration() throws Exception {
        mapSystem.setIterateMask(false);
        Eid key = getDefaultKey();
        Eid passKey = getKey(30);
        oneOf(dao).getSpecific(weq(key), with(SubKeys.AUTH_KEY));
        allowing(dao).getSpecific(weq(passKey), with(SubKeys.AUTH_KEY));
        ret("password");
        assertEquals(null, mapService.getAuthenticationKey(eid));
    }

    @Test
    @Ignore
    public void handleRemoveAuthenticationKey() throws Exception {
        Eid key = getDefaultKey();
        addDsbeRemoveKeyExpectation();
        oneOf(dao).removeSpecific(weq(key), with(SubKeys.AUTH_KEY));
        mapService.removeAuthenticationKey(eid);
    }

    private Eid getDefaultKey() {
        return getKey(32);
    }

    private Eid getKey(int mask) {
        Eid key = LispAddressUtil.asIpv4PrefixEid(eidIpv4String + "/" + Integer.toString(mask));
        return MaskUtil.normalize(key, (short) mask);
    }

    private void addDsbeAddKeyExpectation() {
        ValueSaverAction<AuthenticationKey> dsbeAddKeySaverAction = new ValueSaverAction<AuthenticationKey>() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                mapSystem.addAuthenticationKey(lastValue.getEid(), lastValue.getMappingAuthkey());
                return null;
            }
        };
        oneOf(dsbe).addAuthenticationKey(with(dsbeAddKeySaverAction)); will(dsbeAddKeySaverAction);
    }

    private void addDsbeRemoveKeyExpectation() {
        ValueSaverAction<AuthenticationKey> dsbeRemoveKeySaverAction = new ValueSaverAction<AuthenticationKey>() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                mapSystem.removeAuthenticationKey(lastValue.getEid());
                return null;
            }
        };
        oneOf(dsbe).removeAuthenticationKey(with(dsbeRemoveKeySaverAction)); will(dsbeRemoveKeySaverAction);
    }

}
