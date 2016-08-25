package org.opendaylight.lispflowmapping.implementation.smr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmrNonce {
    protected static final Logger LOG = LoggerFactory.getLogger(SmrNonce.class);

    private long nonce;

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
