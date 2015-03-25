/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.netconf.impl;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;

import org.opendaylight.lispflowmapping.netconf.impl.LispNetconfConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.BuildConnectorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.LfmNetconfConnectorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.rev140706.RemoveConnectorInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.controller.config.api.ConflictingVersionException;
import org.opendaylight.controller.config.api.ValidationException;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.util.concurrent.SettableFuture;


public class LispDeviceNetconfConnector implements AutoCloseable, LfmNetconfConnectorService {

       private static final Logger LOG = LoggerFactory.getLogger(LispDeviceNetconfConnector.class);

       private final ExecutorService executor;
       private LispNetconfConnector nconfConnector;

       public static LispDeviceNetconfConnector createLispDeviceNetconfConnector() {
           return new LispDeviceNetconfConnector(Executors.newFixedThreadPool(1), new LispNetconfConnector());
       }

       public LispDeviceNetconfConnector(ExecutorService executor, LispNetconfConnector nconfConnector) {
           this.executor = executor;
           this.nconfConnector = nconfConnector;
           LOG.info( "LispDeviceNetconfConnector constructed" );
       }

       /**
        * Implemented from the AutoCloseable interface.
        */
       @Override
       public void close() throws ExecutionException, InterruptedException {
           executor.shutdown();
       }


        /**
         * RestConf RPC call implemented from the LfmNetconfConnectorService interface.
         */
        @Override
        public Future<RpcResult<Void>> buildConnector(final BuildConnectorInput input) {
            SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();

            LOG.trace("Received RPC to buildConnector: " + input);

            if (verifyBuildInput(input, futureResult) != true) {
                return futureResult;
            }

            return executor.submit(new MakeConnector(input));

        }

        @Override
        public Future<RpcResult<Void>> removeConnector(final RemoveConnectorInput input) {
            SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();

            if (verifyRemoveInput(input, futureResult) != true) {
                return futureResult;
            }

            return executor.submit(new RemoveConnector(input) );

        }

        private boolean verifyBuildInput(final BuildConnectorInput req, SettableFuture<RpcResult<Void>> futureResult ) {
            if (req.getInstance() == null) {
                LOG.error("Instance name not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "Instance name not initialized")
                        .build());
                return false;
            }

            if (req.getAddress() == null) {
                LOG.error("IP address not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "IP address not initialized")
                        .build());
                return false;
            }

            if (req.getPort() == null) {
                LOG.error("Port not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "Port not initialized")
                        .build());
                return false;
            }

            if (req.getUsername() == null) {
                LOG.error("Username not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "Username not initialized")
                        .build());
                return false;
            }

            if (req.getPassword() == null) {
                LOG.error("Password not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "Password not initialized")
                        .build());
                return false;
            }

            return true;
        }

        private boolean verifyRemoveInput(final RemoveConnectorInput conn, SettableFuture<RpcResult<Void>> futureResult) {
            if (conn.getInstance() == null) {
                LOG.error("Instance name not initialized");
                futureResult.set(RpcResultBuilder.<Void> failed()
                        .withError(ErrorType.APPLICATION, "exception", "Instance name not initialized")
                        .build());
                return false;
            }

            return true;
        }


        private class MakeConnector implements Callable<RpcResult<Void>> {

            final BuildConnectorInput buildConnectorInput;

            public MakeConnector(final BuildConnectorInput buildConnectorInput) {
                this.buildConnectorInput = buildConnectorInput;
            }

            @Override
            public RpcResult<Void> call() {

                try {
                    nconfConnector.createNetconfConnector(buildConnectorInput.getInstance(), buildConnectorInput.getAddress(),
                            buildConnectorInput.getPort().getValue(), buildConnectorInput.getUsername(), buildConnectorInput.getPassword());
                    LOG.info("LispNetconfConnector {} built", buildConnectorInput.getInstance());
                    return RpcResultBuilder.<Void>success().build();
                } catch( InstanceAlreadyExistsException e ) {
                    LOG.error("LispNetconfConnector {} already exists!", buildConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "exists", "LispNetconfConnector exists")
                            .build();
                } catch (ConflictingVersionException ex) {
                    LOG.error("LispNetconfConnector {} version exception", buildConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "exception", "LispNetconfConnector version exception")
                            .build();
                } catch ( ValidationException ex) {
                    LOG.error("LispNetconfConnector {} validation exception", buildConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "exception", "LispNetconfConnector validation exception")
                            .build();
                }

            }

        }

        private class RemoveConnector implements Callable<RpcResult<Void>> {
            final RemoveConnectorInput removeConnectorInput;

            public RemoveConnector(final RemoveConnectorInput connectorInput) {
                this.removeConnectorInput = connectorInput;
            }

            @Override
            public RpcResult<Void> call() {
                try {
                    nconfConnector.removeNetconfConnector(removeConnectorInput.getInstance());
                    LOG.info("LispNetconfConnector {} removed!", removeConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> success().build();
                } catch( InstanceNotFoundException e ) {
                    LOG.info("LispNetconfConnector {} doesn't exists!", removeConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "no-exist", "LispNetconfConnector doesn't exist")
                            .build();
                } catch( ValidationException e ) {
                    LOG.info("LispNetconfConnector {}: Could not validate remove transactions!", removeConnectorInput.getInstance());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "fail", "LispNetconfConnector doesn't exist")
                            .build();
                } catch (ConflictingVersionException e) {
                    LOG.error("LispNetconfConnector {}: Cannot remove due to conflicting version", removeConnectorInput.getInstance() );
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "fail", "Conflicting version exception")
                            .build();
                } catch (Exception e) {
                    LOG.error("LispNetconfConnector {} exception while removing: {}", removeConnectorInput.getInstance(), e.getClass());
                    return RpcResultBuilder.<Void> failed()
                            .withError(ErrorType.APPLICATION, "fail", "Cannot remove LispNetconfConnector: " + removeConnectorInput.getInstance())
                            .build();
                }

            }
        }
    }
