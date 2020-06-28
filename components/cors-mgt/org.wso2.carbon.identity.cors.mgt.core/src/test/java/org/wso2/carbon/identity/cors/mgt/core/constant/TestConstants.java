/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.cors.mgt.core.constant;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for the tests.
 */
public class TestConstants {

    public static final List<String> SAMPLE_ORIGIN_LIST_1 = Arrays.asList(
            "http://foo.com",
            "http://bar.com",
            "https://foobar.com");
    public static final List<String> SAMPLE_ORIGIN_LIST_2 = Arrays.asList(
            "http://abc.com",
            "https://pqr.com",
            "http://xyz.com");
    public static final int TENANT_ID = -1111;
    public static final String TENANT_DOMAIN_NAME = "abc.com";
    public static final String APP_ID_1 = "test_app_1";
    public static final String APP_ID_2 = "test_app_2";

    private TestConstants() {

    }
}
