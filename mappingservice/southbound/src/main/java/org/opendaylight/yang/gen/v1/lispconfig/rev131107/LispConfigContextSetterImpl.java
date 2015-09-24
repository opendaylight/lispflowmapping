/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.lispconfig.rev131107;

import java.io.Closeable;
import java.io.IOException;

import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispSouthboundPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class LispConfigContextSetterImpl implements Closeable {

    public void updateContext(LispConfigModule module) {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        IConfigLispSouthboundPlugin service = (IConfigLispSouthboundPlugin) bundleContext
                .getService(bundleContext.getServiceReference(IConfigLispSouthboundPlugin.class.getName()));
        service.setLispAddress(module.getBindAddress());
    }

    @Override
    public void close() throws IOException {
    }
}
