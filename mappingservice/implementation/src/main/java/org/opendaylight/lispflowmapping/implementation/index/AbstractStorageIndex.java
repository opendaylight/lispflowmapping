/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.index;

/**
 * @author Lorand Jakab
 *
 */
public abstract class AbstractStorageIndex implements StorageIndex {

    private String key;

    @Override
    public String getKey() {
        return this.key;
    }
}
