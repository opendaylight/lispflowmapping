/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.dsbackend;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores data coming from the mapping database RPCs into the MD-SAL datastore.
 *
 * @author Lorand Jakab
 */
public class DataStoreBackEnd {
    private static final Logger LOG = LoggerFactory.getLogger(DataStoreBackEnd.class);
    private static final InstanceIdentifier<MappingDatabase> DATABASE_ROOT =
            InstanceIdentifier.create(MappingDatabase.class);
    private static final InstanceIdentifier<LastUpdated> LAST_UPDATED =
            InstanceIdentifier.create(MappingDatabase.class).child(LastUpdated.class);

    private final TransactionChain configTxChain;
    private final TransactionChain operTxChain;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Non-final for mocking")
    public DataStoreBackEnd(final DataBroker broker) {
        LOG.debug("Creating DataStoreBackEnd transaction chain...");
        configTxChain = broker.createMergingTransactionChain();
        operTxChain = broker.createMergingTransactionChain();
        configTxChain.addCallback(new FutureCallback<Empty>() {
            @Override
            public void onSuccess(final Empty result) {
                onTransactionChainSuccessful(configTxChain);
            }

            @Override
            public void onFailure(final Throwable cause) {
                onTransactionChainFailed(configTxChain, cause);
            }
        });
        operTxChain.addCallback(new FutureCallback<Empty>() {
            @Override
            public void onSuccess(final Empty result) {
                onTransactionChainSuccessful(operTxChain);
            }

            @Override
            public void onFailure(final Throwable cause) {
                onTransactionChainFailed(operTxChain, cause);
            }
        });
    }

    public void addAuthenticationKey(final AuthenticationKey authenticationKey) {
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

    public void addMapping(final Mapping mapping) {
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
    public void addXtrIdMapping(final XtrIdMapping mapping) {
        XtrId xtrId = mapping.getMappingRecord().getXtrId();
        requireNonNull(xtrId, "Make sure you only call addXtrIdMapping when the MappingRecord contains an xTR-ID");
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Adding mapping for {}, xTR-ID {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()), xtrId);
        }

        InstanceIdentifier<XtrIdMapping> path = InstanceIdentifierUtil
                .createXtrIdMappingIid(mapping.getMappingRecord().getEid(), MappingOrigin.Southbound, xtrId);
        writePutTransaction(path, mapping, LogicalDatastoreType.OPERATIONAL,
                "Adding xTR-ID mapping to MD-SAL datastore failed");
    }

