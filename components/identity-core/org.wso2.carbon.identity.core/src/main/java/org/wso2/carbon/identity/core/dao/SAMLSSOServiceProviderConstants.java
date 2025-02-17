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

package org.wso2.carbon.identity.core.dao;

/**
 * Constants related to SAML service provider configurations.
 */
public class SAMLSSOServiceProviderConstants {

    public static final String SAML_STORAGE_CONFIG = "DataStorageType.SAML";
    public static final String SAML_SCHEMA_VERSION = "1.0.0";
    public static final String BACKCHANNEL_LOGOUT_BINDING = "BackChannel";
    public static final String CERTIFICATE_PROPERTY_NAME = "CERTIFICATE";

    private SAMLSSOServiceProviderConstants() {

    }

    public enum MultiValuedPropertyKey {

        ASSERTION_CONSUMER_URLS("ASSERTION_CONSUMER_URLS"),
        AUDIENCES("AUDIENCES"),
        RECIPIENTS("RECIPIENTS"),
        SLO_RETURN_TO_URLS("SLO_RETURN_TO_URLS");

        private final String value;

        MultiValuedPropertyKey(String value) {

            this.value = value;
        }

        @Override
        public String toString() {

            return value;
        }
    }

    public static class SAML2TableColumns {

        // IDN_SAML2_SERVICE_PROVIDER table.
        public static final String ID = "ID";
        public static final String ISSUER = "ISSUER";
        public static final String DEFAULT_ASSERTION_CONSUMER_URL = "DEFAULT_ASSERTION_CONSUMER_URL";
        public static final String NAME_ID_FORMAT = "NAME_ID_FORMAT";
        public static final String CERT_ALIAS = "CERT_ALIAS";
        public static final String REQ_SIG_VALIDATION = "REQ_SIG_VALIDATION";
        public static final String SIGN_RESPONSE = "SIGN_RESPONSE";
        public static final String SIGN_ASSERTIONS = "SIGN_ASSERTIONS";
        public static final String SIGNING_ALGO = "SIGNING_ALGO";
        public static final String DIGEST_ALGO = "DIGEST_ALGO";
        public static final String ENCRYPT_ASSERTION = "ENCRYPT_ASSERTION";
        public static final String ASSERTION_ENCRYPTION_ALGO = "ASSERTION_ENCRYPTION_ALGO";
        public static final String KEY_ENCRYPTION_ALGO = "KEY_ENCRYPTION_ALGO";
        public static final String ATTR_PROFILE_ENABLED = "ATTR_PROFILE_ENABLED";
        public static final String ATTR_SERVICE_INDEX = "ATTR_SERVICE_INDEX";
        public static final String SLO_PROFILE_ENABLED = "SLO_PROFILE_ENABLED";
        public static final String SLO_METHOD = "SLO_METHOD";
        public static final String SLO_RESPONSE_URL = "SLO_RESPONSE_URL";
        public static final String SLO_REQUEST_URL = "SLO_REQUEST_URL";
        public static final String IDP_INIT_SSO_ENABLED = "IDP_INIT_SSO_ENABLED";
        public static final String IDP_INIT_SLO_ENABLED = "IDP_INIT_SLO_ENABLED";
        public static final String QUERY_REQUEST_PROFILE_ENABLED = "QUERY_REQUEST_PROFILE_ENABLED";
        public static final String ECP_ENABLED = "ECP_ENABLED";
        public static final String ARTIFACT_BINDING_ENABLED = "ARTIFACT_BINDING_ENABLED";
        public static final String ARTIFACT_RESOLVE_REQ_SIG_VALIDATION = "ARTIFACT_RESOLVE_REQ_SIG_VALIDATION";
        public static final String IDP_ENTITY_ID_ALIAS = "IDP_ENTITY_ID_ALIAS";
        public static final String ISSUER_QUALIFIER = "ISSUER_QUALIFIER";
        public static final String SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES = "SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String VERSION = "VERSION";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";
        public static final String ATTR_NAME_FORMAT = "ATTR_NAME_FORMAT";

        // IDN_SAML2_SP_PROPERTIES table.
        public static final String PROPERTY_NAME = "PROPERTY_NAME";
        public static final String PROPERTY_VALUE = "PROPERTY_VALUE";
        public static final String SP_ID = "SP_ID";

        private SAML2TableColumns() {

        }
    }

    public static class SQLQueries {

