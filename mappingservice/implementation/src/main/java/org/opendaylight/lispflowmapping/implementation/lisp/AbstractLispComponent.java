/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;


import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLispComponent {
    public static final Logger LOG = LoggerFactory.getLogger(AbstractLispComponent.class);

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
}
