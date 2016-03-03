/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;

/**
 * Test for {@link HashMapDb} class
 */
public class HashMapDbTest {

    private HashMapDb map;

    @Before
    public void setUp() throws Exception {
        map = new HashMapDb();
    }

    /**
     * Test insertion of one entry to the {@link HashMapDb}
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
     * Test insertion of three entries to the {@link HashMapDb}
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
     * Test retrieving a specific value
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

        Assert.assertEquals("MapEntry should be empty after removal the last entry", 0, map.get(dbEntryKey).size());
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
    public void testCleanOld() throws Exception {
        map.setRecordTimeOut(240);
        map.setTimeUnit(TimeUnit.SECONDS);

        Object entry1Key = "entry1Key";
        String entry1recordKey = SubKeys.RECORD;
        String entry1recordValue = "record1";
        String entry1regdateKey = SubKeys.REGDATE;
        Date entry1regdateValue = new Date();

        Object entry2Key = "entry2Key";
        String entry2recordKey = SubKeys.RECORD;
        String entry2recordValue = "record2";
        String entry2regdateKey = SubKeys.REGDATE;
        Date entry2regdateValue = new Date(
                System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(map.getRecordTimeOut(), map.getTimeUnit()));

        map.put(entry1Key,
                new MappingEntry<>(entry1recordKey, entry1recordValue),
                new MappingEntry<>(entry1regdateKey, entry1regdateValue));
        map.put(entry2Key,
                new MappingEntry<>(entry2recordKey, entry2recordValue),
                new MappingEntry<>(entry2regdateKey, entry2regdateValue));
        map.cleanOld();

        Assert.assertEquals("Entry should be present in the DB due to its time didn't expired",
                entry1recordValue, map.getSpecific(entry1Key, entry1recordKey));
        Assert.assertNull("Entry should not be present in the DB due to its time expired",
                map.getSpecific(entry2Key, entry2recordKey));

    }

    @Test
    public void testSetGetTimeUnit() throws Exception {
        TimeUnit timeUnit = TimeUnit.DAYS;
        map.setTimeUnit(timeUnit);
        Assert.assertEquals(timeUnit, map.getTimeUnit());
    }

    @Test
    public void testSetGetRecordTimeOut() throws Exception {
        int recordTimeOut = 12345;
        map.setRecordTimeOut(recordTimeOut);
        Assert.assertEquals(recordTimeOut, map.getRecordTimeOut());
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