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

    public static final String TEST_USER_STORE_DOMAIN_NAME = "PRIMARY";
    public static final String TEST_USER_STORE_DOMAIN_ID = Base64.getEncoder()
            .encodeToString(TEST_USER_STORE_DOMAIN_NAME.getBytes(StandardCharsets.UTF_8));
    public static final String ATTRIBUTES = "attributes";
    public static final List<String> TEST_ATTRIBUTES = Arrays.asList("http://wso2.org/attribute1",
            "http://wso2.org/attribute2");
    public static final List<String> UPDATED_TEST_ATTRIBUTES = Collections.singletonList("http://wso2.org/attribute3");
    public static final String INVALID_TEST_ATTRIBUTES = "attribute1";
    public static final List<Integer> INVALID_TEST_ATTRIBUTES_VALUES = Arrays.asList(1, 1);
}
