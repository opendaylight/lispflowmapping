/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class temporarily stores the nonce from Solicit Map Request
 */
public class SmrNonce {
    protected static final Logger LOG = LoggerFactory.getLogger(SmrNonce.class);

    private long nonce;

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void clear() {
        this.nonce = 0L;
    }
}
