/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb.radixtrie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadixTrieTest {
    protected static final Logger LOG = LoggerFactory.getLogger(RadixTrieTest.class);

    private static RadixTrie<Integer> radixTrie4;
    private static RadixTrie<Integer> radixTrie6;

    private static byte[] IP4_DEFAULT;
    private static byte[] IP4_BYTES1;
    private static byte[] IP4_BYTES2;
    private static byte[] IP4_BYTES3;
    private static byte[] IP4_BYTES4;
    private static byte[] IP4_BYTES5;
    private static byte[] IP4_BYTES6;
    private static byte[] IP4_BYTES7;
    private static byte[] IP4_BYTES8;
    private static byte[] IP4_BYTES9;
    private static byte[] IP4_BYTES10;
    private static byte[] IP4_BYTES11;
    private static byte[] IP4_BYTES12;
    private static byte[] IP4_BYTES13;
    private static byte[] IP4_BYTES14;
    private static byte[] IP4_BYTES15;
    private static byte[] IP4_BYTES16;
    private static byte[] IP4_BYTES17;
    private static byte[] IP4_BYTES18;
    private static byte[] IP4_BYTES19;
    private static byte[] IP4_BYTES20;
    private static byte[] IP4_BYTES21;
    private static byte[] IP4_BYTES22;
    private static byte[] IP4_BYTES23;
    private static byte[] IP4_BYTES24;
    private static byte[] IP4_BYTES25;

    ArrayList<byte []> itPrefixList4;
    ArrayList<Integer> itPreflenList4;

    private static byte[] IP6_BYTES1;
    private static byte[] IP6_BYTES2;
    private static byte[] IP6_BYTES3;
    private static byte[] IP6_BYTES4;
    private static byte[] IP6_BYTES5;
    private static byte[] IP6_BYTES6;
    private static byte[] IP6_BYTES7;

    @Before
    public void init() {
        try {
            IP4_DEFAULT = InetAddress.getByName("0.0.0.0").getAddress();
            IP4_BYTES1 = InetAddress.getByName("192.168.0.0").getAddress();
            IP4_BYTES2 = InetAddress.getByName("192.167.0.0").getAddress();
            IP4_BYTES3 = InetAddress.getByName("192.169.0.0").getAddress();
            IP4_BYTES4 = InetAddress.getByName("192.168.1.0").getAddress();
            IP4_BYTES5 = InetAddress.getByName("192.168.2.0").getAddress();
            IP4_BYTES6 = InetAddress.getByName("192.168.1.0").getAddress();
            IP4_BYTES7 = InetAddress.getByName("192.168.1.1").getAddress();
            IP4_BYTES8 = InetAddress.getByName("193.168.1.1").getAddress();
            IP4_BYTES9 = InetAddress.getByName("192.168.3.0").getAddress();
            IP4_BYTES10 = InetAddress.getByName("129.214.0.0").getAddress();
            IP4_BYTES11 = InetAddress.getByName("192.168.2.0").getAddress();
            IP4_BYTES12 = InetAddress.getByName("128.0.0.0").getAddress();
            IP4_BYTES13 = InetAddress.getByName("1.1.128.0").getAddress();
            IP4_BYTES14 = InetAddress.getByName("1.1.32.0").getAddress();
            IP4_BYTES15 = InetAddress.getByName("1.1.127.10").getAddress();
            IP4_BYTES16 = InetAddress.getByName("1.1.64.0").getAddress();
            IP4_BYTES17 = InetAddress.getByName("40.218.27.124").getAddress();
            IP4_BYTES18 = InetAddress.getByName("40.192.0.0").getAddress();
            IP4_BYTES19 = InetAddress.getByName("64.142.3.176").getAddress();
            IP4_BYTES20 = InetAddress.getByName("64.228.0.0").getAddress();
            IP4_BYTES21 = InetAddress.getByName("64.0.0.0").getAddress();
            IP4_BYTES22 = InetAddress.getByName("1.1.1.1").getAddress();
            IP4_BYTES23 = InetAddress.getByName("2.2.2.2").getAddress();
            IP4_BYTES24 = InetAddress.getByName("10.10.10.10").getAddress();
            IP4_BYTES25 = InetAddress.getByName("20.20.20.20").getAddress();

            IP6_BYTES1 = InetAddress.getByName("192:168::0:0").getAddress();
            IP6_BYTES2 = InetAddress.getByName("192:167::0:0").getAddress();
            IP6_BYTES3 = InetAddress.getByName("192:169::0:0").getAddress();
            IP6_BYTES4 = InetAddress.getByName("192:168::1:0").getAddress();
            IP6_BYTES5 = InetAddress.getByName("192:168::2:0").getAddress();
            IP6_BYTES6 = InetAddress.getByName("192:168::1:0").getAddress();
            IP6_BYTES7 = InetAddress.getByName("192:168::1:1").getAddress();

            itPrefixList4 = new ArrayList<>();
            itPrefixList4.add(IP4_BYTES2);
            itPrefixList4.add(IP4_BYTES7);
            itPrefixList4.add(IP4_BYTES4);
            itPrefixList4.add(IP4_BYTES4);
            itPrefixList4.add(IP4_BYTES5);
            itPrefixList4.add(IP4_BYTES1);
            itPrefixList4.add(IP4_BYTES3);

            itPreflenList4 = new ArrayList<>();
            itPreflenList4.add(16);
            itPreflenList4.add(32);
            itPreflenList4.add(25);
            itPreflenList4.add(24);
            itPreflenList4.add(24);
            itPreflenList4.add(16);
            itPreflenList4.add(16);

        } catch (UnknownHostException e) {
            LOG.error("Caught exception: ", e);
        }
    }

    /**
     * Tests v4 CRD operations.
     */
    @Test
    public void testIp4() {
        RadixTrie<Integer>.TrieNode res;
        radixTrie4 = new RadixTrie<>(32);
        addIp4Addresses(radixTrie4);

        res = radixTrie4.lookupBest(IP4_BYTES7, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES7));
        assertTrue(res.prefixLength() == 32);
        assertTrue(res.data() == 7);

        res = radixTrie4.lookupBest(IP4_BYTES5, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES5));
        assertTrue(res.prefixLength() == 24);
        assertTrue(res.data() == 5);

        radixTrie4.remove(IP4_BYTES5, 24);
        res = radixTrie4.lookupBest(IP4_BYTES5, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES1));
        assertTrue(res.prefixLength() == 16);
        assertTrue(res.data() == 1);

        radixTrie4.remove(IP4_BYTES4, 24);
        radixTrie4.remove(IP4_BYTES7, 32);
        res = radixTrie4.lookupBest(IP4_BYTES7, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES6));
        assertTrue(res.prefixLength() == 25);
        assertTrue(res.data() == 6);

        radixTrie4.removeAll();
        assertTrue(radixTrie4.getRoot() == null);
    }

    /**
     * Tests v4 inserts that overlap on byte edge.
     */
    @Test
    public void testIp4ByteBoundary() {
        radixTrie4 = new RadixTrie<>(32);

        radixTrie4.insert(IP4_BYTES13, 17, 1);
        radixTrie4.insert(IP4_BYTES14, 19, 1);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupWidestNegative(IP4_BYTES15, 32);
        assertTrue(Arrays.equals(res.prefix, IP4_BYTES16));
        assertTrue(res.prefixLength() == 18);

        radixTrie4.removeAll();
    }

    /**
     * Tests iterator.
     */
    @Test
    public void testIterator() {
        RadixTrie<Integer>.TrieNode res;

        addIp4Addresses(radixTrie4);

        radixTrie4.remove(IP4_BYTES5, 24);
        radixTrie4.remove(IP4_BYTES4, 24);
        radixTrie4.insert(IP4_BYTES5, 24, 1);
        radixTrie4.insert(IP4_BYTES6, 24, 1);

        Iterator<RadixTrie<Integer>.TrieNode> it = radixTrie4.getRoot().iterator();

        int iterator = 0;
        while (it.hasNext()) {
            res = it.next();
            assertTrue(Arrays.equals(res.prefix(), itPrefixList4.get(iterator)));
            assertTrue(res.prefixLength() == itPreflenList4.get(iterator));
            iterator++;
        }
    }

    /**
     * Tests v4 CRD operations with 0/0 as root.
     */
    @Test
    public void testIp4ZeroRoot() {
        radixTrie4 = new RadixTrie<>(32, true);

        RadixTrie<Integer>.TrieNode res;

        addIp4Addresses(radixTrie4);

        res = radixTrie4.lookupBest(IP4_BYTES7, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES7));
        assertTrue(res.prefixLength() == 32);

        res = radixTrie4.lookupBest(IP4_BYTES5, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES5));
        assertTrue(res.prefixLength() == 24);

        radixTrie4.remove(IP4_BYTES5, 24);
        res = radixTrie4.lookupBest(IP4_BYTES5, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES1));
        assertTrue(res.prefixLength() == 16);

        radixTrie4.remove(IP4_BYTES4, 24);
        radixTrie4.remove(IP4_BYTES7, 32);
        res = radixTrie4.lookupBest(IP4_BYTES7, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES6));
        assertTrue(res.prefixLength() == 25);

        res = radixTrie4.lookupBest(IP4_BYTES8, 32);
        assertTrue(res == null);

        radixTrie4.insert(IP4_DEFAULT, 0, 0);
        res = radixTrie4.lookupBest(IP4_BYTES8, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_DEFAULT));

        radixTrie4.removeAll();
        assertTrue(!Arrays.equals(radixTrie4.getRoot().prefix(), IP4_DEFAULT));
        assertTrue(radixTrie4.getRoot().bit == 0);
    }

    /**
     * Tests v4 parent lookup.
     */
    @Test
    public void testIp4ParentLookup() {
        radixTrie4 = new RadixTrie<>(32, true);

        addIp4Addresses(radixTrie4);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupParent(IP4_BYTES7, 32);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES6));
        assertEquals(res.bit, 25);

        res = radixTrie4.lookupParent(IP4_BYTES6, 25);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES4));
        assertEquals(res.bit, 24);

        res = radixTrie4.lookupParent(IP4_BYTES4, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES1));
        assertEquals(res.bit, 16);
    }

    /**
     * Tests v4 sibling lookup.
     */
    @Test
    public void testIp4SiblingLookup() {
        radixTrie4 = new RadixTrie<>(32, true);

        addIp4Addresses(radixTrie4);

        radixTrie4.insert(IP4_BYTES1, 24, 8);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupSibling(IP4_BYTES3, 16);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES1));
        assertEquals(res.bit, 16);

        res = radixTrie4.lookupSibling(IP4_BYTES6, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES1));
        assertEquals(res.bit, 24);
    }

    /**
     * Tests v4 virtual parent sibling lookup.
     */
    @Test
    public void testIp4VirtualParentSiblingLookup() {
        radixTrie4 = new RadixTrie<>(32, true);

        addIp4Addresses(radixTrie4);

        radixTrie4.insert(IP4_BYTES1, 24, 8);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupVirtualParentSibling(IP4_BYTES1, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES5));
        assertEquals(res.bit, 24);

        res = radixTrie4.lookupVirtualParentSibling(IP4_BYTES5, 24);
        assertNull(res);
    }

    /**
     * Tests v4 widest negative prefix.
     */
    @Test
    public void testIp4WidestNegativePrefix() {
        radixTrie4 = new RadixTrie<>(32, true);

        addIp4Addresses(radixTrie4);

        radixTrie4.remove(IP4_BYTES5, 24);
        radixTrie4.remove(IP4_BYTES4, 24);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupWidestNegative(IP4_BYTES9, 24);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES11));
        assertTrue(res.prefixLength() == 23);
        res = radixTrie4.lookupWidestNegative(IP4_BYTES10, 16);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES12));
        assertTrue(res.prefixLength() == 2);
    }

    /**
     * Tests v6 CRD operations.
     * It just makes sure v6 prefix lengths don't generate problems. Therefore it's not as thorough as v4.
     */
    @Test
    public void testIp6() {
        RadixTrie<Integer>.TrieNode res;
        radixTrie6 = new RadixTrie<>(128);

        addIp6Addresses(radixTrie6);

        res = radixTrie6.lookupBest(IP6_BYTES7, 128);
        assertTrue(Arrays.equals(res.prefix(), IP6_BYTES4));
        assertTrue(res.prefixLength() == 113);

        res = radixTrie6.lookupBest(IP6_BYTES5, 112);
        assertTrue(Arrays.equals(res.prefix(), IP6_BYTES5));
        assertTrue(res.prefixLength() == 112);

        radixTrie6.remove(IP6_BYTES5, 112);
        res = radixTrie6.lookupBest(IP6_BYTES5, 112);
        assertTrue(Arrays.equals(res.prefix(), IP6_BYTES1));
        assertTrue(res.prefixLength() == 32);

        radixTrie6.remove(IP6_BYTES4, 112);
        res = radixTrie6.lookupBest(IP6_BYTES7, 128);
        assertTrue(Arrays.equals(res.prefix(), IP6_BYTES6));
        assertTrue(res.prefixLength() == 113);

        radixTrie6.removeAll();
        assertTrue(radixTrie6.getRoot() == null);
    }

    /**
     * Tests that parent insertions are done correctly.
     */
    @Test
    public void testParentInsertion() {
        radixTrie4 = new RadixTrie<>(32);

        radixTrie4.insert(IP4_BYTES17, 30, 1);
        radixTrie4.insert(IP4_BYTES18, 11, 2);

        RadixTrie<Integer>.TrieNode res = radixTrie4.lookupBest(IP4_BYTES17, 30);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES17));

        radixTrie4.insert(IP4_BYTES19, 28, 3);
        radixTrie4.insert(IP4_BYTES20, 14, 3);
        radixTrie4.insert(IP4_BYTES21, 8, 3);

        res = radixTrie4.lookupBest(IP4_BYTES19, 28);
        assertTrue(Arrays.equals(res.prefix(), IP4_BYTES19));
    }

    /**
     * Test {@link RadixTrie#removeAll}.
     */
    @Test
    public void testRemoveAll() {
        radixTrie4 = new RadixTrie<>(32, true);
        addIp4Addresses(radixTrie4);

        radixTrie4.removeAll();
        Assert.assertEquals(0, radixTrie4.getSize());
    }

    private void addIp4Addresses(RadixTrie<Integer> trie) {
        trie.insert(IP4_BYTES7, 32, 7);
        trie.insert(IP4_BYTES6, 25, 6);
        trie.insert(IP4_BYTES5, 24, 5);
        trie.insert(IP4_BYTES4, 24, 4);
        trie.insert(IP4_BYTES3, 16, 3);
        trie.insert(IP4_BYTES2, 16, 2);
        trie.insert(IP4_BYTES1, 16, 1);
    }

    private void addIp6Addresses(RadixTrie<Integer> trie) {
        trie.insert(IP6_BYTES1, 32, 1);
        trie.insert(IP6_BYTES2, 32, 1);
        trie.insert(IP6_BYTES3, 8, 1);
        trie.insert(IP6_BYTES4, 112, 1);
        trie.insert(IP6_BYTES5, 112, 1);
        trie.insert(IP6_BYTES6, 113, 1);
    }

    @Test
    public void testClearIntermediariesToRoot() {
        RadixTrie<String> radixTrie4 = new RadixTrie<>(32);

        // Add prefixes
        radixTrie4.insert(IP4_BYTES22, 32, "1.1.1.1");
        radixTrie4.insert(IP4_BYTES23, 32, "2.2.2.2");
        radixTrie4.insert(IP4_BYTES24, 32, "10.10.10.10");
        radixTrie4.insert(IP4_BYTES25, 32, "20.20.20.20");

        // Remove 10.10.10.10 & 20.20.20.20 (root should be updated)
        radixTrie4.remove(IP4_BYTES24, 32);
        radixTrie4.remove(IP4_BYTES25, 32);

        // Re-add 10.10.10.10 & 20.20.20.20
        radixTrie4.insert(IP4_BYTES24, 32, "10.10.10.10");
        radixTrie4.insert(IP4_BYTES25, 32, "20.20.20.20");

        // Make sure 10.10.10.10 & 20.20.20.20 lookup succeeds
        RadixTrie<String>.TrieNode res2 = radixTrie4.lookupBest(IP4_BYTES24, 32);
        assertTrue("Lookup 10.10.10.10 result is NULL", res2 != null);
        assertTrue("Lookup Result for 10.10.10.10 is not right", res2.data().equals("10.10.10.10"));
        RadixTrie<String>.TrieNode res3 = radixTrie4.lookupBest(IP4_BYTES25, 32);
        assertTrue("Lookup 20.20.20.20 result is NULL", res3 != null);
        assertTrue("Lookup Result for 20.20.20.20 is not right", res3.data().equals("20.20.20.20"));

        // Remove 10.10.10.10 & 20.20.20.20 (root should be updated)
        radixTrie4.remove(IP4_BYTES24, 32);
        radixTrie4.remove(IP4_BYTES25, 32);

        // Add 10.10.10.0 & 20.20.20.0
        radixTrie4.insert(IP4_BYTES25, 24, "20.20.20.0");
        radixTrie4.insert(IP4_BYTES24, 24, "10.10.10.0");

        RadixTrie<String>.TrieNode res4 = radixTrie4.lookupBest(IP4_BYTES23, 32);
        assertTrue("Lookup 2.2.2.2 result is NULL", res4 != null);
        assertTrue("Lookup Result for 2.2.2.2 is not right", res4.data().equals("2.2.2.2"));
        RadixTrie<String>.TrieNode res5 = radixTrie4.lookupBest(IP4_BYTES22, 32);
        assertTrue("Lookup 1.1.1.1 result is NULL", res5 != null);
        assertTrue("Lookup Result for 1.1.1.1 is not right", res5.data().equals("1.1.1.1"));

        RadixTrie<String>.TrieNode res6 = radixTrie4.lookupBest(IP4_BYTES25, 24);
        assertTrue("Lookup 20.20.20.20 result is NULL", res6 != null);
        assertTrue("Lookup Result for 20.20.20.0 is not right", res6.data().equals("20.20.20.0"));
        RadixTrie<String>.TrieNode res7 = radixTrie4.lookupBest(IP4_BYTES24, 24);
        assertTrue("Lookup 10.10.10.10 result is NULL", res7 != null);
        assertTrue("Lookup Result for 10.10.10.0 is not right", res7.data().equals("10.10.10.0"));
    }

    @Test
    public void testLookupExact() {
        RadixTrie<String> radixTrie4 = new RadixTrie<>(32);

        radixTrie4.insert(IP4_BYTES1, 16, "192.168.0.0");

        RadixTrie<String>.TrieNode res1 = radixTrie4.lookupExact(IP4_BYTES4, 24);
        assertTrue("Lookup for 192.168.1.0 is not NULL", res1 == null);

        RadixTrie<String>.TrieNode res2 = radixTrie4.lookupExact(IP4_BYTES1, 16);
        assertTrue("Lookup for 192.168.0.0 is NULL", res2 != null);
    }
}
