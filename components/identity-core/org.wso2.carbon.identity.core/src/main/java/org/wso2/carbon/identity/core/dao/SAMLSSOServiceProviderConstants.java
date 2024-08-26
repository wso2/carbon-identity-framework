/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

public class SAMLSSOServiceProviderConstants {

    private SAMLSSOServiceProviderConstants() {

    }

    public static class SAML2TableColumns {

        private SAML2TableColumns() {

        }

        // IDN_SAML2_SSO table
        public static final String ID = "ID";
        public static final String TENANT_UUID = "TENANT_UUID";
        public static final String ISSUER = "ISSUER";
        public static final String DEFAULT_ASSERTION_CONSUMER_URL = "DEFAULT_ASSERTION_CONSUMER_URL";
        public static final String ISSUER_CERT_ALIAS = "ISSUER_CERT_ALIAS";
        public static final String NAME_ID_FORMAT = "NAME_ID_FORMAT";
        public static final String SIGNING_ALGORITHM = "SIGNING_ALGORITHM";
        public static final String DIGEST_ALGORITHM = "DIGEST_ALGORITHM";
        public static final String ASSERTION_ENCRYPTION_ALGORITHM = "ASSERTION_ENCRYPTION_ALGORITHM";
        public static final String KEY_ENCRYPTION_ALGORITHM = "KEY_ENCRYPTION_ALGORITHM";
        public static final String ENABLE_SINGLE_LOGOUT = "ENABLE_SINGLE_LOGOUT";
        public static final String ENABLE_SIGN_RESPONSE = "ENABLE_SIGN_RESPONSE";
        public static final String ENABLE_ASSERTION_QUERY_REQUEST_PROFILE = "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE";
        public static final String ENABLE_SAML2_ARTIFACT_BINDING = "ENABLE_SAML2_ARTIFACT_BINDING";
        public static final String ENABLE_SIGN_ASSERTIONS = "ENABLE_SIGN_ASSERTIONS";
        public static final String ENABLE_ECP = "ENABLE_ECP";
        public static final String ENABLE_ATTRIBUTES_BY_DEFAULT = "ENABLE_ATTRIBUTES_BY_DEFAULT";
        public static final String ENABLE_IDP_INIT_SSO = "ENABLE_IDP_INIT_SSO";
        public static final String ENABLE_IDP_INIT_SLO = "ENABLE_IDP_INIT_SLO";
        public static final String ENABLE_ENCRYPTED_ASSERTION = "ENABLE_ENCRYPTED_ASSERTION";
        public static final String VALIDATE_SIGNATURE_IN_REQUESTS = "VALIDATE_SIGNATURE_IN_REQUESTS";
        public static final String VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE";

        // IDN_SAML2_SSO_ATTRIBUTE table
        public static final String SAML2_SSO_ID = "SAML2_SSO_ID";
        public static final String ATTR_NAME = "ATTR_NAME";
        public static final String ATTR_VALUE = "ATTR_VALUE";

    }

    public static class SqlQueries {

        private SqlQueries() {

        }

        public static final String ADD_SAML2_SSO_CONFIG =
                "INSERT INTO IDN_SAML2_SSO " +
                        "(ID, TENANT_UUID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, ISSUER_CERT_ALIAS, " +
                        "NAME_ID_FORMAT, SIGNING_ALGORITHM, DIGEST_ALGORITHM, ASSERTION_ENCRYPTION_ALGORITHM, " +
                        "KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, ENABLE_SIGN_RESPONSE, " +
                        "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                        "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                        "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                        "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE) " +
                        "VALUES (:ID;, :TENANT_UUID;, :ISSUER;, :DEFAULT_ASSERTION_CONSUMER_URL;, " +
                        ":ISSUER_CERT_ALIAS;, :NAME_ID_FORMAT;, :SIGNING_ALGORITHM;, :DIGEST_ALGORITHM;, " +
                        ":ASSERTION_ENCRYPTION_ALGORITHM;, :KEY_ENCRYPTION_ALGORITHM;, :ENABLE_SINGLE_LOGOUT;, " +
                        ":ENABLE_SIGN_RESPONSE;, :ENABLE_ASSERTION_QUERY_REQUEST_PROFILE;, " +
                        ":ENABLE_SAML2_ARTIFACT_BINDING;, :ENABLE_SIGN_ASSERTIONS;, :ENABLE_ECP;, " +
                        ":ENABLE_ATTRIBUTES_BY_DEFAULT;, :ENABLE_IDP_INIT_SSO;, :ENABLE_IDP_INIT_SLO;, " +
                        ":ENABLE_ENCRYPTED_ASSERTION;, :VALIDATE_SIGNATURE_IN_REQUESTS;, " +
                        ":VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE;)";

