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
        CORS
    }

    /**
     * Enum for role operation types.
     */
    public enum RoleOperationType implements OperationType {
        CREATE,
        DELETE,
        UPDATE_ROLE_NAME,
        UPDATE_ROLE_USER_LIST,
        UPDATE_ROLE_GROUP_LIST,
        UPDATE_ROLE_PERMISSION_LIST,
    }

    /**
     * Enum for claim operation types.
     */
    public enum ClaimOperationType implements OperationType {
        ADD_CLAIM_DIALECT,
        RENAME_CLAIM_DIALECT,
        REMOVE_CLAIM_DIALECT,
        ADD_LOCAL_CLAIM,
        UPDATE_LOCAL_CLAIM,
        UPDATE_LOCAL_CLAIM_MAPPINGS,
        REMOVE_LOCAL_CLAIM,
        ADD_EXTERNAL_CLAIM,
        UPDATE_EXTERNAL_CLAIM,
        REMOVE_EXTERNAL_CLAIM,
        REMOVE_CLAIM_MAPPING_ATTRIBUTES,
        REMOVE_ALL_CLAIMS,
    }

    /**
     * Enum for claim operation types.
     */
    public enum ApplicationOperationType implements OperationType {
        ADD_APPLICATION,
        CREATE_APPLICATION_WITH_TEMPLATE,
        UPDATE_APPLICATION,
        DELETE_APPLICATION,
        DELETE_APPLICATIONS,
        CREATE_APPLICATION_TEMPLATE,
        CREATE_APPLICATION_TEMPLATE_FROM_SP,
        DELETE_APPLICATION_TEMPLATE,
        UPDATE_APPLICATION_TEMPLATE,
        CREATE_APPLICATION,
        UPDATE_APPLICATION_BY_RESOURCE_ID,
        DELETE_APPLICATION_BY_RESOURCE_ID,
    }

    /**
     * Enum for user operation types.
     */
    public enum OauthOperationType implements OperationType {
        UPDATE_AND_RETRIEVE_OAUTH_SECRET_KEY,
        REGISTER_AND_RETRIEVE_OAUTH_APPLICATION_DATA,
        REGISTER_OAUTH_CONSUMER,
        UPDATE_CONSUMER_APPLICATION,
        ADD_SCOPE,
        ADD_SCOPE_DTO,
        DELETE_SCOPE,
        UPDATE_SCOPE,
        UPDATE_SCOPE_DTO,
        UPDATE_CONSUMER_APP_STATE,
        REMOVE_OAUTH_APPLICATION_DATA,
        REMOVE_ALL_OAUTH_APPLICATION_DATA,
        REVOKE_AUTHZ_FOR_APPS_BY_RESOURCE_OWNER,
        REVOKE_ISSUED_TOKENS_BY_APPLICATION,
        UPDATE_APPROVE_ALWAYS_FOR_APP_CONSENT_BY_RESOURCE_OWNER
    }

    /**
     * Enum for user operation types.
     */
    public enum SAMLOperationType implements OperationType {
        ADD_RP_SERVICE_PROVIDER,
        CREATE_SERVICE_PROVIDER,
        UPLOAD_RP_SERVICE_PROVIDER,
        CREATE_SERVICE_PROVIDER_WITH_METADATA_URL,
        REMOVE_SERVICE_PROVIDER
    }

    /**
     * Enum for user operation types.
     */
    public enum CorsOperationType implements OperationType {
        SET_CORS_ORIGINS,
        ADD_CORS_ORIGINS,
        DELETE_CORS_ORIGINS,
        SET_CORS_CONFIGURATIONS
    }

    /**
     * Enum for user operation types.
     */
    public enum IdpOperationType implements OperationType {

        ADD_IDP_WITH_RESOURCE_ID,
        ADD_RESIDENT_IDP,
        UPDATE_RESIDENT_IDP,
        DELETE_IDP,
        DELETE_IDPS,
        DELETE_IDP_BY_RESOURCE_ID,
        FORCE_DELETE_IDP,
        FORCE_DELETE_IDP_BY_RESOURCE_ID,
        UPDATE_IDP,
        UPDATE_IDP_BY_RESOURCE_ID
    }
}
