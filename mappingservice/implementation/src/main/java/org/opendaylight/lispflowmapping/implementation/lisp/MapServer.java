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

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.lispflowmapping.implementation.authentication.LispAuthenticationUtil;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.DAOMappingUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.MapNotifyBuilderHelper;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceRLOCGroup;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceSubscriberRLOC;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapNotifyHandler;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapServerAsync;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafKeyValue;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.DistinguishedName;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServer extends AbstractLispComponent implements IMapServerAsync {

    protected static final Logger logger = LoggerFactory.getLogger(MapServer.class);

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
                logger.debug("Interface " + current.toString());
                if (!current.isUp() || current.isLoopback() || current.isVirtual())
                    continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress current_addr = addresses.nextElement();
                    // Skip loopback and link local addresses
                    if (current_addr.isLoopbackAddress() || current_addr.isLinkLocalAddress())
                        continue;
                    logger.debug(current_addr.getHostAddress());
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

        builder.setEidRecord(new ArrayList<org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord>());
        LispAddressContainer container = eidRecord.getLispAddressContainer();
        builder.getEidRecord().add(new EidRecordBuilder().setMask((short) eidRecord.getMaskLength()).setLispAddressContainer(container).build());

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
            logger.warn("handleMapRegister called while dao is uninitialized");
        } else {
            boolean failed = false;
            String password = null;
            for (EidToLocatorRecord eidRecord : mapRegister.getEidToLocatorRecord()) {
                if (shouldAuthenticate()) {
                    password = getPassword(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
                    if (!LispAuthenticationUtil.validate(mapRegister, password)) {
                        logger.warn("Authentication failed");
                        failed = true;
                        break;
                    }
                }
                boolean mappingChanged = saveRlocs(eidRecord, smr);

                if (smr && mappingChanged) {
                    HashSet<MappingServiceSubscriberRLOC> subscribers = getSubscribers(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
                    if (subscribers != null) {
                        MapRequest mapRequest = buildSMR(eidRecord);
                        logger.trace("Built SMR packet: " + mapRequest.toString());
                        for (MappingServiceSubscriberRLOC rloc : subscribers) {
                            if (rloc.timedOut()) {
                                logger.trace("Lazy removing expired subscriber entry " + rloc.toString());
                                subscribers.remove(rloc);
                            } else {
                                try {
                                    callback.handleSMR(mapRequest, rloc.getSrcRloc());
                                } catch (Exception e) {
                                    logger.error("Errors encountered while handling SMR:" + e.getStackTrace());
                                }
                            }
                        }
                        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eidRecord.getLispAddressContainer(),
                                eidRecord.getMaskLength());
                        dao.put(key, new MappingEntry<HashSet<MappingServiceSubscriberRLOC>>(SUBSCRIBERS_SUBKEY, subscribers));
                    }
                }

            }
            if (!failed) {
                MapNotifyBuilder builder = new MapNotifyBuilder();
                if (BooleanUtils.isTrue(mapRegister.isWantMapNotify())) {
                    logger.trace("MapRegister wants MapNotify");
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
        List<MappingServiceRLOCGroup> oldLocators = null, newLocators = null;
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(eidRecord.getLispAddressContainer(), eidRecord.getMaskLength());
        Map<String, MappingServiceRLOCGroup> rlocGroups = new HashMap<String, MappingServiceRLOCGroup>();
        if (eidRecord.getLocatorRecord() != null) {
            for (LocatorRecord locatorRecord : eidRecord.getLocatorRecord()) {
                String subkey = getLocatorKey(locatorRecord);
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
        if (checkForChanges) {
            oldLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterateMask());
        }
        dao.put(key, entries.toArray(new MappingEntry[entries.size()]));
        if (checkForChanges) {
            newLocators = DAOMappingUtil.getLocatorsByEidToLocatorRecord(eidRecord, dao, shouldIterateMask());
            if (!newLocators.equals(oldLocators)) {
                return true;
            }
        }
        return false;
    }

    private String getLocatorKey(LocatorRecord locatorRecord) {
        if (locatorRecord.getLispAddressContainer().getAddress() instanceof LcafKeyValue) {
            LcafKeyValue keyVal = (LcafKeyValue) locatorRecord.getLispAddressContainer().getAddress();
            if (keyVal.getKey().getPrimitiveAddress() instanceof DistinguishedName) {
                return ((DistinguishedName) keyVal.getKey().getPrimitiveAddress()).getDistinguishedName();
            }
        }
        if (shouldOverwrite()) {
            return ADDRESS_SUBKEY;
        } else {
            return String.valueOf(locatorRecord.getLispAddressContainer().getAddress().hashCode());
        }
    }

    public String getAuthenticationKey(LispAddressContainer address, int maskLen) {
        return getPassword(address, maskLen);
    }

    public void removeAuthenticationKey(LispAddressContainer address, int maskLen) {
        IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        dao.removeSpecific(key, PASSWORD_SUBKEY);
    }

    public void addAuthenticationKey(LispAddressContainer address, int maskLen, String key) {
        IMappingServiceKey mappingServiceKey = MappingServiceKeyUtil.generateMappingServiceKey(address, maskLen);
        dao.put(mappingServiceKey, new MappingEntry<String>(PASSWORD_SUBKEY, key));
    }

    public boolean shouldOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
