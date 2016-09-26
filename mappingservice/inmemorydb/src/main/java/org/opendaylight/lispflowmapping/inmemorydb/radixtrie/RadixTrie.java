/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.inmemorydb.radixtrie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Radix trie/tree (also known as Patricia tree) implementation. Supports CRD operations for
 * generic, big endian (network byte order) prefixes. It can do exact and longest prefix matching,
 * post order iteration over the entries in the tree and can lookup widest negative prefixes (i.e.,
 * shortest overlapping prefix not registered in the tree).
 *
 * @author Florin Coras
 *
 * @param <T> Data type stored in each tree node
 */
public class RadixTrie<T> {
    private int maxBits;
    private TrieNode root;
    private long nbActiveNodes;
    private boolean rootZero;

    /**
     * RadixTrie constructor.
     *
     * @param bits Maximum prefix length supported.
     */
    public RadixTrie(int bits) {
        maxBits = bits;
        root = null;
        nbActiveNodes = 0;
    }

    /**
     * Radix trie constructors.
     *
     * @param bits Maximum prefix length supported
     * @param rootZero Flag that decides if 0/0 should be inserted as a non-prefix root node or not
     */
    public RadixTrie(int bits, boolean rootZero) {
        maxBits = bits;
        root = rootZero ? new TrieNode(null, 0, null) : null;
        nbActiveNodes = 0;
        this.rootZero = rootZero;
    }

    public TrieNode getRoot() {
        return root;
    }

    private void setRoot(TrieNode root) {
        this.root = root;
    }

    public int getMaxbits() {
        return maxBits;
    }

    public long getSize() {
        return nbActiveNodes;
    }

    /**
     * Test bit in byte. Assumes bits are numbered from 0 to 7
     * @param byteArg byte
     * @param bitPosition the position to be tested
     * @return 1 if bit at position is 1, 0 otherwise.
     */
    private boolean testBitInByte(byte byteArg, int bitPosition) {
        return (byteArg & (0x80 >> bitPosition)) != 0;
    }

    private boolean testBitInPrefixByte(byte[] prefix, int bitPosition) {
        return testBitInByte(prefix[bitPosition / 8], bitPosition & 0x07);
    }

    /**
     * Mask prefix.
     *
     * @param prefix Prefix to be masked
     * @param prefLen Prefix length
     * @return Prefix after masking
     */
    private byte [] maskPrefix(byte[] prefix, int prefLen) {
        byte[] res = prefix.clone();
        res[prefLen / 8] = (byte) (res[prefLen / 8] & (0xFF << (7 - (prefLen & 0x07))));
        for (int i = prefLen / 8 + 1; i < maxBits / 8; i++) {
            res[i] = 0;
        }
        return res;
    }

    /**
     * Insert prefix-data tuple into radix trie.
     *
     * @param prefix Big endian (network order) byte array representation of the prefix
     * @param preflen Prefix length
     * @param data Data to be stored in the tree
     * @return Newly inserted TrieNode
     */
    public TrieNode insert(byte[] prefix, int preflen, T data) {
        if (preflen > maxBits) {
            return null;
        }

        // trie is empty
        if (root == null) {
            root = new TrieNode(prefix, preflen, data);
            return root;
        }

        // find closest prefix starting at ROOT
        TrieNode closest = root.findClosest(prefix, preflen);

        // find first different bit <= min(closestNode.preflen, preflen)
        int diffbit = closest.firstDifferentBit(prefix, preflen);

        // find the first node with bit less than diffbit
        TrieNode node = closest.parentWithBitLessThan(diffbit);

        // insert new prefix
        return node.insert(prefix, preflen, diffbit, data, closest.prefix());
    }

