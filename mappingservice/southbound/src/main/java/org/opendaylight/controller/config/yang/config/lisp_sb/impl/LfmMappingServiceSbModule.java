package org.opendaylight.controller.config.yang.config.lisp_sb.impl;

import org.opendaylight.lispflowmapping.southbound.LispSouthboundPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfmMappingServiceSbModule extends org.opendaylight.controller.config.yang.config.lisp_sb.impl.AbstractLfmMappingServiceSbModule {
    private final static Logger LOG = LoggerFactory.getLogger(LfmMappingServiceSbModule.class);
    private LispSouthboundPlugin sbPlugin;

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LfmMappingServiceSbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.lisp_sb.impl.LfmMappingServiceSbModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("LfmMappingServiceSb Module up!");

        sbPlugin = new LispSouthboundPlugin();
        sbPlugin.setNotificationPublishService(getNotificationPublishServiceDependency());
        sbPlugin.setRpcRegistryDependency(getRpcRegistryDependency());
        sbPlugin.setBindingAwareBroker(getBrokerDependency());
        sbPlugin.init();

        return sbPlugin;
    }

}
