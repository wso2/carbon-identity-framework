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

package org.wso2.carbon.identity.core.constant;

/**
 * Test constants for SAMLSSOServiceProvider tests.
 */
public class TestConstants {

    public static final int TENANT_ID = 1;
    public static final String ISSUER1 = "issuer1";
    public static final String ISSUER2 = "issuer2";
    public static final String ISSUER_QUALIFIER = "issuerQualifier";
    public static final String[] ASSERTION_CONSUMER_URLS = {"http://localhost:8080/acs", "http://localhost:8080/acs1"};
    public static final String DEFAULT_ASSERTION_CONSUMER_URL = "http://localhost:8080/acs";
    public static final String CERT_ALIAS = "wso2carbon";
    public static final String SLO_RESPONSE_URL = "http://localhost:8080/sloResponse";
    public static final String SLO_REQUEST_URL = "http://localhost:8080/sloRequest";
    public static final boolean DO_SINGLE_LOGOUT = true;
    public static final boolean DO_SIGN_RESPONSE = true;
    public static final boolean DO_SIGN_ASSERTIONS = true;
    public static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "index";
    public static final String[] REQUESTED_AUDIENCES = {"audience1", "audience2"};
    public static final String[] REQUESTED_RECIPIENTS = {"recipient1", "recipient2"};
    public static final boolean ENABLE_ATTRIBUTES_BY_DEFAULT = true;
    public static final String NAME_ID_FORMAT = "nameIDFormat";
    public static final boolean IS_IDP_INIT_SSO_ENABLED = true;
    public static final boolean IDP_INIT_SLO_ENABLED = true;
    public static final String[] IDP_INIT_SLO_RETURN_TO_URLS =
            {"http://localhost:8080/returnTo1", "http://localhost:8080/returnTo2"};
    public static final boolean DO_ENABLE_ENCRYPTED_ASSERTION = false;
    public static final boolean DO_VALIDATE_SIGNATURE_IN_REQUESTS = false;
    public static final boolean DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = false;
    public static final String SIGNING_ALGORITHM_URI = "signingAlgorithmUri";
    public static final String DIGEST_ALGORITHM_URI = "digestAlgorithmUri";
    public static final String ASSERTION_ENCRYPTION_ALGORITHM_URI = "assertionEncryptionAlgorithmUri";
    public static final String KEY_ENCRYPTION_ALGORITHM_URI = "keyEncryptionAlgorithmUri";
    public static final boolean IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED = true;
    public static final String SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES = "supportedTypes";
    public static final boolean ENABLE_SAML2_ARTIFACT_BINDING = true;
    public static final boolean SAML_ECP = true;
    public static final String IDP_ENTITY_ID_ALIAS = "idpEntityIDAlias";
    public static final boolean DO_FRONT_CHANNEL_LOGOUT = true;
    public static final String FRONT_CHANNEL_LOGOUT_BINDING = "frontChannelLogoutBinding";
    public static final String ATTRIBUTE_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";

    public static final boolean UPDATED_DO_SINGLE_LOGOUT = false;
    public static final String[] UPDATED_REQUESTED_RECIPIENTS = {"updatedRecipient1", "updatedRecipient2"};
}
