package org.opendaylight.lispflowmapping.implementation.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceNoMaskKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingValueKey;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReplyAction;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;

/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

public class InMemoryDAOTest extends BaseTestCase {
    private InMemoryDAO testedInMemoryDao;
    private LispIpv4Address eid;
    private LispIpv4Address rloc;

    class MappingServiceNoMaskKeyConvertor implements ILispTypeConverter<MappingServiceNoMaskKey, Integer> {
    }

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        testedInMemoryDao = new InMemoryDAO();
        testedInMemoryDao.register(TestLispIpv4AddressConverter.class);
        testedInMemoryDao.register(MappingServiceNoMaskKeyConvertor.class);
        eid = new LispIpv4Address(1);
        rloc = new LispIpv4Address(11);
    }

    class TestLispIpv4AddressConverter implements ILispTypeConverter<LispIpv4Address, Integer> {
    }

    @Test
    public void put__OverrideValues() throws Exception {
        testedInMemoryDao.put(eid, new MappingEntry<Integer>("Val", 1),//
                new MappingEntry<String>("Val2", "Yes"));

        testedInMemoryDao.put(eid, new MappingEntry<Integer>("Val", 2),//
                new MappingEntry<String>("Val5", "Bla"));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid);
        assertEquals(2, rlocs.size());

        assertEquals(2, rlocs.get("Val"));
        assertEquals("Bla", rlocs.get("Val5"));

        assertEquals(2, testedInMemoryDao.getSpecific(eid, "Val"));
        assertEquals(2, testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("Val")));

        assertEquals("Bla", testedInMemoryDao.getSpecific(eid, "Val5"));
        assertEquals("Bla", testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("Val5")));
    }

    @Test
    public void remove__Basic() throws Exception {
        testedInMemoryDao.put(eid, new MappingEntry<LispIpv4Address>("IP", rloc));
        assertTrue(testedInMemoryDao.remove(eid));

        assertNull(testedInMemoryDao.get(eid));

        assertFalse(testedInMemoryDao.remove(eid));
    }

    @Test
    public void timeOut__Basic() throws Exception {

        MappingServiceValue value = new MappingServiceValue();
        value.setRlocs(new ArrayList<MappingServiceRLOC>());
        MappingServiceRLOC mappingServiceRloc = new MappingServiceRLOC(new LocatorRecord().setLocator(rloc), 120, MapReplyAction.NoAction, true);
        value.getRlocs().add(mappingServiceRloc);
        testedInMemoryDao.put(MappingServiceKeyUtil.generateMappingServiceKey(eid), new MappingEntry<MappingServiceValue>("value", value));

        assertEquals(rloc, ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(eid)).get("value"))
                .getRlocs().get(0).getRecord().getLocator());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);

        assertNull(testedInMemoryDao.get(eid));

        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);

        assertEquals(rloc, ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(eid)).get("value"))
                .getRlocs().get(0).getRecord().getLocator());
    }

    @Test
    public void clean__Basic() throws Exception {

        MappingServiceValue value = new MappingServiceValue();
        value.setRlocs(new ArrayList<MappingServiceRLOC>());
        MappingServiceRLOC mappingServiceRloc = new MappingServiceRLOC(new LocatorRecord().setLocator(rloc), 120, MapReplyAction.NoAction, true);
        value.getRlocs().add(mappingServiceRloc);
        testedInMemoryDao.put(MappingServiceKeyUtil.generateMappingServiceKey(eid), new MappingEntry<MappingServiceValue>("value", value));

        assertEquals(rloc, ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(eid)).get("value"))
                .getRlocs().get(0).getRecord().getLocator());

        testedInMemoryDao.cleanOld();

        assertEquals(rloc, ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(eid)).get("value"))
                .getRlocs().get(0).getRecord().getLocator());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);

        assertNull(testedInMemoryDao.get(eid));

        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);

        assertEquals(rloc, ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(eid)).get("value"))
                .getRlocs().get(0).getRecord().getLocator());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);
        testedInMemoryDao.cleanOld();
        assertNull(testedInMemoryDao.get(eid));
        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);
        assertNull(testedInMemoryDao.get(eid));

    }

    @Test(expected = IllegalArgumentException.class)
    public void get__NullKey() throws Exception {
        testedInMemoryDao.get(null);
    }

    @Test
    public void Scenario__MultiValues() throws Exception {
        testedInMemoryDao.put(eid, new MappingEntry<LispIpv4Address>("IP", rloc), //
                new MappingEntry<Integer>("Val", 1),//
                new MappingEntry<String>("Val2", "Yes"));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid);
        assertEquals(3, rlocs.size());
        assertEquals(rloc, rlocs.get("IP"));
        assertEquals(1, rlocs.get("Val"));
        assertEquals("Yes", rlocs.get("Val2"));

        assertEquals(rloc, testedInMemoryDao.getSpecific(eid, "IP"));
        assertEquals(rloc, testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("IP")));

        assertEquals(1, testedInMemoryDao.getSpecific(eid, "Val"));
        assertEquals(1, testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("Val")));

        assertEquals("Yes", testedInMemoryDao.getSpecific(eid, "Val2"));
        assertEquals("Yes", testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("Val2")));
    }

    @Test
    public void Scenario__BasicFlow() throws Exception {
        testedInMemoryDao.put(eid, new MappingEntry<LispIpv4Address>("IP", rloc));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid);
        assertEquals(rloc, rlocs.get("IP"));

        assertEquals(rloc, testedInMemoryDao.getSpecific(eid, "IP"));
        assertEquals(rloc, testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("IP")));
    }

    @Test
    public void get__MissingValue() throws Exception {
        assertNull(testedInMemoryDao.get(eid));

        assertNull(testedInMemoryDao.getSpecific(eid, "IP"));
        assertNull(testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("IP")));

        testedInMemoryDao.put(eid, new MappingEntry<LispIpv4Address>("Key1", rloc));
        assertNull(testedInMemoryDao.getSpecific(eid, "IP"));
        assertNull(testedInMemoryDao.getSpecific(eid, new MappingValueKey<LispIpv4Address>("IP")));
    }

    @SuppressWarnings("unused")
    @Test(expected = ClassCastException.class)
    public void get__DifferentType() throws Exception {
        testedInMemoryDao.put(eid, new MappingEntry<LispIpv4Address>("IP", rloc));
        Integer a = testedInMemoryDao.getSpecific(eid, new MappingValueKey<Integer>("IP"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void put__WithoutRegister() throws Exception {
        testedInMemoryDao.put(2, new MappingEntry<Integer>("IP", Integer.valueOf(11)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void get__WithoutRegister() throws Exception {
        testedInMemoryDao.get(1);
    }
}
