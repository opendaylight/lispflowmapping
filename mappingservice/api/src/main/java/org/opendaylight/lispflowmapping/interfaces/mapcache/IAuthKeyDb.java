/*
 * Copyright (c) 2016 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.interfaces.mapcache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;

/**
 * Authentication key database interface.
 *
 * @author Lorand Jakab
 *
 */

public interface IAuthKeyDb {
    /**
     * Add authentication key.
     *
     * @param key
     *            The key for which the authentication key is added
     * @param authKey
     *            The authentication key
     */
    void addAuthenticationKey(Eid key, MappingAuthkey authKey);

    /**
     * Retrieve authentication key.
     *
     * @param key
     *            The key for which the authentication key is being looked up.
     * @return The authentication key.
     */
    MappingAuthkey getAuthenticationKey(Eid key);

    /**
     * Remove authentication key.
     *
     * @param key
     *            Key for which the authentication key should be removed.
     */
    void removeAuthenticationKey(Eid key);

    /**
     * Print authentication keys in database. Used for testing, debugging and the karaf shell.
     *
     * @return a String consisting of all the authentication keys in the database
     */
    String printKeys();

    /**
     * Print keys in cache in a human friendly format.
     *
     * @return a String consisting of all the keys in the cache
     */
    String prettyPrintKeys();
}
