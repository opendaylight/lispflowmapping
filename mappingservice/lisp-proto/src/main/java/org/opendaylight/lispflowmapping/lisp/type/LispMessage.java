/*
 * Copyright (c) 2014, 2017 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.type;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;
import org.opendaylight.yangtools.yang.common.Uint16;

public interface LispMessage {
    int PORT_NUM = 4342;
    Uint16 PORT_NUMBER = Uint16.valueOf(PORT_NUM).intern();
    int XTR_PORT_NUM = 4343;
    Uint16 XTR_PORT_NUMBER = Uint16.valueOf(XTR_PORT_NUM).intern();
    Action NEGATIVE_MAPPING_ACTION = Action.NativelyForward;

    interface Pos {
        int TYPE = 0;

    }
}
