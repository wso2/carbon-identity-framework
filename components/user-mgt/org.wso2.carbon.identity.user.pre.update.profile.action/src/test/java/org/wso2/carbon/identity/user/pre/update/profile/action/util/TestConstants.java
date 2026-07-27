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

package org.wso2.carbon.identity.user.pre.update.profile.action.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for the Pre Update Profile action component tests.
 */
public class TestConstants {

    public static final String TENANT_DOMAIN = "wso2.com";
    public static final String TEST_ID = "test-id";
    public static final String TEST_ACTION = "Test Action";
    public static final String TEST_DESCRIPTION = "Test Description";
    public static final String TEST_URL = "https://test.endpoint";
    public static final String TEST_USERNAME = "test-username";
    public static final String TEST_PASSWORD = "test-password";

    public static final String TEST_ACCESSING_ORG_ID = "9a56eb19-23c4-4306-ae13-75299c2a40af";
    public static final String TEST_ACCESSING_ORG_NAME = "mySubOrg";
    public static final String TEST_ACCESSING_ORG_HANDLE = "mySubOrg.com";
    public static final int TEST_ACCESSING_ORG_DEPTH = 20;
    public static final String TEST_RESIDENT_ORG_ID = "6a56eba9-23c4-4306-ae13-11259c2a40ae";
    public static final String TEST_RESIDENT_ORG_NAME = "mySubOrg2";
    public static final String TEST_RESIDENT_ORG_HANDLE = "mySubOrg2.com";
    public static final int TEST_RESIDENT_ORG_DEPTH = 20;
    public static final int ROOT_ORG_TENANT_ID = 12;
    public static final String ROOT_ORG_TENANT_DOMAIN = "primaryOrg.com";
    public static final String ROOT_ORG_ID = "6a56eba9-23c4-4306-ae13-11259c2a40ae";

    public static final String TEST_USER_STORE_DOMAIN_NAME = "PRIMARY";
    public static final String TEST_USER_STORE_DOMAIN_ID = Base64.getEncoder()
            .encodeToString(TEST_USER_STORE_DOMAIN_NAME.getBytes(StandardCharsets.UTF_8));
    public static final String ATTRIBUTES = "attributes";

    public static final List<String> ROLES_CLAIM_ATTRIBUTE =
            Collections.singletonList("http://wso2.org/claims/roles");
    public static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    public static final String SAMPLE_LOCAL_CLAIM_URI_1 = "http://wso2.org/accountDisabled";
    public static final String SAMPLE_LOCAL_CLAIM_URI_2 = "http://wso2.org/claims/identity/accountLocked";
    public static final String SAMPLE_LOCAL_CLAIM_URI_3 = "http://wso2.org/claims/identity/accountState";
    public static final String SAMPLE_LOCAL_CLAIM_URI_4 = "http://wso2.org/claims/active";
    public static final List<String> TEST_ATTRIBUTES = Arrays.asList("http://wso2.org/accountDisabled",
            "http://wso2.org/claims/identity/accountLocked");
    public static final List<String> UPDATED_TEST_ATTRIBUTES = Collections.singletonList(
            "http://wso2.org/claims/identity/accountState");
    public static final String INVALID_TEST_ATTRIBUTES_TYPE = "attribute1";
    public static final List<String> INVALID_TEST_ATTRIBUTES = Arrays.asList("attribute1", "attribute2");
    public static final List<String> TEST_EMPTY_ATTRIBUTES = Collections.emptyList();
    public static final List<Integer> INVALID_TEST_ATTRIBUTES_VALUES = Arrays.asList(1, 1);
    public static final List<String> INVALID_TEST_ATTRIBUTES_COUNT = Collections.nCopies(11,
            SAMPLE_LOCAL_CLAIM_URI_1);
    public static final List<String> DUPLICATED_TEST_ATTRIBUTES = Collections.nCopies(10,
            SAMPLE_LOCAL_CLAIM_URI_1);
}
