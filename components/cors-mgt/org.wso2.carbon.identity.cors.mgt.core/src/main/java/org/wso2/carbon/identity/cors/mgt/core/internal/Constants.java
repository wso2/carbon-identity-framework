/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal;

/**
 * Constants for the CORS Service.
 */
public class Constants {

    public static final String CORS_ORIGIN_RESOURCE_TYPE_NAME = "CORS_ORIGIN";
    public static final String CORS_ORIGIN_RESOURCE_TYPE_DESCRIPTION = "A resource type to keep a tenant CORS origin.";
    public static final String CORS_CONFIGURATION_RESOURCE_TYPE_NAME = "CORS_CONFIGURATION";
    public static final String CORS_CONFIGURATION_RESOURCE_TYPE_DESCRIPTION =
            "A resource type to keep the tenant CORS configuration.";
    public static final String CORS_CONFIGURATION_RESOURCE_NAME = "TENANT_CORS_CONFIGURATION";

    /**
     * The delimiter that will be using when serializing/deserializing between a {@code Set<String>} and {@code String}.
     */
    public static final String SERIALIZATION_DELIMITER = ";";

    private Constants() {

    }

    /**
     * CORSConfiguration attributes.
     */
    public static class CORSConfigurationAttributes {

        public static final String ALLOW_GENERIC_HTTP_REQUESTS = "allowGenericHttpRequests";
        public static final String ALLOW_ANY_ORIGIN = "allowAnyOrigin";
        public static final String ALLOWED_ORIGINS = "allowedOrigins";
        public static final String ALLOW_SUBDOMAINS = "allowSubdomains";
        public static final String SUPPORTED_METHODS = "supportedMethods";
        public static final String SUPPORT_ANY_HEADER = "supportAnyHeader";
        public static final String SUPPORTED_HEADERS = "supportedHeaders";
        public static final String EXPOSED_HEADERS = "exposedHeaders";
        public static final String SUPPORTS_CREDENTIALS = "supportsCredentials";
        public static final String MAX_AGE = "maxAge";
        public static final String TAG_REQUESTS = "tagRequests";

        private CORSConfigurationAttributes() {

        }
    }
}
