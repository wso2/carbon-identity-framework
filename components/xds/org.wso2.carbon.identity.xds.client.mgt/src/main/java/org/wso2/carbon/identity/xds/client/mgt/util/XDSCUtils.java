/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.xds.client.mgt.util;

import discovery.service.api.EventOuterClass;
import discovery.service.api.EventServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.xds.client.mgt.datasource.GRPCConnection;
import org.wso2.carbon.identity.xds.common.constant.OperationType;
import org.wso2.carbon.identity.xds.common.constant.XDSConstants;

/**
 * Util class for throttler listener.
 */
public class XDSCUtils {

    private static final Log LOG = LogFactory.getLog(XDSCUtils.class);

    /**
     * Publish data to gRPC server.
     *
     * @param tenantDomain Tenant domain.
     * @param data         Data to be published.
     * @param eventType    Event type.
     * @param operation    Operation type.
     * @return True if data is published successfully.
     */
    public static boolean publishData(String tenantDomain, String username, String data,
                                      XDSConstants.EventType eventType, OperationType operation) {

        try {
            ManagedChannel channel = GRPCConnection.getInstance().getEventServiceChannel();
            EventServiceGrpc.EventServiceBlockingStub
                    stub = EventServiceGrpc.newBlockingStub(channel);
            EventOuterClass.Event.Builder builder =
                    EventOuterClass.Event.newBuilder()
                            .setType(eventType.toString())
                            .setOperation(operation.toString())
                            .setValue(data)
                            .setTenantDomain(tenantDomain)
                            .setUsername(username);
            EventOuterClass.Event bac = stub.addEvent(builder.build());
            LOG.info(" value posted:" + bac.getValue());
        } catch (StatusRuntimeException e) {
            LOG.error("Error while publishing data to gRPC server", e);
            return false;
        }

        return true;
    }

    /**
     * Read listener property from identity.xml file.
     *
     * @param key Key of the property.
     * @return Property in String format.
     */
    public static String getConfig(String key) {

        String propertyValue = IdentityUtil.getProperty(key);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved listener property. Key: " + key + " value: " + propertyValue);
        }

        if (StringUtils.isBlank(propertyValue)) {
            LOG.warn("Value for listener property key: " + key + " is EMPTY.");
        }

        return propertyValue;
    }
}
