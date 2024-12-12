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

package org.wso2.carbon.identity.application.common.cache;

import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache Entry for the user defined local application authenticator configurations.
 */
public class AuthenticatorCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -6234723984328871924L;

    private UserDefinedLocalAuthenticatorConfig authenticatorConfig;

    public AuthenticatorCacheEntry(UserDefinedLocalAuthenticatorConfig authenticatorConfig) {

        this.authenticatorConfig = authenticatorConfig;
    }

    public UserDefinedLocalAuthenticatorConfig getAuthenticatorConfig() {

        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(UserDefinedLocalAuthenticatorConfig authenticatorConfig) {

        this.authenticatorConfig = authenticatorConfig;
    }
}
