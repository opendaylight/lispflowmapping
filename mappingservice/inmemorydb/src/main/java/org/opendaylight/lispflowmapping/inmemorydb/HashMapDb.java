/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.lispflowmapping.inmemorydb.radixtrie.RadixTrie;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6PrefixBinary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashMapDb implements ILispDAO, AutoCloseable {

    protected static final Logger LOG = LoggerFactory.getLogger(HashMapDb.class);
    private static final Object TABLES = (Object) "tables";
    private ConcurrentMap<Object, ConcurrentMap<String, Object>> data = new ConcurrentHashMap<>();

    // IPv4 and IPv6 radix tries used for longest prefix matching
    private RadixTrie<Object> ip4Trie = new RadixTrie<>(32, true);
    private RadixTrie<Object> ip6Trie = new RadixTrie<>(128, true);

    public void tryAddToIpTrie(Object key) {
        if (key instanceof Eid) {
            Eid eid = (Eid) key;
            if (eid.getAddress() instanceof Ipv4PrefixBinary) {
                Ipv4PrefixBinary prefix = (Ipv4PrefixBinary) eid.getAddress();
                ip4Trie.insert(prefix.getIpv4AddressBinary().getValue(), prefix.getIpv4MaskLength(), key);
            } else if (eid.getAddress() instanceof Ipv6PrefixBinary) {
                Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) eid.getAddress();
                ip6Trie.insert(prefix.getIpv6AddressBinary().getValue(), prefix.getIpv6MaskLength(), key);
            }
        }
    }

    @Override
    public void put(Object key, MappingEntry<?>... values) {
        if (!data.containsKey(key)) {
            data.put(key, new ConcurrentHashMap<String, Object>());
        }
        for (MappingEntry<?> entry : values) {
            tryAddToIpTrie(key);
            data.get(key).put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object getSpecific(Object key, String valueKey) {
        Map<String, Object> keyToValues = data.get(key);
        if (keyToValues == null) {
            return null;
        }
        return keyToValues.get(valueKey);
    }

    @Override
    public Map<String, Object> get(Object key) {
        return data.get(key);
    }

    private RadixTrie<Object>.TrieNode getNode(Eid eid) {
        if (eid.getAddress() instanceof Ipv4PrefixBinary) {
            Ipv4PrefixBinary prefix = (Ipv4PrefixBinary) eid.getAddress();
            return ip4Trie.lookupBest(prefix.getIpv4AddressBinary().getValue(), prefix.getIpv4MaskLength());
        } else if (eid.getAddress() instanceof Ipv6PrefixBinary) {
            Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) eid.getAddress();
            return ip6Trie.lookupBest(prefix.getIpv6AddressBinary().getValue(), prefix.getIpv6MaskLength());
        }
        return null;
    }

    @Override
    public Map<String, Object> getBest(Object key) {
        if (key instanceof Eid) {
            Eid eid = (Eid) key;
            RadixTrie<Object>.TrieNode node = getNode(eid);
            if (node == null) {
                return get(key);
            }
            return get(node.data());
        }
        return null;
    }

    @Override
    public SimpleImmutableEntry<Eid, Map<String, ?>> getBestPair(Object key) {
        Map<String, ?> data = null;

        if (key instanceof Eid) {
            Eid eid = (Eid) key;
            RadixTrie<Object>.TrieNode node = getNode(eid);
            if (node == null) {
                data = get(key);
                return (data == null) ? null : new SimpleImmutableEntry<>((Eid)key, data);
            }
            data = get(node.data());
            return (data == null) ? null : new SimpleImmutableEntry<>((Eid)node.data(), data);
        }
        return null;
    }

    @Override
    public void getAll(IRowVisitor visitor) {
        for (ConcurrentMap.Entry<Object, ConcurrentMap<String, Object>> keyEntry : data.entrySet()) {
            for (Map.Entry<String, Object> valueEntry : keyEntry.getValue().entrySet()) {
                visitor.visitRow(keyEntry.getKey(), valueEntry.getKey(), valueEntry.getValue());
            }
        }
    }

    @Override
    public Eid getWidestNegativePrefix(Eid key) {
        RadixTrie<Object>.TrieNode node = null;

        if (key.getAddress() instanceof Ipv4PrefixBinary) {
            Ipv4PrefixBinary prefix = (Ipv4PrefixBinary) key.getAddress();
            node = ip4Trie.lookupWidestNegative(prefix.getIpv4AddressBinary().getValue(), prefix.getIpv4MaskLength());
            if (node != null) {
                return LispAddressUtil.asIpv4PrefixBinaryEid(key, node.prefix(), (short) node.prefixLength());
            }
        } else if (key.getAddress() instanceof Ipv6PrefixBinary) {
            Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) key.getAddress();
            node = ip6Trie.lookupWidestNegative(prefix.getIpv6AddressBinary().getValue(), prefix.getIpv6MaskLength());
            if (node != null) {
                return LispAddressUtil.asIpv6PrefixBinaryEid(key, node.prefix(), (short) node.prefixLength());
            }
        }
        return null;
    }

    private void tryRemoveFromTrie(Object key) {
        if (key instanceof Eid) {
            Eid eid = (Eid) key;
            if (eid.getAddress() instanceof Ipv4PrefixBinary) {
                Ipv4PrefixBinary prefix = (Ipv4PrefixBinary) eid.getAddress();
                ip4Trie.remove(prefix.getIpv4AddressBinary().getValue(), prefix.getIpv4MaskLength());
            } else if (eid.getAddress() instanceof Ipv6PrefixBinary) {
                Ipv6PrefixBinary prefix = (Ipv6PrefixBinary) eid.getAddress();
                ip6Trie.remove(prefix.getIpv6AddressBinary().getValue(), prefix.getIpv6MaskLength());
            }
        }
    }

    @Override
    public void remove(Object key) {
        tryRemoveFromTrie(key);
        data.remove(key);
    }

    @Override
    public void removeSpecific(Object key, String valueKey) {
        if (data.containsKey(key) && data.get(key).containsKey(valueKey)) {
            data.get(key).remove(valueKey);
        }
    }

    @Override
    public void removeAll() {
        ip4Trie.removeAll();
        ip6Trie.removeAll();
        data.clear();
    }

    public void close() throws Exception {
        data.clear();
    }

    @Override
    public ILispDAO putNestedTable(Object key, String valueKey) {
        ILispDAO nestedTable = (ILispDAO) getSpecific(key, valueKey);
        if (nestedTable != null) {
            LOG.warn("Trying to add nested table that already exists. Aborting!");
            return nestedTable;
        }
        nestedTable = new HashMapDb();
        put(key, new MappingEntry<>(valueKey, nestedTable));
        return nestedTable;
    }

    @Override
    public ILispDAO putTable(String key) {
        ILispDAO table = (ILispDAO) getSpecific(TABLES, key);
        if (table != null) {
            LOG.warn("Trying to add table that already exists. Aborting!");
            return table;
        }
        table = new HashMapDb();
        put(TABLES, new MappingEntry<>(key, table));
        return table;
    }
}