    /**
     * Longest prefix match of prefix/preflen.
     *
     * @param prefix Big endian byte array representation of the prefix to be looked up.
     * @param preflen Prefix length
     * @return Node with longest prefix match or null if nothing is found.
     */
    public TrieNode lookupBest(byte[] prefix, int preflen) {
        if (root == null || preflen > maxBits) {
            return null;
        }

        TrieNode node = root;
        ArrayList<TrieNode> candidates = new ArrayList<>();

        while (node != null && node.bit < preflen) {
            if (node.prefix != null) {
                candidates.add(node);
            }

            if (testBitInPrefixByte(prefix, node.bit)) {
                node = node.right;
            } else {
                node = node.left;
            }
        }

        if (node != null && node.prefix != null) {
            candidates.add(node);
        }

        ListIterator<TrieNode> it = candidates.listIterator(candidates.size());
        while (it.hasPrevious()) {
            node = it.previous();
            if (node.comparePrefix(prefix)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Lookup widest negative (i.e., overlapping but not present in trie) prefix for given prefix and prefix length.
     *
     * @param prefix Prefix looked up.
     * @param preflen Prefix length.
     * @return Node containing the widest negative prefix.
     */
    public TrieNode lookupWidestNegative(byte [] prefix, int preflen) {
        if (root == null || preflen > maxBits) {
            return null;
        }

        TrieNode node = root.findClosest(prefix, preflen);

        // not a negative match
        if (node.prefix != null && node.prefixLength() <= preflen && node.comparePrefix(prefix)) {
            return null;
        }
        int diffbit = node.firstDifferentBit(prefix, preflen);
        return new TrieNode(maskPrefix(prefix, diffbit), diffbit + 1, null);
    }

    /**
     * Exact prefix match of prefix/preflen.
     *
     * @param prefix Big endian byte array representation of the prefix to be looked up.
     * @param preflen Prefix length
     * @return Node with exact prefix match or null
     */
    public TrieNode lookupExact(byte[] prefix, int preflen) {
        if (root == null || preflen > maxBits) {
            return null;
        }

        TrieNode node = root.findClosest(prefix, preflen);

        // if no node is found or if node not a prefix or if mask is too long
        if (node == null || node.prefix == null || node.bit > preflen) {
            return null;
        }

        // check that we have exact match
        if (node.comparePrefix(prefix)) {
            return node;
        }

        return null;
    }


    /**
     * Remove prefix from radix trie.
     *
     * @param prefix Big endian byte array representation of the prefix to be removed.
     * @param preflen Prefix length.
     */
    public void remove(byte[] prefix, int preflen) {
        TrieNode node = lookupExact(prefix, preflen);
        if (node != null) {
            node.erase();
        }
    }

    /**
     * Remove node's subtree.
     *
     * @param node Node who's subtree is to be removed
     */
    public void removeSubtree(TrieNode node) {
        TrieNode itNode;
        Iterator<TrieNode> it = node.iterator();

        while (it.hasNext()) {
            itNode = it.next();
            itNode.erase();
        }
    }

    /**
     * Remove subtree for longest match.
     *
     * @param prefix Prefix to be looked up
     * @param preflen Prefix length
     */
    public void removeSubtree(byte[] prefix, int preflen) {
        TrieNode itNode = lookupBest(prefix, preflen);

        if (itNode == null) {
            return;
        }

        Iterator<TrieNode> it = itNode.iterator();

        while (it.hasNext()) {
            itNode = it.next();
            itNode.erase();
        }
    }

    /**
     * Remove all entries in the trie.
     */
    public void removeAll() {
        Iterator<TrieNode> it = root.iterator();
        while (it.hasNext()) {
            TrieNode node = it.next();
            node.erase();
        }
    }

    /**
     * Trie node definition.
     */
    public class TrieNode implements Iterable<TrieNode> {
        // since bits are counted from 0, bit and prefix length are equal
        int bit;
        byte[] prefix;
        TrieNode left;
        TrieNode right;
        TrieNode up;
        T data;

        TrieNode(byte[] prefix, int prefixlen, T data) {
            this.bit = prefixlen;
            this.prefix = prefix;
            this.data = data;
            left = right = null;
        }

        public byte[] prefix() {
            return prefix;
        }

        public int prefixLength() {
            return bit;
        }

        public T data() {
            return data;
        }

        /**
         * Finds closest prefix NOT the longest prefix match.
         *
         * @param prefix Searched prefix
         * @param preflen Searched prefix length
         * @return The node found
         */
        public TrieNode findClosest(byte[] prefix, int preflen) {
            TrieNode node = this;

            while (node.prefix == null || node.bit < preflen) {
                if (testBitInPrefixByte(prefix, node.bit)) {
                    if (node.right == null) {
                        break;
                    }
                    node = node.right;
                } else {
                    if (node.left == null) {
                        break;
                    }
                    node = node.left;
                }
            }
            return node;
        }


        private int toUnsignedInt(byte byteToConvert) {
            return 0xFF & byteToConvert;
        }

        /**
         * Compares prefix to node's prefix and returns position of first different bit.
         *
         * @param prefix Prefix to be compared.
         * @param preflen Prefix length.
         * @return Position of first different bit.
         */
        public int firstDifferentBit(byte[] prefix, int preflen) {
            int bitxor;

            int maxbit = (bit < preflen) ? bit : preflen;
            int diffbit = 0;
            for (int i = 0; i * 8 < maxbit; i++) {
                // if match, move to next byte
                if ((bitxor = ((toUnsignedInt(this.prefix[i])) ^ toUnsignedInt(prefix[i]))) == 0) {
                    diffbit = (i + 1) * 8;
                    continue;
                }
                // if not matched, find first diff bit (0 to 7)
                diffbit = i * 8 + Integer.numberOfLeadingZeros(bitxor) - 24;
                break;
            }

            diffbit = (diffbit > maxbit) ? maxbit : diffbit;
            return diffbit;
        }

        /**
         * Find parent with bit less than given value.
         *
         * @param bitlen Bit value
         * @return Parent with bit less than given value
         */
        public TrieNode parentWithBitLessThan(int bitlen) {
            TrieNode node = this;
            TrieNode parent = node.up;
            while (parent != null && parent.bit >= bitlen) {
                node = parent;
                parent = node.up;
            }
            return node;
        }

        /**
         * Inserts node in trie near this node with prefix that has the first bit difference at diffbit.
         *
         * @param pref Prefix to be inserted.
         * @param preflen Prefix length of the prefix to be inserted.
         * @param diffbit Bit index of the first different bit between prefix and current node
         * @param data Data to be stored together with the prefix
         * @return The trie node created or current node if it's an overwrite.
         */
        public TrieNode insert(byte[] pref, int preflen, int diffbit, T data, byte[] closest) {
            TrieNode parent;

            // same node, check if prefix needs saving
            if (diffbit == preflen && bit == preflen) {
                if (prefix != null) {
                    return this;
                }
                this.prefix = pref;
                this.data = data;
                nbActiveNodes++;
                return this;
            }

            TrieNode newNode = new TrieNode(pref, preflen, data);

            // node is more specific, add new prefix as parent
            if (preflen == diffbit) {
                if (prefix == null ? testBitInPrefixByte(closest, preflen) : testBitInPrefixByte(prefix, preflen)) {
                    newNode.right = this;
                } else {
                    newNode.left = this;
                }
                newNode.up = up;

                if (up == null) {
                    setRoot(newNode);
                } else if (this.equals(up.right)) {
                    up.right = newNode;
                } else {
                    up.left = newNode;
                }

                up = newNode;
            // new prefix is more specific than node, add as child
            } else if (bit == diffbit) {
                newNode.up = this;
                if (testBitInPrefixByte(pref, bit)) {
                    right = newNode;
                } else {
                    left = newNode;
                }
            // new prefix is a sibling of/on a different branch from node, add common parent
            } else {
                parent = new TrieNode(null, diffbit, null);
                parent.up = up;
                if (testBitInPrefixByte(pref, diffbit)) {
                    parent.right = newNode;
                    parent.left = this;
                } else {
                    parent.right = this;
                    parent.left = newNode;
                }
                newNode.up = parent;
                if (up == null) {
                    setRoot(parent);
                } else if (this.equals(up.right)) {
                    up.right = parent;
                } else {
                    up.left = parent;
                }
                up = parent;
            }
            nbActiveNodes++;
            return newNode;
        }

        /**
         * Erase node.
         */
        public void erase() {
            TrieNode cur = this;
            boolean isRoot = false;

            if (this.equals(root)) {
                isRoot = true;
            }

            if (prefix != null) {
                resetData();
            }

            // as long as one of the children is null and this is an intermediary node
            // link parent to child. If parent null, child becomes the root.
            while (cur != null && cur.prefix == null && (cur.left == null || cur.right == null)) {
                TrieNode parent = cur.up;
                TrieNode child = cur.left != null ? cur.left : cur.right;

                if (parent == null) {
                    setRoot(child);
                } else {
                    if (parent.left != null && parent.left.equals(cur)) {
                        parent.left = child;
                    } else {
                        parent.right = child;
                    }
                    if (child != null) {
                        child.up = parent;
                    }
                }

                cur.resetData();
                cur = parent;
            }

            if (isRoot) {
                setRoot((rootZero ? new TrieNode(null, 0, null) : null));
            }

            nbActiveNodes--;
        }

        /**
         * Clear node data.
         */
        public void resetData() {
            prefix = null;
            data = null;
        }

        /**
         * Compare node prefix with prefix.
         *
         * @param pref Prefix to be compared
         * @return True if prefixes are equal, false otherwise
         */
        public boolean comparePrefix(byte[] pref) {
            int iterator;
            int remainder;

            for (iterator = 0; iterator < bit / 8; iterator++) {
                if (prefix[iterator] != pref[iterator]) {
                    return false;
                }
            }
            if ((remainder = bit % 8) != 0) {
                int mask = (0xFF << remainder) & 0xFF;
                return ((prefix[iterator] & mask) == (pref[iterator] & mask));
            }
            return true;
        }

        /**
         * Helper method that converts prefix and prefix length to dotted decimal, string, representation.
         *
         * @return String representation of prefix.
         */
        public String asIpPrefix() {
            try {
                return InetAddress.getByAddress(prefix).getHostAddress() + "/" + bit;
            } catch (UnknownHostException e) {
                return "NaA/" + bit;
            }
        }

        /**
         * Retrieve iterator.
         */
        @Override
        public Iterator<RadixTrie<T>.TrieNode> iterator() {
            return new TriePostOrderIterator(this);
        }

        /**
         * Post order iterator implementation for prefix trie. It's safe to use it to remove nodes.
         */
        private class TriePostOrderIterator implements Iterator<RadixTrie<T>.TrieNode> {
            private TrieNode current;
            private TrieNode lastNodeVisited;
            private Deque<RadixTrie<T>.TrieNode> stack;

            TriePostOrderIterator(TrieNode node) {
                stack = new ArrayDeque<>();
                current = node;
                lastNodeVisited = null;
            }

            @Override
            public boolean hasNext() {
                TrieNode peekNode;
                TrieNode last = lastNodeVisited;
                if (current != null && (current.left != null || current.right != null)) {
                    return true;
                } else {
                    Iterator<TrieNode> it = stack.iterator();
                    while (it.hasNext()) {
                        peekNode = it.next();
                        if (peekNode.right != null && !peekNode.right.equals(last)) {
                            return true;
                        } else {
                            last = peekNode;
                            if (peekNode.prefix != null) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public RadixTrie<T>.TrieNode next() {
                TrieNode peekNode;
                TrieNode next = current;
                while (!stack.isEmpty() || next != null) {
                    if (next != null) {
                        stack.push(next);
                        next = next.left;
                    } else {
                        peekNode = stack.peek();
                        if (peekNode.right != null && !peekNode.right.equals(lastNodeVisited)) {
                            next = peekNode.right;
                        } else {
                            lastNodeVisited = stack.pop();
                            if (peekNode.prefix != null) {
                                current = null;
                                return peekNode;
                            }
                        }
                    }
                }
                return null;
            }
        }
    }
}
