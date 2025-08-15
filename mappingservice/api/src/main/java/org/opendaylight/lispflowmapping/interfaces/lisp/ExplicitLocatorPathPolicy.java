/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * Enumeration of methods of how to build a Map-Reply southbound message from a mapping containing an Explicit Locator
 * Path (ELP) RLOC. It is used for compatibility with dataplane devices that don’t understand the ELP LCAF format.
 */
public enum ExplicitLocatorPathPolicy {
    /**
     * Do not alter the mapping, returning all RLOCs unmodified.
     */
    DEFAULT,
    /**
     * Add a new RLOC to the mapping, with a lower priority than the ELP, that is the next hop in the service chain. To
     * determine the next hop, it searches the source RLOC of the Map-Request in the ELP, and chooses the next hop,
     * if it exists, otherwise it chooses the first hop.
     */
    BOTH,
    /**
     * Add a new RLOC using the same algorithm as {@link #BOTH}, but using the origin priority of the ELP RLOC, which is
     * removed from the mapping.
     */
    REPLACE,
}
