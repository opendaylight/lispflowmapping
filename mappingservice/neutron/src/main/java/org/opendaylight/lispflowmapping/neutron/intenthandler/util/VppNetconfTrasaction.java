/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler.util;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shakib Ahmed on 1/12/17.
 */
public final class VppNetconfTrasaction {
    private static final Logger LOG = LoggerFactory.getLogger(VppNetconfTrasaction.class);

    public static final byte RETRY_COUNT = 5;

    private VppNetconfTrasaction() {
    }

    public static synchronized <T extends DataObject> Optional<T> read(DataBroker dataBroker,
                                                                       LogicalDatastoreType datastoreType,
                                                                       InstanceIdentifier<T> instanceIdentifier) {
        LOG.trace("Started Netconf transaction on VPP Node");
        requireNonNull(dataBroker);

        Optional<T> returnData;

        int retryCounter = RETRY_COUNT;

        while (retryCounter > 0) {
            ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
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
        return Optional.empty();
    }

    private static <T extends DataObject> Optional<T> readTransaction(InstanceIdentifier<T> instanceIdentifier,
                                                                      LogicalDatastoreType datastoreType,
                                                                      ReadTransaction readTransaction)
            throws IllegalStateException, InterruptedException, ExecutionException {
        return readTransaction.read(datastoreType, instanceIdentifier).get();
    }
}
