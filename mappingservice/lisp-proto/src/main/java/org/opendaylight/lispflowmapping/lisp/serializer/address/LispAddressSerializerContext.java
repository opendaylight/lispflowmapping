/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.serializer.address;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Class to pass around (de)serialization context information.
 *
 * @author Lorand Jakab
 *
 */
public class LispAddressSerializerContext {
    public static final Uint8 MASK_LEN_MISSING = Uint8.MAX_VALUE;
    private InstanceIdType vni;
    private Uint8 maskLen;

    public LispAddressSerializerContext(InstanceIdType vni) {
        this(vni, MASK_LEN_MISSING);
    }

    public LispAddressSerializerContext(Uint8 maskLen) {
        this(null, maskLen);
    }

    public LispAddressSerializerContext(InstanceIdType vni, Uint8 maskLength) {
        this.vni = vni;
        this.maskLen = maskLength;
    }

    InstanceIdType getVni() {
        return vni;
    }

    void setVni(InstanceIdType vni) {
        this.vni = vni;
    }

    Uint8 getMaskLen() {
        return maskLen;
    }

    void setMaskLen(Uint8 maskLen) {
        this.maskLen = maskLen;
    }
}
