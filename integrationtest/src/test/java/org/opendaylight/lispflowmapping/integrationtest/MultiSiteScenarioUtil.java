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

    static final Site SITE_A = new SiteA();
    static final Site SITE_B = new SiteB();
    static final Site SITE_B_RLOC_10 = new SiteBRloc10();
    static final Site SITE_C = new SiteC();
    static final Site SITE_D4 = new SiteD4();
    static final Site SITE_D5 = new SiteD5();

    private MultiSiteScenarioUtil() {
        throw new UnsupportedOperationException();
    }

    static abstract class Site {
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

        Site(final String eidPrefix, final String[] host, final String rloc, final SiteId siteId, final
             InstanceIdType vni) {
            this.eidPrefix = eidPrefix;
            this.host = host;
            this.rloc = rloc;
            this.siteId = siteId;
            this.vni = vni;
        }

    }

    static class SiteA extends Site {
        private static final String PREFIX_DIFFERENCE = "1";
        private static final String EID_PREFIX_INNER = "192.0."+PREFIX_DIFFERENCE;
        public static final String EID_PREFIX = EID_PREFIX_INNER + ".0";
        public static final String[] HOST = new String[]{
                ""
                ,EID_PREFIX_INNER + ".1"
                ,EID_PREFIX_INNER + ".2"
                ,EID_PREFIX_INNER + ".3"
                ,EID_PREFIX_INNER + ".4"
                ,EID_PREFIX_INNER + ".5"
        };
        public static final String RLOC = PREFIX_DIFFERENCE + "." + PREFIX_DIFFERENCE  + "." + PREFIX_DIFFERENCE + "." +
                PREFIX_DIFFERENCE;
        private final static byte[] SITE_ID_VALUE = new byte[]{'A', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public static final SiteId SITE_ID = new SiteId(SITE_ID_VALUE);
        public static final int VNI_VALUE = 2;
        public static final InstanceIdType VNI = new InstanceIdType((long) VNI_VALUE);

        private SiteA() {
            super(EID_PREFIX, HOST, RLOC, SITE_ID, VNI);
        }
    }

    static class SiteB extends Site {
        private static final String PREFIX_DIFFERENCE = "2";
        private static final String EID_PREFIX_INNER = "192.0."+PREFIX_DIFFERENCE;
        public static final String EID_PREFIX = EID_PREFIX_INNER + ".0";
        public static final String[] HOST = new String[]{
                ""
                ,EID_PREFIX_INNER + ".1"
                ,EID_PREFIX_INNER + ".2"
                ,EID_PREFIX_INNER + ".3"
                ,EID_PREFIX_INNER + ".4"
                ,EID_PREFIX_INNER + ".5"
        };
        public static final String RLOC = PREFIX_DIFFERENCE + "." + PREFIX_DIFFERENCE  + "." + PREFIX_DIFFERENCE + "." +
            PREFIX_DIFFERENCE;
        private final static byte[] SITE_ID_VALUE = new byte[]{'B', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public static final SiteId SITE_ID = new SiteId(SITE_ID_VALUE);
        public static final int VNI_VALUE = 2;
        public static final InstanceIdType VNI = new InstanceIdType((long) VNI_VALUE);

        private SiteB() {
            super(EID_PREFIX, HOST, RLOC, SITE_ID, VNI);
        }
    }

    static class SiteBRloc10 extends SiteB {
        private static final String RLOC = "10.10.10.10";

        private SiteBRloc10() {
            this.rloc = RLOC;
        }
    }

    static class SiteC extends Site {
        private static final String PREFIX_DIFFERENCE = "3";
        private static final String EID_PREFIX_INNER = "192.0."+PREFIX_DIFFERENCE;
        public static final String EID_PREFIX = EID_PREFIX_INNER + ".0";
        public static final String[] HOST = new String[]{
                ""
                ,EID_PREFIX_INNER + ".1"
                ,EID_PREFIX_INNER + ".2"
                ,EID_PREFIX_INNER + ".3"
                ,EID_PREFIX_INNER + ".4"
                ,EID_PREFIX_INNER + ".5"
        };
        public static final String RLOC = PREFIX_DIFFERENCE + "." + PREFIX_DIFFERENCE  + "." + PREFIX_DIFFERENCE + "." +
                PREFIX_DIFFERENCE;
        private final static byte[] SITE_ID_VALUE = new byte[]{'C', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public static final SiteId SITE_ID = new SiteId(SITE_ID_VALUE);
        public static final int VNI_VALUE = 2;
        public static final InstanceIdType VNI = new InstanceIdType((long) VNI_VALUE);

        private SiteC() {
            super(EID_PREFIX, HOST, RLOC, SITE_ID, VNI);
        }
    }

    static class SiteD4 extends Site {
        private static final String PREFIX_DIFFERENCE = "4";
        private static final String EID_PREFIX_INNER = "192.0."+PREFIX_DIFFERENCE;
        public static final String EID_PREFIX = EID_PREFIX_INNER + ".0";
        public static final String[] HOST = new String[]{
                ""
                ,EID_PREFIX_INNER + ".1"
                ,EID_PREFIX_INNER + ".2"
                ,EID_PREFIX_INNER + ".3"
                ,EID_PREFIX_INNER + ".4"
                ,EID_PREFIX_INNER + ".5"
        };
        public static final String RLOC = PREFIX_DIFFERENCE + "." + PREFIX_DIFFERENCE  + "." + PREFIX_DIFFERENCE + "." +
                PREFIX_DIFFERENCE;
        private final static byte[] SITE_ID_VALUE = new byte[]{'D', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public static final SiteId SITE_ID = new SiteId(SITE_ID_VALUE);
        public static final int VNI_VALUE = 2;
        public static final InstanceIdType VNI = new InstanceIdType((long) VNI_VALUE);

        private SiteD4() {
            super(EID_PREFIX, HOST, RLOC, SITE_ID, VNI);
        }
    }

    static class SiteD5 extends Site {
        private static final String PREFIX_DIFFERENCE = "5";
        private static final String EID_PREFIX_INNER = "192.0."+PREFIX_DIFFERENCE;
        public static final String EID_PREFIX = EID_PREFIX_INNER + ".0";
        public static final String[] HOST = new String[]{
                ""
                ,EID_PREFIX_INNER + ".1"
                ,EID_PREFIX_INNER + ".2"
                ,EID_PREFIX_INNER + ".3"
                ,EID_PREFIX_INNER + ".4"
                ,EID_PREFIX_INNER + ".5"
        };
        public static final String RLOC = PREFIX_DIFFERENCE + "." + PREFIX_DIFFERENCE  + "." + PREFIX_DIFFERENCE + "." +
                PREFIX_DIFFERENCE;
        private final static byte[] SITE_ID_VALUE = new byte[]{'D', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public static final SiteId SITE_ID = new SiteId(SITE_ID_VALUE);
        public static final int VNI_VALUE = 3;
        public static final InstanceIdType VNI = new InstanceIdType((long) VNI_VALUE);

        private SiteD5() {
            super(EID_PREFIX, HOST, RLOC, SITE_ID, VNI);
        }

    }

}
