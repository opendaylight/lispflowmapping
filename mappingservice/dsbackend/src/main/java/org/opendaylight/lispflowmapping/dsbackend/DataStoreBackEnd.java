/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.dsbackend;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.Date;
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
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingDatabase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.AuthenticationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.Mapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.db.instance.mapping.XtrIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.LastUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.LastUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.mapping.database.VirtualNetworkIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores data coming from the mapping database RPCs into the MD-SAL datastore.
 *
 * @author Lorand Jakab
 *
 */
public class DataStoreBackEnd implements TransactionChainListener {
    protected static final Logger LOG = LoggerFactory.getLogger(DataStoreBackEnd.class);
    private static final InstanceIdentifier<MappingDatabase> DATABASE_ROOT =
            InstanceIdentifier.create(MappingDatabase.class);
    private static final InstanceIdentifier<LastUpdated> LAST_UPDATED =
            InstanceIdentifier.create(MappingDatabase.class).child(LastUpdated.class);
    private BindingTransactionChain txChain;

    public DataStoreBackEnd(DataBroker broker) {
        this.txChain = broker.createTransactionChain(this);
    }


    public void addAuthenticationKey(AuthenticationKey authenticationKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Adding authentication key '{}' for {}",
                    authenticationKey.getMappingAuthkey().getKeyString(),
                    LispAddressStringifier.getString(authenticationKey.getEid()));
        }

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        writePutTransaction(path, authenticationKey, LogicalDatastoreType.CONFIGURATION,
                "Adding authentication key to MD-SAL datastore failed");
    }

    public void addMapping(Mapping mapping) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Adding mapping for {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));
        }

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        writePutTransaction(path, mapping, getDestinationDatastore(mapping),
                "Adding mapping to MD-SAL datastore failed");
    }

    // This method assumes that it is only called for southbound originated Map-Registers
    public void addXtrIdMapping(XtrIdMapping mapping) {
        XtrId xtrId = mapping.getMappingRecord().getXtrId();
        Preconditions.checkNotNull(xtrId, "Make sure you only call addXtrIdMapping when the MappingRecord "
                + "contains an xTR-ID");
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Adding mapping for {}, xTR-ID {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()), xtrId);
        }

        InstanceIdentifier<XtrIdMapping> path = InstanceIdentifierUtil
                .createXtrIdMappingIid(mapping.getMappingRecord().getEid(), MappingOrigin.Southbound, xtrId);
        writePutTransaction(path, mapping, LogicalDatastoreType.OPERATIONAL,
                "Adding xTR-ID mapping to MD-SAL datastore failed");
    }

    public void removeAuthenticationKey(AuthenticationKey authenticationKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Removing authentication key for {}",
                    LispAddressStringifier.getString(authenticationKey.getEid()));
        }

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        deleteTransaction(path, LogicalDatastoreType.CONFIGURATION,
                "Deleting authentication key from MD-SAL datastore failed");
    }

    public void removeMapping(Mapping mapping) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Removing mapping for {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));
        }

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        deleteTransaction(path, getDestinationDatastore(mapping), "Deleting mapping from MD-SAL datastore failed");
    }

    public void removeXtrIdMapping(XtrIdMapping mapping) {
        XtrId xtrId = mapping.getMappingRecord().getXtrId();
        Preconditions.checkNotNull(xtrId, "Make sure you only call addXtrIdMapping when the MappingRecord "
                + "contains an xTR-ID");
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Removing mapping for {}, xTR-ID {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()), xtrId);
        }

        InstanceIdentifier<XtrIdMapping> path = InstanceIdentifierUtil
                .createXtrIdMappingIid(mapping.getMappingRecord().getEid(), MappingOrigin.Southbound, xtrId);
        deleteTransaction(path, LogicalDatastoreType.OPERATIONAL,
                "Deleting xTR-ID mapping from MD-SAL datastore failed");
    }

    public void removeAllDatastoreContent() {
        LOG.debug("MD-SAL: Removing all mapping database datastore content (mappings and keys)");
        removeAllConfigDatastoreContent();
        removeAllOperationalDatastoreContent();
    }

    public void removeAllConfigDatastoreContent() {
        deleteTransaction(DATABASE_ROOT, LogicalDatastoreType.CONFIGURATION,
                "Removal of all database content in config datastore failed");
    }

    public void removeAllOperationalDatastoreContent() {
        deleteTransaction(DATABASE_ROOT, LogicalDatastoreType.OPERATIONAL,
                "Removal of all database content in operational datastore failed");
    }

    public void updateAuthenticationKey(AuthenticationKey authenticationKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Updating authentication key for {} with '{}'",
                    LispAddressStringifier.getString(authenticationKey.getEid()),
                    authenticationKey.getMappingAuthkey().getKeyString());
        }

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        writePutTransaction(path, authenticationKey, LogicalDatastoreType.CONFIGURATION,
                "Updating authentication key in MD-SAL datastore failed");
    }

    public void updateMapping(Mapping mapping) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Updating mapping for {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));
        }

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        writePutTransaction(path, mapping, getDestinationDatastore(mapping),
                "Updating mapping in MD-SAL datastore failed");
    }

    public List<Mapping> getAllMappings() {
        List<Mapping> mappings = getAllMappings(LogicalDatastoreType.CONFIGURATION);
        mappings.addAll(getAllMappings(LogicalDatastoreType.OPERATIONAL));
        return mappings;
    }

    public List<Mapping> getAllMappings(LogicalDatastoreType logicalDataStore) {
        LOG.debug("MD-SAL: Get all mappings from datastore");
        List<Mapping> mappings = new ArrayList<Mapping>();
        MappingDatabase mdb = readTransaction(DATABASE_ROOT, logicalDataStore);

        if (mdb != null && mdb.getVirtualNetworkIdentifier() != null) {
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
        MappingDatabase mdb = readTransaction(DATABASE_ROOT, LogicalDatastoreType.CONFIGURATION);

        if (mdb != null && mdb.getVirtualNetworkIdentifier() != null) {
            for (VirtualNetworkIdentifier id : mdb.getVirtualNetworkIdentifier()) {
                List<AuthenticationKey> keys = id.getAuthenticationKey();
                if (keys != null) {
                    authKeys.addAll(keys);
                }
            }
        }

        return authKeys;
    }

    public void saveLastUpdateTimestamp() {
        Long timestamp = System.currentTimeMillis();
        LOG.debug("MD-SAL: Saving last update timestamp to operational datastore: {}", new Date(timestamp).toString());
        writePutTransaction(LAST_UPDATED, new LastUpdatedBuilder().setLastUpdated(timestamp).build(),
                LogicalDatastoreType.OPERATIONAL, "Couldn't save last update timestamp to operational datastore");
    }

    public Long getLastUpdateTimestamp() {
        LastUpdated lastUpdated = readTransaction(LAST_UPDATED, LogicalDatastoreType.OPERATIONAL);
        if (lastUpdated != null && lastUpdated.getLastUpdated() != null) {
            Long timestamp = lastUpdated.getLastUpdated();
            LOG.debug("MD-SAL: Retrieved last update timestamp from operational datastore: {}",
                    new Date(timestamp).toString());
            return timestamp;
        } else {
            LOG.debug("MD-SAL: Couldn't retrieve last update timestamp from operational datastore");
            return null;
        }
    }

    private static LogicalDatastoreType getDestinationDatastore(Mapping mapping) {
        return mapping.getOrigin().equals(MappingOrigin.Southbound) ? LogicalDatastoreType.OPERATIONAL
                : LogicalDatastoreType.CONFIGURATION;
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void writePutTransaction(
            InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType, String errMsg) {
        WriteTransaction writeTx = txChain.newWriteOnlyTransaction();
        writeTx.put(logicalDatastoreType, addIID, data, true);
        Futures.addCallback(writeTx.submit(), new FutureCallback<Void>() {

            public void onSuccess(Void result) {
            }

            public void onFailure(Throwable throwable) {
                LOG.error("Transaction failed:", throwable);
            }
        });
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> U readTransaction(
            InstanceIdentifier<U> readIID, LogicalDatastoreType logicalDatastoreType) {
        ReadOnlyTransaction readTx = txChain.newReadOnlyTransaction();
        CheckedFuture<Optional<U>, ReadFailedException> readFuture = readTx.read(logicalDatastoreType, readIID);
        readTx.close();
        try {
            Optional<U> optionalDataObject = readFuture.checkedGet();
            if (optionalDataObject != null && optionalDataObject.isPresent()) {
                return optionalDataObject.get();
            } else {
                LOG.debug("{}: Failed to read", Thread.currentThread().getStackTrace()[1]);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to ....", e);
        }
        return null;
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void deleteTransaction(
            InstanceIdentifier<U> deleteIID, LogicalDatastoreType logicalDatastoreType, String errMsg) {

        WriteTransaction writeTx = txChain.newWriteOnlyTransaction();
        writeTx.delete(logicalDatastoreType, deleteIID);
        Futures.addCallback(writeTx.submit(), new FutureCallback<Void>() {

            public void onSuccess(Void result) {
            }

            public void onFailure(Throwable throwable) {
                LOG.error("Transaction failed:", throwable);
            }
        });
    }

    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction,
            Throwable cause) {
        LOG.error("Broken chain {} in DataStoreBackEnd, transaction {}, cause {}", chain, transaction.getIdentifier(),
                cause);
    }

    public void onTransactionChainSuccessful(TransactionChain<?, ?> chain) {
        LOG.info("DataStoreBackEnd closed successfully, chain {}", chain);
    }
}
