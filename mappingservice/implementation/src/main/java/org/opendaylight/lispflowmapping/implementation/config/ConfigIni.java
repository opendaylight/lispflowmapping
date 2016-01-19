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
    private boolean mappingMerge;
    private boolean mappingOverwrite;
    private boolean smr;
    private String elpPolicy;
    private int lookupPolicy;

    // 'lisp.mappingMerge' and 'lisp.mappingOverWrite' are not independent, and they can't be both 'true'
    // when there is a conflict, the setting in 'lisp.mappingMerge' takes precendence
    // 'lisp.mappingOverwrite' defines the database behavior while 'lisp.mappingMerge' affects the result
    // returned in Map-Replies

    private static final String LISP_LOOKUP_POLICY = "lisp.lookupPolicy";
    private static final String LISP_MAPPING_MERGE = "lisp.mappingMerge";
    private static final String LISP_MAPPING_OVERWRITE = "lisp.mappingOverwrite";
    private static final String LISP_SMR = "lisp.smr";
    private static final String LISP_ELP_POLICY = "lisp.elpPolicy";

    // lookupPolicy options
    public static final int NB_FIRST = 0;
    public static final int NB_AND_SB = 1;

    private static final ConfigIni INSTANCE = new ConfigIni();

    private ConfigIni() {
        Bundle b = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = null;
        if (b != null) {
            context = b.getBundleContext();
        }

        // Initialize mappingMerge first, since mappingOverwrite depends on it
        initMappingMerge(context);
        initMappingOverwrite(context);
        initSmr(context);
        initElpPolicy(context);
        initLookupPolicy(context);
    }

    private void initLookupPolicy(BundleContext context) {
        // set the default value first
        this.lookupPolicy = NB_FIRST;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_LOOKUP_POLICY);
        }

        if (str == null) {
            str = System.getProperty(LISP_LOOKUP_POLICY);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'northboundFirst' (Southbound is only looked up if Northbound is empty) ",
                        LISP_LOOKUP_POLICY);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("northboundAndSouthbound")) {
            this.lookupPolicy = NB_AND_SB;
            LOG.debug("Setting configuration variable '{}' to 'northboundAndSouthbound' (Southbound is always looked up and can filter Northbound if intersection is not empty)", LISP_LOOKUP_POLICY);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'northboundFirst' (Southbound is only looked up if Northbound is empty)", LISP_LOOKUP_POLICY);
        }
    }

    private void initMappingMerge(BundleContext context) {
        // set the default value first
        this.mappingMerge = false;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_MAPPING_MERGE);
        }

        if (str == null) {
            str = System.getProperty(LISP_MAPPING_MERGE);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'false'", LISP_MAPPING_MERGE);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("true")) {
            this.mappingMerge = true;
            LOG.debug("Setting configuration variable '{}' to 'true'", LISP_MAPPING_MERGE);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_MAPPING_MERGE);
        }
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
                if (this.mappingMerge) {
                    // If merge is enabled and overwriting configuration is not set, disable it
                    LOG.debug("Configuration variable '{}' is unset. Since '{}'=true setting to 'false'",
                            LISP_MAPPING_OVERWRITE, LISP_MAPPING_MERGE);
                    this.mappingOverwrite = false;
                } else {
                    LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'true'",
                            LISP_MAPPING_OVERWRITE);
                }
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("false")) {
            this.mappingOverwrite = false;
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_MAPPING_OVERWRITE);
        } else {
            if (this.mappingMerge) {
                LOG.warn("Can't set configuration variable '{}' to 'true' since '{}' is enabled",
                        LISP_MAPPING_OVERWRITE, LISP_MAPPING_MERGE);
                LOG.warn("If you really need to enable overwriting, please disable merging.");
                LOG.debug("Setting configuration variable '{}' to 'false'", LISP_MAPPING_OVERWRITE);
                this.mappingOverwrite = false;
            } else {
                LOG.debug("Setting configuration variable '{}' to 'true'", LISP_MAPPING_OVERWRITE);
            }
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

    public boolean mappingMergeIsSet() {
        return mappingMerge;
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

    public int getLookupPolicy() {
        return lookupPolicy;
    }

    public static ConfigIni getInstance() {
        return INSTANCE;
    }
}
