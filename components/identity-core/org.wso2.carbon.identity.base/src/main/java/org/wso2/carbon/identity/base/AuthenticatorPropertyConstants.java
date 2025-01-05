/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.base;

public class AuthenticatorPropertyConstants {

    public static final String TAG_2FA = "2FA";
    public static final String TAG_CUSTOM = "Custom";

    /**
     * The Defined by Types - SYSTEM: system define authenticator, USER: user defined authentication extension.
     */
    public static enum DefinedByType {

        SYSTEM,
        USER
    }

    /**
     * The Authentication Type -
     * Identification: Can collect the user identifier, credential and authenticate. Can engage in any step of
     *                  the login flow.
     * Verification: Only perform additional verification on top of a user identity provided as input. Can engage in 2nd
     *                  or later step of the login flow.
     */
    public enum AuthenticationType {

        IDENTIFICATION,
        VERIFICATION
    }
}
