/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.HashSet;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceSubscriberRLOC;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;

public abstract class AbstractLispComponent {

    public static final String PASSWORD_SUBKEY = "password";
    public static final String ADDRESS_SUBKEY = "address";
    public static final String SUBSCRIBERS_SUBKEY = "subscribers";

    protected ILispDAO dao;
    protected volatile boolean iterateMask;
    protected volatile boolean authenticate;

    public AbstractLispComponent(ILispDAO dao, boolean authenticate, boolean iterateMask) {
        this.dao = dao;
        this.iterateMask = iterateMask;
        this.authenticate = authenticate;
    }

    public boolean shouldIterateMask() {
        return iterateMask;
    }

    public void setShouldIterateMask(boolean shouldIterateMask) {
        this.iterateMask = shouldIterateMask;
    }

    public boolean shouldAuthenticate() {
        return authenticate;
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.authenticate = shouldAuthenticate;
    }

    protected String getPassword(LispAddressContainer prefix, int maskLength) {
        while (maskLength >= 0) {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
            Object password = dao.getSpecific(key, PASSWORD_SUBKEY);
            if (password != null && password instanceof String) {
                return (String) password;
            } else if (shouldIterateMask()) {
                maskLength -= 1;
            } else {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected HashSet<MappingServiceSubscriberRLOC> getSubscribers(LispAddressContainer prefix, int maskLength) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
        Object subscribers = dao.getSpecific(key, SUBSCRIBERS_SUBKEY);
        if (subscribers != null && subscribers instanceof HashSet<?>) {
            return (HashSet<MappingServiceSubscriberRLOC>) subscribers;
        }
        return null;
    }
}
