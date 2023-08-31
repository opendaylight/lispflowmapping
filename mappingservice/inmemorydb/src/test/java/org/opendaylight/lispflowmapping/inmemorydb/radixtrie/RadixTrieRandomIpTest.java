/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.inmemorydb.radixtrie;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class RadixTrieRandomIpTest {
    private static final int IPV4_COUNT = 5000;
    private static final int IPV6_COUNT = 5000;
    private static final long SEED = 10L;

    final List<EIDEntry> ipv4Pref = new ArrayList<>();
    final List<EIDEntry> ipv6Pref = new ArrayList<>();

    private static RadixTrie<Integer> radixTrie4;
    private static RadixTrie<Integer> radixTrie6;

    private static final int IPV4 = 4;
    private static final int IPV6 = 6;

    private static final class EIDEntry {
        InetAddress eid;
        int preflen;
    }

    @Test
    public void testRandomMultipleRuns() throws UnknownHostException {
        testRandomIpv4();
        testRandomIpv6();
    }

    private void testRandomIpv4() throws UnknownHostException {
        RadixTrie<Integer>.TrieNode res;
        radixTrie4 = new RadixTrie<>(32);

        // fill the list of EIDEntries
        generateRandomIPAddress(IPV4);

        // insert the randomly genrated EIDs into the trie
        for (EIDEntry eid : ipv4Pref) {
            radixTrie4.insert(eid.eid.getAddress(), eid.preflen, 1);
        }

        for (EIDEntry eid : ipv4Pref) {
            res = radixTrie4.lookupBest(eid.eid.getAddress(), eid.preflen);
            if (res == null) {
                Assert.fail("Result is null");
            }
        }
    }

    private void testRandomIpv6() throws UnknownHostException {
        RadixTrie<Integer>.TrieNode res;
        radixTrie6 = new RadixTrie<>(128);

        // fill the list of EIDEntries
        generateRandomIPAddress(IPV6);

        // insert the randomly genrated EIDs into the trie
        for (EIDEntry eid : ipv6Pref) {
            radixTrie6.insert(eid.eid.getAddress(), eid.preflen, 1);
        }

        for (EIDEntry eid : ipv6Pref) {
            res = radixTrie6.lookupBest(eid.eid.getAddress(), eid.preflen);
            if (res == null) {
                Assert.fail("Result is null");
            }
        }
    }

    private void generateRandomIPAddress(final int version) throws UnknownHostException {
        final Random random = new Random(SEED);
        int preflen;
        final ByteBuffer b = ByteBuffer.allocate(4);
        byte[] result;
        int mask;

        EIDEntry eid;

        switch (version) {
            case IPV4:
                int word;

                for (int i = 0; i < IPV4_COUNT; i++) {
                    preflen = random.nextInt(32);
                    if (preflen < 8) {
                        preflen = 8;
                    }
                    mask = 0xffffffff;
                    for (int j = 0; j < 32 - preflen; j++) {
                        mask = mask << 1 ;
                    }

                    word = random.nextInt();
                    word = word & mask;
                    b.putInt(word);
                    result = b.array();
                    eid = new EIDEntry();
                    b.clear();
                    eid.eid = InetAddress.getByAddress(result);
                    eid.preflen = preflen;
                    ipv4Pref.add(eid);
                }
                break;

            case IPV6:
                for (int i = 0; i < IPV6_COUNT; i++) {
                    final BigInteger v6 = new BigInteger(127, random);
                    preflen = random.nextInt(127);
                    if (preflen < 64) {
                        preflen = 64;
                    }
                    eid = new EIDEntry();

                    result = v6.toByteArray();

                    if (result.length == 16) {
                        eid.eid = InetAddress.getByAddress(result);
                        eid.preflen = preflen;
                        ipv6Pref.add(eid);
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
