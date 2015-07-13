package org.opendaylight.controller.config.yang.config.lfm.ms.dao.inmemorydb;

import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;

public class InMemoryDbModule extends org.opendaylight.controller.config.yang.config.lfm.ms.dao.inmemorydb.AbstractInMemoryDbModule {
    public InMemoryDbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public InMemoryDbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lfm.ms.dao.inmemorydb.InMemoryDbModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final HashMapDb hashMapDb = new HashMapDb();
        hashMapDb.setRecordTimeOut(getRecordTimeout());
        return hashMapDb;
    }
}
