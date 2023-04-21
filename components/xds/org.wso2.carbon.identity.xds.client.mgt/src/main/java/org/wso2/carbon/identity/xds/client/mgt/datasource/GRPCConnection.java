/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.xds.client.mgt.datasource;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.xds.client.mgt.util.XDSUtils;
import org.wso2.carbon.identity.xds.common.constant.XDSConstants;


/**
 * Class containing the gRPC connections.
 */
public class GRPCConnection {

    private static final Log LOG = LogFactory.getLog(GRPCConnection.class);

    private ManagedChannel eventServiceChannel;
    private static volatile GRPCConnection instance;

    private GRPCConnection() {

        initChannels();
    }

    /**
     * Get a RPCConnection connection instance.
     *
     * @return RPCConnection object.
     */
    public static GRPCConnection getInstance() {

        if (instance == null) {
            synchronized (GRPCConnection.class) {
                if (instance == null) {
                    instance = new GRPCConnection();
                }
            }
        }
        return instance;
    }

    private void initChannels() {

        eventServiceChannel = ManagedChannelBuilder.forAddress(
                XDSUtils.getConfig(XDSConstants.PROP_GRPC_EVENT_SERVICE_CONNECTION_URL),
                        Integer.parseInt(XDSUtils.getConfig(XDSConstants.PROP_GRPC_EVENT_SERVICE_CONNECTION_PORT)))
                .usePlaintext()
                .build();
    }

    /**
     * Get connection for resource metadata service.
     *
     * @return ManagedChannel object.
     */
    public ManagedChannel getEventServiceChannel() {

        return eventServiceChannel;
    }

    /**
     * Shutdown all the channels.
     */
    public void shutdownChannels() {

        eventServiceChannel.shutdown();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Grpc connection channel shutdown.");
        }
    }
}
