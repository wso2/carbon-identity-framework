/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.utils;

import java.util.regex.Pattern;

/**
 * Constants used for log management.
 */
public class LogConstants {

    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";
    public static final String INITIATOR_ID = "initiatorId";
    public static final String INITIATOR_TYPE = "initiatorType";
    public static final String TARGET_ID = "targetId";
    public static final String TARGET_TYPE = "targetType";

    /**
     * Constants related to masking sensitive info in logs.
     */
    public static final String USER_ID_CLAIM_URI = "http://wso2.org/claims/userid";
    public static final String MASKING_CHARACTER = "*";
    public static final String ENABLE_LOG_MASKING = "MaskingLogs.Enabled";
    public static final Pattern LOG_MASKING_PATTERN = Pattern.compile("(?<=.).(?=.)");

    /**
     * Define common and reusable Input keys for diagnostic logs.
     */
    public static class InputKeys {

        public static final String SERVICE_PROVIDER = "service provider";
        public static final String TENANT_DOMAIN = "tenant domain";
        public static final String USER = "user";
        public static final String USER_ID = "user id";
        public static final String AUTHENTICATOR_NAME = "authenticator name";
        public static final String STEP = "step";
        public static final String COUNT = "count";
        public static final String IDP = "idp";
        public static final String APPLICATION_NAME = "application name";
        public static final String SUBJECT = "subject";
        public static final String CLIENT_ID = "client id";
        public static final String REDIREDCT_URI = "redirect uri";
        public static final String SCOPE = "scope";
    }
}
