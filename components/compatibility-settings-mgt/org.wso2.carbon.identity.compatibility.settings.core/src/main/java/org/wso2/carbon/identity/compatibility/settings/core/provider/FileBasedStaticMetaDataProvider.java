/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.provider;

import com.google.gson.JsonParseException;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeParseException;

import static org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil.handleServerException;

/**
 * File based static implementation of CompatibilitySettingMetaDataProvider.
 * Reads compatibility setting metadata from a JSON file located at the specified path.
 */
public class FileBasedStaticMetaDataProvider implements CompatibilitySettingMetaDataProvider {

    private static final int PRIORITY = 10;
    private static final String PROVIDER_NAME = "FileBasedStaticMetaDataProvider";
    private static final String DEFAULT_COMPATIBILITY_SETTING_FILE_NAME = "compatibility-settings-metadata.json";
    private static final String[] DEFAULT_COMPATIBILITY_SETTING_FILE_PATH = { "repository", "conf" };

    private CompatibilitySettingMetaData metaData;
    private final String fileName;
    private final String[] filePath;

    /**
     * Default constructor that loads metadata from the default file location.
     *
     * @throws CompatibilitySettingServerException If an error occurs while loading metadata.
     */
    public FileBasedStaticMetaDataProvider() throws CompatibilitySettingServerException {

        this.fileName = DEFAULT_COMPATIBILITY_SETTING_FILE_NAME;
        this.filePath = DEFAULT_COMPATIBILITY_SETTING_FILE_PATH;
        loadMetaData();
    }

    /**
     * Constructor with custom file name and path.
     *
     * @param fileName The name of the metadata file.
     * @param filePath Array of path segments to the file location.
     * @throws CompatibilitySettingServerException If an error occurs while loading metadata.
     */
    public FileBasedStaticMetaDataProvider(String fileName, String[] filePath)
            throws CompatibilitySettingServerException {

        this.fileName = fileName;
        this.filePath = filePath;
        loadMetaData();
    }

    /**
     * Constructor with custom file name and path string with separator.
     *
     * @param fileName  The name of the metadata file.
     * @param filePath  The path to the file location as a string.
     * @param separator The separator to split the path string.
     * @throws CompatibilitySettingServerException If an error occurs while loading metadata.
     */
    public FileBasedStaticMetaDataProvider(String fileName, String filePath, String separator)
            throws CompatibilitySettingServerException {

        this.fileName = fileName;
        this.filePath = filePath.split(separator);
        loadMetaData();
    }

    @Override
    public String getName() {

        return PROVIDER_NAME;
    }

    @Override
    public int getPriority() {

        return PRIORITY;
    }

    @Override
    public CompatibilitySettingMetaData getMetaData() throws CompatibilitySettingException {

        return this.metaData;
    }

    @Override
    public CompatibilitySettingMetaDataGroup getMetaDataByGroup(String settingGroup)
            throws CompatibilitySettingException {

        return this.metaData.getSettingMetaDataGroup(settingGroup);
    }

    @Override
    public CompatibilitySettingMetaDataEntry getMetaDataByGroupAndSetting(String settingGroup, String setting)
            throws CompatibilitySettingException {

        return this.metaData.getSettingMetaDataEntry(settingGroup, setting);
    }

    /**
     * Get the file path of the compatibility settings metadata JSON file.
     *
     * @return The full file path as a string.
     */
    public String getFilePath() {

        StringBuilder filePath = new StringBuilder();
        for (String pathSegment : this.filePath) {
            filePath.append(pathSegment).append(File.separator);
        }
        filePath.append(this.fileName);
        return filePath.toString();
    }

    private void loadMetaData() throws CompatibilitySettingServerException {

        try {
            this.metaData = IdentityCompatibilitySettingsUtil.parseCompatibilitySettingsFromJSONFile(getFilePath());
        } catch (IOException e) {
            throw handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages.ERROR_CODE_FILE_NOT_FOUND, e, getFilePath()
            );
        } catch (JsonParseException e) {
            throw handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages.ERROR_CODE_ERROR_PARSING_JSON, e,
                    getFilePath()
            );
        } catch (DateTimeParseException e) {
            throw handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages.ERROR_CODE_ERROR_PARSING_DATE_TIME, e,
                    getFilePath()
            );
        }
    }
}
