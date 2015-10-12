/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LcafSourceDestHelper;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.lispflowmapping.lisp.util.MaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingserviceListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.SiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class MapServer implements IMapServerAsync, MappingserviceListener {

    protected static final Logger LOG = LoggerFactory.getLogger(MapServer.class);
    private IMappingService mapService;
    private boolean authenticate;
    private boolean subscriptionService;
    private IMapNotifyHandler notifyHandler;
    private NotificationService notificationService;

    public MapServer(IMappingService mapService, boolean authenticate, boolean subscriptionService,
            IMapNotifyHandler notifyHandler, NotificationService notificationService) {
        Preconditions.checkNotNull(mapService);
        this.mapService = mapService;
        this.authenticate = authenticate;
        this.subscriptionService = subscriptionService;
        this.notifyHandler = notifyHandler;
        this.notificationService = notificationService;
        if (notificationService != null) {
            notificationService.registerNotificationListener(this);
        }
    }

    @Override
    public void setSubscriptionService(boolean subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public boolean shouldAuthenticate() {
        return authenticate;
    }

    @Override
    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        authenticate = shouldAuthenticate;
    }

    public void handleMapRegister(MapRegister mapRegister) {
        boolean failed = false;
        String password = null;
        for (EidToLocatorRecord record : mapRegister.getEidToLocatorRecord()) {
            if (authenticate) {
                password = mapService.getAuthenticationKey(record.getLispAddressContainer());
                if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                    LOG.warn("Authentication failed");
                    failed = true;
                    break;
                }
            }
            EidToLocatorRecord oldMapping = (EidToLocatorRecord) mapService.getMapping(MappingOrigin.Southbound,
                    record.getLispAddressContainer());
            mapService.addMapping(MappingOrigin.Southbound, record.getLispAddressContainer(), getSiteId(mapRegister),
                    record);
            if (subscriptionService && !record.equals(oldMapping)) {
                LOG.debug("Sending SMRs for subscribers of {}", record.getLispAddressContainer());
                Set<SubscriberRLOC> subscribers = getSubscribers(record.getLispAddressContainer());
                sendSmrs(record, subscribers);
            }
        }
        if (!failed) {
            MapNotifyBuilder builder = new MapNotifyBuilder();
            if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                LOG.trace("MapRegister wants MapNotify");
                MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
                if (authenticate) {
                    builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(),
                            password));
                }
                notifyHandler.handleMapNotify(builder.build());
            }
        }
    }

    private SiteId getSiteId(MapRegister mapRegister) {
        return (mapRegister.getSiteId() != null) ? new SiteId(mapRegister.getSiteId()) : null;
    }

    @Override
    public void onMappingChanged(MappingChanged notification) {
        if (subscriptionService) {
            sendSmrs(new EidToLocatorRecordBuilder(notification.getMapping()).build(), getSubscribers(notification
                    .getMapping().getLispAddressContainer()));
            if (notification.getChange().equals(MappingChange.Removed)) {
                removeSubscribers(notification.getMapping().getLispAddressContainer());
            }
        }
    }

    private void sendSmrs(EidToLocatorRecord record, Set<SubscriberRLOC> subscribers) {
        LispAddressContainer eid = record.getLispAddressContainer();
        handleSmr(record, subscribers, notifyHandler);

        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer dstAddr = LispAFIConvertor.toContainer(LcafSourceDestHelper.getDstAfi(eid));
            short dstMask = LcafSourceDestHelper.getDstMask(eid);
            subscribers = getSubscribers(dstAddr);
            EidToLocatorRecord newRecord = new EidToLocatorRecordBuilder().setAction(record.getAction())
                    .setAuthoritative(record.isAuthoritative()).setLocatorRecord(record.getLocatorRecord())
                    .setMapVersion(record.getMapVersion()).setRecordTtl(record.getRecordTtl())
                    .setLispAddressContainer(dstAddr).setMaskLength(dstMask).build();
            handleSmr(newRecord, subscribers, notifyHandler);
        }
    }

    private void handleSmr(EidToLocatorRecord record, Set<SubscriberRLOC> subscribers, IMapNotifyHandler callback) {
        if (subscribers == null) {
            return;
        }
        MapRequestBuilder mrb = MapRequestUtil.prepareSMR(record.getLispAddressContainer(),
                LispAFIConvertor.toContainer(getLocalAddress()));
        LOG.trace("Built SMR packet: " + mrb.build().toString());
        for (SubscriberRLOC subscriber : subscribers) {
            if (subscriber.timedOut()) {
                LOG.trace("Lazy removing expired subscriber entry " + subscriber.toString());
                subscribers.remove(subscriber);
            } else {
                try {
                    // The address stored in the SMR's EID record is used as Source EID in the SMR-invoked Map-Request.
                    // To ensure consistent behavior it is set to the value used to originally request a given mapping
                    mrb.setEidRecord(new ArrayList<EidRecord>());
                    mrb.getEidRecord()
                            .add(new EidRecordBuilder()
                                    .setMask(
                                            (short) MaskUtil.getMaxMask(LispAFIConvertor.toAFI(subscriber.getSrcEid())))
                                    .setLispAddressContainer(subscriber.getSrcEid()).build());
                    callback.handleSMR(mrb.build(), subscriber.getSrcRloc());
                } catch (Exception e) {
                    LOG.error("Errors encountered while handling SMR:" + ExceptionUtils.getStackTrace(e));
                }
            }
        }
        addSubscribers(record.getLispAddressContainer(), subscribers);
    }

    @SuppressWarnings("unchecked")
    private Set<SubscriberRLOC> getSubscribers(LispAddressContainer address) {
        return (Set<SubscriberRLOC>) mapService.getData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);
    }

    private void removeSubscribers(LispAddressContainer address) {
        mapService.removeData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS);
    }

    private void addSubscribers(LispAddressContainer address, Set<SubscriberRLOC> subscribers) {
        mapService.addData(MappingOrigin.Southbound, address, SubKeys.SUBSCRIBERS, subscribers);
    }

    private static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                LOG.debug("Interface " + current.toString());
                if (!current.isUp() || current.isLoopback() || current.isVirtual())
                    continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress current_addr = addresses.nextElement();
                    // Skip loopback and link local addresses
                    if (current_addr.isLoopbackAddress() || current_addr.isLinkLocalAddress())
                        continue;
                    LOG.debug(current_addr.getHostAddress());
                    return current_addr;
                }
            }
        } catch (SocketException se) {
        }
        return null;
    }
}
