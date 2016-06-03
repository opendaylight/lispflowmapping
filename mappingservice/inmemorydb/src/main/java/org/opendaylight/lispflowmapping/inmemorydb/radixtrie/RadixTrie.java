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
 * generic, big endian (network byte order) prefixes. It can do exact and longest prefix matchin,
 * post order iteration over the entries in the tree and can lookup widest negative prefixes (i.e.,
 * shortest overlapping prefix not registered in the tree).
 *
 * @author Florin Coras
 *
 * @param <T> Data type stored in each tree node
 */
public class RadixTrie<T> {
    private int MAXBITS;
    private TrieNode ROOT;
    private long nbActiveNodes;
    private boolean rootZero;

    /**
     * RadixTrie constructor
     *
     * @param bits Maximum prefix length supported.
     */
    public RadixTrie(int bits) {
        MAXBITS = bits;
        ROOT = null;
        nbActiveNodes = 0;
    }

    /**
     * Radix trie constructors
     * @param bits Maximum prefix length supported
     * @param rootZero Flag that decides if 0/0 should be inserted as a non-prefix root node or not
     */
    public RadixTrie(int bits, boolean rootZero) {
        MAXBITS = bits;
        ROOT = rootZero ? new TrieNode(null, 0, null) : null;
        nbActiveNodes = 0;
        this.rootZero = rootZero;
    }

    public TrieNode getRoot() {
        return ROOT;
    }

    private void setRoot(TrieNode root) {
        ROOT = root;
    }

    public int getMaxbits() {
        return MAXBITS;
    }

    public long getSize() {
        return nbActiveNodes;
    }

    /**
     * Test bit in byte. Assumes bits are numbered from 0 to 7
     * @param b byte
     * @param bitPosition the position to be tested
     * @return 1 if bit at position is 1, 0 otherwise.
     */
    private boolean testBitInByte(byte b, int bitPosition) {
        return (b & (0x80 >> bitPosition)) != 0;
    }

    private boolean testBitInPrefixByte(byte[] prefix, int bitPosition) {
        return testBitInByte(prefix[bitPosition / 8], bitPosition & 0x07);
    }

    /**
     * Mask prefix
     * @param prefix Prefix to be masked
     * @param prefLen Prefix length
     * @return Prefix after masking
     */
    private byte [] maskPrefix(byte[] prefix, int prefLen) {
        int i;
        byte[] res = prefix.clone();
        res[prefLen/8] = (byte) (res[prefLen/8] & (0xFF << (7 - (prefLen & 0x07))));
        for (i = prefLen/8 + 1; i < MAXBITS/8; i++) {
            res[i] = 0;
        }
        return res;
    }

    /**
     * Insert prefix-data tuple into radix trie
     * @param prefix Big endian (network order) byte array representation of the prefix
     * @param preflen Prefix length
     * @param data Data to be stored in the tree
     * @return Newly inserted TrieNode
     */
    public TrieNode insert(byte[] prefix, int preflen, T data) {
        TrieNode node;
        int diffbit;

        if (preflen > MAXBITS) {
            return null;
        }

        // trie is empty
        if (ROOT == null) {
            ROOT = new TrieNode(prefix, preflen, data);
            return ROOT;
        }

        // find closest prefix starting at ROOT
        node = ROOT.findClosest(prefix, preflen);

        // find first different bit
        diffbit = node.firstDifferentBit(prefix, preflen);

        // find the first node with bit less than diffbit
        node = node.parentWithBitLessThan(diffbit);

        // insert new prefix
        return node.insert(prefix, preflen, diffbit, data);
    }

