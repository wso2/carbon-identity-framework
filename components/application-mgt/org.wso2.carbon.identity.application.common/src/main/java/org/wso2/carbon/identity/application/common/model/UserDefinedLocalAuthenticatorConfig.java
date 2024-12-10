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

package org.wso2.carbon.identity.application.common.model;

import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;

/**
 * The user defined local authenticator configuration model.
 */
public class UserDefinedLocalAuthenticatorConfig extends LocalAuthenticatorConfig {

    private static final String TAG_2FA = "2FA";
    private static final String TAG_CUSTOM = "CUSTOM";

    protected UserDefinedAuthenticatorEndpointConfig endpointConfig;

    public UserDefinedLocalAuthenticatorConfig(AuthenticationType type) {

        definedByType = DefinedByType.USER;
        if (AuthenticationType.VERIFICATION == type) {
            setTags(new String[]{TAG_CUSTOM, TAG_2FA});
        } else {
            setTags(new String[]{TAG_CUSTOM});
        }
    }

    /**
     * Get the endpoint configurations of the User defined local authenticator config.
     *
     * @return UserDefinedAuthenticatorEndpointConfig
     */
    public UserDefinedAuthenticatorEndpointConfig getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * Set the endpoint configurations of the User defined local authenticator config.
     *
     * @param endpointConfig    The endpoint config of the User defined local authenticator config.
     */
    public void setEndpointConfig(UserDefinedAuthenticatorEndpointConfig endpointConfig) {

        this.endpointConfig = endpointConfig;
    }
}
