/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.xds.client.mgt;

import discovery.service.api.EventOuterClass;
import discovery.service.api.EventServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.xds.client.mgt.datasource.GRPCConnection;
import org.wso2.carbon.identity.xds.common.constant.XDSConstants;
import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

/**
 * Util class for throttler listener.
 */
public class XDSClientService {

    private static final Log LOG = LogFactory.getLog(XDSClientService.class);

    /**
     * Publish data to gRPC server.
     *
     * @param tenantDomain Tenant domain.
     * @param data         Data to be published.
     * @param eventType    Event type.
     * @param operation    Operation type.
     * @return True if data is published successfully.
     */
    public boolean publishData(String tenantDomain, String username, String data,
                                      XDSConstants.EventType eventType, XDSOperationType operation) {

        try {
            ManagedChannel channel = GRPCConnection.getInstance().getEventServiceChannel();
            EventServiceGrpc.EventServiceBlockingStub
                    stub = EventServiceGrpc.newBlockingStub(channel);
            EventOuterClass.Event.Builder builder =
                    EventOuterClass.Event.newBuilder()
                            .setType(eventType.toString())
                            .setOperation(operation.toString())
                            .setValue(data == null ? "" : data)
                            .setTenantDomain(tenantDomain == null ? "" : tenantDomain)
                            .setUsername(username == null ? "" : username);
            EventOuterClass.Event bac = stub.addEvent(builder.build());
            LOG.info(" value posted:" + bac.getValue());
        } catch (StatusRuntimeException e) {
            LOG.error("Error while publishing data to gRPC server", e);
            return false;
        }

        return true;
    }
}
