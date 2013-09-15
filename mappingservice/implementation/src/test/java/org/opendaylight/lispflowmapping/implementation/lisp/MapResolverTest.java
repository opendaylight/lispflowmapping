package org.opendaylight.lispflowmapping.implementation.lisp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;

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
    private LispIpv6Address v6Address;

    private HashMap<IMappingServiceKey, Map<String, Object>> daoResults;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        lispDAO = context.mock(ILispDAO.class);
        testedMapResolver = new MapResolver(lispDAO);

        mapRequest = new MapRequest();
        v4Address = new LispIpv4Address("1.2.3.4");
        v6Address = new LispIpv6Address("0:0:0:0:0:0:0:1");
        daoResults = new HashMap<IMappingServiceKey, Map<String, Object>>();
    }

    @Test
    public void handleMapRequest__NumRLOCsMismatch() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x44332211)));
        Map<String, Object> rlocs = prepareMapping(record);
        rlocs.put("NumRLOCs", 5);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators.getLocators().size());
        assertLocator(new LispIpv4Address(0x44332211), eidToLocators.getLocators().get(0));
    }

    @Test
    public void handleMapRequest__BadRLOCType() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x71717171)));
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x44332211)));
        Map<String, Object> rlocs = prepareMapping(record);
        rlocs.put("RLOC0", "Ooga booga");

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators.getLocators().size());
        assertLocator(new LispIpv4Address(0x44332211), eidToLocators.getLocators().get(0));
    }

    @Test
    public void handleMapRequest__NoNumRLOCs() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        allowing(lispDAO).get(wany(MappingServiceKey.class));
        ret(new HashMap<String, Object>());

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(0, eidToLocators.getLocators().size());
    }

    @Test
    public void handleMapRequest__IllegalNumRLOCs() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x44332211)));
        Map<String, Object> rlocs = prepareMapping(record);
        rlocs.put("NumRLOCs", "Bla");

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(0, eidToLocators.getLocators().size());
    }

    @Test
    public void handleMapRequest__ReplyWithSingleLocator() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, new LispIpv4Address("1.2.3.4")));

        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators.getLocators().size());

        assertLocator(new LispIpv4Address(0x04030201), eidToLocators.getLocators().get(0));
    }

    private Map<String, Object> prepareMapping(EidToLocatorRecord... records) {
        if (records.length > 0) {
            for (EidToLocatorRecord eidToLocatorRecord : records) {
                Map<String, Object> result = null;
                List<LispAddress> locators = new ArrayList<LispAddress>();
                for (LocatorRecord locator : eidToLocatorRecord.getLocators()) {
                    locators.add(locator.getLocator());
                }
                result = new HashMap<String, Object>();
                result.put("NumRLOCs", locators.size());
                for (int i = 0; i < locators.size(); i++) {
                    result.put("RLOC" + i, new MappingServiceValue(eidToLocatorRecord.getLocators().get(i),(byte)eidToLocatorRecord.getRecordTtl()));
                }
                if (eidToLocatorRecord.getMaskLength() > 0) {
                    daoResults.put(new MappingServiceKey(eidToLocatorRecord.getPrefix(), (byte)eidToLocatorRecord.getMaskLength()), result);
                } else {
                    daoResults.put(new MappingServiceNoMaskKey(eidToLocatorRecord.getPrefix()), result);
                }
            }
        }

        ValueSaverAction<IMappingServiceKey> daoGetSaverAction = new ValueSaverAction<IMappingServiceKey>() {
            @Override
            protected boolean validate(IMappingServiceKey value) {
                return true;
            }
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                return daoResults.get(lastValue);
            }
        };
        
        allowing(lispDAO).get(with(daoGetSaverAction));
        will(daoGetSaverAction);

        return daoResults.get(new MappingServiceNoMaskKey(v4Address));
    }

    @Test
    public void handleMapRequest__VerifyBasicFields() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals((byte) 0, eidToLocators.getMaskLength());
        assertEquals(v4Address, eidToLocators.getPrefix());
    }
    
    @Test
    public void handleMapRequest__VerifyMask() throws Exception {
        EidRecord mapRequestRecord = new EidRecord((byte) 0, v4Address);
        mapRequestRecord.setMaskLength(32);
        mapRequest.addEidRecord(mapRequestRecord);
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(new LispIpv4Address("1.2.3.0")).setMaskLength(24);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals((byte) 32, eidToLocators.getMaskLength());
        assertEquals(v4Address, eidToLocators.getPrefix());
    }
    
    @Test
    public void handleMapRequest__VerifyMaskIPv6() throws Exception {
        EidRecord mapRequestRecord = new EidRecord((byte) 0, v6Address);
        mapRequestRecord.setMaskLength(128);
        mapRequest.addEidRecord(mapRequestRecord);
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(new LispIpv6Address("0:0:0:0:0:0:0:0")).setMaskLength(128);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        assertEquals(mapRequest.getNonce(), mapReply.getNonce());
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals((byte) 128, eidToLocators.getMaskLength());
        assertEquals(v6Address, eidToLocators.getPrefix());
    }
    
    @Test
    public void handleMapRequest__VerifyMaskIPv6NoMatch() throws Exception {
        EidRecord mapRequestRecord = new EidRecord((byte) 128, v6Address);
        mapRequest.addEidRecord(mapRequestRecord);
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(new LispIpv6Address("0:0:0:0:0:0:0:1")).setMaskLength(112);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        
        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(0, eidToLocators.getLocators().size());
    }
    
    @Test
    public void handleMapRequest__VerifyMaskNoMatch() throws Exception {
        EidRecord mapRequestRecord = new EidRecord((byte) 0, v4Address);
        mapRequestRecord.setMaskLength(32);
        mapRequest.addEidRecord(mapRequestRecord);
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(new LispIpv4Address("1.2.4.0")).setMaskLength(24);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        prepareMapping(record);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);
        
        EidToLocatorRecord eidToLocators = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(0, eidToLocators.getLocators().size());

    }


    @Test
    public void handleMapRequest__ReplyWithMultipleLocators() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        
        EidToLocatorRecord record = new EidToLocatorRecord().setPrefix(v4Address);
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        record.addLocator(new LocatorRecord().setLocator(new LispIpv6Address("::1")));
        record.addLocator(new LocatorRecord().setLocator(new LispIpv4Address("1.8.2.7")));
        prepareMapping(record);

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

    @Test
    public void handleMapRequest__MultipleEIDs() throws Exception {
        mapRequest.addEidRecord(new EidRecord((byte) 0, v4Address));
        mapRequest.addEidRecord(new EidRecord((byte) 0, v6Address));
        
        EidToLocatorRecord record1 = new EidToLocatorRecord().setPrefix(v4Address);
        record1.addLocator(new LocatorRecord().setLocator(new LispIpv4Address(0x04030201)));
        
        EidToLocatorRecord record2 = new EidToLocatorRecord().setPrefix(v6Address);
        record2.addLocator(new LocatorRecord().setLocator(new LispIpv6Address("0:0:0:0:0:0:0:1")));
//        prepareMapping(record2);
        prepareMapping(record1,record2);

        MapReply mapReply = testedMapResolver.handleMapRequest(mapRequest);

        EidToLocatorRecord eidToLocators1 = mapReply.getEidToLocatorRecords().get(0);
        assertEquals(1, eidToLocators1.getLocators().size());
        assertLocator(new LispIpv4Address(0x04030201), eidToLocators1.getLocators().get(0));

        EidToLocatorRecord eidToLocators2 = mapReply.getEidToLocatorRecords().get(1);
        assertEquals(1, eidToLocators2.getLocators().size());
        assertLocator(new LispIpv6Address("0:0:0:0:0:0:0:1"), eidToLocators2.getLocators().get(0));
    }
}
