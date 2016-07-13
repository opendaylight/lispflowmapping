/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron;

import org.opendaylight.yangtools.yang.binding.DataObject;

public interface DataProcessor<T extends DataObject> {

    /**
     * This method creates a new DataObject.
     * @param object - the object of type DataObject that has changed
     */
    void create(T object);

    /**
     * This method updates a DataObject.
     * @param object - the object of type DataObject that has changed
     */
    void update(T object);

    /**
     * This method removes a DataObject.
     * @param object - the object of type DataObject that has changed
     */
    void delete(T object);
}
