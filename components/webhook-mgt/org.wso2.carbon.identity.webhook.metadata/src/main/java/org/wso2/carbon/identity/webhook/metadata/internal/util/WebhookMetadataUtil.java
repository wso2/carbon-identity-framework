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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for webhook metadata operations.
 */
public class WebhookMetadataUtil {

    private static final Log log = LogFactory.getLog(WebhookMetadataUtil.class);
    private static final String EVENT_PROFILES_DIR = "eventprofiles";
    private static final String EVENT_PROFILES_PATH = "repository/conf/" + EVENT_PROFILES_DIR;

    private static Path eventProfilesDirectory = null;

    /**
     * Get the directory path for event profiles.
     *
     * @return Path to event profiles directory
     */
    public static Path getEventProfilesDirectory() {

        if (eventProfilesDirectory != null) {
            return eventProfilesDirectory;
        }

        synchronized (WebhookMetadataUtil.class) {
            if (eventProfilesDirectory != null) {
                return eventProfilesDirectory;
            }

            String carbonHome = System.getProperty("carbon.home");
            if (carbonHome == null) {
                throw new IllegalStateException("carbon.home system property is not set");
            }

            Path eventProfilesPath = Paths.get(carbonHome, EVENT_PROFILES_PATH);

            File directory = eventProfilesPath.toFile();
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    log.debug("Created directory for event profiles: " + eventProfilesPath);
                } else {
                    String errorMsg = "Failed to create directory for event profiles: " + eventProfilesPath;
                    log.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                }
            }

            eventProfilesDirectory = eventProfilesPath;
            return eventProfilesPath;
        }
    }
}
