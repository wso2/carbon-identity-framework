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

import org.wso2.carbon.utils.CarbonUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constants for extension management.
 */
public class ExtensionMgtConstants {

    public static final Path EXTENSION_RESOURCES_PATH = Paths.get(CarbonUtils.getCarbonHome(), "repository",
            "resources", "identity", "extensions");

    public static final String INFO_FILE_NAME = "info.json";

    public static final String TEMPLATE_FILE_NAME = "template.json";

    public static final String METADATA_FILE_NAME = "metadata.json";

    public static final String UTF8 = "UTF-8";

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String IMAGE = "image";

    public static final String DISPLAY_ORDER = "displayOrder";

    public static final String TAGS = "tags";

    public static final String CATEGORY = "category";

    public static final String EXTENSION_TYPES_CONFIG = "ExtensionManagementService.ExtensionTypes";

    public static final String CUSTOM_ATTRIBUTES = "customAttributes";

    public static final String KEY = "key";

    public static final String VALUE = "value";
}
