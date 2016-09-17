/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.lispflowmapping.dsbackend.DataStoreBackEnd;
import org.opendaylight.lispflowmapping.implementation.config.ConfigIni;
import org.opendaylight.lispflowmapping.implementation.mdsal.AuthenticationKeyDataListener;
import org.opendaylight.lispflowmapping.implementation.mdsal.MappingDataListener;
import org.opendaylight.lispflowmapping.implementation.util.DSBEInputUtil;
import org.opendaylight.lispflowmapping.implementation.util.RPCInputConvertorUtil;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.mappingservice.IMappingService;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.SiteId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.eid.container.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.authkey.container.MappingAuthkey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.AddMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetAllMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingWithXtrIdOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.GetMappingsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.MappingOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.OdlMappingserviceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.RemoveMappingsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.mappingservice.rev150906.UpdateMappingsInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher of API calls that implements the RPC and Java APIs in mappingservice.yang and IMappingService
 * respectively. It also coordinates and acts as container for all objects in charge of
 *  - saving and updating md-sal datastore stored mappings
 *  - monitoring md-sal datastore mapping updates and pushing them to the in memory mapping-system
 *  - in memory mapping-system
 *
 * @author Lorand Jakab
 * @author Florin Coras
 *
 */
public class MappingService implements OdlMappingserviceService, IMappingService, AutoCloseable {
    protected static final Logger LOG = LoggerFactory.getLogger(MappingService.class);
    private static final String NOT_FOUND_TAG = "data-missing";
    private static final String DATA_EXISTS_TAG = "data-exists";

    private MappingSystem mappingSystem;
    private DataStoreBackEnd dsbe;
    private AuthenticationKeyDataListener keyListener;
    private MappingDataListener mappingListener;
    private final ILispDAO dao;

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;

    private boolean overwritePolicy = ConfigIni.getInstance().mappingOverwriteIsSet();
    private boolean notificationPolicy = ConfigIni.getInstance().smrIsSet();
    private boolean iterateMask = true;
    private boolean isMaster = false;

    public MappingService(final DataBroker broker,
            final NotificationPublishService notificationPublishService,
            final ILispDAO lispDAO) {
        this.dataBroker = broker;
        this.notificationPublishService = notificationPublishService;
        this.dao = lispDAO;

        LOG.debug("MappingService created!");
    }


    @Override
    public void setMappingOverwrite(boolean overwrite) {
        this.overwritePolicy = overwrite;
        if (mappingSystem != null) {
            mappingSystem.setOverwritePolicy(overwrite);
            ConfigIni.getInstance().setMappingOverwrite(overwrite);
        }
    }

    @Override
    public void setLookupPolicy(IMappingService.LookupPolicy policy) {
        ConfigIni.getInstance().setLookupPolicy(policy);
    }

    public void initialize() {
        LOG.info("Mapping Service initializing...");
        dsbe = new DataStoreBackEnd(dataBroker);

        mappingSystem = new MappingSystem(dao, iterateMask, notificationPolicy, overwritePolicy);
        mappingSystem.setDataStoreBackEnd(dsbe);
        mappingSystem.initialize();

        keyListener = new AuthenticationKeyDataListener(dataBroker, mappingSystem);
        mappingListener = new MappingDataListener(dataBroker, mappingSystem, notificationPublishService);
        LOG.info("Mapping Service loaded.");
    }

    @Override
    public Future<RpcResult<Void>> addKey(AddKeyInput input) {
        Preconditions.checkNotNull(input, "add-key RPC input must be not null!");
        LOG.trace("RPC received to add the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key != null) {
            String message = "Key already exists! Please use update-key if you want to change it.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, DATA_EXISTS_TAG, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        dsbe.addAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> addMapping(AddMappingInput input) {
        Preconditions.checkNotNull(input, "add-mapping RPC input must be not null!");
        LOG.trace("RPC received to add the following mapping: " + input.toString());

        dsbe.addMapping(RPCInputConvertorUtil.toMapping(input));

        RpcResultBuilder<Void> rpcResultBuilder;

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public void addMapping(MappingOrigin origin, Eid key, SiteId siteId, Object data, boolean merge) {
        // SB registrations are first written to the MappingSystem and only afterwards are persisted to the datastore
        if (origin.equals(MappingOrigin.Southbound)) {
            // Store data first in MapCache and only afterwards persist to datastore. This should be used only for SB
            // registrations
            mappingSystem.addMapping(origin, key, data, merge);
            dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, (MappingRecord) data));
            if (((MappingRecord) data).getXtrId() != null) {
                dsbe.addXtrIdMapping(DSBEInputUtil.toXtrIdMapping((MappingRecord) data));
            }
        } else {
            dsbe.addMapping(DSBEInputUtil.toMapping(origin, key, siteId, (MappingRecord) data));
        }
    }