    public void removeAuthenticationKey(final AuthenticationKey authenticationKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Removing authentication key for {}",
                    LispAddressStringifier.getString(authenticationKey.getEid()));
        }

        InstanceIdentifier<AuthenticationKey> path = InstanceIdentifierUtil
                .createAuthenticationKeyIid(authenticationKey.getEid());
        deleteTransaction(path, LogicalDatastoreType.CONFIGURATION,
                "Deleting authentication key from MD-SAL datastore failed");
    }

    public void removeMapping(final Mapping mapping) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD-SAL: Removing mapping for {}",
                    LispAddressStringifier.getString(mapping.getMappingRecord().getEid()));
        }

        InstanceIdentifier<Mapping> path = InstanceIdentifierUtil
                .createMappingIid(mapping.getMappingRecord().getEid(), mapping.getOrigin());
        deleteTransaction(path, getDestinationDatastore(mapping), "Deleting mapping from MD-SAL datastore failed");
    }

    public void removeXtrIdMapping(final XtrIdMapping mapping) {
        XtrId xtrId = mapping.getMappingRecord().getXtrId();
        requireNonNull(xtrId, "Make sure you only call addXtrIdMapping when the MappingRecord contains an xTR-ID");
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

    public void updateAuthenticationKey(final AuthenticationKey authenticationKey) {
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

    public void updateMapping(final Mapping mapping) {
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

    public List<Mapping> getAllMappings(final LogicalDatastoreType logicalDataStore) {
        LOG.debug("MD-SAL: Get all mappings from {} datastore",
                logicalDataStore == LogicalDatastoreType.CONFIGURATION ? "config" : "operational");
        List<Mapping> mappings = new ArrayList<>();
        MappingDatabase mdb = readTransaction(DATABASE_ROOT, logicalDataStore);

        if (mdb != null && mdb.getVirtualNetworkIdentifier() != null) {
            for (VirtualNetworkIdentifier id : mdb.nonnullVirtualNetworkIdentifier().values()) {
                List<Mapping> ms = new ArrayList<>(id.nonnullMapping().values());
                if (ms != null) {
                    mappings.addAll(ms);
                }
            }
        }

        return mappings;
    }

    public List<AuthenticationKey> getAllAuthenticationKeys() {
        LOG.debug("MD-SAL: Get all authentication keys from datastore");
        List<AuthenticationKey> authKeys = new ArrayList<>();
        MappingDatabase mdb = readTransaction(DATABASE_ROOT, LogicalDatastoreType.CONFIGURATION);

        if (mdb != null && mdb.getVirtualNetworkIdentifier() != null) {
            for (VirtualNetworkIdentifier id : mdb.nonnullVirtualNetworkIdentifier().values()) {
                List<AuthenticationKey> keys = new ArrayList<>(id.nonnullAuthenticationKey().values());
                if (keys != null) {
                    authKeys.addAll(keys);
                }
            }
        }

        return authKeys;
    }

    public void saveLastUpdateTimestamp() {
        Long timestamp = System.currentTimeMillis();
        LOG.debug("MD-SAL: Saving last update timestamp to operational datastore: {}", new Date(timestamp));
        writePutTransaction(LAST_UPDATED, new LastUpdatedBuilder().setLastUpdated(timestamp).build(),
                LogicalDatastoreType.OPERATIONAL, "Couldn't save last update timestamp to operational datastore");
    }

    public void removeLastUpdateTimestamp() {
        LOG.debug("MD-SAL: Removing last update timestamp from operational datastore");
        deleteTransaction(LAST_UPDATED, LogicalDatastoreType.OPERATIONAL,
                "Couldn't remove last update timestamp from operational datastore");
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

    private static LogicalDatastoreType getDestinationDatastore(final Mapping mapping) {
        return mapping.getOrigin().equals(MappingOrigin.Southbound) ? LogicalDatastoreType.OPERATIONAL
                : LogicalDatastoreType.CONFIGURATION;
    }

    private TransactionChain getChain(final LogicalDatastoreType logicalDatastoreType) {
        return switch (logicalDatastoreType) {
            case CONFIGURATION -> configTxChain;
            case OPERATIONAL -> operTxChain;
        };
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void writePutTransaction(
            final InstanceIdentifier<U> addIID, final U data, final LogicalDatastoreType logicalDatastoreType,
            final String errMsg) {
        WriteTransaction writeTx = getChain(logicalDatastoreType).newWriteOnlyTransaction();
        // TODO: is is a utility method, hence we do not have enough lifecycle knowledge to use plain put()
        writeTx.mergeParentStructurePut(logicalDatastoreType, addIID, data);
        writeTx.commit().addCallback(new FutureCallback<CommitInfo>() {

            @Override
            public void onSuccess(final CommitInfo result) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Transaction failed:", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> U readTransaction(
            final InstanceIdentifier<U> readIID, final LogicalDatastoreType logicalDatastoreType) {
        final ListenableFuture<Optional<U>> readFuture;
        try (ReadTransaction readTx = getChain(logicalDatastoreType).newReadOnlyTransaction()) {
            readFuture = readTx.read(logicalDatastoreType, readIID);
        }
        try {
            Optional<U> optionalDataObject = readFuture.get();
            if (optionalDataObject != null && optionalDataObject.isPresent()) {
                return optionalDataObject.orElseThrow();
            } else {
                LOG.debug("{}: Failed to read", Thread.currentThread().getStackTrace()[1]);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Failed to ....", e);
        }
        return null;
    }

    private <U extends org.opendaylight.yangtools.yang.binding.DataObject> void deleteTransaction(
            final InstanceIdentifier<U> deleteIID, final LogicalDatastoreType logicalDatastoreType,
            final String errMsg) {
        WriteTransaction writeTx = getChain(logicalDatastoreType).newWriteOnlyTransaction();
        writeTx.delete(logicalDatastoreType, deleteIID);
        writeTx.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Transaction failed:", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    @VisibleForTesting
    void onTransactionChainFailed(final TransactionChain chain, final Throwable cause) {
        LOG.error("Broken chain {} in DataStoreBackEnd, cause {}", chain, cause.getMessage());
    }

    @VisibleForTesting
    void onTransactionChainSuccessful(final TransactionChain chain) {
        LOG.info("DataStoreBackEnd closed successfully, chain {}", chain);
    }

    public void closeTransactionChain() {
        LOG.debug("Closing DataStoreBackEnd transaction chain...");
        configTxChain.close();
        operTxChain.close();
    }
}
