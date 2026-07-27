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

import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_2FA;
import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_CUSTOM;

/**
 * The user defined local authenticator configuration model.
 */
public class UserDefinedLocalAuthenticatorConfig extends LocalAuthenticatorConfig {

    private AuthenticationType authenticationType;
    private UserDefinedAuthenticatorEndpointConfig endpointConfig;
    private String imageUrl;
    private String description;

    public UserDefinedLocalAuthenticatorConfig(AuthenticationType type) {

        authenticationType = type;
        definedByType = DefinedByType.USER;
        if (AuthenticationType.VERIFICATION == type) {
            setTags(new String[]{TAG_CUSTOM, TAG_2FA});
        } else {
            setTags(new String[]{TAG_CUSTOM});
        }
    }

    /**
     * Get the endpoint configurations of the user defined local authenticator config.
     *
     * @return UserDefinedAuthenticatorEndpointConfig
     */
    public UserDefinedAuthenticatorEndpointConfig getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * Set the endpoint configurations of the user defined local authenticator config.
     *
     * @param endpointConfig    The endpoint config of the user defined local authenticator config.
     */
    public void setEndpointConfig(UserDefinedAuthenticatorEndpointConfig endpointConfig) {

        this.endpointConfig = endpointConfig;
    }

    /**
     * Get the authentication type of the user defined local authenticator config.
     *
     * @return AuthenticationType.
     */
    public AuthenticationType getAuthenticationType() {

        return authenticationType;
    }

    /**
     * Set the authentication type of the user defined local authenticator config.
     *
     * @param authenticationType    The authentication type of the user defined local authenticator config.
     */
    public void setAuthenticationType(AuthenticationType authenticationType) {

        this.authenticationType = authenticationType;
    }

    /**
     * Get the image url of the local authenticator config.
     *
     * @return Image
     */
    public String getImageUrl() {

        return imageUrl;
    }

    /**
     * Set the image url of the local authenticator config.
     *
     * @param imageUrl The image of the local authenticator config.
     */
    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    /**
     * Get the description of the local authenticator config.
     *
     * @return Description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set the description of the local authenticator config.
     *
     * @param description The description of the local authenticator config.
     */
    public void setDescription(String description) {

        this.description = description;
    }
}