    @Override
    public void updateMappingRegistration(MappingOrigin origin, Eid key, Long timestamp) {
        mappingSystem.updateMappingRegistration(origin, key, timestamp);
    }

    @Override
    public Future<RpcResult<GetKeyOutput>> getKey(GetKeyInput input) {
        Preconditions.checkNotNull(input, "get-key RPC input must be not null!");
        LOG.trace("RPC received to get the following key: " + input.toString());

        RpcResultBuilder<GetKeyOutput> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key == null) {
            String message = "Key was not found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetKeyOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            rpcResultBuilder = RpcResultBuilder.success(new GetKeyOutputBuilder().setMappingAuthkey(key));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetMappingOutput>> getMapping(GetMappingInput input) {
        Preconditions.checkNotNull(input, "get-mapping RPC input must be not null!");
        LOG.trace("RPC received to get the following mapping: " + input.toString());

        RpcResultBuilder<GetMappingOutput> rpcResultBuilder;

        MappingRecord reply = (MappingRecord) mappingSystem.getMapping(convertToBinaryIfNecessary(input.getEid()));

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            final MappingRecord convertedReply = convertFromBinaryIfNecessary(reply);
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingOutputBuilder().setMappingRecord(convertedReply));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Object getMapping(MappingOrigin origin, Eid key) {
        return mappingSystem.getMapping(origin, key);
    }

    @Override
    public Object getMapping(Eid key) {
        return mappingSystem.getMapping(key);
    }

    @Override
    public Object getMapping(Eid srcKey, Eid dstKey) {
        return mappingSystem.getMapping(srcKey, dstKey);
    }

