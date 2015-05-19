/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.lispflowmapping.implementation.util.InstanceIdentifierUtil;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mapping.database.rev150314.db.instance.Mapping;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.CheckedFuture;

/**
 * Stores data coming from the mapping database RPCs into the config datastore.
 *
 * @author Lorand Jakab
 *
 */
public class DataStoreBackEnd {
    protected static final Logger LOG = LoggerFactory.getLogger(DataStoreBackEnd.class);

    private DataBroker broker;

    public DataStoreBackEnd(DataBroker broker) {
        this.broker = broker;
    }

    public void addAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Adding authentication key '{}' for {}/{}", authenticationKey.getAuthkey(),
                LispAFIConvertor.toString(authenticationKey.getLispAddressContainer()),
                authenticationKey.getMaskLength());

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getLispAddressContainer());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, path, authenticationKey, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Adding authentication key to config datastrore failed");
    }

    public void addMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Adding mapping for {}/{}",
                LispAFIConvertor.toString(mapping.getLispAddressContainer()), mapping.getMaskLength());

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getLispAddressContainer(), mapping.getOrigin());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, path, mapping, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Adding mapping to config datastrore failed");
    }

    public void removeAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Removing authentication key for {}/{}",
                LispAFIConvertor.toString(authenticationKey.getLispAddressContainer()),
                authenticationKey.getMaskLength());

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getLispAddressContainer());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, path);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Deleting authentication key from config datastrore failed");
    }

    public void removeMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Removing mapping for {}/{}",
                LispAFIConvertor.toString(mapping.getLispAddressContainer()), mapping.getMaskLength());

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getLispAddressContainer(), mapping.getOrigin());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, path);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Deleting mapping from config datastrore failed");
    }

    public void updateAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Updating authentication key for {}/{} with '{}'",
                LispAFIConvertor.toString(authenticationKey.getLispAddressContainer()),
                authenticationKey.getMaskLength(), authenticationKey.getAuthkey());

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getLispAddressContainer());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, path, authenticationKey, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Updating authentication key in config datastrore failed");
    }

    public void updateMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Updating mapping for {}/{}",
                LispAFIConvertor.toString(mapping.getLispAddressContainer()), mapping.getMaskLength());

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getLispAddressContainer(), mapping.getOrigin());
        WriteTransaction transaction = broker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, path, mapping, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        checkTransaction(future, "Updating mapping in config datastrore failed");
    }

    void checkTransaction(CheckedFuture<Void, TransactionCommitFailedException> future, String errMsg) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn(errMsg + e);
        }
    }
}
