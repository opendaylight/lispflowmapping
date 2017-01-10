/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheikahm on 1/12/17.
 */
public class VppNetconfTrasaction {
    private static final Logger LOG = LoggerFactory.getLogger(VppNetconfTrasaction.class);

    public static final byte RETRY_COUNT = 5;

    public static synchronized <T extends DataObject> Optional<T> read(DataBroker dataBroker,
                                                                       LogicalDatastoreType datastoreType,
                                                                       InstanceIdentifier<T> instanceIdentifier) {
        LOG.trace("Started Netconf transaction on VPP Node");
        Preconditions.checkNotNull(dataBroker);

        Optional<T> returnData;

        int retryCounter = RETRY_COUNT;

        while (retryCounter > 0) {
            ReadOnlyTransaction readTransaction = dataBroker.newReadOnlyTransaction();
            try {
                returnData = readTransaction(instanceIdentifier, datastoreType, readTransaction);
                LOG.trace("Netconf READ transaction SUCCESSFUL. Data present: {}", returnData.isPresent());
                readTransaction.close();
                return returnData;
            } catch (IllegalStateException e) {

                LOG.warn("Assuming that netconf read-transaction failed, retrying. Retry Count: " + retryCounter,
                            e.getMessage());
                readTransaction.close();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Exception while reading data. Retry Aborted.", e.getMessage());
                readTransaction.close();
                break;
            }
            retryCounter--;
        }
        return Optional.absent();
    }

    private static <T extends DataObject> Optional<T> readTransaction(InstanceIdentifier<T> instanceIdentifier,
                                                                      LogicalDatastoreType datastoreType,
                                                                      ReadOnlyTransaction readTransaction)
            throws IllegalStateException, InterruptedException, ExecutionException {

        CheckedFuture<Optional<T>, ReadFailedException> futureData =
                readTransaction.read(datastoreType, instanceIdentifier);

        return futureData.get();
    }
}
