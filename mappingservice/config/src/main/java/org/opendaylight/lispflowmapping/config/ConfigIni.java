/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.config;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigIni {

    protected static final Logger LOG = LoggerFactory.getLogger(ConfigIni.class);
    private boolean mappingMerge;
    private boolean smr;
    private String elpPolicy;
    private IMappingService.LookupPolicy lookupPolicy;
    private long registrationValiditySb;
    private long smrTimeout;
    private int smrRetryCount;
    private int numberOfBucketsInTimeBucketWheel;

    /*
     * XXX  When configuration options are added or removed, they should also be added/removed in the karaf
     * `etc/org.opendaylight.lispflowmapping.startup.cfg file` during runtime and in starup.cfg at [0] for prebuilt.
     * The "Configuring LISP Flow Mapping" section in the LISP Flow Mapping User Guide [1] has to be updated too,
     * including when a configuration option's semantics or behavior is changed, in addition to having added/removed
     * and option. Since we don't document options extensively in this file, the User Guide is the canonical
     * documentation for them.
     *
     * [0] https://git.opendaylight.org/gerrit/gitweb?p=lispflowmapping.git;a=tree;f=mappingservice/implementation/src/main/resources;h=ee95f21f5c319ce31946a94d058c3a516dd77f67;hb=HEAD
     * [1] https://git.opendaylight.org/gerrit/gitweb?p=docs.git;a=blob;f=docs/user-guide/lisp-flow-mapping-user-guide.rst
     */
    private static final String LISP_LOOKUP_POLICY = "lisp.lookupPolicy";
    private static final String LISP_MAPPING_MERGE = "lisp.mappingMerge";
    private static final String LISP_SMR = "lisp.smr";
    private static final String LISP_ELP_POLICY = "lisp.elpPolicy";
    private static final String LISP_REGISTER_VALIDITY_SB = "lisp.registerValiditySb";
    private static final String LISP_SMR_RETRY_COUNT = "lisp.smrRetryCount";
    private static final String LISP_SMR_TIMEOUT = "lisp.smrTimeout";

    // SB Map Register validity period in milliseconds. Default is 3.3 minutes.
    private static final long MIN_REGISTRATION_VALIDITY_SB = 200000L;
    private static final long DEFAULT_SMR_TIMEOUT = 3000L;
    private static final int DEFAULT_SMR_RETRY_COUNT = 5;
    private static final int MIN_NUMBER_OF_BUCKETS_IN_TIME_BUCKET_WHEEL = 2;
    private static final int TIMEOUT_TOLERANCE_MULTIPLIER_IN_TIME_BUCKET_WHEEL = 2;

    private static final ConfigIni INSTANCE = new ConfigIni();

    private ConfigIni() {
        initConfigPropertiesWithDefaultValue();
    }

    private void initConfigPropertiesWithDefaultValue() {
        initMappingMerge(null);
        initSmr(null);
        initElpPolicy(null);
        initLookupPolicy(null);
        initRegisterValiditySb(null);
        initSmrRetryCount(null);
        initSmrTimeout(null);
    }

    public HashMap<String, Consumer> provideConfigToConsumerMapper() {
        HashMap<String, Consumer> configConsumer = new HashMap<>();

        configConsumer.put(LISP_MAPPING_MERGE, configStr -> initMappingMerge((String) configStr));
        configConsumer.put(LISP_SMR, configStr -> initSmr((String) configStr));
        configConsumer.put(LISP_ELP_POLICY, configStr -> initElpPolicy((String) configStr));
        configConsumer.put(LISP_LOOKUP_POLICY, configStr -> initLookupPolicy((String) configStr));
        configConsumer.put(LISP_REGISTER_VALIDITY_SB, configStr -> initRegisterValiditySb((String) configStr));
        configConsumer.put(LISP_SMR_RETRY_COUNT, configStr -> initSmrRetryCount((String) configStr));
        configConsumer.put(LISP_SMR_TIMEOUT, configStr -> initSmrTimeout((String) configStr));

        return configConsumer;
    }

    private void initRegisterValiditySb(String configStr) {
        // set the default value first
        this.registrationValiditySb = MIN_REGISTRATION_VALIDITY_SB;

        if (configStr == null) {
            configStr = System.getProperty(LISP_REGISTER_VALIDITY_SB);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '3.33 minutes' ",
                        LISP_REGISTER_VALIDITY_SB);
                initBucketNumber();
                return;
            }
        }

        try {
            final long regValidity = Long.parseLong(configStr.trim());
            setRegistrationValiditySb(regValidity);
        } catch (NumberFormatException e) {
            this.registrationValiditySb = MIN_REGISTRATION_VALIDITY_SB;
            LOG.debug("Configuration variable 'registerValiditySb' was not set correctly. Registration validity for"
                    + "South Bound Map Registers is set to default value of 3.3 minutes");
        }
        initBucketNumber();
    }

    private void initLookupPolicy(String configStr) {
        // set the default value first
        this.lookupPolicy = IMappingService.LookupPolicy.NB_FIRST;

        if (configStr == null) {
            configStr = System.getProperty(LISP_LOOKUP_POLICY);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'northboundFirst' "
                        + "(Southbound is only looked up if Northbound is empty) ", LISP_LOOKUP_POLICY);
                return;
            }
        }

        if (configStr.trim().equalsIgnoreCase("northboundAndSouthbound")) {
            this.lookupPolicy = IMappingService.LookupPolicy.NB_AND_SB;
            LOG.debug("Setting configuration variable '{}' to 'northboundAndSouthbound' (Southbound is always "
                    + "looked up and can filter Northbound if intersection is not empty)", LISP_LOOKUP_POLICY);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'northboundFirst' (Southbound is only looked up "
                    + "if Northbound is empty)", LISP_LOOKUP_POLICY);
        }
    }

    private void initMappingMerge(String configStr) {
        // set the default value first
        this.mappingMerge = false;

        if (configStr == null) {
            configStr = System.getProperty(LISP_MAPPING_MERGE);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'false'",
                        LISP_MAPPING_MERGE);
                return;
            }
        }

        if (configStr.trim().equalsIgnoreCase("true")) {
            this.mappingMerge = true;
            LOG.debug("Setting configuration variable '{}' to 'true'", LISP_MAPPING_MERGE);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_MAPPING_MERGE);
        }
    }

    private void initSmr(String configStr) {
        // set the default value first
        this.smr = true;

        if (configStr == null) {
            configStr = System.getProperty(LISP_SMR);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'true'", LISP_SMR);
                return;
            }
        }

        if (configStr.trim().equalsIgnoreCase("false")) {
            this.smr = false;
            LOG.debug("Setting configuration variable '{}' to 'false'", LISP_SMR);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'true'", LISP_SMR);
        }
    }

    private void initElpPolicy(String configStr) {
        // set the default value first
        this.elpPolicy = "default";

        if (configStr == null) {
            configStr = System.getProperty(LISP_ELP_POLICY);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: 'default' (ELP only)",
                        LISP_ELP_POLICY);
                return;
            }
        }

        if (configStr.trim().equalsIgnoreCase("both")) {
            this.elpPolicy = "both";
            LOG.debug("Setting configuration variable '{}' to 'both' (keep ELP, add next hop)", LISP_ELP_POLICY);
        } else if (configStr.trim().equalsIgnoreCase("replace")) {
            this.elpPolicy = "replace";
            LOG.debug("Setting configuration variable '{}' to 'replace' (next hop only)", LISP_ELP_POLICY);
        } else {
            LOG.debug("Setting configuration variable '{}' to 'default' (ELP only)", LISP_ELP_POLICY);
        }
    }

    private void initSmrRetryCount(String configStr) {
        // set the default value first
        this.smrRetryCount = DEFAULT_SMR_RETRY_COUNT;

        if (configStr == null) {
            configStr = System.getProperty(LISP_SMR_RETRY_COUNT);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '{}'", LISP_SMR_RETRY_COUNT,
                        smrRetryCount);
                return;
            }
        }

        try {
            this.smrRetryCount = Integer.valueOf(configStr);
            LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_RETRY_COUNT, smrRetryCount);
        } catch (NumberFormatException e) {
            LOG.debug("Configuration variable '{}' was not set correctly. SMR retry count "
                    + "is set to default value ({})", LISP_SMR_RETRY_COUNT, smrRetryCount);
        }
    }

    private void initSmrTimeout(String configStr) {
        // set the default value first
        this.smrTimeout = DEFAULT_SMR_TIMEOUT;

        if (configStr == null) {
            configStr = System.getProperty(LISP_SMR_TIMEOUT);
            if (configStr == null) {
                LOG.debug("Configuration variable '{}' is unset. Setting to default value: '{}'", LISP_SMR_TIMEOUT,
                        smrTimeout);
                return;
            }
        }

        try {
            this.smrTimeout = Long.valueOf(configStr);
            LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_TIMEOUT, smrTimeout);
        } catch (NumberFormatException e) {
            LOG.debug("Configuration variable '{}' was not set correctly. SMR timeout "
                    + "is set to default value ({})", LISP_SMR_TIMEOUT, smrTimeout);
        }
    }

    //one bucket should contain mapping of approximate 1 min time frame
    private void initBucketNumber() {
        numberOfBucketsInTimeBucketWheel = (int) (TimeUnit.MILLISECONDS.toMinutes(getRegistrationValiditySb()) + 1);

        numberOfBucketsInTimeBucketWheel = Math.max(numberOfBucketsInTimeBucketWheel,
                MIN_NUMBER_OF_BUCKETS_IN_TIME_BUCKET_WHEEL);
    }

    public boolean mappingMergeIsSet() {
        return mappingMerge;
    }

    public void setMappingMerge(boolean mappingMerge) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_MAPPING_MERGE, (mappingMerge));
        this.mappingMerge = (mappingMerge);
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

    public void setRegistrationValiditySb(long registrationValiditySb) {
        this.registrationValiditySb = registrationValiditySb;
        if (registrationValiditySb < MIN_REGISTRATION_VALIDITY_SB) {
            LOG.warn("Registration validity is less than the default 3.33 minutes!!!");
        }
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

    public int getNumberOfBucketsInTimeBucketWheel() {
        return numberOfBucketsInTimeBucketWheel;
    }

    public long  getMaximumTimeoutTolerance() {
        return TIMEOUT_TOLERANCE_MULTIPLIER_IN_TIME_BUCKET_WHEEL * getRegistrationValiditySb();
    }

    public static ConfigIni getInstance() {
        return INSTANCE;
    }
}
