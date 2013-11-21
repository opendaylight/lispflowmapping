package org.opendaylight.lispflowmapping.implementation.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispTypeConverter;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOC;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceValue;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingValueKey;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

public class InMemoryDAOTest extends BaseTestCase {
    private InMemoryDAO testedInMemoryDao;
    private Ipv4 eid;
    private Ipv4 rloc;

    class MappingServiceNoMaskKeyConvertor implements ILispTypeConverter<MappingServiceNoMaskKey, Integer> {
    }

    class MappingServiceMaskKeyConvertor implements ILispTypeConverter<MappingServiceKey, Integer> {
    }

    class MappingServiceIpv4Convertor implements ILispTypeConverter<Ipv4Address, Integer> {
    }

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        testedInMemoryDao = new InMemoryDAO();
        testedInMemoryDao.register(MappingServiceIpv4Convertor.class);
        testedInMemoryDao.register(MappingServiceMaskKeyConvertor.class);
        testedInMemoryDao.register(MappingServiceNoMaskKeyConvertor.class);
        eid = LispAFIConvertor.asIPAfiAddress("0.0.0.1");
        rloc = LispAFIConvertor.asIPAfiAddress("0.0.0.11");
    }

    @Test
    public void put__OverrideValues() throws Exception {
        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Integer>("Val", 1),//
                new MappingEntry<String>("Val2", "Yes"));

        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Integer>("Val", 2),//
                new MappingEntry<String>("Val5", "Bla"));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid.getIpv4Address());
        assertEquals(2, rlocs.size());

        assertEquals(2, rlocs.get("Val"));
        assertEquals("Bla", rlocs.get("Val5"));

        assertEquals(2, testedInMemoryDao.getSpecific(eid.getIpv4Address(), "Val"));
        assertEquals(2, testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("Val")));

        assertEquals("Bla", testedInMemoryDao.getSpecific(eid.getIpv4Address(), "Val5"));
        assertEquals("Bla", testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("Val5")));
    }

    @Test
    public void remove__Basic() throws Exception {
        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Ipv4>("IP", rloc));
        assertTrue(testedInMemoryDao.remove(eid.getIpv4Address()));

        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));

        assertFalse(testedInMemoryDao.remove(eid.getIpv4Address()));
    }

    @Test
    public void timeOut__Basic() throws Exception {

        MappingServiceValue value = new MappingServiceValue();
        value.setRlocs(new ArrayList<MappingServiceRLOC>());
        MappingServiceRLOC mappingServiceRloc = new MappingServiceRLOC(new LocatorRecordBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(rloc)).build(), 120, Action.NoAction, true);
        value.getRlocs().add(mappingServiceRloc);
        testedInMemoryDao.put(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid)),
                new MappingEntry<MappingServiceValue>("value", value));

        assertEquals(
                LispAFIConvertor.toContainer(rloc),
                ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid))).get(
                        "value")).getRlocs().get(0).getRecord().getLispAddressContainer());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);

        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));

        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);

        assertEquals(
                LispAFIConvertor.toContainer(rloc),
                ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid))).get(
                        "value")).getRlocs().get(0).getRecord().getLispAddressContainer());
    }

    @Test
    public void clean__Basic() throws Exception {

        MappingServiceValue value = new MappingServiceValue();
        value.setRlocs(new ArrayList<MappingServiceRLOC>());
        MappingServiceRLOC mappingServiceRloc = new MappingServiceRLOC(new LocatorRecordBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(rloc)).build(), 120, Action.NoAction, true);
        value.getRlocs().add(mappingServiceRloc);
        testedInMemoryDao.put(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid)),
                new MappingEntry<MappingServiceValue>("value", value));

        assertEquals(
                LispAFIConvertor.toContainer(rloc),
                ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid))).get(
                        "value")).getRlocs().get(0).getRecord().getLispAddressContainer());

        testedInMemoryDao.cleanOld();

        assertEquals(
                LispAFIConvertor.toContainer(rloc),
                ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid))).get(
                        "value")).getRlocs().get(0).getRecord().getLispAddressContainer());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);

        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));

        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);

        assertEquals(
                LispAFIConvertor.toContainer(rloc),
                ((MappingServiceValue) testedInMemoryDao.get(MappingServiceKeyUtil.generateMappingServiceKey(LispAFIConvertor.toContainer(eid))).get(
                        "value")).getRlocs().get(0).getRecord().getLispAddressContainer());

        testedInMemoryDao.setTimeUnit(TimeUnit.NANOSECONDS);
        testedInMemoryDao.cleanOld();
        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));
        testedInMemoryDao.setTimeUnit(TimeUnit.MINUTES);
        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void get__NullKey() throws Exception {
        testedInMemoryDao.get(null);
    }

    @Test
    public void Scenario__MultiValues() throws Exception {
        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Ipv4>("IP", rloc), //
                new MappingEntry<Integer>("Val", 1),//
                new MappingEntry<String>("Val2", "Yes"));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid.getIpv4Address());
        assertEquals(3, rlocs.size());
        assertEquals(rloc, rlocs.get("IP"));
        assertEquals(1, rlocs.get("Val"));
        assertEquals("Yes", rlocs.get("Val2"));

        assertEquals(rloc, testedInMemoryDao.getSpecific(eid.getIpv4Address(), "IP"));
        assertEquals(rloc, testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("IP")));

        assertEquals(1, testedInMemoryDao.getSpecific(eid.getIpv4Address(), "Val"));
        assertEquals(1, testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("Val")));

        assertEquals("Yes", testedInMemoryDao.getSpecific(eid.getIpv4Address(), "Val2"));
        assertEquals("Yes", testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("Val2")));
    }

    @Test
    public void Scenario__BasicFlow() throws Exception {
        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Ipv4>("IP", rloc));

        Map<String, ?> rlocs = testedInMemoryDao.get(eid.getIpv4Address());
        assertEquals(rloc, rlocs.get("IP"));

        assertEquals(rloc, testedInMemoryDao.getSpecific(eid.getIpv4Address(), "IP"));
        assertEquals(rloc, testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("IP")));
    }

    @Test
    public void get__MissingValue() throws Exception {
        assertNull(testedInMemoryDao.get(eid.getIpv4Address()));

        assertNull(testedInMemoryDao.getSpecific(eid.getIpv4Address(), "IP"));
        assertNull(testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("IP")));

        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Ipv4>("Key1", rloc));
        assertNull(testedInMemoryDao.getSpecific(eid.getIpv4Address(), "IP"));
        assertNull(testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Ipv4>("IP")));
    }

    @SuppressWarnings("unused")
    @Test(expected = ClassCastException.class)
    public void get__DifferentType() throws Exception {
        testedInMemoryDao.put(eid.getIpv4Address(), new MappingEntry<Ipv4>("IP", rloc));
        Integer a = testedInMemoryDao.getSpecific(eid.getIpv4Address(), new MappingValueKey<Integer>("IP"));
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
