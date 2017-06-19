/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.dataprocessors;

import org.opendaylight.yangtools.yang.binding.DataObject;

public interface DataProcessor<T extends DataObject> {

    /**
     * This method creates a new DataObject.
     * @param objectAfter - the objectAfter of type DataObject that has changed
     */
    void create(T objectAfter);

    /**
     * This method updates a DataObject.
     * @param objectBefore - the object of type DataObject that was before the change
     * @param objectAfter - the object of type DataObject that is after the change
     */
    void update(T objectBefore, T objectAfter);

    /**
     * This method removes a DataObject.
     * @param objectBefore - the objectBefore of type DataObject that has changed
     */
    void delete(T objectBefore);
}
