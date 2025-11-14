/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.utils;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;

/**
 * Utility class for debug framework shared logic.
 */
public class DebugUtils {
    private static final String DEBUG_CALLBACK_PATH = "/commonauth";

    /**
     * Finds the authenticator configuration for the specified authenticator name.
     *
     * @param idp Identity Provider containing authenticator configurations.
     * @param authenticatorName Name of the authenticator to find.
     * @return FederatedAuthenticatorConfig if found, null otherwise.
     */
    public static FederatedAuthenticatorConfig findAuthenticatorConfig(IdentityProvider idp, String authenticatorName) {
        FederatedAuthenticatorConfig[] configs = idp.getFederatedAuthenticatorConfigs();
        if (configs != null) {
            for (FederatedAuthenticatorConfig config : configs) {
                if (authenticatorName.equals(config.getName())) {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * Gets property value from authenticator configuration, trying multiple possible property names.
     *
     * @param config Authenticator configuration.
     * @param propertyNames Possible property names to look for.
     * @return Property value if found, null otherwise.
     */
    public static String getPropertyValue(FederatedAuthenticatorConfig config, String... propertyNames) {
        if (config.getProperties() != null) {
            for (Property prop : config.getProperties()) {
                for (String propName : propertyNames) {
                    if (propName.equalsIgnoreCase(prop.getName())) {
                        return prop.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Generates the debug callback URL for OAuth 2.0 redirect_uri.
     * Uses clean URL without query parameters to match registered redirect URIs.
     *
     * @param context AuthenticationContext for session data (optional, can be null).
     * @return Callback URL string.
     */
    public static String buildDebugCallbackUrl(AuthenticationContext context) {
        try {
            String baseUrl = ServiceURLBuilder.create().build().getAbsolutePublicURL();
            String callbackUrl = baseUrl + DEBUG_CALLBACK_PATH;
            if (context != null) {
                context.setProperty("DEBUG_STEP_CALLBACK_URL_BUILT", true);
                context.setProperty("DEBUG_STEP_CALLBACK_URL", callbackUrl);
            }
            return callbackUrl;
        } catch (URLBuilderException e) {
            String fallbackUrl = "https://localhost:9443/commonauth";
            if (context != null) {
                context.setProperty("DEBUG_STEP_CALLBACK_URL_FALLBACK_USED", true);
                context.setProperty("DEBUG_STEP_CALLBACK_URL", fallbackUrl);
            }
            return fallbackUrl;
        }
    }
}
