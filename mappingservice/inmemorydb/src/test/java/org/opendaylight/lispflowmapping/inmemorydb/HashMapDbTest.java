/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

/**
 * Test for {@link HashMapDb} class.
 */
public class HashMapDbTest {

    private HashMapDb map;

    @Before
    public void setUp() throws Exception {
        map = new HashMapDb();
    }

    /**
     * Test insertion of one entry to the {@link HashMapDb}.
     */
    @Test
    public void testPutGet_oneEntry() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        map.put(dbEntryKey,  new MappingEntry<>(mapKey1, mapValue1));

        Assert.assertEquals("Map entry did not match",
                Collections.<String, Object>singletonMap(mapKey1, mapValue1),
                map.get(dbEntryKey));
    }

    /**
     * Test insertion of three entries to the {@link HashMapDb}.
     */
    @Test
    public void testPutGet_threeEntries() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        String mapKey2 = "mapKey2";
        String mapValue2 = "mapValue2";
        String mapKey3 = "mapKey3";
        String mapValue3 = "mapValue3";
        map.put(dbEntryKey,
                new MappingEntry<Object>(mapKey1, mapValue1),
                new MappingEntry<Object>(mapKey2, mapValue2),
                new MappingEntry<Object>(mapKey3, mapValue3));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(mapKey1, mapValue1);
        resultMap.put(mapKey2, mapValue2);
        resultMap.put(mapKey3, mapValue3);

        Assert.assertEquals("Presence of all 3 entries was expected", resultMap, map.get(dbEntryKey));
    }

    /**
     * Test retrieving a specific value.
     */
    @Test
    public void testGetSpecific() throws Exception {
        Assert.assertNull("Should return the null when map is empty", map.getSpecific("dnEntryKey", "mapEntryKey"));

        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        map.put(dbEntryKey, new MappingEntry<Object>(mapKey1, mapValue1));

        Assert.assertEquals(mapValue1, map.getSpecific(dbEntryKey, mapKey1));
    }

    /**
     * Test {@link HashMapDb#getBest} with IP prefix.
     */
    @Test
    public void testGetBest_withIpPrefix() throws Exception {
        final Eid ipv4PrefixEid1 = LispAddressUtil.asIpv4PrefixBinaryEid("192.168.0.0" + "/16");
        final Eid ipv4PrefixEid2 = LispAddressUtil.asIpv4PrefixBinaryEid("192.169.0.0" + "/16");
        final Eid ipv4PrefixEid3 = LispAddressUtil.asIpv4PrefixBinaryEid("192.168.1.1" + "/32");
        final String mapSubKey1 = "mapSubKey1";
        final String mapSubKey2 = "mapSubKey2";
        final String mapValue1 = "mapValue1";
        final String mapValue2 = "mapValue2";

        final MappingEntry<Object> mapEntry1 = new MappingEntry<>(mapSubKey1, mapValue1);
        final MappingEntry<Object> mapEntry2 = new MappingEntry<>(mapSubKey2, mapValue2);

        map.put(ipv4PrefixEid1, mapEntry1);
        map.put(ipv4PrefixEid2, mapEntry2);

        Map<String, ?> res = map.getBest(ipv4PrefixEid3);
        Assert.assertEquals(Collections.<String, Object>singletonMap(mapSubKey1, mapValue1), res);
    }

    /**
     * Test {@link HashMapDb#getBest} with non-IP prefix.
     */
    @Test
    public void testGetBest_withNonIpPrefix() throws Exception {
        final Eid mac1 = LispAddressUtil.asMacEid("01:02:03:04:05:06");
        final Eid mac2 = LispAddressUtil.asMacEid("01:02:03:04:05:07");
        final String mapSubKey1 = "mapSubKey1";
        final String mapSubKey2 = "mapSubKey2";
        final String mapValue1 = "mapValue1";
        final String mapValue2 = "mapValue2";
        final MappingEntry<Object> mapEntry1 = new MappingEntry<>(mapSubKey1, mapValue1);
        final MappingEntry<Object> mapEntry2 = new MappingEntry<>(mapSubKey2, mapValue2);

        map.put(mac1, mapEntry1);
        map.put(mac2, mapEntry2);
        Assert.assertEquals(Collections.<String, Object>singletonMap(mapSubKey1, mapValue1), map.getBest(mac1));
    }

    /**
     * Test {@link HashMapDb#getBestPair} with IP prefix.
     */
    @Test
    public void testGetBestPair_withIpPrefix() throws Exception {
        final Eid ipv4PrefixEid1 = LispAddressUtil.asIpv4PrefixBinaryEid("192.168.0.0" + "/16");
        final Eid ipv4PrefixEid2 = LispAddressUtil.asIpv4PrefixBinaryEid("192.169.0.0" + "/16");
        final Eid ipv4PrefixEid3 = LispAddressUtil.asIpv4PrefixBinaryEid("192.168.1.1" + "/32");
        final String mapSubKey1 = "mapSubKey1";
        final String mapSubKey2 = "mapSubKey2";
        final String mapValue1 = "mapValue1";
        final String mapValue2 = "mapValue2";
        final MappingEntry<Object> mapEntry1 = new MappingEntry<>(mapSubKey1, mapValue1);
        final MappingEntry<Object> mapEntry2 = new MappingEntry<>(mapSubKey2, mapValue2);

        map.put(ipv4PrefixEid1, mapEntry1);
        map.put(ipv4PrefixEid2, mapEntry2);

        SimpleImmutableEntry<Eid, Map<String, ?>> res =  map.getBestPair(ipv4PrefixEid3);
        Assert.assertEquals(ipv4PrefixEid1, res.getKey());
        Assert.assertEquals(Collections.<String, Object>singletonMap(mapSubKey1, mapValue1), res.getValue());
    }

    /**
     * Test {@link HashMapDb#getBestPair} with non-IP prefix.
     */
    @Test
    public void testGetBestPair_withNonIpPrefix() throws Exception {
        final Eid mac1 = LispAddressUtil.asMacEid("01:02:03:04:05:06");
        final Eid mac2 = LispAddressUtil.asMacEid("01:02:03:04:05:07");
        final String mapSubKey1 = "mapSubKey1";
        final String mapSubKey2 = "mapSubKey2";
        final String mapValue1 = "mapValue1";
        final String mapValue2 = "mapValue2";
        final MappingEntry<Object> mapEntry1 = new MappingEntry<>(mapSubKey1, mapValue1);
        final MappingEntry<Object> mapEntry2 = new MappingEntry<>(mapSubKey2, mapValue2);


        map.put(mac1, mapEntry1);
        map.put(mac2, mapEntry2);
        SimpleImmutableEntry<Eid, Map<String, ?>> res =  map.getBestPair(mac1);
        Assert.assertEquals(mac1, res.getKey());
        Assert.assertEquals(Collections.<String, Object>singletonMap(mapSubKey1, mapValue1), res.getValue());
    }

    @Test
    public void testGetAll() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        String mapKey2 = "mapKey2";
        String mapValue2 = "mapValue2";
        String mapKey3 = "mapKey3";
        String mapValue3 = "mapValue3";
        map.put(dbEntryKey,
                new MappingEntry<Object>(mapKey1, mapValue1),
                new MappingEntry<Object>(mapKey2, mapValue2),
                new MappingEntry<Object>(mapKey3, mapValue3));

        Map mapResult = new HashMap<>();
        Map mapSample = new HashMap<>();
        mapSample.put(mapKey1, mapValue1);
        mapSample.put(mapKey2, mapValue2);
        mapSample.put(mapKey3, mapValue3);
        map.getAll((keyId, valueKey, value) -> {
            Assert.assertEquals(dbEntryKey, keyId);
            mapResult.put(valueKey, value);
        });
        Assert.assertEquals(mapSample, mapResult);
    }

    @Test
    public void testRemove() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        map.put(dbEntryKey,
                new MappingEntry<Object>(mapKey1, mapValue1));
        map.remove(dbEntryKey);
        Assert.assertNull("DB should be empty after entry removal", map.get(dbEntryKey));
    }

    /**
     * Test {@link HashMapDb#remove} with IP-prefix.
     */
    @Test
    public void testRemove_withIpPrefix() throws Exception {
        final Eid ipv4PrefixEid1 = LispAddressUtil.asIpv4PrefixBinaryEid("192.168.0.0" + "/16");
        final String mapSubKey1 = "mapSubKey1";
        final String mapValue1 = "mapValue1";

        final MappingEntry<Object> mapEntry1 = new MappingEntry<>(mapSubKey1, mapValue1);

        map.put(ipv4PrefixEid1, mapEntry1);
        map.remove(ipv4PrefixEid1);

        Assert.assertNull(map.getBest(ipv4PrefixEid1));
        Assert.assertNull(map.getBestPair(ipv4PrefixEid1));
    }

    @Test
    public void testRemoveSpecific() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        String mapValue1 = "mapValue1";
        String mapKey2 = "mapKey2";
        String mapValue2 = "mapValue2";
        String mapKey3 = "mapKey3";
        String mapValue3 = "mapValue3";
        map.put(dbEntryKey,
                new MappingEntry<Object>(mapKey1, mapValue1),
                new MappingEntry<Object>(mapKey2, mapValue2),
                new MappingEntry<Object>(mapKey3, mapValue3));

        map.removeSpecific(dbEntryKey, mapKey1);
        Assert.assertNull("Entry should not be present in DB", map.getSpecific(dbEntryKey, mapKey1));

        map.removeSpecific(dbEntryKey, mapKey2);
        Assert.assertNull("Entry should not be present in DB", map.getSpecific(dbEntryKey, mapKey2));

        map.removeSpecific(dbEntryKey, mapKey3);
        Assert.assertNull("Entry should not be present in DB", map.getSpecific(dbEntryKey, mapKey3));

        Assert.assertNull("MapEntry should not be present after removal the last entry", map.get(dbEntryKey));
    }

    @Test
    public void testRemoveAll() throws Exception {
        Object dbEntryKey1 = "dbEntryKey";
        map.put(dbEntryKey1, new MappingEntry<>("mapKey1", "mapValue1"));
        Object dbEntryKey2 = "dbEntryKey";
        map.put(dbEntryKey2, new MappingEntry<>("mapKey2", "mapValue2"));

        map.removeAll();
        map.getAll((keyId, valueKey, value) -> Assert.fail("DB should be empty"));
    }

    @Test
    public void testClose() throws Exception {
        Object dbEntryKey1 = "dbEntryKey";
        map.put(dbEntryKey1, new MappingEntry<>("mapKey1", "mapValue1"));
        Object dbEntryKey2 = "dbEntryKey";
        map.put(dbEntryKey2, new MappingEntry<>("mapKey2", "mapValue2"));

        map.close();
        map.getAll((keyId, valueKey, value) -> Assert.fail("DB should be empty"));
    }

    @Test
    public void testPutNestedTable_newEntry() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        map.putNestedTable(dbEntryKey, mapKey1);
        Assert.assertTrue(map.getSpecific(dbEntryKey, mapKey1) instanceof ILispDAO);
    }

    @Test
    public void testPutNestedTable_entryExists() throws Exception {
        Object dbEntryKey = "dbEntryKey";
        String mapKey1 = "mapKey1";
        ILispDAO mapValue1 = new HashMapDb();
        map.put(dbEntryKey, new MappingEntry<>(mapKey1, mapValue1));
        Assert.assertEquals(mapValue1, map.putNestedTable(dbEntryKey, mapKey1));
        Assert.assertEquals(mapValue1, map.getSpecific(dbEntryKey, mapKey1));
    }

    @Test
    public void testPutTable_newEntry() throws Exception {
        Object dbEntryKey = "tables";
        String mapKey1 = "mapKey1";
        map.putTable(mapKey1);
        Assert.assertTrue(map.getSpecific(dbEntryKey, mapKey1) instanceof ILispDAO);
    }

    @Test
    public void testPutTable_entryExists() throws Exception {
        Object dbEntryKey = "tables";
        String mapKey1 = "mapKey1";
        ILispDAO mapValue1 = new HashMapDb();
        map.put(dbEntryKey, new MappingEntry<>(mapKey1, mapValue1));
        Assert.assertEquals(mapValue1, map.putTable(mapKey1));
        Assert.assertEquals(mapValue1, map.getSpecific(dbEntryKey, mapKey1));
    }
}
