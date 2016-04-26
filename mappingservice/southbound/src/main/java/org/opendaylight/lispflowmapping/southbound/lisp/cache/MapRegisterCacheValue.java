/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.lisp.cache;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;

public interface MapRegisterCacheValue {

    byte[] getVal();

    boolean isWantMapNotifyBitSet();

    boolean isMergeBitSet();

    List<Eid> getEids();

    void setEids(List<Eid> eids);

}

