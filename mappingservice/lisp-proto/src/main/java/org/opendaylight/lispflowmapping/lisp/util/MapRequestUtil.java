/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.util;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv4Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.binary.address.types.rev160504.augmented.lisp.address.address.Ipv6Binary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.rloc.container.Rloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MapRequestUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(MapRequestUtil.class);

    // Utility class, should not be instantiated
    private MapRequestUtil() {
    }

    public static InetAddress selectItrRloc(MapRequest request) {
        if (request.getItrRloc() == null) {
            return null;
        }
        InetAddress selectedItrRloc = null;
        for (ItrRloc itr : request.getItrRloc()) {
            Address addr = itr.getRloc().getAddress();
            if (addr instanceof Ipv4Binary) {
                try {
                    selectedItrRloc = InetAddress.getByAddress(((Ipv4Binary) addr).getIpv4Binary().getValue());
                } catch (UnknownHostException e) {
                    LOG.debug("Cannot convert Ipv6Binary to InetAddress", e);
                }
                break;
            }
            if (addr instanceof Ipv6Binary) {
                try {
                    selectedItrRloc = InetAddress.getByAddress(((Ipv6Binary) addr).getIpv6Binary().getValue());
                } catch (UnknownHostException e) {
                    LOG.debug("Cannot convert Ipv6Binary to InetAddress", e);
                }
                break;
            }
            if (addr instanceof Ipv4) {
                selectedItrRloc = InetAddresses.forString(((Ipv4) addr).getIpv4().getValue());
                break;
            }
            if (addr instanceof Ipv6) {
                selectedItrRloc = InetAddresses.forString(((Ipv6) addr).getIpv6().getValue());
                break;
            }
        }
        return selectedItrRloc;
    }

    public static MapRequestBuilder prepareSMR(Eid srcEid, Rloc itrRloc) {
        MapRequestBuilder builder = new MapRequestBuilder();
        builder.setAuthoritative(false);
        builder.setMapDataPresent(false);
        builder.setPitr(false);
        builder.setProbe(false);
        builder.setSmr(true);
        builder.setSmrInvoked(false);

        builder.setSourceEid(new SourceEidBuilder().setEid(srcEid).build());
        builder.setItrRloc(new ArrayList<ItrRloc>());
        builder.getItrRloc().add(new ItrRlocBuilder().setRloc(itrRloc).build());
        builder.setMapReply(null);
        builder.setNonce(new Random().nextLong());

        return builder;
    }

}
