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
    public static final String INSERT_APPLICATION =
            "INSERT INTO SP_APP (ID, TENANT_ID, APP_NAME, UUID) " +
                    "VALUES (?, ?, ?, ?)";

    private TestConstants() {

    }

    public static class SampleTenant {

        public static final int ID = 4;
        public static final String DOMAIN_NAME = "abc.com";

        private SampleTenant() {

        }
    }

    public static class SampleApp1 {

        public static final int ID = 1;
        public static final String NAME = "App 1";
        public static final String UUID = "c0881fadfb6f4d08b4ad2680364bd998";

        private SampleApp1() {

        }
    }

    public static class SampleApp2 {

        public static final int ID = 2;
        public static final String NAME = "App 2";
        public static final String UUID = "4d34790b8d164e03b6c5476c0bf31038";

        private SampleApp2() {

        }
    }

    public static class SampleApp3 {

        public static final int ID = 3;
        public static final String NAME = "App 3";
        public static final String UUID = "1q67890b8d164jh39os574h5c6f36s38";

        private SampleApp3() {

        }
    }
}
