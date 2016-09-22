/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.util.container;

import java.util.HashMap;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;


/**
 * Created by sheikahm on 9/26/16.
 */
public class BucketElement {
    private HashMap<String, ILispDAO> element;

    public BucketElement() {
        this.element = new HashMap<>();
    }

    public void addNewElement(String key, ILispDAO value) {
        element.put(key, value);
    }

    public ILispDAO getAnElement(String key) {
        return element.get(key);
    }

    public void deleteAnElement(String key) {
        element.remove(key);
    }

    public HashMap<String, ILispDAO> getElement() {
        return element;
    }
}