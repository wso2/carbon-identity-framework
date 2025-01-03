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

package org.wso2.carbon.identity.extension.mgt.utils;

import java.util.Optional;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.extension.mgt.exception.ExtensionManagementException;

import java.nio.file.Path;
import java.util.Objects;
import org.wso2.carbon.user.core.UserCoreConstants;

import static org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants.EXTENSION_TYPES_CONFIG;

/**
 * Utility class for extension management.
 */
public class ExtensionMgtUtils {

    /**
     * Get the path of the extension type.
     *
     * @param extensionType Type of the extension.
     * @return Path of the extension type.
     */
    public static Path getExtensionPath(String extensionType) {

        return ExtensionMgtConstants.EXTENSION_RESOURCES_PATH.resolve(extensionType);
    }

    /**
     * Get the path of the extension.
     *
     * @return Path of the extension.
     */
    public static String[] getExtensionTypes() {

        return Objects.requireNonNull(IdentityUtil.getProperty(EXTENSION_TYPES_CONFIG)).split("\\s*,\\s*");
    }

    /**
     * Validate the extension type.
     *
     * @param extensionType Type of the extension.
     * @throws ExtensionManagementException ExtensionManagementException.
     */
    public static void validateExtensionType(String extensionType) throws ExtensionManagementException {

        if (!ArrayUtils.contains(getExtensionTypes(), extensionType)) {
            throw new ExtensionManagementException("Invalid extension type: " + extensionType);
        }
    }

    /**
     * Modify the JIT provisioning user store in the connection template if the specified user store is set to
     * `PRIMARY` and the primary domain name has been altered in the deployment configuration settings.
     *
     * @param connectionTemplate - Connection template configuration data.
     */
    public static void resolveConnectionJITPrimaryDomainName(JSONObject connectionTemplate) {

        if (connectionTemplate == null) {
            return;
        }

        Optional.ofNullable(connectionTemplate.optJSONObject(ExtensionMgtConstants.IDP_CONFIG_KEY))
                .map(idpConfigs -> idpConfigs.optJSONObject(ExtensionMgtConstants.IDP_PROVISIONING_CONFIG_KEY))
                .map(provisioningConfigs -> provisioningConfigs.optJSONObject(
                        ExtensionMgtConstants.IDP_PROVISIONING_JIT_CONFIG_KEY))
                .ifPresent(jitConfigs -> {
                    String jitDomainName =
                            jitConfigs.optString(ExtensionMgtConstants.IDP_PROVISIONING_JIT_DOMAIN_NAME_KEY);
                    String primaryDomainName = IdentityUtil.getPrimaryDomainName();

                    if (StringUtils.equalsIgnoreCase(jitDomainName, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) &&
                            !StringUtils.equalsIgnoreCase(primaryDomainName,
                                    UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                        jitConfigs.put(ExtensionMgtConstants.IDP_PROVISIONING_JIT_DOMAIN_NAME_KEY, primaryDomainName);
                    }
                });
    }
}
