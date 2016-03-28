/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint;

public class Constants {

    public static class UserRegistrationConstants {

        public static final String WSO2_DIALECT = "http://wso2.org/claims";
        public static final String FIRST_NAME = "First Name";
        public static final String LAST_NAME = "Last Name";
        public static final String EMAIL_ADDRESS = "Email";
        public static final String USER_REGISTRATION_SERVICE = "/UserRegistrationAdminService" +
                                                               ".UserRegistrationAdminServiceHttpsSoap11Endpoint/";

        private UserRegistrationConstants() {

        }
    }

    public static final String USER_INFORMATION_RECOVERY_SERVICE = "/UserInformationRecoveryService" +
                                                           ".UserInformationRecoveryServiceHttpsSoap11Endpoint/";
    private Constants() {
    }
}
