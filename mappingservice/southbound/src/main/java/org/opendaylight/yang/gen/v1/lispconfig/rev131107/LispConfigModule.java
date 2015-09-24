package org.opendaylight.yang.gen.v1.lispconfig.rev131107;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.opendaylight.controller.config.api.JmxAttributeValidationException;

/**
* Actual state of lisp configuration.
*/
public class LispConfigModule extends org.opendaylight.yang.gen.v1.lispconfig.rev131107.AbstractLispConfigModule {
    public LispConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LispConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.lispconfig.rev131107.LispConfigModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        super.customValidation();
        try {
            Inet4Address.getByName(getBindAddress());
        } catch (UnknownHostException e) {
            throw new JmxAttributeValidationException("LISP bind address is not a valid ipv4 address: " + getBindAddress(), bindAddressJmxAttribute);
        }
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LispConfigContextSetterImpl setter = new LispConfigContextSetterImpl();
        setter.updateContext(this);
        return setter;
    }

}
