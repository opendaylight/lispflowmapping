/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

public interface LispMessage {
    int PORT_NUM = 4342;
    int XTR_PORT_NUM = 4343;

    interface Pos {
        int TYPE = 0;

    }
}
