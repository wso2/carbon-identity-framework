/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Loader for device-fields.json.
 * Must be initialised once at bundle activation via {@link #load()} before any call to
 * {@link #getInstance()}. If the config file is absent or unparseable, {@link #load()} throws
 * {@link PolicyManagementServerException}, causing activation to fail loudly.
 */
public class DeviceFieldConfigLoader {

    private static final Log LOG = LogFactory.getLog(DeviceFieldConfigLoader.class);
    private static final String CONFIG_PATH = "repository" + File.separator + "resources"
            + File.separator + "identity" + File.separator + "device-policy"
            + File.separator + "device-fields.json";

    private static volatile DeviceFieldConfigLoader instance;

    private final List<DeviceFieldConfig> fields;

    private DeviceFieldConfigLoader(List<DeviceFieldConfig> fields) {

        this.fields = fields;
    }

    /**
     * Loads device-fields.json from the IS config directory and initialises the singleton.
     * Must be called once from the OSGi component {@code @Activate} method.
     *
     * @throws PolicyManagementServerException If the file is absent or cannot be parsed.
     */
    public static void load() throws PolicyManagementServerException {

        String filePath = CarbonUtils.getCarbonHome() + File.separator + CONFIG_PATH;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_FIELD_CONFIG_NOT_FOUND, file.getAbsolutePath());
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);
            List<DeviceFieldConfig> loaded =
                    mapper.convertValue(root.get("fields"), new TypeReference<List<DeviceFieldConfig>>() { });
            instance = new DeviceFieldConfigLoader(loaded);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded " + loaded.size() + " device field configs from: " + filePath);
            }
        } catch (IOException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_FIELD_CONFIG_PARSE_FAILED, e, filePath);
        }
    }

    /**
     * Returns the singleton instance initialised by {@link #load()}.
     *
     * @return The loaded {@link DeviceFieldConfigLoader}.
     */
    public static DeviceFieldConfigLoader getInstance() {

        return instance;
    }

    /**
     * Returns an unmodifiable view of the loaded device field configurations.
     *
     * @return List of {@link DeviceFieldConfig}.
     */
    public List<DeviceFieldConfig> getFields() {

        return Collections.unmodifiableList(fields);
    }
}
