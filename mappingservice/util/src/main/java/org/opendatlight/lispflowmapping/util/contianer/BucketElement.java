/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendatlight.lispflowmapping.util.contianer;

import java.util.HashMap;

/**
 * Created by sheikahm on 9/26/16.
 */
public class BucketElement {
    private HashMap<String, MappingAllInfo> element;

    public BucketElement() {
        this.element = new HashMap<>();
    }

    public void addNewElement(String key, MappingAllInfo mappingAllInfo) {
        element.put(key, mappingAllInfo);
    }

    public MappingAllInfo getSpecificElement(String key) {
        return element.get(key);
    }

    public void deleteSpecificElement(String key) {
        element.remove(key);
    }

    public HashMap<String, MappingAllInfo> getElement() {
        return element;
    }
}