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
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.util.DAOMappingUtil;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.SubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.lisp.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.lisp.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.lisp.util.MapRequestUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev150820.maprequestnotification.MapRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer extends AbstractLispComponent implements IMapServerAsync {

    protected static final Logger LOG = LoggerFactory.getLogger(MapServer.class);

    private static final ConfigIni configIni = new ConfigIni();
    private static final boolean overwriteConfig = configIni.mappingOverwriteIsSet();
    private boolean overwrite;

    public MapServer(ILispDAO dao) {
        this(dao, overwriteConfig);
    }

    public MapServer(ILispDAO dao, boolean overwrite) {
        this(dao, overwrite, true);
    }

    public MapServer(ILispDAO dao, boolean overwrite, boolean authenticate) {
        this(dao, overwrite, authenticate, true);
    }

    public MapServer(ILispDAO dao, boolean overwrite, boolean authenticate, boolean iterateAuthenticationMask) {
        super(dao, authenticate, iterateAuthenticationMask);
        this.overwrite = overwrite;
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

    public void handleMapRegister(MapRegister mapRegister, boolean smr, IMapNotifyHandler callback) {
        if (dao == null) {
            LOG.warn("handleMapRegister called while dao is uninitialized");
        } else {
            boolean failed = false;
            String password = null;
            for (EidToLocatorRecord eidRecord : mapRegister.getEidToLocatorRecord()) {
                if (shouldAuthenticate()) {
                    password = DAOMappingUtil.getPassword(eidRecord.getLispAddressContainer(),
                            eidRecord.getMaskLength(), dao, shouldIterateMask());
                    if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                        LOG.warn("Authentication failed");
                        failed = true;
                        break;
                    }
                }
                boolean mappingChanged = DAOMappingUtil.saveRlocs(eidRecord, smr, dao, shouldIterateMask(),
                        shouldOverwrite());
                if (smr && mappingChanged) {
                    Set<SubscriberRLOC> subscribers = DAOMappingUtil.getSubscribers(
                            eidRecord.getLispAddressContainer(), eidRecord.getMaskLength(), dao);
                    sendSmrs(eidRecord, subscribers, callback);
                }
            }
            if (!failed) {
                MapNotifyBuilder builder = new MapNotifyBuilder();
                if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                    LOG.trace("MapRegister wants MapNotify");
                    MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
                    if (shouldAuthenticate()) {
                        builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(),
                                password));
                    }
                    callback.handleMapNotify(builder.build());
                }
            }
        }
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return DAOMappingUtil.getPassword(address, maskLen, dao, shouldIterateMask());
    }

    public void removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        DAOMappingUtil.removeAuthenticationKey(address, maskLen, dao);
    }

    public void removeMapping(LispAddressContainer address, int maskLen, boolean smr, IMapNotifyHandler callback) {
        if (smr) {
            // mapping is removed before first SMR is sent to avoid inconsistent replies
            DAOMappingUtil.removeMapping(address, maskLen, dao, shouldOverwrite());
            Set<SubscriberRLOC> subscribers = DAOMappingUtil.getSubscribers(address, maskLen, dao);
            sendSmrs(new EidToLocatorRecordBuilder().setLispAddressContainer(address).
                    setMaskLength((short) maskLen).build(), subscribers, callback);
            DAOMappingUtil.removeSubscribers(address, maskLen, dao, shouldOverwrite());
        } else {
            DAOMappingUtil.removeMapping(address, maskLen, dao, shouldOverwrite());
            DAOMappingUtil.removeSubscribers(address, maskLen, dao, shouldOverwrite());
        }
    }

    private void sendSmrs(EidToLocatorRecord record, Set<SubscriberRLOC> subscribers, IMapNotifyHandler callback) {
        LispAddressContainer eid = record.getLispAddressContainer();
        handleSmr(record, subscribers, callback);

        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer dstAddr = LispAFIConvertor.toContainer(DAOMappingUtil.getDstForLcafSrcDst(eid));
            short dstMask = DAOMappingUtil.getDstMaskForLcafSrcDst(eid);
            subscribers = DAOMappingUtil.getSubscribers(dstAddr, dstMask, dao);
            EidToLocatorRecord newRecord = new EidToLocatorRecordBuilder().setAction(record.getAction()).
                    setAuthoritative(record.isAuthoritative()).setLocatorRecord(record.getLocatorRecord()).
                    setMapVersion(record.getMapVersion()).setRecordTtl(record.getRecordTtl()).
                    setLispAddressContainer(dstAddr).setMaskLength(dstMask).build();
            handleSmr(newRecord, subscribers, callback);
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
                    mrb.getEidRecord().add(new EidRecordBuilder()
                                .setMask((short)MaskUtil.getMaxMask(LispAFIConvertor.toAFI(subscriber.getSrcEid())))
                                .setLispAddressContainer(subscriber.getSrcEid()).build());
                    callback.handleSMR(mrb.build(), subscriber.getSrcRloc());
                } catch (Exception e) {
                    LOG.error("Errors encountered while handling SMR:" + ExceptionUtils.getStackTrace(e));
                }
            }
        }
        DAOMappingUtil.addSubscribers(record.getLispAddressContainer(), record.getMaskLength(), subscribers, dao);
    }

    public boolean shouldOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        DAOMappingUtil.addAuthenticationKey(address, maskLen, key, dao);
    }

}
