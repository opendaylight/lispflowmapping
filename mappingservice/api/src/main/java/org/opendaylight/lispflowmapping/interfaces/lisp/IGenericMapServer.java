/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * Methods to be implemented by a generic Map Server.
 */
public interface IGenericMapServer {

    /**
     * Configure Map Server to notify mapping subscribers on mapping updates.
     *
     * @param subscriptionService
     *            Set subscription service
     */
    void setSubscriptionService(boolean subscriptionService);
}
