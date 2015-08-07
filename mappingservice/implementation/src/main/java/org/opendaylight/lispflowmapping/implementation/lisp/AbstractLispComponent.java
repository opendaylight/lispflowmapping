/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.lisp;

import java.util.Set;

import org.opendaylight.lispflowmapping.implementation.dao.MappingServiceKeyUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.util.MaskUtil;
import org.opendaylight.lispflowmapping.inmemorydb.HashMapDb;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IMappingServiceKey;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingEntry;
import org.opendaylight.lispflowmapping.interfaces.dao.MappingServiceSubscriberRLOC;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.LispAFIAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.control.plane.rev150314.lispaddress.lispaddresscontainer.address.LcafSourceDest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLispComponent {

    public static final String PASSWORD_SUBKEY = "password";
    public static final String ADDRESS_SUBKEY = "address";
    public static final String SUBSCRIBERS_SUBKEY = "subscribers";
    public static final String LCAF_SRCDST_SUBKEY = "lcaf_srcdst";
    public static final Logger LOG = LoggerFactory.getLogger(AbstractLispComponent.class);

    protected ILispDAO dao;
    protected volatile boolean iterateMask;
    protected volatile boolean authenticate;

    public AbstractLispComponent(ILispDAO dao, boolean authenticate, boolean iterateMask) {
        this.dao = dao;
        this.iterateMask = iterateMask;
        this.authenticate = authenticate;
    }

    public boolean shouldIterateMask() {
        return iterateMask;
    }

    public void setShouldIterateMask(boolean shouldIterateMask) {
        this.iterateMask = shouldIterateMask;
    }

    public boolean shouldAuthenticate() {
        return authenticate;
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.authenticate = shouldAuthenticate;
    }

    protected String getPassword(LispAddressContainer prefix, int maskLength) {
        if (MaskUtil.isMaskable(LispAFIConvertor.toAFI(prefix))) {
            return getPasswordForMaskable(prefix, maskLength, dao);
        } else if (prefix.getAddress() instanceof LcafSourceDest) {
            ILispDAO srcDstDao = getSrcDstInnerDao(prefix, maskLength);
            if (srcDstDao != null) {
                return getPasswordForMaskable(LispAFIConvertor.toContainer(getSrcForLcafSrcDst(prefix)),
                        getSrcMaskForLcafSrcDst(prefix), srcDstDao);
            }
            return null;
        } else {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
            Object password = dao.getSpecific(key, PASSWORD_SUBKEY);
            if (password != null && password instanceof String) {
                return (String) password;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
    }

    private String getPasswordForMaskable(LispAddressContainer prefix, int maskLength, ILispDAO db) {
        while (maskLength >= 0) {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
            Object password = db.getSpecific(key, PASSWORD_SUBKEY);
            if (password != null && password instanceof String) {
                return (String) password;
            } else if (shouldIterateMask()) {
                maskLength -= 1;
            } else {
                LOG.warn("Failed to find password!");
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Set<MappingServiceSubscriberRLOC> getSubscribers(LispAddressContainer prefix, int maskLength) {
        Object subscribers;
        if (prefix.getAddress() instanceof LcafSourceDest) {
            IMappingServiceKey srcKey = MappingServiceKeyUtil.generateMappingServiceKey(getSrcForLcafSrcDst(prefix),
                    getSrcMaskForLcafSrcDst(prefix));
            ILispDAO srcDstDao = getSrcDstInnerDao(prefix, maskLength);
            subscribers = srcDstDao.getSpecific(srcKey, SUBSCRIBERS_SUBKEY);
        } else {
            IMappingServiceKey key = MappingServiceKeyUtil.generateMappingServiceKey(prefix, maskLength);
            subscribers = dao.getSpecific(key, SUBSCRIBERS_SUBKEY);
        }

        if (subscribers != null && subscribers instanceof Set<?>) {
            return (Set<MappingServiceSubscriberRLOC>) subscribers;
        }
        return null;
    }

    protected static LispAFIAddress getSrcForLcafSrcDst(LispAddressContainer container) {
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().
                getSrcAddress().getPrimitiveAddress());
    }

    protected static LispAFIAddress getDstForLcafSrcDst(LispAddressContainer container) {
        return LispAFIConvertor.toAFIfromPrimitive(((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().
                getDstAddress().getPrimitiveAddress());
    }

    protected static short getSrcMaskForLcafSrcDst(LispAddressContainer container) {
        return ((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().getSrcMaskLength();
    }

    protected static short getDstMaskForLcafSrcDst(LispAddressContainer container) {
        return ((LcafSourceDest) container.getAddress()).getLcafSourceDestAddr().getDstMaskLength();
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src. This method returns the DAO
    // associated to a dst or creates it if it doesn't exist.
    protected ILispDAO getOrInstantiateSrcDstInnerDao(LispAddressContainer address, int maskLen) {
        IMappingServiceKey dstKey = MappingServiceKeyUtil.generateMappingServiceKey(getDstForLcafSrcDst(address),
                getDstMaskForLcafSrcDst(address));
        ILispDAO srcDstDao = (ILispDAO) dao.getSpecific(dstKey, LCAF_SRCDST_SUBKEY);
        if (srcDstDao == null) {
            srcDstDao = new HashMapDb();
            dao.put(dstKey, new MappingEntry<>(LCAF_SRCDST_SUBKEY, srcDstDao));
        }
        return srcDstDao;
    }

    // SrcDst LCAFs are stored in a 2-tier DAO with dst having priority over src. This method returns the DAO
    // associated to a dst or null if it doesn't exist.
    protected ILispDAO getSrcDstInnerDao(LispAddressContainer address, int maskLen) {
        IMappingServiceKey dstKey = MappingServiceKeyUtil.generateMappingServiceKey(getDstForLcafSrcDst(address),
                getDstMaskForLcafSrcDst(address));
        return (ILispDAO) dao.getSpecific(dstKey, LCAF_SRCDST_SUBKEY);
    }
}
