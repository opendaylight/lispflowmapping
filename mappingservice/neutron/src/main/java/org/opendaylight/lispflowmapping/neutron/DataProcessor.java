package org.opendaylight.lispflowmapping.neutron;

import org.opendaylight.yangtools.yang.binding.DataObject;

public interface DataProcessor<T extends DataObject> {

    void create(T object);
    void update(T object);
    void delete(T object);
}
