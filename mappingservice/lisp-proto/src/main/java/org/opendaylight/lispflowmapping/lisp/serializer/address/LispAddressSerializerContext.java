/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;

/**
 * @author Lorand Jakab
 *
 */
public class LispAddressSerializerContext {
    public static final short MASK_LEN_MISSING = -1;
    private InstanceIdType vni;
    private short maskLen;

    public LispAddressSerializerContext(InstanceIdType vni) {
        this(vni, MASK_LEN_MISSING);
    }

    public LispAddressSerializerContext(short maskLen) {
        this(null, maskLen);
    }

    public LispAddressSerializerContext(InstanceIdType vni, short maskLength) {
        this.vni = vni;
        this.maskLen = maskLength;
    }

    InstanceIdType getVni() {
        return vni;
    }

    void setVni(InstanceIdType vni) {
        this.vni = vni;
    }

    short getMaskLen() {
        return maskLen;
    }

    void setMaskLen(short maskLen) {
        this.maskLen = maskLen;
    }
}
