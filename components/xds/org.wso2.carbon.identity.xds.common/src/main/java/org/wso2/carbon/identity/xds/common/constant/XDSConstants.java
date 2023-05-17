/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.xds.common.constant;

/**
 * Constants class for throttler listener.
 */
public class XDSConstants {

    public static final String PROP_GRPC_EVENT_SERVICE_CONNECTION_URL = "GRPC.EventService.Connection.URL";
    public static final String PROP_GRPC_EVENT_SERVICE_CONNECTION_PORT = "GRPC.EventService.Connection.Port";

    /**
     * Enum for event types.
     */
     public enum EventType {
        APPLICATION,
        IDP,
        USER,
        CLAIM,
        SCOPE,
        ROLE,
        OAUTH,
        SAML,
        CORS,
        CONFIGURATION,
        EVENT_PUBLISHER,
        WORKFLOW,
        KEYSTORE,
        EMAIL_TEMPLATE,
        NOTIFICATION_TEMPLATE,
        REMOTE_FETCH,
        NOTIFICATION_SENDER,
        TEMPLATE,
        USER_STORE

    }
}
