/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.mdsal;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.lispflowmapping.implementation.util.InstanceIdentifierUtil;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

/**
 * Stores data coming from the mapping database RPCs into the config datastore.
 *
 * @author Lorand Jakab
 *
 */
public class DataStoreBackEnd implements TransactionChainListener {
    protected static final Logger LOG = LoggerFactory.getLogger(DataStoreBackEnd.class);

    private DataBroker broker;
    private BindingTransactionChain txChain;

    public DataStoreBackEnd(DataBroker broker) {
        this.broker = broker;
        this.txChain = broker.createTransactionChain(this);
    }

    public void addAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Adding authentication key '{}' for {}", authenticationKey.getMappingAuthkey().getKeyString(),
                LispAddressStringifier.getString(authenticationKey.getEid()));

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        writePutTransaction(path, authenticationKey, LogicalDatastoreType.CONFIGURATION,
                "Adding authentication key to config datastrore failed");
    }

    public void addMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Adding mapping for {}",
                LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        writePutTransaction(path, mapping, LogicalDatastoreType.CONFIGURATION,
                "Adding mapping to config datastrore failed");
    }

    public void removeAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Removing authentication key for {}",
                LispAddressStringifier.getString(authenticationKey.getEid()));

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        deleteTransaction(path, LogicalDatastoreType.CONFIGURATION,
                "Deleting authentication key from config datastrore failed");
    }

    public void removeMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Removing mapping for {}",
                LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        deleteTransaction(path, LogicalDatastoreType.CONFIGURATION, "Deleting mapping from config datastrore failed");
    }

    public void removeAllMappings() {
        LOG.debug("MD-SAL: Removing all mappings");
        InstanceIdentifier<MappingDatabase> path = InstanceIdentifier.create(MappingDatabase.class);
        deleteTransaction(path, LogicalDatastoreType.CONFIGURATION,
                "Removing of all mappings in config datastore failed");
    }

    public void updateAuthenticationKey(AuthenticationKey authenticationKey) {
        LOG.debug("MD-SAL: Updating authentication key for {} with '{}'",
                LispAddressStringifier.getString(authenticationKey.getEid()),
                authenticationKey.getMappingAuthkey().getKeyString());

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        writePutTransaction(path, authenticationKey, LogicalDatastoreType.CONFIGURATION,
                "Updating authentication key in config datastrore failed");
    }

    public void updateMapping(Mapping mapping) {
        LOG.debug("MD-SAL: Updating mapping for {}",
                LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        writePutTransaction(path, mapping, LogicalDatastoreType.CONFIGURATION,
                "Updating mapping in config datastrore failed");
    }

    public List<Mapping> getAllMappings() {
        LOG.debug("MD-SAL: Get all mappings from datastore");
        List<Mapping> mappings = new ArrayList<Mapping>();
        InstanceIdentifier<MappingDatabase> path = InstanceIdentifier.create(MappingDatabase.class);
        MappingDatabase mdb = readTransaction(path, LogicalDatastoreType.CONFIGURATION);

        if (mdb != null) {
            for (VirtualNetworkIdentifier id : mdb.getVirtualNetworkIdentifier()) {
                List<Mapping> ms = id.getMapping();
                if (ms != null) {
                    mappings.addAll(ms);
                }
            }
        }

        return mappings;
    }

    public List<AuthenticationKey> getAllAuthenticationKeys() {
        LOG.debug("MD-SAL: Get all authentication keys from datastore");
        List<AuthenticationKey> authKeys = new ArrayList<AuthenticationKey>();
        InstanceIdentifier<MappingDatabase> path = InstanceIdentifier.create(MappingDatabase.class);
        MappingDatabase mdb = readTransaction(path, LogicalDatastoreType.CONFIGURATION);

        if (mdb != null) {
            for (VirtualNetworkIdentifier id : mdb.getVirtualNetworkIdentifier()) {
                List<AuthenticationKey> keys = id.getAuthenticationKey();
                if (keys != null) {
                    authKeys.addAll(keys);
                }
            }
        }

        return authKeys;
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void writePutTransaction(
            InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType, String errMsg) {
        WriteTransaction writeTx = txChain.newWriteOnlyTransaction();
        writeTx.put(logicalDatastoreType, addIID, data, true);
        Futures.addCallback(writeTx.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
            @Override
            public void onFailure(Throwable t) {
                LOG.error("Transaction failed:", t);
            }
        });
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> U readTransaction(
            InstanceIdentifier<U> readIID, LogicalDatastoreType logicalDatastoreType) {
        U ret = null;
        ReadOnlyTransaction readTx = txChain.newReadOnlyTransaction();
        Optional<U> optionalDataObject;
        CheckedFuture<Optional<U>, ReadFailedException> submitFuture = readTx.read(logicalDatastoreType, readIID);
        try {
            optionalDataObject = submitFuture.checkedGet();
            if (optionalDataObject != null && optionalDataObject.isPresent()) {
                ret = optionalDataObject.get();
            } else {
                LOG.debug("{}: Failed to read", Thread.currentThread().getStackTrace()[1]);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to ....", e);
        }
        return ret;
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void deleteTransaction(
            InstanceIdentifier<U> deleteIID, LogicalDatastoreType logicalDatastoreType, String errMsg) {

        WriteTransaction writeTx = txChain.newWriteOnlyTransaction();
        writeTx.delete(logicalDatastoreType, deleteIID);
        Futures.addCallback(writeTx.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
            @Override
            public void onFailure(Throwable t) {
                LOG.error("Transaction failed:", t);
            }
        });
    }

    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction,
            Throwable cause) {
        LOG.error("Broken chain {} in DataStoreBackEnd, transaction {}, cause {}", chain, transaction.getIdentifier(),
                cause);
    }

    @Override
    public void onTransactionChainSuccessful(TransactionChain<?, ?> chain) {
        LOG.info("DataStoreBackEnd closed successfully, chain {}", chain);
    }
}