        public static final String UPDATE_SAML2_SSO_CONFIG =
                "UPDATE IDN_SAML2_SSO " +
                        "SET ISSUER = :ISSUER;, DEFAULT_ASSERTION_CONSUMER_URL = :DEFAULT_ASSERTION_CONSUMER_URL;, " +
                        "ISSUER_CERT_ALIAS = :ISSUER_CERT_ALIAS;, NAME_ID_FORMAT = :NAME_ID_FORMAT;, " +
                        "SIGNING_ALGORITHM = :SIGNING_ALGORITHM;, DIGEST_ALGORITHM = :DIGEST_ALGORITHM;, " +
                        "ASSERTION_ENCRYPTION_ALGORITHM = :ASSERTION_ENCRYPTION_ALGORITHM;, " +
                        "KEY_ENCRYPTION_ALGORITHM = :KEY_ENCRYPTION_ALGORITHM;, ENABLE_SINGLE_LOGOUT = " +
                        ":ENABLE_SINGLE_LOGOUT;, ENABLE_SIGN_RESPONSE = :ENABLE_SIGN_RESPONSE;, " +
                        "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE = :ENABLE_ASSERTION_QUERY_REQUEST_PROFILE;, " +
                        "ENABLE_SAML2_ARTIFACT_BINDING = :ENABLE_SAML2_ARTIFACT_BINDING;, ENABLE_SIGN_ASSERTIONS = " +
                        ":ENABLE_SIGN_ASSERTIONS;, ENABLE_ECP = :ENABLE_ECP;, ENABLE_ATTRIBUTES_BY_DEFAULT = " +
                        ":ENABLE_ATTRIBUTES_BY_DEFAULT;, ENABLE_IDP_INIT_SSO = :ENABLE_IDP_INIT_SSO;, " +
                        "ENABLE_IDP_INIT_SLO = :ENABLE_IDP_INIT_SLO;, ENABLE_ENCRYPTED_ASSERTION = " +
                        ":ENABLE_ENCRYPTED_ASSERTION;, VALIDATE_SIGNATURE_IN_REQUESTS = :VALIDATE_SIGNATURE_IN_REQUESTS;, " +
                        "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = :VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE; " +
                        "WHERE ID = :ID; AND TENANT_UUID = :TENANT_UUID;";

        public static final String DELETE_SAML2_SSO_CONFIG_BY_ISSUER =
                "DELETE FROM IDN_SAML2_SSO " +
                        "WHERE ISSUER = :ISSUER; AND TENANT_UUID = :TENANT_UUID;";

        public static final String GET_SAML2_SSO_CONFIG_BY_ISSUER =
                "SELECT ID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, ISSUER_CERT_ALIAS, NAME_ID_FORMAT, " +
                        "SIGNING_ALGORITHM, DIGEST_ALGORITHM, ASSERTION_ENCRYPTION_ALGORITHM, " +
                        "KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, ENABLE_SIGN_RESPONSE, " +
                        "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                        "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                        "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                        "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE " +
                        "FROM IDN_SAML2_SSO " +
                        "WHERE ISSUER = :ISSUER; " +
                        "AND TENANT_UUID = :TENANT_UUID;";

        public static final String GET_SAML2_SSO_CONFIGS =
                "SELECT ID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, ISSUER_CERT_ALIAS, NAME_ID_FORMAT, " +
                        "SIGNING_ALGORITHM, DIGEST_ALGORITHM, ASSERTION_ENCRYPTION_ALGORITHM, " +
                        "KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, ENABLE_SIGN_RESPONSE, " +
                        "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                        "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                        "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                        "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE " +
                        "FROM IDN_SAML2_SSO " +
                        "WHERE TENANT_UUID = :TENANT_UUID;";

        public static final String GET_SAML2_SSO_CONFIG_ID_BY_ISSUER =
                "SELECT ID " +
                        "FROM IDN_SAML2_SSO " +
                        "WHERE ISSUER = :ISSUER; " +
                        "AND TENANT_UUID = :TENANT_UUID;";

        public static final String ADD_SAML_SSO_ATTR =
                "INSERT INTO IDN_SAML2_SSO_ATTRIBUTE " +
                        "(ID, SAML2_SSO_ID, ATTR_NAME, ATTR_VALUE) " +
                        "VALUES (:ID;, :SAML2_SSO_ID;, :ATTR_NAME;, :ATTR_VALUE;)";

        public static final String UPDATE_SAML_SSO_ATTR_BY_ID =
                "UPDATE IDN_SAML2_SSO_ATTRIBUTE " +
                        "SET ATTR_NAME = :ATTR_NAME;, ATTR_VALUE = :ATTR_VALUE; " +
                        "WHERE ID = :ID; AND SAML2_SSO_ID = :SAML2_SSO_ID;";

        public static final String DELETE_SAML_SSO_ATTR =
                "DELETE FROM IDN_SAML2_SSO_ATTRIBUTE " +
                        "WHERE SAML2_SSO_ID IN (" + GET_SAML2_SSO_CONFIG_ID_BY_ISSUER + ")";

        public static final String DELETE_SAML_SSO_ATTR_BY_ID =
                "DELETE FROM IDN_SAML2_SSO_ATTRIBUTE " +
                        "WHERE SAML2_SSO_ID = :SAML2_SSO_ID;";

        public static final String GET_SAML_SSO_ATTR_BY_ID =
                "SELECT ID, ATTR_NAME, ATTR_VALUE " +
                        "FROM IDN_SAML2_SSO_ATTRIBUTE " +
                        "WHERE SAML2_SSO_ID = :SAML2_SSO_ID;";

    }

}
