/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class ApplicationConstants {

    private ApplicationConstants() {

    }

    public static final int LOCAL_IDP_ID = 1;
    public static final int LOCAL_IDP_AUTHENTICATOR_ID = 1;
    public static final String LOCAL_IDP = "wso2carbon-local-idp";
    public static final String LOCAL_IDP_NAME = "LOCAL";
    public static final String LOCAL_SP = "wso2carbon-local-sp";
    public static final String LOCAL_IDP_DEFAULT_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String STANDARD_APPLICATION = "standardAPP";
    public static final String WELLKNOWN_APPLICATION_TYPE = "appType";
    public static final String SERVICE_PROVIDERS = "ServiceProviders";

    public static final String AUTH_TYPE_DEFAULT = "default";
    public static final String AUTH_TYPE_LOCAL = "local";
    public static final String AUTH_TYPE_FEDERATED = "federated";
    public static final String AUTH_TYPE_FLOW = "flow";

    public static final String IDP_NAME = "idpName";
    public static final String IDP_AUTHENTICATOR_NAME = "authenticatorName";
    public static final String IDP_AUTHENTICATOR_DISPLAY_NAME = "authenticatorDisplayName";
    public static final String APPLICATION_DOMAIN = "Application";
    // Regex for validating application name.
    public static final String APP_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9 ._-]*$";

    public static final String TEMPLATE_CATEGORY = "category";
    public static final String CATEGORY_DISPLAY_NAME = "displayName";
    public static final String CATEGORY_ORDER = "order";
    public static final String CATEGORY_TEMPLATES = "templates";
    public static final String UNCATEGORIZED = "uncategorized";
    public static final String DISPLAY_NAME_FOR_UNCATEGORIZED = "Uncategorized";
    public static final int ORDER_FOR_UNCATEGORIZED = 10000;
    public static final String FILE_EXT_JSON = ".json";
    public static final String CATEGORIES_METADATA_FILE = "categories.json";
    public static final String TEMPLATES_DIR_PATH = CarbonUtils.getCarbonHome() + File.separator + "repository"
        + File.separator + "resources" + File.separator + "identity" + File.separator + "authntemplates" + File
        .separator;
    public static final String PURPOSE_GROUP_TYPE_SP = "SP";
    public static final String PURPOSE_GROUP_TYPE_SYSTEM = "SYSTEM";
    public static final String PURPOSE_GROUP_SHARED = "SHARED";
    
    public static final String TENANT_DEFAULT_SP_TEMPLATE_NAME = "default";
    public static final String MY_SQL = "MySQL";
    public static final String POSTGRE_SQL = "PostgreSQL";
    public static final String DB2 = "DB2";
    public static final String MICROSOFT = "Microsoft";
    public static final String S_MICROSOFT = "microsoft";
    public static final String INFORMIX = "Informix";
    public static final String H2 = "H2";
    public static final String DEFAULT_AUTH_SEQ = "default_sequence";
    public static final int DEFAULT_RESULTS_PER_PAGE = 10;
    public static final String ITEMS_PER_PAGE_PROPERTY = "ItemsPerPage";
    public static final int DEFAULT_FETCH_CHUNK_SIZE = 50;
    public static final String FETCH_CHUNK_SIZE = "FetchChunkSize";


    public static class ApplicationTableColumns {

        public static final String ID = "ID";
        public static final String APP_NAME = "APP_NAME";
        public static final String DESCRIPTION = "DESCRIPTION";

        public static final String USERNAME = "USERNAME";
        public static final String USER_STORE = "USER_STORE";
        public static final String TENANT_ID = "TENANT_ID";

        public static final String ROLE_CLAIM = "ROLE_CLAIM";
        public static final String AUTH_TYPE = "AUTH_TYPE";
        public static final String PROVISIONING_USERSTORE_DOMAIN = "PROVISIONING_USERSTORE_DOMAIN";

        public static final String IS_LOCAL_CLAIM_DIALECT = "IS_LOCAL_CLAIM_DIALECT";
        public static final String IS_SEND_LOCAL_SUBJECT_ID = "IS_SEND_LOCAL_SUBJECT_ID";
        public static final String IS_SEND_AUTH_LIST_OF_IDPS = "IS_SEND_AUTH_LIST_OF_IDPS";
        public static final String IS_USE_TENANT_DOMAIN_SUBJECT = "IS_USE_TENANT_DOMAIN_SUBJECT";
        public static final String IS_USE_USER_DOMAIN_SUBJECT = "IS_USE_USER_DOMAIN_SUBJECT";
        public static final String ENABLE_AUTHORIZATION = "ENABLE_AUTHORIZATION";

        public static final String SUBJECT_CLAIM_URI = "SUBJECT_CLAIM_URI";
        public static final String IS_SAAS_APP = "IS_SAAS_APP";
        public static final String IS_DUMB_MODE = "IS_DUMB_MODE";

        public static final String UUID = "UUID";
        public static final String IMAGE_URL = "IMAGE_URL";
        public static final String LOGIN_URL = "LOGIN_URL";
    }
}
