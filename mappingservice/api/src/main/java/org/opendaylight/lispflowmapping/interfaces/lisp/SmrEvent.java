/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.inet.binary.types.rev160303.IpAddressBinary;

/**
 * Carries information about received SMR-invoked request.
 */
public class SmrEvent {

    private final List<IpAddressBinary> subscriberAddresses;

    public SmrEvent(List<IpAddressBinary> subscriberAddresses) {
        this.subscriberAddresses = subscriberAddresses;
    }

    public List<IpAddressBinary> getSubscriberAddressList() {
        return subscriberAddresses;
    }
}
