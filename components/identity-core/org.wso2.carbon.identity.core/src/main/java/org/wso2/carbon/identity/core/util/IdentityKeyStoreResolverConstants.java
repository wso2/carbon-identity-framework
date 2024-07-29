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
    
    // CustomKeyStoreMapping config path.
    public static final String CONFIG_ELEM_SECURITY = "Security";
    public static final String CONFIG_ELEM_KEYSTORE_MAPPINGS = "CustomKeyStoreMappings";
    public static final String CONFIG_ELEM_KEYSTORE_MAPPING = "CustomKeyStoreMapping";

    // CustomKeyStoreMapping config attributes.
    public static final String ATTR_NAME_PROTOCOL = "Protocol";
    public static final String ATTR_NAME_KEYSTORE_NAME = "KeyStoreName";
    public static final String ATTR_NAME_USE_IN_ALL_TENANTS = "UseInAllTenants";

    // KeyStore Constants.
    public static final String KEY_STORE_EXTENSION = ".jks";

    // Inbound Protocols.
    private static final String INBOUND_PROTOCOL_OAUTH = "oauth2.0";
    public static final String INBOUND_PROTOCOL_SAML = "saml2.0";
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
}
