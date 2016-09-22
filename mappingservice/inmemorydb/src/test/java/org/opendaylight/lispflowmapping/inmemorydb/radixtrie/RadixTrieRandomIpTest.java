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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.fail;

public class RadixTrieRandomIpTest {
    private static final int numIpv4Pref = 1000;
    private static final int numIpv6Pref = 50;

    final List <EIDEntry> ipv4Pref = new ArrayList <EIDEntry>();
    final List <EIDEntry> ipv6Pref = new ArrayList <EIDEntry>();

    private static RadixTrie<Integer> radixTrie4;
    private static RadixTrie<Integer> radixTrie6;

    private static final int IPv4 = 4;
    private static final int IPv6 = 6;

    private static class EIDEntry {
        InetAddress EID;
        int preflen;
    }

    @Test
    public void testRandomMultipleRuns() throws UnknownHostException {
        for (int i = 0; i < 50; i++) {
            testRandomIpv4();
        }
    }

    private void testRandomIpv4() throws UnknownHostException {
        RadixTrie<Integer>.TrieNode res;
        radixTrie4 = new RadixTrie<Integer>(32);

        // fill the list of EIDEntries
        generateRandomIPAddress(IPv4);

        // insert the randomly genrated EIDs into the trie
        for (EIDEntry eid : ipv4Pref) {
            radixTrie4.insert(eid.EID.getAddress(), eid.preflen, 1);
        }

        for (EIDEntry eid : ipv4Pref) {
            res = radixTrie4.lookupBest(eid.EID.getAddress(), eid.preflen);
            if (res == null) {
                fail("Result is null");
            }
        }
    }

    private void generateRandomIPAddress (int version) throws UnknownHostException {
        final Random random = new Random();
        int preflen;
        final ByteBuffer b = ByteBuffer.allocate(4);
        byte[] result;
        int mask;

        EIDEntry eid;

        if (version == 4) {
            int word;

            for (int i = 0; i < numIpv4Pref; i++) {
                preflen = random.nextInt(32);
                if (preflen < 8) {
                    preflen = 8;
                }
                mask = 0xffffffff;
                for (int j=0; j<(32-preflen); j++) {
                    mask = mask << 1 ;
                }

                word = random.nextInt();
                word = word & mask;
                b.putInt(word);
                result = b.array();
                eid = new EIDEntry();
                b.clear();
                eid.EID = InetAddress.getByAddress(result);
                eid.preflen = preflen;
                ipv4Pref.add(eid);
            }
        } else {
            for (int i = 0; i < numIpv6Pref; i++) {
                BigInteger v6 = new BigInteger(127, random);
                preflen = random.nextInt(127);
                if (preflen < 64) {
                    preflen = 64;
                }
                eid = new EIDEntry();

                result = v6.toByteArray();

                if (result.length == 16) {
                    eid.EID = InetAddress.getByAddress(result);
                    eid.preflen = preflen;
                    ipv6Pref.add(eid);
                }
            }
        }
    }
}
