/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.config;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 4/19/17.
 */
public class ConfigurationService implements ManagedService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationService.class);

    public static CountDownLatch confirugationRendered = new CountDownLatch(0);

    HashMap<String, Consumer> configMethods;

    public ConfigurationService() {
        confirugationRendered = new CountDownLatch(1);

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_PID, "org.opendaylight.lispflowmapping.startup");
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = null;
        if (bundle != null) {
            context = bundle.getBundleContext();
        }

        // this method needs to be called before context.registerService() method
        mapConfigMethods();

        context.registerService(ManagedService.class.getName(), this, properties);
        LOG.info("Registered!");
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        Enumeration keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            if (configMethods.containsKey(key)) {
                configMethods.get(key).accept(dictionary.get(key));
            } else {
                LOG.debug("Configuration {} = {} being ignored because no consumer for this "
                        + "configuration key has been mapped", keys, dictionary.get(key));
            }
        }
        confirugationRendered.countDown();
    }

    private void mapConfigMethods() {
        configMethods = ConfigIni.getInstance().provideConfigToConsumerMapper();
    }
}
