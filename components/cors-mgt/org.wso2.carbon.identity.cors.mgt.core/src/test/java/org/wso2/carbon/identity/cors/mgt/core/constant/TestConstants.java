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

import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for the tests.
 */
public class TestConstants {

    public static final List<CORSOrigin> SAMPLE_ORIGIN_LIST_1 = Arrays.asList(
            new CORSOrigin(String.valueOf("http://foo.com".hashCode()), "http://foo.com"),
            new CORSOrigin(String.valueOf("http://bar.com".hashCode()), "http://bar.com"),
            new CORSOrigin(String.valueOf("https://foobar.com".hashCode()), "https://foobar.com"));
    public static final List<CORSOrigin> SAMPLE_ORIGIN_LIST_2 = Arrays.asList(
            new CORSOrigin(String.valueOf("http://abc.com".hashCode()), "http://abc.com"),
            new CORSOrigin(String.valueOf("https://pqr.com".hashCode()), "https://pqr.com"),
            new CORSOrigin(String.valueOf("http://xyz.com".hashCode()), "http://xyz.com"));
    public static final int TENANT_ID = -1111;
    public static final String TENANT_DOMAIN_NAME = "abc.com";

    private TestConstants() {

    }
}
