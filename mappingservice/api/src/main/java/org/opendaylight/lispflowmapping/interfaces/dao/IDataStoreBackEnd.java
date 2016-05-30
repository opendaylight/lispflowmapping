/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.dao;

import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;

public interface IDataStoreBackEnd extends AutoCloseable, TransactionChainListener {

    void addAuthenticationKey(AuthenticationKey authenticationKey);

    void addMapping(Mapping mapping);

    // This method assumes that it is only called for southbound originated Map-Registers
    void addXtrIdMapping(XtrIdMapping mapping);

    void removeAuthenticationKey(AuthenticationKey authenticationKey);

    void removeMapping(Mapping mapping);

    void removeXtrIdMapping(XtrIdMapping mapping);

    void removeAllDatastoreContent();

    void removeAllConfigDatastoreContent();

    void removeAllOperationalDatastoreContent();

    void updateAuthenticationKey(AuthenticationKey authenticationKey);

    void updateMapping(Mapping mapping);

    List<Mapping> getAllMappings();

    List<Mapping> getAllMappings(LogicalDatastoreType logicalDataStore);

    List<AuthenticationKey> getAllAuthenticationKeys();

}
