/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.lisp;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.lisp.IMapResolver;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapResolver implements IMapResolver {
    private ILispDAO dao;
    protected static final Logger logger = LoggerFactory.getLogger(MapResolver.class);

    public MapResolver(ILispDAO dao) {
        this.dao = dao;
    }

    public MapReply handleMapRequest(MapRequest request) {
        if (dao == null) {
            logger.warn("handleMapRequest called while dao is uninitialized");
            return null;
        }
        MapReply mapReply = new MapReply();

        EidRecord eid = request.getEids().get(0);
        mapReply.setNonce(request.getNonce());
        EidToLocatorRecord eidToLocators = (EidToLocatorRecord) dao.get(eid.getPrefix()).get("RLOCS");
        mapReply.addEidToLocator(eidToLocators);
        return mapReply;
    }


}
