/*
 * Copyright (c) 2014, 2017 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord.Action;

public interface LispMessage {
    int PORT_NUM = 4342;
    int XTR_PORT_NUM = 4343;
    Action NEGATIVE_MAPPING_ACTION = Action.NativelyForward;

    interface Pos {
        int TYPE = 0;

    }
}
