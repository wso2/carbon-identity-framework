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

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.extension.mgt.exception.ExtensionManagementException;

import java.nio.file.Path;
import java.util.Objects;

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
}
