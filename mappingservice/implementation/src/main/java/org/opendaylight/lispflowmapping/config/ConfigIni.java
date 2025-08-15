/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.config;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.lispflowmapping.interfaces.lisp.IGenericMapResolver.ExplicitLocatorPathPolicy;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService.LookupPolicy;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = ConfigIni.class)
public final class ConfigIni {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigIni.class);

    private final ExplicitLocatorPathPolicy elpPolicy;

    private boolean mappingMerge;
    private boolean smr;
    private LookupPolicy lookupPolicy;
    private long registrationValiditySb;
    private long smrTimeout;
    private int smrRetryCount;
    private int negativeMappingTTL;

    /*
     * XXX  When configuration options are added or removed, they should also be added/removed in the karaf
     * `etc/custom.properties file`, hosted in the odlparent repository [0]. The "Configuring LISP Flow Mapping"
     * section in the LISP Flow Mapping User Guide [1] has to be updated too, including when a configuration option's
     * semantics or behavior is changed, in addition to having added/removed and option. Since we don't document
     * options extensively in this file, the User Guide is the canonical documentation for them.
     *
     * [0] https://git.opendaylight.org/gerrit/gitweb?p=odlparent.git;a=blob;f=karaf/opendaylight-karaf-resources/src/main/resources/etc/custom.properties
     * [1] https://git.opendaylight.org/gerrit/gitweb?p=docs.git;a=blob;f=docs/user-guide/lisp-flow-mapping-user-guide.rst
     */
    private static final String LISP_MAPPING_MERGE = "lisp.mappingMerge";
    private static final String LISP_LOOKUP_POLICY = "lisp.lookupPolicy";
    private static final String LISP_SMR = "lisp.smr";
    private static final String LISP_ELP_POLICY = "lisp.elpPolicy";
    private static final String LISP_REGISTER_VALIDITY_SB = "lisp.registerValiditySb";
    private static final String LISP_SMR_RETRY_COUNT = "lisp.smrRetryCount";
    private static final String LISP_SMR_TIMEOUT = "lisp.smrTimeout";
    private static final String LISP_NEGATIVE_MAPPING_TTL = "lisp.negativeMappingTTL";

    // SB Map Register validity period in milliseconds. Default is 3.3 minutes.
    private static final long MIN_REGISTRATION_VALIDITY_SB = 200000L;
    private static final long DEFAULT_SMR_TIMEOUT = 3000L;
    private static final int DEFAULT_SMR_RETRY_COUNT = 5;
    private static final int DEFAULT_NEGATIVE_MAPPING_TTL = 15;

    public ConfigIni(boolean mappingMerge, boolean smr, ExplicitLocatorPathPolicy elpPolicy,
            LookupPolicy lookupPolicy, long registrationValidityMillis, int smrRetryCount, long smrTimeoutMillis,
            int negativeMappingTtl) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_MAPPING_MERGE, mappingMerge);
        this.mappingMerge = mappingMerge;
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR, smr);
        this.smr = smr;
        LOG.debug("Setting configuration variable '{}' to {}", LISP_ELP_POLICY, switch (elpPolicy) {
            case BOTH -> "'both' (keep ELP, add next hop)";
            case DEFAULT -> "'default' (ELP only)";
            case REPLACE -> "'replace' (next hop only)";
        });
        this.elpPolicy = elpPolicy;
        LOG.debug("Setting configuration variable '{}' to {}", LISP_LOOKUP_POLICY, switch (lookupPolicy) {
            case NB_AND_SB -> "'northboundAndSouthbound' (Southbound is always looked up and can filter Northbound if "
                + "intersection is not empty)";
            case NB_FIRST -> "'northboundFirst' (Southbound is only looked up if Northbound is empty)";
        });
        this.lookupPolicy = lookupPolicy;
        LOG.debug("Setting configuration variable '{}' to {}ms", LISP_REGISTER_VALIDITY_SB,
            registrationValidityMillis);
        setRegistrationValiditySb(registrationValidityMillis);
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_SMR_RETRY_COUNT, smrRetryCount);
        this.smrRetryCount = smrRetryCount;
        LOG.debug("Setting configuration variable '{}' to {}ms'", LISP_SMR_TIMEOUT, smrTimeoutMillis);
        smrTimeout = smrTimeoutMillis;
        LOG.debug("Setting configuration variable '{}' to {} minuts", LISP_NEGATIVE_MAPPING_TTL, negativeMappingTtl);
        this.negativeMappingTTL = negativeMappingTtl;
    }

    private ConfigIni(String mappingMerge, String smr, String elpPolicy, String lookupPolicy,
           String registrationValidity, String smrRetryCount, String smrTimeout, String negativeMappingTtl) {
        this(
            mappingMerge != null && mappingMerge.trim().equalsIgnoreCase("true"),
            smr == null || !smr.trim().equalsIgnoreCase("false"),
            parseElpPolicy(elpPolicy), parseLookupPolicy(lookupPolicy),
            parseRegistrationValidity(registrationValidity), parseSmrRetryCount(smrRetryCount),
            parseSmrTimeout(smrTimeout), parseNegativeMappingTtl(negativeMappingTtl));
    }

    @Inject
    public ConfigIni() {
        this(System.getProperty(LISP_MAPPING_MERGE), System.getProperty(LISP_SMR),
            System.getProperty(LISP_ELP_POLICY), System.getProperty(LISP_LOOKUP_POLICY),
            System.getProperty(LISP_REGISTER_VALIDITY_SB), System.getProperty(LISP_SMR_RETRY_COUNT),
            System.getProperty(LISP_SMR_TIMEOUT), System.getProperty(LISP_NEGATIVE_MAPPING_TTL));
    }

    @Activate
    public ConfigIni(BundleContext context) {
        this(context.getProperty(LISP_MAPPING_MERGE), context.getProperty(LISP_SMR),
            context.getProperty(LISP_ELP_POLICY), context.getProperty(LISP_LOOKUP_POLICY),
            context.getProperty(LISP_REGISTER_VALIDITY_SB), context.getProperty(LISP_SMR_RETRY_COUNT),
            context.getProperty(LISP_SMR_TIMEOUT), context.getProperty(LISP_NEGATIVE_MAPPING_TTL));
    }

    private static @NonNull ExplicitLocatorPathPolicy parseElpPolicy(@Nullable String str) {
        if (str != null) {
            final var trimmed = str.trim();
            if (trimmed.equalsIgnoreCase("both")) {
                return ExplicitLocatorPathPolicy.BOTH;
            }
            if (trimmed.equalsIgnoreCase("replace")) {
                return ExplicitLocatorPathPolicy.REPLACE;
            }
        }
        return ExplicitLocatorPathPolicy.DEFAULT;
    }

    private static @NonNull LookupPolicy parseLookupPolicy(@Nullable String str) {
        return str != null && str.trim().equalsIgnoreCase("northboundAndSouthbound") ? LookupPolicy.NB_AND_SB
            : LookupPolicy.NB_FIRST;
    }

    private static int parseNegativeMappingTtl(@Nullable String str) {
        if (str != null) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Configuration variable '{}' was not set correctly, using default value of {} minutes",
                    LISP_NEGATIVE_MAPPING_TTL, DEFAULT_NEGATIVE_MAPPING_TTL, e);
            }
        }
        return DEFAULT_NEGATIVE_MAPPING_TTL;
    }

    private static long parseRegistrationValidity(@Nullable String str) {
        if (str != null) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Configuration variable '{}' was not set correctly, using default value of 3.33 minutes",
                    LISP_REGISTER_VALIDITY_SB, e);
            }
        }
        return MIN_REGISTRATION_VALIDITY_SB;
    }

    private static int parseSmrRetryCount(@Nullable String str) {
        if (str != null) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Configuration variable '{}' was not set correctly, using default value of {}",
                    LISP_SMR_RETRY_COUNT, DEFAULT_SMR_RETRY_COUNT, e);
            }
        }
        return DEFAULT_SMR_RETRY_COUNT;
    }

    private static long parseSmrTimeout(@Nullable String str) {
        if (str != null) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Configuration variable '{}' was not set correctly, using default value of {}",
                    LISP_SMR_TIMEOUT, DEFAULT_SMR_TIMEOUT, e);
            }
        }
        return DEFAULT_SMR_TIMEOUT;
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

    public ExplicitLocatorPathPolicy getElpPolicy() {
        return elpPolicy;
    }

    public IMappingService.LookupPolicy getLookupPolicy() {
        return lookupPolicy;
    }

    public long getRegistrationValiditySb() {
        return registrationValiditySb;
    }

    public long getDefaultRegistrationValiditySb() {
        return MIN_REGISTRATION_VALIDITY_SB;
    }

    public void setRegistrationValiditySb(long registrationValiditySb) {
        this.registrationValiditySb = registrationValiditySb;
        if (registrationValiditySb < MIN_REGISTRATION_VALIDITY_SB) {
            LOG.warn("Registration validity is less than the default 3.33 minutes!!!");
        }
    }

    public void setLookupPolicy(IMappingService.LookupPolicy lookupPolicy) {
        this.lookupPolicy = requireNonNull(lookupPolicy);
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

    public void setNegativeMappingTTL(int negativeMappingTTL) {
        LOG.debug("Setting configuration variable '{}' to '{}'", LISP_NEGATIVE_MAPPING_TTL, negativeMappingTTL);
        this.negativeMappingTTL = negativeMappingTTL;
    }

    public int getNegativeMappingTTL() {
        return this.negativeMappingTTL;
    }
}
