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

/**
 * Constants used for log management.
 */
public class LogConstants {

    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";

    /**
     * Constants related to masking sensitive info in logs.
     */
    public static String userIdClaimURI = "http://wso2.org/claims/userid";
    public static boolean isLogMaskingEnable;
    public static final String MASKING_CHARACTER = "*";
    public static final String CONTENT_MASKING_REGEX = "(?<=.).(?=.)";
    public static final String ENABLE_LOG_MASKING = "MaskingLogs.Enabled";
}
