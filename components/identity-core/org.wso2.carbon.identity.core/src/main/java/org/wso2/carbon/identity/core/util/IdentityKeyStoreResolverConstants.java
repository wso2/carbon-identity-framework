/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.util;

/**
 * This class holds the constants used by IdentityKeyStoreManager.
 */
public class IdentityKeyStoreResolverConstants {

    // Primary KeyStore configs.
    public static final String PRIMARY_KEYSTORE_CONFIG_PATH = "Security.KeyStore.";
    
    // CustomKeyStoreMapping config path.
    public static final String CONFIG_ELEM_SECURITY = "Security";
    public static final String CONFIG_ELEM_KEYSTORE_MAPPING = "KeyStoreMapping";

    public static final String CONFIG_ELEM_OAUTH = "OAuth";
    public static final String CONFIG_ELEM_WS_TRUST = "WS-Trust";
    public static final String CONFIG_ELEM_WS_FEDERATION = "WS-Federation";

    // CustomKeyStoreMapping config attributes.
    public static final String ATTR_NAME_PROTOCOL = "Protocol";
    public static final String ATTR_NAME_KEYSTORE_NAME = "KeyStoreName";
    public static final String ATTR_NAME_USE_IN_ALL_TENANTS = "UseInAllTenants";

    // KeyStore Constants.
    public static final String KEY_STORE_EXTENSION = ".jks";
    public static final String KEY_STORE_CONTEXT_SEPARATOR = "--";

    // Inbound Protocols.
    public static final String INBOUND_PROTOCOL_OAUTH = "oauth";
    public static final String INBOUND_PROTOCOL_SAML = "saml";
    public static final String INBOUND_PROTOCOL_WS_TRUST = "ws-trust";
    public static final String INBOUND_PROTOCOL_WS_FEDERATION = "ws-federation";

    /**
     * Enums for inbound protocols.
     */
    public enum InboundProtocol {

        // List of supported inbound protocols
        OAUTH(INBOUND_PROTOCOL_OAUTH),
        SAML(INBOUND_PROTOCOL_SAML),
        WS_TRUST(INBOUND_PROTOCOL_WS_TRUST),
        WS_FEDERATION(INBOUND_PROTOCOL_WS_FEDERATION);

        private final String protocolName;

        InboundProtocol(String protocolName) {
            this.protocolName = protocolName;
        }

        @Override
        public String toString() {
            return protocolName;
        }

        public static InboundProtocol fromString(String protocolName) {
            switch(protocolName) {
                case INBOUND_PROTOCOL_OAUTH:
                    return OAUTH;
                case INBOUND_PROTOCOL_SAML:
                    return SAML;
                case INBOUND_PROTOCOL_WS_TRUST:
                    return WS_TRUST;
                case INBOUND_PROTOCOL_WS_FEDERATION:
                    return WS_FEDERATION;
                default:
                    return null;
            }
        }
    }

    /**
     * ErrorMessages enum holds the error codes and messages.
     * IKSR stands for Identity Key Store Resolver.
     */
    public enum ErrorMessages {
        // Error codes for errors occurred in Carbon Kernel KeyStoreManager side.
        ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE(
                "IKSR-10001", "Error retrieving tenant keystore.",
                "Error occurred when retrieving keystore for tenant: %s."),
        ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE(
                "IKSR-10002", "Error retrieving custom keystore.",
                "Error occurred when retrieving custom keystore: %s."),
        ERROR_CODE_ERROR_RETRIEVING_TENANT_PRIVATE_KEY(
                "IKSR-10003", "Error retrieving tenant private key.",
                "Error occurred when retrieving private key for tenant: %s."),
        ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PRIVATE_KEY(
                "IKSR-10004", "Error retrieving custom keystore private key.",
                "Error occurred when retrieving private key from key store: %s."),
        ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE(
                "IKSR-10005", "Error retrieving tenant public certificate.",
                "Error occurred when retrieving public certificate for tenant: %s."),
        ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PUBLIC_CERTIFICATE(
                "IKSR-10006", "Error retrieving custom keystore public certificate.",
                "Error occurred when retrieving public certificate from key store: %s."),
        ERROR_CODE_ERROR_RETRIEVING_PRIMARY_KEYSTORE_CONFIGURATION(
                "IKSR-10007", "Error retrieving primary keystore configuration.",
                "Error occurred when retrieving primary keystore configuration."),
        ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE_CONFIGURATION(
                "IKSR-10008", "Error retrieving tenant keystore configuration.",
                "Error occurred when retrieving tenant keystore configuration for tenant: %s."),
        ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE_CONFIGURATION(
                "IKSR-10009", "Error retrieving custom keystore configuration.",
                "Error occurred when retrieving custom keystore configuration for: %s."),
        ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST(
                "IKSR-10010", "Error retrieving context public certificate. Keystore doesn't exist.",
                "Error occurred when retrieving context certificate for tenant: %s. " +
                        "Context Keystore doesn't exist."),
        ERROR_WHILE_LOADING_REGISTRY("IKSR-10011", "Error while loading registry.",
                "Error occurred while loading registry for tenant: %s."),
        ERROR_CODE_ERROR_RETRIEVING_TRUSTSTORE(
                "IKSR-10012", "Error retrieving trust store.",
                "Error occurred when retrieving trust store for tenant: %s."),

        // Errors occurred within the IdentityKeyStoreResolver
        ERROR_CODE_INVALID_ARGUMENT(
                "IKSR-20001", "Illegal arguments provided.",
                "%s must not be null or empty.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {
            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return code + " - " + message;
        }
    }
}
