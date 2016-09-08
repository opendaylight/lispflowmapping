/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.interfaces.dao.SmrEvent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;

/**
 * This interface is used to notify of a received SMR-invoked request.
 */
public interface ISmrNotify {

    /**
     * This method is fired when a new smr-invoked request is received.
     *
     * @param event This object carries the nonce of a smr-invoked request and the subscriber's {@link Address}
     *              that sent this request.
     */
    void onSmrInvokedReceived(SmrEvent event);
}
