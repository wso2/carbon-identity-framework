/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.framework.common.testng;

public class TestConstants {
    public static final String CARBON_TENANT_DOMAIN = "carbon.super";
    public static final String LOACALHOST_DOMAIN = "localhost";
    public static final String TEST_USER_NAME = "testUser";
    public static final String ATTRIBUTE_CONSUMER_INDEX = "1234567890";
    public static final String SAMPLE_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    public static final String SESSION_ID = "sessionId4567890";
    public static final String SAMPLE_SERVER_URL = "https://localhost:9443/server";
    public static final String CLAIM_URI1 = "http://wso2.org/claimuri1";
    public static final String CLAIM_URI2 = "http://wso2.org/claimuri2";
    public static final String CLAIM_VALUE1 = "ClaimValue1";
    public static final String CLAIM_VALUE2 = "ClaimValue2";

    public static final String USERSTORE_DOMAIN = "user_store_domain";
    public static final String NEW_ACCESS_TOKEN = "123456789";
    public static final String ERROR = "Error";
    public static final int TENANT_ID = 1234;
    public static final String TENANT_DOMAIN = "TestCase.com";
    public static final String CARBON_PROTOCOL = "carbon.protocol";
    public static final String CARBON_HOST = "carbon.host";
    public static final String CARBON_MANAGEMENT_PORT = "carbon.management.port";
    public static final String CARBON_PROTOCOL_HTTPS = "https";
    public static final String CARBON_HOST_LOCALHOST = "localhost";
    public static final String CARBON_DEFAULT_HTTPS_PORT = "9443";
    public static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";

    public static final String AUTHORIZATION_HANDLER_RESPONSE_TYPE_ID_TOKEN_TOKEN = "id_token token";
    public static final String AUTHORIZATION_HANDLER_RESPONSE_TYPE_TOKEN = "token";
    public static final String AUTHORIZATION_HANDLER_RESPONSE_TYPE_CODE = "code";
    public static final String AUTHORIZATION_HANDLER_RESPONSE_TYPE_ID_TOKEN = "id_token";

    public static final String CLIENT_ID = "ca19a540f544777860e44e75f605d927";
    public static final String ACESS_TOKEN_ID = "2sa9a678f890877856y66e75f605d456";
    public static final String SECRET = "87n9a540f544777860e44e75f605d435";
    public static final String APP_NAME = "myApp";
    public static final String USER_NAME = "user1";
    public static final String APP_STATE = "ACTIVE";
    public static final String CALLBACK = "http://localhost:8080/redirect";
    public static final String USER_DOMAIN_PRIMARY = "PRIMARY";
    public static final String SCOPE_STRING = "default";
    public static final String GRANT_TYPES_STRING =
            "refresh_token implicit password iwa:ntlm client_credentials authorization_code";
    public static final String ACCESS_OKEN = "d43e8da324a33bdc941b9b95cad6a6a2";
    public static final String REFRESH_OKEN = "2881c5a375d03dc0ba12787386451b29";

    public static final String DB_TYPE_H2_SQL = "h2.sql";
    public static final String DB_SCRIPTS_FOLDER_NAME = "dbScripts";
    public static final String JAVA_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
}
