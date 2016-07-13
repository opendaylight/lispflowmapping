package org.opendaylight.lispflowmapping.neutron;

import org.opendaylight.yangtools.yang.binding.DataObject;

public interface DataProcessor<T extends DataObject> {

    /**
     * This method creates a new DataObject.
     * @param object
     */
    void create(T object);

    /**
     * This method updates a DataObject.
     * @param object
     */
    void update(T object);

    /**
     * This method removes a DataObject.
     * @param object
     */
    void delete(T object);
}
