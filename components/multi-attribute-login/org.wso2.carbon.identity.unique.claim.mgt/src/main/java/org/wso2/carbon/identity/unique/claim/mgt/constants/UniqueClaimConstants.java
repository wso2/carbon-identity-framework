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

package org.wso2.carbon.identity.unique.claim.mgt.constants;

/**
 * Constants for unique claim management.
 */
public class UniqueClaimConstants {

    /**
     * Error codes for unique claim management.
     */
    public enum ErrorMessages {

        ERROR_CODE_DUPLICATE_SINGLE_CLAIM("60001", "The value defined for %s is already in use by a different user!"),
        ERROR_CODE_DUPLICATE_MULTIPLE_CLAIMS("60002",
                "The values defined for %s are already in use by different users!");

        private final String code;
        private final String message;
        private static final String UNIQUE_CLAIM_ERROR_CODE_PREFIX = "UCM-";

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * To get the code of specific error.
         *
         * @return Error code.
         */
        public String getCode() {
            return UNIQUE_CLAIM_ERROR_CODE_PREFIX + code;
        }

        /**
         * To get the message of specific error.
         *
         * @return Error message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * To get the message of specific error with arguments.
         *
         * @param args Arguments to be used in the message.
         * @return Error message.
         */
        public String getMessage(Object... args) {
            return String.format(message, args);
        }

        @Override
        public String toString() {
            return getCode() + ":" + message;
        }
    }
}
