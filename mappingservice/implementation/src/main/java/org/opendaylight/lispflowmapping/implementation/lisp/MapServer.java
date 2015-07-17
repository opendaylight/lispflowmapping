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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.DAOMappingUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceSubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRegister;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.MapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispsimpleaddress.primitiveaddress.DistinguishedName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.maprequestnotification.MapRequestBuilder;
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

    private static MapRequest buildSMR(EidToLocatorRecord eidRecord) {
        MapRequestBuilder builder = new MapRequestBuilder();
        builder.setAuthoritative(false);
        builder.setMapDataPresent(false);
        builder.setPitr(false);
        builder.setProbe(false);
        builder.setSmr(true);
        builder.setSmrInvoked(false);

        builder.setEidRecord(new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.eidrecords.EidRecord>());
        LispAddressContainer container = eidRecord.getLispAddressContainer();
        short mask = (short) eidRecord.getMaskLength();
        builder.getEidRecord().add(new EidRecordBuilder().setMask(mask).setLispAddressContainer(container).build());


        builder.setItrRloc(new ArrayList<ItrRloc>());
        builder.getItrRloc().add(new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(getLocalAddress())).build());

        builder.setMapReply(null);
        builder.setNonce(new Random().nextLong());

        // XXX For now we set source EID to queried EID...
        builder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(container).build());
        return builder.build();
    }

    public void handleMapRegister(MapRegister mapRegister, boolean smr, IMapNotifyHandler callback) {
        if (dao == null) {
            LOG.warn("handleMapRegister called while dao is uninitialized");
        } else {
            boolean failed = false;
            String password = null;
            for (EidToLocatorRecord eidRecord : mapRegister.getEidToLocatorRecord()) {
                if (shouldAuthenticate()) {
                    password = getPassword(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
                    if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                        LOG.warn("Authentication failed");
                        failed = true;
                        break;
                    }
                }
                boolean mappingChanged = saveRlocs(eidRecord, smr);
                if (smr && mappingChanged) {
                    sendSmrs(eidRecord, callback);
                }
            }
            if (!failed) {
                MapNotifyBuilder builder = new MapNotifyBuilder();
                if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                    LOG.trace("MapRegister wants MapNotify");
                    MapNotifyBuilderHelper.setFromMapRegister(builder, mapRegister);
                    if (shouldAuthenticate()) {
                        builder.setAuthenticationData(LispAuthenticationUtil.createAuthenticationData(builder.build(), password));
                    }
                    callback.handleMapNotify(builder.build());
                }
            }
        }
    }

    public boolean saveRlocs(EidToLocatorRecord eidRecord, boolean checkForChanges) {
        Map<String, MappingServiceRLOCGroup> rlocGroups = new HashMap<String, MappingServiceRLOCGroup>();
        if (eidRecord.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : eidRecord.getLocatorRecord()) {
                String subkey = getAddressKey(locatorRecord.getLispAddressContainer().getAddress());
                if (!rlocGroups.containsKey(subkey)) {
                    rlocGroups.put(subkey, new MappingServiceRLOCGroup(eidRecord.getRecordTtl(), eidRecord.getAction(), eidRecord.isAuthoritative()));
                }
                rlocGroups.get(subkey).addRecord(locatorRecord);
            }
        }
        List<MappingEntry<MappingServiceRLOCGroup>> entries = new ArrayList<>();
        for (String subkey : rlocGroups.keySet()) {
            entries.add(new MappingEntry<>(subkey, rlocGroups.get(subkey)));
        }

        if (eidRecord.getLispAddressContainer().getAddress() instanceof LcafSourceDest) {
            Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> oldMapping= null, newMapping = null;
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(eidRecord.getLispAddressContainer());
            LispAFIAddress dstAddr = getDstForLcafSrcDst(eidRecord.getLispAddressContainer());
            short srcMask = getSrcMaskForLcafSrcDst(eidRecord.getLispAddressContainer());
            short dstMask = getDstMaskForLcafSrcDst(eidRecord.getLispAddressContainer());

            if (checkForChanges) {
                oldMapping = DAOMappingUtil.getMappingExact(srcAddr, dstAddr, srcMask, dstMask, dao);
            }
            IMappingServiceKey dstKey = MappingServiceKeyUtil.generateMappingServiceKey(dstAddr, dstMask);
            ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(dstKey, LCAF_SRCDST_SUBKEY);
            if (srcDstDao == null) {
                srcDstDao = new HashMapDb();
                dao.put(dstKey, new MappingEntry<>(LCAF_SRCDST_SUBKEY, srcDstDao));
            }
            IMappingServiceKey srcKey = MappingServiceKeyUtil.generateMappingServiceKey(srcAddr, srcMask);
            srcDstDao.put(srcKey, entries.toArray(new MappingEntry[entries.size()]));
            if (checkForChanges) {
                newMapping = DAOMappingUtil.getMappingExact(srcAddr, dstAddr, srcMask, dstMask, dao);
                return (newMapping.getValue() == null) ? oldMapping.getValue() != null :
                                                        !newMapping.getValue().equals(oldMapping.getValue());
            }
        } else {
            List<MappingServiceRLOCGroup> oldLocators = null, newLocators = null;
            if (checkForChanges) {
                oldLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterateMask());
            }
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eidRecord.getLispAddressContainer(),
                    eidRecord.getMaskLength());
            dao.put(key, entries.toArray(new MappingEntry[entries.size()]));
            if (checkForChanges) {
                newLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterateMask());
                return (newLocators == null) ? oldLocators != null : !newLocators.equals(oldLocators);
            }
        }
        return false;
    }

    private String getAddressKey(Address address) {
        if (address instanceof LcafKeyValue) {
            LcafKeyValue keyVal = (LcafKeyValue) address;
            if (keyVal.getLcafKeyValueAddressAddr().getKey().getPrimitiveAddress() instanceof DistinguishedName) {
                return ((DistinguishedName) keyVal.getLcafKeyValueAddressAddr().getKey().getPrimitiveAddress()).getDistinguishedNameAddress().getDistinguishedName();
            }
        }
        if (shouldOverwrite()) {
            return ADDRESS_SUBKEY;
        } else {
            return String.valueOf(address.hashCode());
        }
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return getPassword(address, maskLen);
    }

    public void removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        if (address.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSrcDstInnerDao(address, maskLen);
            if (srcDstDao != null) {
                IMappingServiceKey srcKey = MappingServiceKeyUtil.generateMappingServiceKey(getSrcForLcafSrcDst(address),
                        getSrcMaskForLcafSrcDst(address));
                srcDstDao.removeSpecific(srcKey, PASSWORD_SUBKEY);
            }
        } else {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
            dao.removeSpecific(key, PASSWORD_SUBKEY);
        }
    }

    public void addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        IMappingServiceKey mappingServiceKey = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        if (address.getAddress() instanceof LcafSourceDest) {
            IMappingServiceKey srcKey = MappingServiceKeyUtil.generateMappingServiceKey(getSrcForLcafSrcDst(address),
                    getSrcMaskForLcafSrcDst(address));
            ILispDAO srcDstDao = getOrInstantiateSrcDstInnerDao(address, maskLen);
            srcDstDao.put(srcKey, new MappingEntry<String>(PASSWORD_SUBKEY, key));
        } else {
            dao.put(mappingServiceKey, new MappingEntry<String>(PASSWORD_SUBKEY, key));
        }
    }

    public void removeMapping(LispAddressContainer address, int maskLen, boolean smr, IMapNotifyHandler callback) {
        Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> mapping;
        ILispDAO db;
        if (address.getAddress() instanceof LcafSourceDest) {
            db = getSrcDstInnerDao(address, maskLen);
            LispAFIAddress srcAddr = getSrcForLcafSrcDst(address);
            short srcMask = getSrcMaskForLcafSrcDst(address);
            mapping = DAOMappingUtil.getMappingForEid(srcAddr, srcMask, db);
        } else {
            db = dao;
            mapping = DAOMappingUtil.getMappingForEid(LispAFIConvertor.toAFI(address), maskLen, db);
        }
        if (smr) {
            HashSet<MappingServiceSubscriberRLOC> subscribers = getSubscribers(address, maskLen);
            // mapping is removed before first SMR is sent to avoid inconsistent replies
            removeMappingRlocs(mapping, db);
            handleSmr(new EidToLocatorRecordBuilder().setLispAddressContainer(address).
                    setMaskLength((short) maskLen).build(), subscribers, callback);
            db.removeSpecific(mapping.getKey(), SUBSCRIBERS_SUBKEY);
        } else {
            removeMappingRlocs(mapping, db);
            db.removeSpecific(mapping.getKey(), SUBSCRIBERS_SUBKEY);
        }
    }

    private void removeMappingRlocs(Entry<IMappingServiceKey, List<MappingServiceRLOCGroup>> mapping, ILispDAO db) {
        if (mapping == null || mapping.getValue() == null) {
            return;
        }
        for (MappingServiceRLOCGroup group : mapping.getValue()) {
            for (LocatorRecord record : group.getRecords()) {
                db.removeSpecific(mapping.getKey(), getAddressKey(record.getLispAddressContainer().getAddress()));
            }
        }
    }

    private void sendSmrs(EidToLocatorRecord record, IMapNotifyHandler callback) {
        LispAddressContainer eid = record.getLispAddressContainer();
        HashSet<MappingServiceSubscriberRLOC> subscribers;

        subscribers = getSubscribers(eid, record.getMaskLength());
        handleSmr(record, subscribers, callback);

        // For SrcDst LCAF also send SMRs to Dst prefix
        if (eid.getAddress() instanceof LcafSourceDest) {
            LispAddressContainer dstAddr = LispAFIConvertor.toContainer(getDstForLcafSrcDst(eid));
            short dstMask = getDstMaskForLcafSrcDst(eid);
            subscribers = getSubscribers(dstAddr, dstMask);
            EidToLocatorRecord newRecord = new EidToLocatorRecordBuilder().setAction(record.getAction()).
                    setAuthoritative(record.isAuthoritative()).setLocatorRecord(record.getLocatorRecord()).
                    setMapVersion(record.getMapVersion()).setRecordTtl(record.getRecordTtl()).
                    setLispAddressContainer(dstAddr).setMaskLength(dstMask).build();
            handleSmr(newRecord, subscribers, callback);
        }
    }

    private void handleSmr(EidToLocatorRecord record, HashSet<MappingServiceSubscriberRLOC> subscribers,
            IMapNotifyHandler callback) {
        if (subscribers == null) {
            return;
        }
        MapRequest mapRequest = buildSMR(record);
        LOG.trace("Built SMR packet: " + mapRequest.toString());
        for (MappingServiceSubscriberRLOC rloc : subscribers) {
            if (rloc.timedOut()) {
                LOG.trace("Lazy removing expired subscriber entry " + rloc.toString());
                subscribers.remove(rloc);
            } else {
                try {
                    callback.handleSMR(mapRequest, rloc.getSrcRloc());
                } catch (Exception e) {
                    LOG.error("Errors encountered while handling SMR:" + ExceptionUtils.getStackTrace(e));
                }
            }
        }
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(record.getLispAddressContainer(),
                record.getMaskLength());
        dao.put(key, new MappingEntry<HashSet<MappingServiceSubscriberRLOC>>(SUBSCRIBERS_SUBKEY, subscribers));
    }

    public boolean shouldOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
