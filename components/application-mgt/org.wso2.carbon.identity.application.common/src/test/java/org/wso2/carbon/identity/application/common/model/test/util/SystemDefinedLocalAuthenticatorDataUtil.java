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
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;

public class SystemDefinedLocalAuthenticatorDataUtil {

    private static final Gson gson = new Gson();

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

    public static LocalAuthenticatorConfig createSystemDefinedAuthenticatorConfigForSQLException(
            String uniqueIdentifier) {

        LocalAuthenticatorConfig authenticatorConfigForException =
                createSystemDefinedAuthenticatorConfig(uniqueIdentifier);
        authenticatorConfigForException.setDisplayName("Authenticator name with 254 characters".repeat(50));
        return authenticatorConfigForException;
    }

    /**
     * Update a system defined authenticator configuration.
     *
     * @param config LocalAuthenticatorConfig.
     * @return LocalAuthenticatorConfig.
     */
    public static LocalAuthenticatorConfig updateSystemDefinedAuthenticatorConfig(LocalAuthenticatorConfig config) {

        LocalAuthenticatorConfig updatedAuthenticatorConfig = gson.fromJson(gson.toJson(config),
                LocalAuthenticatorConfig.class);

        config.setName(updatedAuthenticatorConfig.getName());
        config.setDisplayName(updatedAuthenticatorConfig.getDisplayName());
        config.setEnabled(updatedAuthenticatorConfig.isEnabled());
        config.setDefinedByType(updatedAuthenticatorConfig.getDefinedByType());
        config.setProperties(updatedAuthenticatorConfig.getProperties());

        return updatedAuthenticatorConfig;
    }

    /**
     * Update a system defined authenticator configuration for an SQL exception.
     *
     * @param config LocalAuthenticatorConfig.
     * @return LocalAuthenticatorConfig.
     */
    public static LocalAuthenticatorConfig updateSystemDefinedAuthenticatorConfigForSQLException(
            LocalAuthenticatorConfig config) {

        LocalAuthenticatorConfig updatingAuthenticatorConfig = gson.fromJson(gson.toJson(config),
                LocalAuthenticatorConfig.class);
        updatingAuthenticatorConfig.setName(config.getName());
        updatingAuthenticatorConfig.setAmrValue(("Updated Long AMR Value larger than 255 chars").repeat(50));

        return updatingAuthenticatorConfig;
    }
}