        public static final String ADD_SAML2_SSO_CONFIG =
                "INSERT INTO IDN_SAML2_SERVICE_PROVIDER " +
                        "(ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, NAME_ID_FORMAT, CERT_ALIAS, REQ_SIG_VALIDATION, " +
                        "SIGN_RESPONSE, SIGN_ASSERTIONS, SIGNING_ALGO, DIGEST_ALGO, ENCRYPT_ASSERTION, " +
                        "ASSERTION_ENCRYPTION_ALGO, KEY_ENCRYPTION_ALGO, ATTR_PROFILE_ENABLED, ATTR_SERVICE_INDEX, " +
                        "SLO_PROFILE_ENABLED, SLO_METHOD, SLO_RESPONSE_URL, SLO_REQUEST_URL, IDP_INIT_SSO_ENABLED, " +
                        "IDP_INIT_SLO_ENABLED, QUERY_REQUEST_PROFILE_ENABLED, ECP_ENABLED, ARTIFACT_BINDING_ENABLED, " +
                        "ARTIFACT_RESOLVE_REQ_SIG_VALIDATION, IDP_ENTITY_ID_ALIAS, ISSUER_QUALIFIER, " +
                        "SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES, TENANT_ID, VERSION, CREATED_AT, UPDATED_AT, " +
                        "ATTR_NAME_FORMAT) " +
                        "VALUES (:ISSUER;, :DEFAULT_ASSERTION_CONSUMER_URL;, :NAME_ID_FORMAT;, :CERT_ALIAS;, " +
                        ":REQ_SIG_VALIDATION;, :SIGN_RESPONSE;, :SIGN_ASSERTIONS;, :SIGNING_ALGO;, :DIGEST_ALGO;, " +
                        ":ENCRYPT_ASSERTION;, :ASSERTION_ENCRYPTION_ALGO;, :KEY_ENCRYPTION_ALGO;, " +
                        ":ATTR_PROFILE_ENABLED;, :ATTR_SERVICE_INDEX;, :SLO_PROFILE_ENABLED;, :SLO_METHOD;, " +
                        ":SLO_RESPONSE_URL;, :SLO_REQUEST_URL;, :IDP_INIT_SSO_ENABLED;, :IDP_INIT_SLO_ENABLED;, " +
                        ":QUERY_REQUEST_PROFILE_ENABLED;, :ECP_ENABLED;, :ARTIFACT_BINDING_ENABLED;, " +
                        ":ARTIFACT_RESOLVE_REQ_SIG_VALIDATION;, :IDP_ENTITY_ID_ALIAS;, :ISSUER_QUALIFIER;, " +
                        ":SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES;, :TENANT_ID;, :VERSION;, :CREATED_AT;, " +
                        ":UPDATED_AT;, :ATTR_NAME_FORMAT;)";

        public static final String UPDATE_SAML2_SSO_CONFIG =
                "UPDATE IDN_SAML2_SERVICE_PROVIDER " +
                        "SET ISSUER = :ISSUER;, DEFAULT_ASSERTION_CONSUMER_URL = :DEFAULT_ASSERTION_CONSUMER_URL;, " +
                        "NAME_ID_FORMAT = :NAME_ID_FORMAT;, CERT_ALIAS = :CERT_ALIAS;, " +
                        "REQ_SIG_VALIDATION = :REQ_SIG_VALIDATION;, SIGN_RESPONSE = :SIGN_RESPONSE;, " +
                        "SIGN_ASSERTIONS = :SIGN_ASSERTIONS;, SIGNING_ALGO = :SIGNING_ALGO;, " +
                        "DIGEST_ALGO = :DIGEST_ALGO;, ENCRYPT_ASSERTION = :ENCRYPT_ASSERTION;, " +
                        "ASSERTION_ENCRYPTION_ALGO = :ASSERTION_ENCRYPTION_ALGO;, " +
                        "KEY_ENCRYPTION_ALGO = :KEY_ENCRYPTION_ALGO;, ATTR_PROFILE_ENABLED = :ATTR_PROFILE_ENABLED;, " +
                        "ATTR_SERVICE_INDEX = :ATTR_SERVICE_INDEX;, SLO_PROFILE_ENABLED = :SLO_PROFILE_ENABLED;, " +
                        "SLO_METHOD = :SLO_METHOD;, SLO_RESPONSE_URL = :SLO_RESPONSE_URL;, " +
                        "SLO_REQUEST_URL = :SLO_REQUEST_URL;, IDP_INIT_SSO_ENABLED = :IDP_INIT_SSO_ENABLED;, " +
                        "IDP_INIT_SLO_ENABLED = :IDP_INIT_SLO_ENABLED;, " +
                        "QUERY_REQUEST_PROFILE_ENABLED = :QUERY_REQUEST_PROFILE_ENABLED;, " +
                        "ECP_ENABLED = :ECP_ENABLED;, ARTIFACT_BINDING_ENABLED = :ARTIFACT_BINDING_ENABLED;, " +
                        "ARTIFACT_RESOLVE_REQ_SIG_VALIDATION = :ARTIFACT_RESOLVE_REQ_SIG_VALIDATION;, " +
                        "IDP_ENTITY_ID_ALIAS = :IDP_ENTITY_ID_ALIAS;, ISSUER_QUALIFIER = :ISSUER_QUALIFIER;, " +
                        "SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES = :SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES;, " +
                        "UPDATED_AT = :UPDATED_AT;, ATTR_NAME_FORMAT = :ATTR_NAME_FORMAT; " +
                        "WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