    /**
     * Longest prefix match of prefix/preflen
     * @param prefix Big endian byte array representation of the prefix to be looked up.
     * @param preflen Prefix length
     * @return Node with longest prefix match or null if nothing is found.
     */
    public TrieNode lookupBest(byte[] prefix, int preflen) {
        TrieNode node;
        ArrayList<TrieNode> candidates;
        ListIterator<TrieNode> it;

        if (ROOT == null || preflen > MAXBITS) {
            return null;
        }

        node = ROOT;
        candidates = new ArrayList<TrieNode>();

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

        it = candidates.listIterator(candidates.size());
        while (it.hasPrevious()) {
            node = it.previous();
            if (node.comparePrefix(prefix)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Lookup widest negative (i.e., overlapping but not present in trie) prefix for given prefix and prefix length
     * @param prefix Prefix looked up.
     * @param preflen Prefix length.
     * @return Node containing the widest negative prefix.
     */
    public TrieNode lookupWidestNegative(byte [] prefix, int preflen) {
        TrieNode node;
        int diffbit;

        if (ROOT == null || preflen > MAXBITS) {
            return null;
        }

        node = ROOT.findClosest(prefix, preflen);

        // not a negative match
        if (node.prefix != null && node.prefixLength() <= preflen && node.comparePrefix(prefix)) {
            return null;
        }
        diffbit = node.firstDifferentBit(prefix, preflen);
        return new TrieNode(maskPrefix(prefix, diffbit), diffbit + 1, null);
    }

    /**
     * Exact prefix match of prefix/preflen
     * @param prefix Big endian byte array representation of the prefix to be looked up.
     * @param preflen Prefix length
     * @return Node with exact prefix match or null
     */
    public TrieNode lookupExact(byte[] prefix, int preflen) {
        TrieNode node;

        if (ROOT == null || preflen > MAXBITS) {
            return null;
        }

        node = ROOT.findClosest(prefix, preflen);

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
     * Remove prefix from radix trie
     * @param prefix Big endian byte array representation of the prefix to be removed.
     * @param preflen Prefix length.
     */
    public void remove(byte[] prefix, int preflen) {
        TrieNode node;
        node = lookupExact(prefix, preflen);
        if (node != null) {
            node.erase();
        }
    }

    /**
     * Remove node's subtree
     * @param node Node who's subtree is to be removed
     */
    public void removeSubtree(TrieNode node) {
        TrieNode itNode;
        Iterator<TrieNode> it;
        it = node.iterator();

        while (it.hasNext()) {
            itNode = it.next();
            itNode.erase();
        }
    }

    /**
     * Remove subtree for longest match
     * @param prefix Prefix to be looked up
     * @param preflen Prefix length
     */
    public void removeSubtree(byte[] prefix, int preflen) {
        TrieNode itNode;
        Iterator<TrieNode> it;

        itNode = lookupBest(prefix, preflen);

        if (itNode == null) {
            return;
        }

        it = itNode.iterator();

        while (it.hasNext()) {
            itNode = it.next();
            itNode.erase();
        }
    }

    /**
     * Remove all entries in the trie.
     */
    public void removeAll() {
        TrieNode node;
        Iterator<TrieNode> it;

        it = ROOT.iterator();
        while (it.hasNext()) {
            node = it.next();
            node.erase();
        }
    }

    /**
     * Trie node definition.
     *
     */
    public class TrieNode implements Iterable<TrieNode>{
        // since bits are counted from 0, bit and prefix length are equal
        int bit;
        byte[] prefix;
        TrieNode left, right;
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
         * Finds closest prefix NOT the longest prefix match
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

        /**
         * Compares prefix to node's prefix and returns position of first different bit
         * @param prefix Prefix to be compared.
         * @param preflen Prefix length.
         * @return Position of first different bit.
         */
        public int firstDifferentBit(byte[] prefix, int preflen) {
            int maxbit, diffbit, i;
            byte bitxor;

            maxbit = (bit < preflen) ? bit : preflen;
            diffbit = 0;
            for (i = 0; i * 8 < maxbit; i++) {
                // if match, move to next byte
                if ((bitxor = (byte) (this.prefix[i] ^ prefix[i])) == 0) {
                    diffbit = (i+1) * 8;
                    continue;
                }
                // if not matched, find first diff bit (0 to 7)
                diffbit = i * 8 + Integer.numberOfLeadingZeros(bitxor) - 24;
                diffbit = (diffbit > maxbit) ? maxbit : diffbit;
                break;
            }

            return diffbit;
        }

        /**
         * Find parent with bit less than given value
         * @param bitlen Bit value
         * @return Parent with bit less than given value
         */
        public TrieNode parentWithBitLessThan(int bitlen) {
            TrieNode node, parent;
            node = this;
            parent = node.up;
            while (parent != null && parent.bit >= bitlen) {
                node = parent;
                parent = node.up;
            }
            return node;
        }

        /**
         * Inserts node in trie near this node with prefix that has the first bit difference at diffbit
         * @param pref Prefix to be inserted.
         * @param preflen Prefix length of the prefix to be inserted.
         * @param diffbit Bit index of the first different bit between prefix and current node
         * @param data Data to be stored together with the prefix
         * @return The trie node created or current node if it's an overwrite.
         */
        public TrieNode insert(byte[] pref, int preflen, int diffbit, T data) {
            TrieNode parent, newNode;

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

            newNode = new TrieNode(pref, preflen, data);

            // node is more specific, add new prefix as parent
            if (preflen == diffbit) {
                if (testBitInPrefixByte(pref, preflen)) {
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
         * Erase node
         */
        public void erase () {
            TrieNode cur = this, child, parent;
            boolean isRoot = false;

            if (this.equals(ROOT)) {
                isRoot = true;
            }

            if (prefix != null) {
                resetData();
            }

            // as long as one of the children is null and this is an intermediary node
            // link parent to child. If parent null, child becomes the root.
            while (cur != null && cur.prefix == null && (cur.left == null || cur.right == null)) {
                parent = cur.up;
                child = cur.left != null ? cur.left : cur.right;

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
         * Clear node data
         */
        public void resetData () {
            prefix = null;
            data = null;
        }

        /**
         * Compare node prefix with prefix
         * @param pref Prefix to be compared
         * @return True if prefixes are equal, false otherwise
         */
        public boolean comparePrefix(byte[] pref) {
            int i, mask, r;

            for (i = 0; i < bit/8; i++) {
               if (prefix[i] != pref[i]) {
                   return false;
               }
            }
            if ((r = bit % 8) != 0) {
                mask = (0xFF << r) & 0xFF;
                return ((prefix[i] & mask) == (pref[i] & mask));
            }
            return true;
        }

        /**
         * Helper method that converts prefix and prefix length to dotted decimal, string, representation.
         * @return String representation of prefix.
         */
        public String asIpPrefix() {
            try {
                return InetAddress.getByAddress(prefix).getHostAddress() + "/" + bit;
            } catch (UnknownHostException e) {
                return "NaA/"+bit;
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
         * Post order iterator implementation for prefix trie. It's safe to use it to remove nodes
         *
         */
        private class TriePostOrderIterator implements Iterator<RadixTrie<T>.TrieNode> {
            private TrieNode current, lastNodeVisited;
            private Deque<RadixTrie<T>.TrieNode> stack;

            TriePostOrderIterator(TrieNode node) {
                stack = new ArrayDeque<RadixTrie<T>.TrieNode>();
                current = node;
                lastNodeVisited = null;
            }

            @Override
            public boolean hasNext() {
                TrieNode peekNode, last = lastNodeVisited;
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