    @Override
    public Future<RpcResult<GetMappingWithXtrIdOutput>> getMappingWithXtrId(GetMappingWithXtrIdInput input) {
        Preconditions.checkNotNull(input, "get-mapping RPC input must be not null!");
        LOG.trace("RPC received to get the following mapping: " + input.toString());

        RpcResultBuilder<GetMappingWithXtrIdOutput> rpcResultBuilder;

        MappingRecord reply = (MappingRecord) mappingSystem.getMapping(null, convertToBinaryIfNecessary(input.getEid()),
                input.getXtrId());

        if (reply == null) {
            String message = "No mapping was found in the mapping database";
            rpcResultBuilder = RpcResultBuilder.<GetMappingWithXtrIdOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, NOT_FOUND_TAG, message);
        } else {
            final MappingRecord convertedReply = convertFromBinaryIfNecessary(reply);
            rpcResultBuilder = RpcResultBuilder.success(new GetMappingWithXtrIdOutputBuilder()
                    .setMappingRecord(convertedReply));
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeKey(RemoveKeyInput input) {
        Preconditions.checkNotNull(input, "remove-key RPC input must be not null!");
        LOG.trace("RPC received to remove the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeMapping(RemoveMappingInput input) {
        Preconditions.checkNotNull(input, "remove-mapping RPC input must be not null!");
        LOG.trace("RPC received to remove the following mapping: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.removeMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public void removeMapping(MappingOrigin origin, Eid key) {
        dsbe.removeMapping(DSBEInputUtil.toMapping(origin, key));
    }

    @Override
    public Future<RpcResult<Void>> updateKey(UpdateKeyInput input) {
        Preconditions.checkNotNull(input, "update-key RPC input must be not null!");
        LOG.trace("RPC received to update the following key: " + input.toString());

        RpcResultBuilder<Void> rpcResultBuilder;

        MappingAuthkey key = mappingSystem.getAuthenticationKey(convertToBinaryIfNecessary(input.getEid()));

        if (key == null) {
            String message = "Key doesn't exist! Please use add-key if you want to create a new authentication key.";
            rpcResultBuilder = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.PROTOCOL, NOT_FOUND_TAG, message);
            return Futures.immediateFuture(rpcResultBuilder.build());
        }

        dsbe.updateAuthenticationKey(RPCInputConvertorUtil.toAuthenticationKey(input));
        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> updateMapping(UpdateMappingInput input) {
        LOG.trace("RPC received to update the following mapping: " + input.toString());
        Preconditions.checkNotNull(input, "update-mapping RPC input must be not null!");

        RpcResultBuilder<Void> rpcResultBuilder;

        dsbe.updateMapping(RPCInputConvertorUtil.toMapping(input));

        rpcResultBuilder = RpcResultBuilder.success();

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeKeys(RemoveKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeMappings(RemoveMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetKeysOutput>> getKeys(GetKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addMappings(AddMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateKeys(UpdateKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeAllMappings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeAllKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetAllKeysOutput>> getAllKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateMappings(UpdateMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addKeys(AddKeysInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetAllMappingsOutput>> getAllMappings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetMappingsOutput>> getMappings(
            GetMappingsInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Eid getWidestNegativePrefix(Eid key) {
        return mappingSystem.getWidestNegativePrefix(key);
    }

    @Override
    public void addAuthenticationKey(Eid key, MappingAuthkey authKey) {
        dsbe.addAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, authKey));
    }

    @Override
    public MappingAuthkey getAuthenticationKey(Eid key) {
        return mappingSystem.getAuthenticationKey(key);
    }

    @Override
    public void removeAuthenticationKey(Eid key) {
        dsbe.removeAuthenticationKey(DSBEInputUtil.toAuthenticationKey(key, null));
    }

    @Override
    public void addData(MappingOrigin origin, Eid key, String subKey, Object data) {
        mappingSystem.addData(origin, key, subKey, data);
    }

    @Override
    public Object getData(MappingOrigin origin, Eid key, String subKey) {
        return mappingSystem.getData(origin, key, subKey);
    }

    @Override
    public void removeData(MappingOrigin origin, Eid key, String subKey) {
        mappingSystem.removeData(origin, key, subKey);
    }

    @Override
    public String printMappings() {
        return mappingSystem.printMappings();
    }

    @Override
    public void close() throws Exception {
        LOG.info("Mapping Service is being destroyed!");
        keyListener.closeDataChangeListener();
        mappingListener.closeDataChangeListener();
    }

    @Override
    public void cleanCachedMappings() {
        mappingSystem.cleanCaches();
        dsbe.removeAllDatastoreContent();
    }

    private static Eid convertToBinaryIfNecessary(Eid eid) {
        if (LispAddressUtil.addressNeedsConversionToBinary(eid.getAddress())) {
            return LispAddressUtil.convertToBinary(eid);
        }
        return eid;
    }

    private static MappingRecord convertFromBinaryIfNecessary(MappingRecord originalRecord) {
        List<LocatorRecord> originalLocators = originalRecord.getLocatorRecord();

        List<LocatorRecord> convertedLocators = null;
        if (originalLocators != null) {
            // If convertedLocators is non-null, while originalLocators is also non-null, conversion has been made
            convertedLocators = convertFromBinaryIfNecessary(originalLocators);
        }

        if (LispAddressUtil.addressNeedsConversionFromBinary(originalRecord.getEid().getAddress())
                || (originalLocators != null && convertedLocators != null)) {
            MappingRecordBuilder mrb = new MappingRecordBuilder(originalRecord);
            mrb.setEid(LispAddressUtil.convertFromBinary(originalRecord.getEid()));
            if (convertedLocators != null) {
                mrb.setLocatorRecord(convertedLocators);
            }
            return mrb.build();
        }
        return originalRecord;
    }

    private static List<LocatorRecord> convertFromBinaryIfNecessary(List<LocatorRecord> originalLocators) {
        List<LocatorRecord> convertedLocators = null;
        for (LocatorRecord record : originalLocators) {
            if (LispAddressUtil.addressNeedsConversionFromBinary(record.getRloc().getAddress())) {
                LocatorRecordBuilder lrb = new LocatorRecordBuilder(record);
                lrb.setRloc(LispAddressUtil.convertFromBinary(record.getRloc()));
                if (convertedLocators == null) {
                    convertedLocators = new ArrayList<LocatorRecord>();
                }
                convertedLocators.add(lrb.build());
            }
        }
        if (convertedLocators != null) {
            return convertedLocators;
        }
        return originalLocators;
    }

    @Override
    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
        mappingSystem.setIsMaster(isMaster);
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }
}
