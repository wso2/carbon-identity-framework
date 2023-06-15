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

    public static class SqlQueries {

        private SqlQueries() {

        }

        public static final String ADD_SAML2_SSO_CONFIG = "INSERT INTO IDN_SAML2_SSO (ID, TENANT_UUID, " +
                "ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, ISSUER_CERT_ALIAS, NAME_ID_FORMAT, SIGNING_ALGORITHM, " +
                "DIGEST_ALGORITHM, ASSERTION_ENCRYPTION_ALGORITHM, KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, " +
                "ENABLE_SIGN_RESPONSE, ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?)";

        public static final String UPDATE_SAML2_SSO_CONFIG = "UPDATE IDN_SAML2_SSO SET " +
                "ISSUER = ?, DEFAULT_ASSERTION_CONSUMER_URL = ?, ISSUER_CERT_ALIAS = ?, NAME_ID_FORMAT = ?, " +
                "SIGNING_ALGORITHM = ?, DIGEST_ALGORITHM = ?, ASSERTION_ENCRYPTION_ALGORITHM = ?, " +
                "KEY_ENCRYPTION_ALGORITHM = ?, ENABLE_SINGLE_LOGOUT = ?, ENABLE_SIGN_RESPONSE = ?, " +
                "ENABLE_ASSERTION_QUERY_REQUEST_PROFILE = ?, ENABLE_SAML2_ARTIFACT_BINDING = ?, " +
                "ENABLE_SIGN_ASSERTIONS = ?, ENABLE_ECP = ?, ENABLE_ATTRIBUTES_BY_DEFAULT = ?, " +
                "ENABLE_IDP_INIT_SSO = ?, ENABLE_IDP_INIT_SLO = ?, ENABLE_ENCRYPTED_ASSERTION = ?, " +
                "VALIDATE_SIGNATURE_IN_REQUESTS = ?, VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = ? " +
                "WHERE ID = ? AND TENANT_UUID = ?";

        public static final String DELETE_SAML2_SSO_CONFIG_BY_ISSUER = "DELETE FROM IDN_SAML2_SSO " +
                "WHERE ISSUER = ? AND TENANT_UUID = ?";

        public static final String GET_SAML2_SSO_CONFIG_BY_ISSUER = "SELECT ID, ISSUER, " +
                "DEFAULT_ASSERTION_CONSUMER_URL, ISSUER_CERT_ALIAS, NAME_ID_FORMAT, SIGNING_ALGORITHM, " +
                "DIGEST_ALGORITHM, ASSERTION_ENCRYPTION_ALGORITHM, KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, " +
                "ENABLE_SIGN_RESPONSE, ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE FROM IDN_SAML2_SSO WHERE ISSUER = ? AND TENANT_UUID = ?";

        public static final String GET_SAML2_SSO_CONFIGS = "SELECT ID, ISSUER, DEFAULT_ASSERTION_CONSUMER_URL, " +
                "ISSUER_CERT_ALIAS, NAME_ID_FORMAT, SIGNING_ALGORITHM, DIGEST_ALGORITHM, " +
                "ASSERTION_ENCRYPTION_ALGORITHM, KEY_ENCRYPTION_ALGORITHM, ENABLE_SINGLE_LOGOUT, " +
                "ENABLE_SIGN_RESPONSE, ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, ENABLE_SAML2_ARTIFACT_BINDING, " +
                "ENABLE_SIGN_ASSERTIONS, ENABLE_ECP, ENABLE_ATTRIBUTES_BY_DEFAULT, ENABLE_IDP_INIT_SSO, " +
                "ENABLE_IDP_INIT_SLO, ENABLE_ENCRYPTED_ASSERTION, VALIDATE_SIGNATURE_IN_REQUESTS, " +
                "VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE FROM IDN_SAML2_SSO WHERE TENANT_UUID = ?";

        public static final String GET_SAML2_SSO_CONFIG_ID_BY_ISSUER = "SELECT ID FROM IDN_SAML2_SSO WHERE " +
                "ISSUER = ? AND TENANT_UUID = ?";

        public static final String ADD_SAML_SSO_ATTR = "INSERT INTO IDN_SAML2_SSO_ATTRIBUTE (ID, SAML2_SSO_ID, " +
                "ATTR_NAME, ATTR_VALUE) VALUES (?, ?, ?, ?)";

        public static final String UPDATE_SAML_SSO_ATTR_BY_ID = "UPDATE IDN_SAML2_SSO_ATTRIBUTE SET " +
                "ATTR_NAME = ?, ATTR_VALUE = ? WHERE ID = ? AND SAML2_SSO_ID = ?";

        public static final String DELETE_SAML_SSO_ATTR = "DELETE FROM IDN_SAML2_SSO_ATTRIBUTE WHERE SAML2_SSO_ID IN " +
                "(" + GET_SAML2_SSO_CONFIG_ID_BY_ISSUER + ")";

        public static final String DELETE_SAML_SSO_ATTR_BY_ID = "DELETE FROM IDN_SAML2_SSO_ATTRIBUTE WHERE " +
                "SAML2_SSO_ID = ?";

        public static final String GET_SAML_SSO_ATTR_BY_ID = "SELECT ID, ATTR_NAME, ATTR_VALUE FROM " +
                "IDN_SAML2_SSO_ATTRIBUTE WHERE SAML2_SSO_ID = ?";

    }

}