        public static final String DELETE_SAML2_SSO_CONFIG_BY_ISSUER =
                "DELETE FROM IDN_SAML2_SERVICE_PROVIDER " +
                        "WHERE ISSUER = :ISSUER; AND TENANT_ID = :TENANT_ID;";

        public static final String GET_SAML2_SSO_CONFIG_BY_ISSUER =
                "SELECT ID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, NAME_ID_FORMAT, CERT_ALIAS, REQ_SIG_VALIDATION, " +
                        "SIGN_RESPONSE, SIGN_ASSERTIONS, SIGNING_ALGO, DIGEST_ALGO, ENCRYPT_ASSERTION, " +
                        "ASSERTION_ENCRYPTION_ALGO, KEY_ENCRYPTION_ALGO, ATTR_PROFILE_ENABLED, ATTR_SERVICE_INDEX, " +
                        "SLO_PROFILE_ENABLED, SLO_METHOD, SLO_RESPONSE_URL, SLO_REQUEST_URL, IDP_INIT_SSO_ENABLED, " +
                        "IDP_INIT_SLO_ENABLED, QUERY_REQUEST_PROFILE_ENABLED, ECP_ENABLED, ARTIFACT_BINDING_ENABLED, " +
                        "ARTIFACT_RESOLVE_REQ_SIG_VALIDATION, IDP_ENTITY_ID_ALIAS, ISSUER_QUALIFIER, " +
                        "SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES, TENANT_ID, ATTR_NAME_FORMAT " +
                        "FROM IDN_SAML2_SERVICE_PROVIDER " +
                        "WHERE ISSUER = :ISSUER; " +
                        "AND TENANT_ID = :TENANT_ID;";

        public static final String GET_SAML2_SSO_CONFIGS =
                "SELECT ID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, NAME_ID_FORMAT, CERT_ALIAS, REQ_SIG_VALIDATION, " +
                        "SIGN_RESPONSE, SIGN_ASSERTIONS, SIGNING_ALGO, DIGEST_ALGO, ENCRYPT_ASSERTION, " +
                        "ASSERTION_ENCRYPTION_ALGO, KEY_ENCRYPTION_ALGO, ATTR_PROFILE_ENABLED, ATTR_SERVICE_INDEX, " +
                        "SLO_PROFILE_ENABLED, SLO_METHOD, SLO_RESPONSE_URL, SLO_REQUEST_URL, IDP_INIT_SSO_ENABLED, " +
                        "IDP_INIT_SLO_ENABLED, QUERY_REQUEST_PROFILE_ENABLED, ECP_ENABLED, ARTIFACT_BINDING_ENABLED, " +
                        "ARTIFACT_RESOLVE_REQ_SIG_VALIDATION, IDP_ENTITY_ID_ALIAS, ISSUER_QUALIFIER, " +
                        "SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES, TENANT_ID, ATTR_NAME_FORMAT " +
                        "FROM IDN_SAML2_SERVICE_PROVIDER " +
                        "WHERE TENANT_ID = :TENANT_ID;";

        public static final String GET_SAML_SP_ID_BY_ISSUER =
                "SELECT ID " +
                        "FROM IDN_SAML2_SERVICE_PROVIDER " +
                        "WHERE ISSUER = :ISSUER; " +
                        "AND TENANT_ID = :TENANT_ID;";

        public static final String ADD_SAML_SSO_ATTR =
                "INSERT INTO IDN_SAML2_SP_PROPERTIES " +
                        "(PROPERTY_NAME, PROPERTY_VALUE, SP_ID) " +
                        "VALUES (:PROPERTY_NAME;, :PROPERTY_VALUE;, :SP_ID;)";

        public static final String DELETE_SAML_SSO_ATTR =
                "DELETE FROM IDN_SAML2_SP_PROPERTIES " +
                        "WHERE SP_ID IN (" + GET_SAML_SP_ID_BY_ISSUER + ")";

        public static final String DELETE_SAML_SSO_ATTR_BY_ID =
                "DELETE FROM IDN_SAML2_SP_PROPERTIES " +
                        "WHERE SP_ID = :SP_ID;";

        public static final String GET_SAML_SSO_ATTR_BY_ID =
                "SELECT ID, PROPERTY_NAME, PROPERTY_VALUE " +
                        "FROM IDN_SAML2_SP_PROPERTIES " +
                        "WHERE SP_ID = :SP_ID;";

        public static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID = "SELECT " +
                "META.VALUE FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID " +
                "AND SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";

        public static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2 = "SELECT " +
                "META.`VALUE` FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID " +
                "AND SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";

        private SQLQueries() {

        }
    }
}
