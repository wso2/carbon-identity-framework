/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.constant;

/**
 * Constants for Webhook Metadata Service.
 */
public class WebhookMetadataConstants {

    private WebhookMetadataConstants() {

    }

    /**
     * Adapter configuration related constants.
     */
    public static class AdapterConfig {

        public static final String CONFIG_FILE_NAME = "identity-outbound-adapter.properties";
        public static final String ADAPTER_PREFIX = "adapter.";
        public static final String ENABLED_KEY = "enabled";
        public static final String ENABLED_VALUE_TRUE = "true";
        public static final String TYPE_KEY = "type";

        private AdapterConfig() {

        }
    }

    /**
     * Metadata property field names.
     */
    public static class MetadataPropertyFields {

        public static final String ORGANIZATION_POLICY_FIELD = "organization policy";
        public static final String ORGANIZATION_POLICY_PROPERTY_NAME = "organizationPolicy";

        private MetadataPropertyFields() {

        }
    }
}
