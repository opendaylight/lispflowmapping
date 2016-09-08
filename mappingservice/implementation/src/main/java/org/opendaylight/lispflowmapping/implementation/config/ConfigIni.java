/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.config;

import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
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
    private IMappingService.LookupPolicy lookupPolicy;
    private long registrationValiditySb;
    private long smrTimeout;
    private int smrRetryCount;

    // 'lisp.mappingMerge' and 'lisp.mappingOverWrite' are not independent, and they can't be both 'true'
    // when there is a conflict, the setting in 'lisp.mappingMerge' takes precendence
    // 'lisp.mappingOverwrite' defines the database behavior while 'lisp.mappingMerge' affects the result
    // returned in Map-Replies

    private static final String LISP_LOOKUP_POLICY = "lisp.lookupPolicy";
    private static final String LISP_MAPPING_MERGE = "lisp.mappingMerge";
    private static final String LISP_MAPPING_OVERWRITE = "lisp.mappingOverwrite";
    private static final String LISP_SMR = "lisp.smr";
    private static final String LISP_ELP_POLICY = "lisp.elpPolicy";
    private static final String LISP_REGISTER_VALIDITY_SB = "lisp.registerValiditySb";
    private static final String LISP_SMR_RETRY_COUNT = "lisp.smrRetryCount";
    private static final String LISP_SMR_TIMEOUT = "lisp.smrTimeout";

    // SB Map Register validity period in milliseconds. Default is 3.3 minutes.
    public static final long MIN_REGISTRATION_VALIDITY_SB = 200000L;
    private static final long DEFAULT_SMR_TIMEOUT = 3000L;
    private static final int DEFAULT_SMR_RETRY_COUNT = 5;

    private static final ConfigIni INSTANCE = new ConfigIni();

    private ConfigIni() {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = null;
        if (bundle != null) {
            context = bundle.getBundleContext();
        }

        // Initialize mappingMerge first, since mappingOverwrite depends on it
        initMappingMerge(context);
        initMappingOverwrite(context);
        initSmr(context);
        initElpPolicy(context);
        initLookupPolicy(context);
        initRegisterValiditySb(context);
        initSmrRetryCount(context);
        initSmrTimeout(context);
    }

    private void initRegisterValiditySb(BundleContext context) {
        // set the default value first
        this.registrationValiditySb = MIN_REGISTRATION_VALIDITY_SB;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_REGISTER_VALIDITY_SB);
        }

        if (str == null) {
            str = System.getProperty(LISP_REGISTER_VALIDITY_SB);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '3.33 minutes' ",
                        LISP_REGISTER_VALIDITY_SB);
                return;
            }
        }

        try {
            final long regValidity = Long.parseLong(str.trim());
            if (regValidity >= MIN_REGISTRATION_VALIDITY_SB) {
                this.registrationValiditySb = regValidity;
            }
        } catch (NumberFormatException e) {
            this.registrationValiditySb = MIN_REGISTRATION_VALIDITY_SB;
            LOG.debug("Configuration variable 'registerValiditySb' was not set correctly. Registration validity for"
                    + "South Bound Map Registers is set to default value of 3.3 minutes");
        }
    }

    private void initLookupPolicy(BundleContext context) {
        // set the default value first
        this.lookupPolicy = IMappingService.LookupPolicy.NB_FIRST;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_LOOKUP_POLICY);
        }

        if (str == null) {
            str = System.getProperty(LISP_LOOKUP_POLICY);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'northboundFirst' "
                        + "(Southbound is only looked up if Northbound is empty) ", LISP_LOOKUP_POLICY);
                return;
            }
        }

        if (str.trim().equalsIgnoreCase("northboundAndSouthbound")) {
            this.lookupPolicy = IMappingService.LookupPolicy.NB_AND_SB;
            LOG.debug("Setting configuration variable '{}' to 'northboundAndSouthbound' (Southbound is always "
                    + "looked up and can filter Northbound if intersection is not empty)", LISP_LOOKUP_POLICY);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'northboundFirst' (Southbound is only looked up "
                    + "if Northbound is empty)", LISP_LOOKUP_POLICY);
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
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'false'",
                        LISP_MAPPING_MERGE);
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

    private void initSmrRetryCount(BundleContext context) {
        // set the default value first
        this.smrRetryCount = DEFAULT_SMR_RETRY_COUNT;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_SMR_RETRY_COUNT);
        }

        if (str == null) {
            str = System.getProperty(LISP_SMR_RETRY_COUNT);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '{}'", LISP_SMR_RETRY_COUNT,
                        smrRetryCount);
                return;
            }
        }

        try {
            this.smrRetryCount = Integer.valueOf(str);
            LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_RETRY_COUNT, smrRetryCount);
        } catch (NumberFormatException e) {
            LOG.debug("Configuration variable '{}' was not set correctly. SMR retry count " +
                    "is set to default value ({})", LISP_SMR_RETRY_COUNT, smrRetryCount);
        }
    }

    private void initSmrTimeout(BundleContext context) {
        // set the default value first
        this.smrTimeout = DEFAULT_SMR_TIMEOUT;

        String str = null;

        if (context != null) {
            str = context.getProperty(LISP_SMR_TIMEOUT);
        }

        if (str == null) {
            str = System.getProperty(LISP_SMR_TIMEOUT);
            if (str == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '{}'", LISP_SMR_TIMEOUT,
                        smrTimeout);
                return;
            }
        }

        try {
            this.smrTimeout = Long.valueOf(str);
            LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_TIMEOUT, smrTimeout);
        } catch (NumberFormatException e) {
            LOG.debug("Configuration variable '{}' was not set correctly. SMR timeout " +
                    "is set to default value ({})", LISP_SMR_TIMEOUT, smrTimeout);
        }
    }

    public boolean mappingMergeIsSet() {
        return mappingMerge;
    }

    public boolean mappingOverwriteIsSet() {
        return mappingOverwrite;
    }

    public void setMappingOverwrite(boolean mappingOverwrite) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_MAPPING_OVERWRITE, mappingOverwrite);
        this.mappingOverwrite = mappingOverwrite;
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_MAPPING_MERGE, !(mappingOverwrite));
        this.mappingMerge = !(mappingOverwrite);
    }

    public boolean smrIsSet() {
        return smr;
    }

    public void setSmr(boolean smr) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR, smr);
        this.smr = smr;
    }

    public String getElpPolicy() {
        return elpPolicy;
    }

    public IMappingService.LookupPolicy getLookupPolicy() {
        return lookupPolicy;
    }

    public long getRegistrationValiditySb() {
        return registrationValiditySb;
    }

    public void setLookupPolicy(IMappingService.LookupPolicy lookupPolicy) {
        this.lookupPolicy = lookupPolicy;
    }

    public void setSmrRetryCount(int smrRetryCount) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_RETRY_COUNT, smrRetryCount);
        this.smrRetryCount = smrRetryCount;
    }

    public int getSmrRetryCount() {
        return smrRetryCount;
    }

    public void setSmrTimeout(long smrTimeout) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_TIMEOUT, smrTimeout);
        this.smrTimeout = smrTimeout;
    }

    public long getSmrTimeout() {
        return this.smrTimeout;
    }

    public static ConfigIni getInstance() {
        return INSTANCE;
    }
}
