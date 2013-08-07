package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


public class MapResolverTest extends BaseTestCase {

    private MapResolver testedMapResolver;

    private ILispDAO lispDAO;
    private MapRequest mapRequest;
    private LispIpv4Address v4Address;


    @Override
    @Before
    public void before() throws Exception {
        super.before();
        lispDAO = context.mock(ILispDAO.class);
        testedMapResolver = new MapResolver(lispDAO);

        mapRequest = new MapRequest();
        v4Address = new LispIpv4Address(0x01020304);
    }

    @Test
    public void handleMapRequest__NumRLOCsMismatch() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        Map<String, Object> rlocs = prepareMapping(new EidRecord((byte) 0, v4Address), new LispIpv4Address(0x44332211));
        rlocs.put("NumRLOCs", 5);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators.getLocators().size());
        assertLocator(new LispIpv4Address(0x44332211), eidToLocators.getLocators().get(0));
    }

    @Test
    public void handleMapRequest__BadRLOCType() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        Map<String, Object> rlocs = prepareMapping(new EidRecord((byte) 0, v4Address), new LispIpv4Address(0x71717171), new LispIpv4Address(0x44332211));
        rlocs.put("RLOC0", "Ooga booga");

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(2, eidToLocators.getLocators().size());
        assertLocator(new LispIpv4Address(0x71717171), eidToLocators.getLocators().get(0));
    }


    @Test
    public void handleMapRequest__ReplyWithSingleLocator() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));

        prepareMapping(new EidRecord((byte) 0, v4Address), new LispIpv4Address(0x04030201));

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators.getLocators().size());

        assertLocator(new LispIpv4Address(0x04030201), eidToLocators.getLocators().get(0));
    }

    private Map<String, Object> prepareMapping(EidRecord eid, LispAddress... address) {
        EidToLocatorRecord record = new EidToLocatorRecord();
        record.setPrefix(eid.getPrefix());
        record.setMaskLength(eid.getMaskLength());
        Map<String, Object> result = null;
        if (address.length > 0) {
            result = new HashMap<String, Object>();
            result.put("RLOCS", record);
            for (int i = 0; i < address.length; i++) {
                record.addLocator((new LocatorRecord()).setLocator(address[i]).setRouted(true));
            }
        }

        oneOf(lispDAO).get(weq(v4Address));
        ret(result);

        return result;
    }

    @Test
    public void handleMapRequest__VerifyBasicFields() throws Exception {
        EidRecord record = new EidRecord((byte) 0x20, v4Address);
        mapRequest.addEidRecord(record);
        prepareMapping(record, new LispIpv4Address(0x04030201));

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);
        
        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals((byte) 0x20, eidToLocators.getMaskLength());
        assertEquals(v4Address, eidToLocators.getPrefix());
    }

    @Test
    public void handleMapRequest__ReplyWithMultipleLocators() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));

        prepareMapping(new EidRecord((byte) 0, v4Address), new LispIpv4Address(0x04030201), new LispIpv6Address("::1"), new LispIpv4Address("1.8.2.7"));

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(3, eidToLocators.getLocators().size());

        assertLocator(new LispIpv4Address(0x04030201), eidToLocators.getLocators().get(0));
        assertLocator(new LispIpv6Address("::1"), eidToLocators.getLocators().get(1));
        assertLocator(new LispIpv4Address("1.8.2.7"), eidToLocators.getLocators().get(2));
    }

    private void assertLocator(LispAddress expectedAddress, LocatorRecord locatorRecord) {
        assertEquals(expectedAddress, locatorRecord.getLocator());
        Assert.assertTrue(locatorRecord.isRouted());
    }
}
