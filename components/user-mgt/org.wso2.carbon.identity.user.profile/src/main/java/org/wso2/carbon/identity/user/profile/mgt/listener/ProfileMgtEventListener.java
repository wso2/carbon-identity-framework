/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.identity.user.profile.mgt.listener;

import org.wso2.carbon.identity.base.IdentityValidationUtil;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

public class ProfileMgtEventListener extends AbstractIdentityUserOperationEventListener {

    private static final String ALPHANUMERICS_ONLY = "ALPHANUMERICS_ONLY";
    private static final String DIGITS_ONLY = "DIGITS_ONLY";
    private static final String WHITESPACE_EXISTS = "WHITESPACE_EXISTS";
    private static final String URI_RESERVED_EXISTS = "URI_RESERVED_EXISTS";
    private static final String HTML_META_EXISTS = "HTML_META_EXISTS";
    private static final String XML_META_EXISTS = "XML_META_EXISTS";
    private static final String REGEX_META_EXISTS = "REGEX_META_EXISTS";
    private static final String URL = "URL";

    @Override
    public int getExecutionOrderId() {
        return 110 ;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        //The following black listed patterns contain possible invalid inputs for profile which could be used for a stored
        //XSS attack.
        if (!IdentityValidationUtil.isValid(profileName, new String[]{ALPHANUMERICS_ONLY, DIGITS_ONLY}, new String[]{
                WHITESPACE_EXISTS, URI_RESERVED_EXISTS, HTML_META_EXISTS, XML_META_EXISTS, REGEX_META_EXISTS,
                URL})) {
            throw new UserStoreException("profile name contains invalid characters!");
        }
        return true;
    }
}
