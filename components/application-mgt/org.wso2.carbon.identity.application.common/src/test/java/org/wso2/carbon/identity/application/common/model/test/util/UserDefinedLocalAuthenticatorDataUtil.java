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

package org.wso2.carbon.identity.application.common.model.test.util;

import com.google.gson.Gson;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;

import java.util.HashMap;

public class UserDefinedLocalAuthenticatorDataUtil {

    private static final Gson gson = new Gson();

    /**
     * Create a user defined authenticator configuration.
     *
     * @param uniqueIdentifier Unique identifier for the authenticator.
     * @param type             Authentication type.
     * @return UserDefinedLocalAuthenticatorConfig
     */
    public static UserDefinedLocalAuthenticatorConfig createUserDefinedAuthenticatorConfig(String uniqueIdentifier,
                        AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig authenticatorConfig = new
                UserDefinedLocalAuthenticatorConfig(AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        authenticatorConfig.setName(uniqueIdentifier);
        authenticatorConfig.setDisplayName("Custom " + uniqueIdentifier);
        authenticatorConfig.setEnabled(true);
        authenticatorConfig.setDefinedByType(AuthenticatorPropertyConstants.DefinedByType.USER);
        authenticatorConfig.setAuthenticationType(type);
        authenticatorConfig.setProperties(buildAuthenticatorProperties());
        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder = buildAuthenticatorEndpointConfig();
        authenticatorConfig.setEndpointConfig(endpointConfigBuilder.build());
        authenticatorConfig.setImageUrl("https://localhost:8080/test/image");
        authenticatorConfig.setDescription("Description for " + uniqueIdentifier);

        return authenticatorConfig;
    }

    /**
     * Create a system defined authenticator configuration.
     *
     * @param uniqueIdentifier Unique identifier for the authenticator.
     * @return LocalAuthenticatorConfig
     */
    public static LocalAuthenticatorConfig createSystemDefinedAuthenticatorConfig(String uniqueIdentifier) {

        LocalAuthenticatorConfig authenticatorConfig = new LocalAuthenticatorConfig();
        authenticatorConfig.setName(uniqueIdentifier);
        authenticatorConfig.setDisplayName("Custom " + uniqueIdentifier);
        authenticatorConfig.setEnabled(true);
        authenticatorConfig.setDefinedByType(AuthenticatorPropertyConstants.DefinedByType.SYSTEM);
        Property prop1 = new Property();
        prop1.setName("PropertyName1_" + uniqueIdentifier);
        prop1.setValue("PropertyValue1_" + uniqueIdentifier);
        prop1.setConfidential(false);
        Property prop2 = new Property();
        prop2.setName("PropertyName2_" + uniqueIdentifier);
        prop2.setValue("PropertyValue2_" + uniqueIdentifier);
        prop2.setConfidential(true);
        authenticatorConfig.setProperties(new Property[]{prop1, prop2});

        return authenticatorConfig;
    }

    /**
     * Create a user defined authenticator configuration for an SQL exception.
     *
     * @param uniqueIdentifier Unique identifier for the authenticator.
     * @param type             Authentication type.
     * @return UserDefinedLocalAuthenticatorConfig
     */
    public static UserDefinedLocalAuthenticatorConfig createUserDefinedAuthenticatorConfigForSQLException(
            String uniqueIdentifier, AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig authenticatorConfigForException =
                createUserDefinedAuthenticatorConfig(uniqueIdentifier, type);
        authenticatorConfigForException.setDisplayName("Authenticator name with 254 characters".repeat(50));

        return authenticatorConfigForException;
    }

    /**
     * Build the endpoint configuration for the user defined authenticator.
     *
     * @return UserDefinedAuthenticatorEndpointConfigBuilder
     */
    public static UserDefinedAuthenticatorEndpointConfigBuilder buildAuthenticatorEndpointConfig() {

        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri("https://localhost:8080/test");
        endpointConfigBuilder.authenticationType(Authentication.Type.BASIC.getName());
        HashMap<String, String> authProperties = new HashMap<>();
        authProperties.put("username", "admin");
        authProperties.put("password", "admin");
        endpointConfigBuilder.authenticationProperties(authProperties);
        return endpointConfigBuilder;
    }

    /**
     * Update the user defined authenticator configuration.
     *
     * @param authenticatorConfig UserDefinedLocalAuthenticatorConfig
     * @return UserDefinedLocalAuthenticatorConfig
     */
    public static UserDefinedLocalAuthenticatorConfig updateUserDefinedAuthenticatorConfig(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig) {

        UserDefinedLocalAuthenticatorConfig updatingConfig = gson.fromJson(gson.toJson(authenticatorConfig),
                UserDefinedLocalAuthenticatorConfig.class);
        updatingConfig.setName(authenticatorConfig.getName());
        updatingConfig.setImageUrl("https://localhost:8080/test/imageUpdated");
        updatingConfig.setDescription("Updated description");
        updatingConfig.setDisplayName("UpdatedDisplayName");
        updatingConfig.setEnabled(false);

        return updatingConfig;
    }

    public static UserDefinedLocalAuthenticatorConfig updateUserDefinedAuthenticatorConfigForSQLException(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig) {

        UserDefinedLocalAuthenticatorConfig updatingConfig = gson.fromJson(gson.toJson(authenticatorConfig),
                UserDefinedLocalAuthenticatorConfig.class);
        updatingConfig.setName(authenticatorConfig.getName());
        updatingConfig.setEnabled(false);
        updatingConfig.setDisplayName("Authenticator name with 254 characters".repeat(50));

        return updatingConfig;
    }


        /**
         * Build the properties for the user defined authenticator.
         *
         * @return Property[]
         */
    public static Property[] buildAuthenticatorProperties() {

        Property property = new Property();
        property.setName("actionId");
        property.setValue("actionId");

        return new Property[]{property};
    }
}
