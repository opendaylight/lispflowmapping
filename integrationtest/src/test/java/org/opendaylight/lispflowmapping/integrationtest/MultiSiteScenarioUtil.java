/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;

class MultiSiteScenarioUtil {
    private final static String COMMON_IPV4_PREFIX = "192.0.";
    private final static String COMMON_IPV4_SUFFIX = ".0";
    private final static String HOST1 = ".1";
    private final static String HOST2 = ".2";
    private final static String HOST3 = ".3";
    private final static String HOST4 = ".4";
    private final static String HOST5 = ".5";

    protected final static long VNI2 = 2L;
    private final static long VNI3 = 3L;

    static final Short DEFAULT_PRIORITY = 1;
    static final Short DEFAULT_WEIGHT = 1;


    /**
     * constants for test scenario A
     */
    static final Site SITE_A = new Site("1", 'A', VNI2);
    static final Site SITE_B = new Site("2", 'B', VNI2);
    static final Site SITE_C = new Site("3", 'C', VNI2);
    static final Site SITE_C_RLOC_10 = new Site("3", 'C', VNI2, "10");
    static final Site SITE_D4 = new Site("4", 'D', VNI2);
    static final Site SITE_D5 = new Site("5", 'D', VNI3);

    /**
     * constants for test scenario B
     */
    static final Site SITE_A_SB = SITE_A;
    static final Site SITE_B_SB = SITE_B;
    static final Site SITE_C_SB = SITE_C;
    static final Site SITE_C_WP_50_2_SB = new Site("3", 'C', VNI2, "3", (short) 50, (short) 2);
    static final Site SITE_C_WP_100_1_SB = new Site("3", 'C', VNI2, "3", (short) 100, (short) 1);
    static final Site SITE_D_DELETE_SB = new Site("3", 'D', VNI2, "4", true);
    static final Site SITE_D_SB = new Site("3", 'D', VNI2, "4");
    static final Site SITE_D_WP_50_2_SB = new Site("3", 'D', VNI2, "4", (short) 50, (short) 2);
    static final Site SITE_D_WP_100_1_SB = new Site("3", 'D', VNI2, "4", (short) 100, (short) 1);
    static final Site SITE_E_SB = new Site("10", 'E', VNI3);

    private MultiSiteScenarioUtil() {
        throw new UnsupportedOperationException();
    }

    static class Site {
        protected final boolean isForDeletion;
        protected final String eidPrefix;
        protected final String[] host;
        protected final String rloc;
        protected final XtrId xtrId;
        protected final InstanceIdType vni;
        protected final short weight;
        protected final short priority;

        String getEidPrefix() {
            return eidPrefix;
        }

        String getHost(final int index) {
            return host[index];
        }

        String getRloc() {
            return rloc;
        }

        XtrId getXtrId() {
            return xtrId;
        }

        InstanceIdType getVNI() {
            return vni;
        }

        short getWeight() {
            return weight;
        }

        short getPriority() {
            return priority;
        }

        boolean isForDeletion() {
            return isForDeletion;
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni, final String rloc, final boolean
                isForDeletion) {
            this(siteSpecificIpPart, siteId, vni, rloc, DEFAULT_WEIGHT, DEFAULT_PRIORITY, isForDeletion);
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni) {
            this(siteSpecificIpPart, siteId, vni, siteSpecificIpPart, DEFAULT_WEIGHT, DEFAULT_PRIORITY, false);
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni, final String rloc) {
            this(siteSpecificIpPart, siteId, vni, rloc, DEFAULT_WEIGHT, DEFAULT_PRIORITY, false);
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni, final String rloc, final short weight,
             final short priority) {
            this(siteSpecificIpPart, siteId, vni, rloc, weight, priority, false);
        }
        Site(final String siteSpecificIpPart, char siteId, final long vni, final String rloc, final short weight,
             final short priority, final boolean isForDeletion) {
            this.eidPrefix = COMMON_IPV4_PREFIX + siteSpecificIpPart + COMMON_IPV4_SUFFIX;
            this.host = new String[]{
                    ""
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST1
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST2
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST3
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST4
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST5
            };
            this.xtrId = new XtrId(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) siteId});
            this.vni = new InstanceIdType(vni);
            this.rloc = rloc + "." + rloc + "." + rloc + "." + rloc;
            this.weight = weight;
            this.priority = priority;
            this.isForDeletion = isForDeletion;
        }

    }

}
