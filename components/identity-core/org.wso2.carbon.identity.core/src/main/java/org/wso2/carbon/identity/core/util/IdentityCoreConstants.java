/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.util;

import java.util.Arrays;

public class IdentityCoreConstants {

    public static final String IDENTITY_CONFIG = "identity.xml";
    public static final String IDENTITY_DEFAULT_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";
    public static final String HOST_NAME = "HostName";
    public static final String MGT_CONSOLE_HOST_NAME = "MgtHostName";
    public static final String SERVER_HOST_NAME = "ServerHostName";
    public static final String AUTHENTICATION_ENDPOINT_HOST_NAME = "AuthenticationEndpoint.HostName";
    public static final String AUTHENTICATION_ENDPOINT_PATH = "AuthenticationEndpoint.Path";
    public static final String RECOVERY_ENDPOINT_HOST_NAME = "RecoveryEndpoint.HostName";
    public static final String RECOVERY_ENDPOINT_PATH = "RecoveryEndpoint.Path";
    public static final String FILE_NAME_REGEX = "FileNameRegEx";
    public static final String PORTS_OFFSET = "Ports.Offset";

    public static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";
    public static final String ENABLE_TENANT_QUALIFIED_URLS = "TenantContext.TenantQualifiedUrls.Enable";
    public static final String REQUIRED_SUPER_TENANT_IN_URLS =
            "TenantContext.TenantQualifiedUrls.RequireSuperTenantInUrls";
    public static final String APPEND_SUPER_TENANT_IN_COOKIE_PATH =
            "TenantContext.TenantQualifiedUrls.AppendSuperTenantInCookiePath";
    public static final String ENABLE_TENANTED_SESSIONS = "TenantContext.TenantQualifiedUrls.EnableTenantedSessions";
    public static final String PROXY_CONTEXT_PATH = "ProxyContextPath";
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final String UTF_8 = "UTF-8";
    public static final String UTC = "UTC";
    public static final int EVENT_LISTENER_ORDER_ID = -1;
    public static final String ENABLE_LEGACY_SAAS_AUTHENTICATION = "EnableLegacySaaSAuthentication";
    public static final String SUPER_TENANT_ALIAS_IN_PUBLIC_URL = "SuperTenantAliasInPublicUrl";

    public static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    public static final String USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS = "UseCaseSensitiveUsernameForCacheKeys";
    public static final String USER_NOT_FOUND = "UserNotFound";
    public static final String EXISTING_USER = "UserAlreadyExisting";
    public final static String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    public final static String MULTI_ATTRIBUTE_SEPARATOR_DEFAULT = ",,,";
    public final static String ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ENABLED =
            "OrgWiseMultiAttributeSeparator";
    public final static String ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_TYPE =
            "ATTRIBUTE_CONFIGURATION";
    public final static String ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_NAME =
            "multi-attribute";
    public final static String ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ATTRIBUTE_NAME =
            "MultiAttributeSeparator";

    public static final String XML_SIGNATURE_ALGORITHM_RSA_SHA256_URI = "http://www.w3.org/2001/04/xmldsig-more#rsa" +
            "-sha256";
    public static final String XML_DIGEST_ALGORITHM_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String XML_ASSERTION_ENCRYPTION_ALGORITHM_AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    public static final String XML_KEY_ENCRYPTION_ALGORITHM_RSAOAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";

    public static final String WEB_CONTEXT_ROOT = "WebContextRoot";


    public static final String USER_ACCOUNT_DISABLED = " User account is disabled";

    //UserCoreConstants class define the rest of the relevant error codes.
    public static final String USER_ACCOUNT_LOCKED_ERROR_CODE = "17003";
    public static final String USER_ACCOUNT_DISABLED_ERROR_CODE = "17004";
    public static final String USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE = "17005";
    public static final String ADMIN_FORCED_USER_PASSWORD_RESET_VIA_EMAIL_LINK_ERROR_CODE = "17006";
    public static final String ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_ERROR_CODE = "17007";
    public static final String ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_MISMATCHED_ERROR_CODE = "17008";
    public static final String USER_ACCOUNT_PENDING_APPROVAL_ERROR_CODE = "17009";
    public static final String USER_INVALID_CREDENTIALS = "17010";

    public static final String USER_ACCOUNT_STATE = "UserAccountState";

    // Pagination constants.
    public static final int DEFAULT_MAXIMUM_ITEMS_PRE_PAGE = 100;
    public static final int DEFAULT_ITEMS_PRE_PAGE = 15;
    public static final String MAXIMUM_ITEMS_PRE_PAGE_PROPERTY = "MaximumItemsPerPage";
    public static final String DEFAULT_ITEMS_PRE_PAGE_PROPERTY = "DefaultItemsPerPage";

    //DB constants
    public static final String H2 = "H2";
    public static final String INFORMIX = "Informix";
    public static final String MY_SQL = "MySQL";
    public static final String MARIADB = "MariaDB";
    public static final String ORACLE = "Oracle";
    public static final String POSTGRE_SQL = "PostgreSQL";
    public static final String DB2 = "DB2";
    public static final String MICROSOFT = "Microsoft";
    public static final String S_MICROSOFT = "microsoft";

    public static class Filter {

        public static final String AND = "and";
        public static final String OR = "or";
        public static final String NOT = "not";
    }

    public static enum UserStoreState {

        ENABLED, DISABLED
    }

    public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    public static final char ENCODED_ZERO = ALPHABET[0];

    public static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    private IdentityCoreConstants(){
    }
}
