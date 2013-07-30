/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.serializer.network;

public interface PacketHeader {

    interface Length {
        int LISP_ENCAPSULATION = 4;
        int IPV4 = 20;
        int IPV6_NO_EXT = 40;
        int UDP = 8;
        int LISP_ENCAPSULATION_TOTAL = LISP_ENCAPSULATION + IPV4 + UDP;
    }
}
