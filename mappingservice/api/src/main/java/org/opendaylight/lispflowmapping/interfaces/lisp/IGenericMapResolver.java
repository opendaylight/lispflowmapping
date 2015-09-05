/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * Methods to be implemented by a generic Map Resolver
 */
public interface IGenericMapResolver {
    /**
     * Configure MapResolver to use authentication
     */
    void setShouldAuthenticate(boolean shouldAuthenticate);

    /**
     * Configure MapResolver to track mappings requesters
     */
    void setSubscriptionService(boolean smr);

    /**
     * Configure how ELPs should be returned in Map-Replies
     */
    void setElpPolicy(String elpPolicy);
}
