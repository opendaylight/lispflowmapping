/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.util.DAOMappingUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingKey;
import org.opendaylight.lispflowmapping.interfaces.dao.RLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapRequestResultHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolverAsync;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafTrafficEngineering;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.No;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapreplymessage.MapReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequest.ItrRloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class MapResolver extends AbstractLispComponent implements IMapResolverAsync {

    private static final int TTL_RLOC_TIMED_OUT = 1;
    private static final int TTL_NO_RLOC_KNOWN = 15;
    protected static final Logger LOG = LoggerFactory.getLogger(MapResolver.class);

    private static final ConfigIni configIni = new ConfigIni();
    private static final String elpPolicy = configIni.getElpPolicy();

    public MapResolver(ILispDAO dao) {
        this(dao, true, true);
    }

    public MapResolver(ILispDAO dao, boolean authenticate, boolean iterateMask) {
        super(dao, authenticate, iterateMask);
    }

    public void handleMapRequest(MapRequest request, boolean smr, IMapRequestResultHandler callback) {
        Preconditions.checkNotNull(dao);

        LispAddressContainer srcEid = null;
        LispAFIAddress srcEidAfi = null;
        if (request.getSourceEid() != null) {
            srcEid = request.getSourceEid().getLispAddressContainer();
            srcEidAfi = LispAFIConvertor.toAFI(srcEid);
        }
        MapReplyBuilder builder = new MapReplyBuilder();
        builder.setEchoNonceEnabled(false);
        builder.setProbe(false);
        builder.setSecurityEnabled(false);
        builder.setNonce(request.getNonce());
        builder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        for (EidRecord eid : request.getEidRecord()) {
            EidToLocatorRecordBuilder recordBuilder = new EidToLocatorRecordBuilder();
            Entry<IMappingKey, List<RLOCGroup>> mapping =
                    DAOMappingUtil.getMapping(srcEidAfi, eid, dao);
            recordBuilder.setRecordTtl(0);
            recordBuilder.setAction(Action.NoAction);
            recordBuilder.setAuthoritative(false);
            recordBuilder.setMapVersion((short) 0);
            recordBuilder.setMaskLength((short) mapping.getKey().getMask());
            recordBuilder.setLispAddressContainer(mapping.getKey().getEID());
            recordBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
            List<RLOCGroup> locators = mapping.getValue();
            if (locators != null && locators.size() > 0) {
                List<ItrRloc> itrRlocs = request.getItrRloc();
                addLocatorGroups(recordBuilder, locators, itrRlocs);
                if (srcEid != null && !(srcEid.getAddress() instanceof No) && itrRlocs != null
                        && itrRlocs.size() > 0) {
                    LispAddressContainer itrRloc = itrRlocs.get(0).getLispAddressContainer();
                    SubscriberRLOC subscriberRloc = new SubscriberRLOC(itrRloc, srcEid);
                    Set<SubscriberRLOC> subscribers = DAOMappingUtil.getSubscribers(mapping.getKey().getEID(),
                            mapping.getKey().getMask(), dao);
                    if (subscribers == null) {
                        subscribers = Sets.newConcurrentHashSet();
                    } else if (subscribers.contains(subscriberRloc)) {
                        // If there is an entry already for this subscriberRloc, remove it, so that it gets the new
                        // timestamp
                        subscribers.remove(subscriberRloc);
                    }
                    if (smr) {
                        LOG.trace("Adding new subscriber: " + subscriberRloc.toString());
                        subscribers.add(subscriberRloc);
                        DAOMappingUtil.addSubscribers(mapping.getKey().getEID(), mapping.getKey().getMask(),
                                subscribers, dao);
                    }
                }
            } else {
                recordBuilder.setAction(Action.NativelyForward);
                if (shouldAuthenticate() && DAOMappingUtil.getPassword(eid.getLispAddressContainer(), eid.getMask(),
                        dao, shouldIterateMask()) != null) {
                    recordBuilder.setRecordTtl(TTL_RLOC_TIMED_OUT);
                } else {
                    recordBuilder.setRecordTtl(TTL_NO_RLOC_KNOWN);

                }
            }
            builder.getEidToLocatorRecord().add(recordBuilder.build());
        }
        callback.handleMapReply(builder.build());
    }

    private void addLocatorGroups(EidToLocatorRecordBuilder recordBuilder, List<RLOCGroup> rlocs,
            List<ItrRloc> itrRlocs) {
        for (RLOCGroup rloc : rlocs) {
            addLocators(recordBuilder, rloc, itrRlocs);
            recordBuilder.setRecordTtl(rloc.getTtl());
        }
    }

    private void addLocators(EidToLocatorRecordBuilder recordBuilder, RLOCGroup locatorObject,
            List<ItrRloc> itrRlocs) {
        Preconditions.checkNotNull(locatorObject);

        recordBuilder.setAction(locatorObject.getAction());
        recordBuilder.setAuthoritative(locatorObject.isAuthoritative());
        recordBuilder.setRecordTtl(locatorObject.getTtl());

        try {
            for (LocatorRecord record : locatorObject.getRecords()) {
                LispAddressContainer container = record.getLispAddressContainer();

                // For non-ELP RLOCs, or when ELP policy is default, or itrRlocs is null, just add the locator and be
                // done
                if ((!(container.getAddress() instanceof LcafTrafficEngineering))
                        || elpPolicy.equalsIgnoreCase("default") || itrRlocs == null) {
                    recordBuilder.getLocatorRecord().add(
                            new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                    .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                    .setPriority(record.getPriority()).setMulticastWeight(record.getMulticastWeight())
                                    .setMulticastPriority(record.getMulticastPriority()).setRouted(record.isRouted())
                                    .setLispAddressContainer(container).setName(record.getName()).build());
                    continue;
                }

                LcafTrafficEngineeringAddress teAddress = ((LcafTrafficEngineering) container.getAddress())
                        .getLcafTrafficEngineeringAddr();
                LispAddressContainer nextHop = getNextELPHop(teAddress, itrRlocs);
                if (nextHop != null) {
                    java.lang.Short priority = record.getPriority();
                    if (elpPolicy.equalsIgnoreCase("both")) {
                        recordBuilder.getLocatorRecord().add(
                                new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                        .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                        .setPriority(record.getPriority())
                                        .setMulticastWeight(record.getMulticastWeight())
                                        .setMulticastPriority(record.getMulticastPriority())
                                        .setRouted(record.isRouted()).setLispAddressContainer(container)
                                        .setName(record.getName()).build());
                        // Make the priority of the added simple locator lower so that ELP is used by default if
                        // the xTR understands ELP.  Exclude 255, since that means don't use for unicast forwarding
                        // XXX Complex cases like several ELPs with different priorities are not handled
                        if (priority != 254 || priority !=255) {
                            priority++;
                        }
                    }
                    // Build and add the simple RLOC
                    recordBuilder.getLocatorRecord().add(
                            new LocatorRecordBuilder().setLocalLocator(record.isLocalLocator())
                                    .setRlocProbed(record.isRlocProbed()).setWeight(record.getWeight())
                                    .setPriority(priority).setMulticastWeight(record.getMulticastWeight())
                                    .setMulticastPriority(record.getMulticastPriority()).setRouted(record.isRouted())
                                    .setLispAddressContainer(nextHop).setName(record.getName()).build());
                }
            }
        } catch (ClassCastException cce) {
            LOG.error("Class Cast Exception while building EidToLocatorRecord: {}", ExceptionUtils.getStackTrace(cce));
        }
    }

    private LispAddressContainer getNextELPHop(LcafTrafficEngineeringAddress elp, List<ItrRloc> itrRlocs) {
        LispAddressContainer nextHop = null;
        List<Hops> hops = elp.getHops();

        if (hops != null && hops.size() > 0) {
            // By default we return the first hop
            nextHop = LispAFIConvertor.toContainer(LispAFIConvertor.toAFIfromPrimitive(hops.get(0).getHop()
                    .getPrimitiveAddress()));
            for (Hops hop : hops) {
                LispAddressContainer hopContainer = LispAFIConvertor.toContainer(LispAFIConvertor
                        .toAFIfromPrimitive(hop.getHop().getPrimitiveAddress()));
                for (ItrRloc itrRloc : itrRlocs) {
                    if (itrRloc.getLispAddressContainer().equals(hopContainer)) {
                        int i = hops.indexOf(hop);
                        if (i < hops.size() - 1) {
                            nextHop = LispAFIConvertor.toContainer(LispAFIConvertor.toAFIfromPrimitive(hops.get(i + 1)
                                    .getHop().getPrimitiveAddress()));
                            return nextHop;
                        }
                    }
                }
            }
        }

        return nextHop;
    }

}
