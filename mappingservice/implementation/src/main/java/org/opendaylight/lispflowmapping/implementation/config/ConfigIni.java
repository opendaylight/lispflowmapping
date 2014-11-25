/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.config;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigIni {
    protected static final Logger logger = LoggerFactory.getLogger(ConfigIni.class);
    private boolean mappingOverwrite;
    private boolean smr;

    private static final String LISP_MAPPINGOVERWRITE = "lisp.mappingOverwrite";
    private static final String LISP_SMR = "lisp.smr";

    public ConfigIni() {
        Bundle b = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = null;
        if (b != null) {
            context = b.getBundleContext();
        }

        initMappingOverwrite(context);
        initSmr(context);
    }

    private void initMappingOverwrite(BundleContext context) {
        // set the default value first
        this.mappingOverwrite = true;

        String str = null;

        if (context != null)
            str = context.getProperty(LISP_MAPPINGOVERWRITE);

        if (str == null) {
            str = System.getProperty(LISP_MAPPINGOVERWRITE);
            if (str == null) {
                logger.debug("Configuration variable '{}' is unset. Setting to default value: true", LISP_MAPPINGOVERWRITE);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("false")) {
            this.mappingOverwrite = false;
            logger.debug("Setting configuration variable '{}' to false", LISP_MAPPINGOVERWRITE);
        } else {
            logger.debug("Setting configuration variable '{}' to true", LISP_MAPPINGOVERWRITE);
        }
    }

    private void initSmr(BundleContext context) {
        // set the default value first
        this.smr = false;

        String str = null;

        if (context != null)
            str = context.getProperty(LISP_SMR);

        if (str == null) {
            str = System.getProperty(LISP_SMR);
            if (str == null) {
                logger.debug("Configuration variable '{}' is unset. Setting to default value: false", LISP_SMR);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("true")) {
            this.smr = true;
            logger.debug("Setting configuration variable '{}' to true", LISP_SMR);
        } else {
            logger.debug("Setting configuration variable '{}' to false", LISP_SMR);
        }
    }

    public boolean mappingOverwriteIsSet() {
        return mappingOverwrite;
    }

    public boolean smrIsSet() {
        return smr;
    }
}
