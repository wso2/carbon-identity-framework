/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.onboard.core.service.util;

/**
 * This class has the miscellaneous functions related to the user onboard core service.
 */
public class UserOnboardCoreUtil {

    private static final String DOMAIN_SEPARATOR = "/";

    /**
     * Get the domain name of the user.
     * @param domainQualifiedUsername Username with user store domain.
     * @return Domain name as a string.
     */
    public static String getUserDomainName(String domainQualifiedUsername) {
        int index = domainQualifiedUsername.indexOf(DOMAIN_SEPARATOR);
        if (index > 0) {
            return domainQualifiedUsername.substring(0, index);
        }
        return null;
    }
}
