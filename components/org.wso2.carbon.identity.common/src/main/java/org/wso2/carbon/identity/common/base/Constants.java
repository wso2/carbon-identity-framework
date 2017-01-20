/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.common.base;

/**
 * Identity common constants.
 */
public class Constants {

    public static final String CLOCK_SKEW_DEFAULT = "300";
    public static final String IP_UNKNOWN = "unknown";
    public static final String WSO2CARBON_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String CLAIM_TENANT_DOMAIN = "http://wso2.org/claims/tenant";
    public static final String SELF_SIGNUP_ROLE = "selfsignup";
    public static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    public static final int HTTPS_PORT_DEFAULT = 443;
    public static final String IDENTITY_CONFIG = "identity.xml";
    public static final String IDENTITY_DEFAULT_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";
    public static final String HOST_NAME = "HostName";
    public static final String FILE_NAME_REGEX = "FileNameRegEx";
    public static final int EVENT_LISTENER_ORDER_DEFAULT = -1;
    public static final String RESIDENT_IDP_RESERVED_NAME = "LOCAL";
    public static final String MULTIVALUED_PROPERTY_CHARACTER = ".";
    public static final String UNIQUE_ID_CONSTANT = "UniqueID";
    public static final String PASSWORD = "password";
    public static final String RANDOM_PHRASE_PREFIX = "random-password-generated!@#$%^&*(0)+_";
    public static final String HMAC_SHA1 = "HmacSHA1";
    public static final String NULL = "null";
    public static final String CARBON_HOME = "carbon.home";

    // HTTP headers which may contain IP address of the client in the order of priority
    static final String[] HEADERS_WITH_IP = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private Constants() {
    }

    /**
     * Server configuration.
     */
    public static class ServerConfig {

        // Server Synchronization Tolerance Config
        public static final String CLOCK_SKEW = "ClockSkew";
        public static final String PROXY_CONTEXT_PATH = "ProxyContextPath";
        public static final String WEB_CONTEXT_ROOT = "WebContextRoot";

        /**
         * Interceptor Config attributes.
         */
        public static class Interceptor {
            public static final String ROOT = "Interceptors";
            public static final String INTERCEPTOR = "Interceptor";
            public static final String TYPE = "type";
            public static final String NAME = "name";
            public static final String ORDER = "order";
            public static final String ENABLE = "enable";
            public static final String PROPERTY = "Property";
            public static final String PROPERTY_NAME = "name";
        }

        /**
         * Cache Config attributes.
         */
        public static class Cache {
            public static final String ROOT = "CacheConfig";
            public static final String MANAGER = "CacheManager";
            public static final String MANAGER_NAME = "name";
            public static final String CACHE = "Cache";
            public static final String NAME = "name";
            public static final String ENABLE = "enable";
            public static final String TIMEOUT = "timeout";
            public static final String CAPACITY = "capacity";
        }

        /**
         * Cookie Config attributes.
         */
        public static class Cookie {
            public static final String ROOT = "Cookies";
            public static final String COOKIE = "Cookie";
            public static final String NAME = "name";
            public static final String DOMAIN = "domain";
            public static final String COMMENT = "comment";
            public static final String VERSION = "version";
            public static final String PATH = "path";
            public static final String MAX_AGE = "maxAge";
            public static final String SECURE = "secure";
            public static final String HTTP_ONLY = "httpOnly";
        }

        /**
         * Carbon place holders.
         */
        public static class CarbonPlaceholders {

            public static final String CARBON_HOST = "${carbon.host}";
            public static final String CARBON_PORT = "${carbon.management.port}";
            public static final String CARBON_PORT_HTTP = "${mgt.transport.http.port}";
            public static final String CARBON_PORT_HTTPS = "${mgt.transport.https.port}";
            public static final String CARBON_PROXY_CONTEXT_PATH = "${carbon.proxycontextpath}";
            public static final String CARBON_WEB_CONTEXT_ROOT = "${carbon.webcontextroot}";
            public static final String CARBON_PROTOCOL = "${carbon.protocol}";
            public static final String CARBON_CONTEXT = "${carbon.context}";

            public static final String CARBON_PORT_HTTP_PROPERTY = "mgt.transport.http.port";
            public static final String CARBON_PORT_HTTPS_PROPERTY = "mgt.transport.https.port";
        }

        /**
         * Config elements.
         */
        public static class ConfigElements {

            public static final String PROPERTIES = "Properties";
            public static final String PROPERTY = "Property";
            public static final String ATTR_NAME = "name";
            public static final String ATTR_ENABLED = "enabled";
            public static final String PROPERTY_TYPE_STRING = "STRING";
            public static final String PROPERTY_TYPE_BLOB = "BLOB";

            private ConfigElements() {

            }

        }

    }

    /**
     * XML.
     */
    public static class XML {

        /**
         * Signature algorithms.
         */
        public static class SignatureAlgorithm {
            public static final String DSA_SHA1 = "DSA with SHA1";
            public static final String ECDSA_SHA1 = "ECDSA with SHA1";
            public static final String ECDSA_SHA256 = "ECDSA with SHA256";
            public static final String ECDSA_SHA384 = "ECDSA with SHA384";
            public static final String ECDSA_SHA512 = "ECDSA with SHA512";
            public static final String RSA_MD5 = "RSA with MD5";
            public static final String RSA_RIPEMD160 = "RSA with RIPEMD160";
            public static final String RSA_SHA1 = "RSA with SHA1";
            public static final String RSA_SHA256 = "RSA with SHA256";
            public static final String RSA_SHA384 = "RSA with SHA384";
            public static final String RSA_SHA512 = "RSA with SHA512";
        }

        /**
         * Signature algorithm URIs.
         */
        public static class SignatureAlgorithmURI {
            public static final String DSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
            public static final String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
            public static final String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
            public static final String ECDSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
            public static final String ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
            public static final String RSA_MD5 = "http://www.w3.org/2001/04/xmldsig-more#rsa-md5";
            public static final String RSA_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";
            public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
            public static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
            public static final String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
            public static final String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
        }

        /**
         * Digest algorithm.
         */
        public static class DigestAlgorithm {
            public static final String MD5 = "MD5";
            public static final String RIPEMD160 = "RIPEMD160";
            public static final String SHA1 = "SHA1";
            public static final String SHA256 = "SHA256";
            public static final String SHA384 = "SHA384";
            public static final String SHA512 = "SHA512";
        }

        /**
         * Digest algorithm URIs.
         */
        public static class DigestAlgorithmURI {
            public static final String MD5 = "http://www.w3.org/2001/04/xmldsig-more#md5";
            public static final String RIPEMD160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";
            public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
            public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
            public static final String SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
            public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
        }
    }


}
