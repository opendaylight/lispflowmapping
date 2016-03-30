/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.integrationtest;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.InstanceIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;

class MultiSiteScenarioUtil {
    private final static String COMMON_IPV4_PREFIX = "192.0.";
    private final static String COMMON_IPV4_SUFFIX = ".0";
    private final static String HOST1 = ".1";
    private final static String HOST2 = ".2";
    private final static String HOST3 = ".3";
    private final static String HOST4 = ".4";
    private final static String HOST5 = ".5";

    private final static long VNI2 = 2L;
    private final static long VNI3 = 3L;

    static final Site SITE_A = new Site("1", 'A', VNI2);
    static final Site SITE_B = new Site("2", 'B', VNI2);
    static final Site SITE_C = new Site("3", 'C', VNI2);
    static final Site SITE_C_RLOC_10 = new Site("3", 'C', VNI2, "10");
    static final Site SITE_D4 = new Site("4", 'D', VNI2);
    static final Site SITE_D5 = new Site("5", 'D', VNI3);


    private MultiSiteScenarioUtil() {
        throw new UnsupportedOperationException();
    }

    static class Site {
        protected String eidPrefix;
        protected String[] host;
        protected String rloc;
        protected SiteId siteId;
        protected final InstanceIdType vni;

        String getEidPrefix() {
            return eidPrefix;
        }

        String getHost(final int index) {
            return host[index];
        }

        String getRloc() {
            return rloc;
        }

        SiteId getSiteId() {
            return siteId;
        }

        InstanceIdType getVNI() {
            return vni;
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni) {
            this(siteSpecificIpPart, siteId, vni, siteSpecificIpPart);
        }

        Site(final String siteSpecificIpPart, char siteId, final long vni, final String rloc) {
            this.eidPrefix = COMMON_IPV4_PREFIX + siteSpecificIpPart + COMMON_IPV4_SUFFIX;
            this.host = new String[]{
                    ""
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST1
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST2
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST3
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST4
                    ,COMMON_IPV4_PREFIX + siteSpecificIpPart + HOST5
            };
            this.siteId = new SiteId(new byte[]{(byte)siteId, ' ', ' ', ' ', ' ', ' ', ' ', ' '});
            this.vni = new InstanceIdType(vni);
            this.rloc = rloc + "." + rloc + "." + rloc + "." + rloc;
        }

    }

}
