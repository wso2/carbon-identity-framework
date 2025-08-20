/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.util;

import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_DIRECTORY_NOT_FOUND;

/**
 * Utility class for webhook metadata operations.
 */
public class WebhookMetadataUtil {

    private static final String EVENT_PROFILES_DIR = "eventprofiles";
    private static final String EVENT_PROFILES_PATH = "repository/resources/identity/" + EVENT_PROFILES_DIR;

    private static Path eventProfilesDirectory = null;

    private WebhookMetadataUtil() {

    }

    /**
     * Get the directory path for event profiles.
     *
     * @return Path to event profiles directory
     */
    public static Path getEventProfilesDirectory() throws WebhookMetadataException {

        if (eventProfilesDirectory != null) {
            return eventProfilesDirectory;
        }

        synchronized (WebhookMetadataUtil.class) {
            if (eventProfilesDirectory != null) {
                return eventProfilesDirectory;
            }

            Path eventProfilesPath =
                    Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), EVENT_PROFILES_PATH);

            File directory = eventProfilesPath.toFile();
            if (!directory.exists()) {
                throw WebhookMetadataExceptionHandler.handleServerException(ERROR_CODE_DIRECTORY_NOT_FOUND,
                        eventProfilesPath.toString());
            }

            eventProfilesDirectory = eventProfilesPath;
            return eventProfilesPath;
        }
    }
}
