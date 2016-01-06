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

public final class ConfigIni {
    protected static final Logger LOG = LoggerFactory.getLogger(ConfigIni.class);
    private boolean mappingOverwrite;
    private boolean smr;
    private String elpPolicy;

    private static final String LISP_MAPPING_OVERWRITE = "lisp.mappingOverwrite";
    private static final String LISP_SMR = "lisp.smr";
    private static final String LISP_ELP_POLICY = "lisp.elpPolicy";

    private static final ConfigIni INSTANCE = new ConfigIni();

    private ConfigIni() {
        Bundle b = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = null;
        if (b != null) {
            context = b.getBundleContext();
        }

        initMappingOverwrite(context);
        initSmr(context);
        initElpPolicy(context);
    }

    private void initMappingOverwrite(BundleContext context) {
        // set the default value first
        this.mappingOverwrite = true;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_MAPPING_OVERWRITE);
        }

        if (str == null) {
            str = System.getProperty(LISP_MAPPING_OVERWRITE);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'true'", LISP_MAPPING_OVERWRITE);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("false")) {
            this.mappingOverwrite = false;
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_MAPPING_OVERWRITE);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'true'", LISP_MAPPING_OVERWRITE);
        }
    }

    private void initSmr(BundleContext context) {
        // set the default value first
        this.smr = true;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_SMR);
        }

        if (str == null) {
            str = System.getProperty(LISP_SMR);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'true'", LISP_SMR);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("false")) {
            this.smr = false;
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_SMR);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'true'", LISP_SMR);
        }
    }

    private void initElpPolicy(BundleContext context) {
        // set the default value first
        this.elpPolicy = "default";

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_ELP_POLICY);
        }

        if (str == null) {
            str = System.getProperty(LISP_ELP_POLICY);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'default' (ELP only)",
                        LISP_ELP_POLICY);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("both")) {
            this.elpPolicy = "both";
            LOG.debug("Setting configuration variable '{}' to 'both' (keep ELP, add next hop)", LISP_ELP_POLICY);
        } else if (str.trim().equalsIgnoreCase("replace")) {
            this.elpPolicy = "replace";
            LOG.debug("Setting configuration variable '{}' to 'replace' (next hop only)", LISP_ELP_POLICY);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'default' (ELP only)", LISP_ELP_POLICY);
        }
    }

    public boolean mappingOverwriteIsSet() {
        return mappingOverwrite;
    }

    public boolean smrIsSet() {
        return smr;
    }

    public String getElpPolicy() {
        return elpPolicy;
    }

    public static ConfigIni getInstance() {
        return INSTANCE;
    }
}
