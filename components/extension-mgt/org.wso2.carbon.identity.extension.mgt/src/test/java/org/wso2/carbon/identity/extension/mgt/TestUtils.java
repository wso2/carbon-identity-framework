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

package org.wso2.carbon.identity.extension.mgt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants;

/**
 * Utility functions for testing components of the extension management service.
 */
public class TestUtils {

    private final static String EXTENSION_RESOURCE_PATH = "extensions/";
    private final static String PATH_SEPARATOR = "/";

    /**
     * Stop initializing the utility class.
     */
    private TestUtils() {}

    /**
     * Read the extension resource info.json.
     *
     * @param type Extension resource type.
     * @param id   Extension resource identifier.
     * @return content of the info file.
     * @throws IOException Exception when file reading fails.
     */
    public static String readExtensionResourceInfo(String type, String id) throws IOException {

        return readResource(EXTENSION_RESOURCE_PATH + type + PATH_SEPARATOR + id + PATH_SEPARATOR +
                ExtensionMgtConstants.INFO_FILE_NAME);
    }

    /**
     * Read the extension resource template.json.
     *
     * @param type Extension resource type.
     * @param id   Extension resource identifier.
     * @return content of the template file.
     * @throws IOException Exception when file reading fails.
     */
    public static String readExtensionResourceTemplate(String type, String id) throws IOException {

        return readResource(EXTENSION_RESOURCE_PATH + type + PATH_SEPARATOR + id + PATH_SEPARATOR +
                ExtensionMgtConstants.TEMPLATE_FILE_NAME);
    }

    /**
     * Read a resource in current class path.
     *
     * @param path File path to be read.
     * @return Content of the file as a String.
     * @throws IOException Exception when file reading fails.
     */
    private static String readResource(String path) throws IOException {

        try (InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }

            return resourceFile.toString();
        }
    }
}
