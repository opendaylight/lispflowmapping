package org.opendaylight.lispflowmapping.config.yang;

import java.io.Closeable;
import java.io.IOException;

import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class LispConfigContextSetterImpl implements Closeable {

    public void updateContext(LispConfigModule module) {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        IConfigLispPlugin service = (IConfigLispPlugin) bundleContext
                .getService(bundleContext.getServiceReference(IConfigLispPlugin.class.getName()));
        service.setLispAddress(module.getBindAddress());
    }

    @Override
    public void close() throws IOException {
    }
}
