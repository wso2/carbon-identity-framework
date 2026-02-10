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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;

import java.util.Map;

import static org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_RESOURCE_TYPE;
import static org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil.handleServerException;

/**
 * Configuration provider that fetches compatibility settings from the configuration store.
 * <p>
 * Configuration Structure:
 * - Resource Type: compatibility-settings (top-level container)
 * - Resource: Setting group (e.g., "scim2", "oauth")
 * - Attribute: Key-value pairs for specific settings
 * <p>
 * Example:
 * Resource Type: compatibility-settings
 * Resource: scim2
 * Attribute: conflictOnClaimUniquenessViolation = true
 */
public class ConfigStoreBasedConfigurationProvider implements CompatibilitySettingConfigurationProvider {

    private static final int PRIORITY = 100;
    private static final String PROVIDER_NAME = "ConfigStoreBasedConfigurationProvider";
    private static final Log log = LogFactory.getLog(ConfigStoreBasedConfigurationProvider.class);

    @Override
    public String getName() {

        return PROVIDER_NAME;
    }

    @Override
    public int getPriority() {

        return PRIORITY;
    }

    @Override
    public CompatibilitySetting getConfigurations(String tenantDomain) throws CompatibilitySettingException {

        CompatibilitySetting settings = new CompatibilitySetting();
        ConfigurationManager configurationManager = getConfigurationManager();
        if (configurationManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("ConfigurationManager is not available. Returning empty settings.");
            }
            return settings;
        }

        try {
            Resources resources = configurationManager.getResourcesByType(COMPATIBILITY_SETTINGS_RESOURCE_TYPE);

            if (resources.getResources() == null || resources.getResources().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No compatibility settings found in configuration store for tenant: " + tenantDomain);
                }
                return settings;
            }

            for (Resource resource : resources.getResources()) {
                String settingGroup = removeResourcePrefix(resource.getResourceName());
                CompatibilitySettingGroup setting = IdentityCompatibilitySettingsUtil
                        .buildCompatibilitySettingGroupFromResource(resource, settingGroup);
                if (setting != null && !setting.getSettings().isEmpty()) {
                    settings.addCompatibilitySetting(setting);
                }
            }
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages
                    .ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                    ConfigurationConstants.ErrorMessages
                            .ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("No compatibility settings found in configuration store for tenant: " + tenantDomain);
                }
                return null;
            }
            throw handleServerException(IdentityCompatibilitySettingsConstants
                            .ErrorMessages.ERROR_CODE_ERROR_READING_COMPATIBILITY_SETTINGS_CONFIG, e, tenantDomain);
        }
        return settings;
    }

    @Override
    public CompatibilitySettingGroup getConfigurationsByGroup(String settingGroup, String tenantDomain)
            throws CompatibilitySettingException {

        return getConfigurationsByGroup(settingGroup, tenantDomain, true);
    }

    @Override
    public String getConfigurationsByGroupAndSetting(String settingGroup, String setting, String tenantDomain)
            throws CompatibilitySettingException {

        if (StringUtils.isBlank(settingGroup) || StringUtils.isBlank(setting)) {
            if (log.isDebugEnabled()) {
                log.debug("Setting group or setting name is empty. Cannot fetch configurations.");
            }
            return null;
        }

        CompatibilitySettingGroup retrievedSettingGroup  = getConfigurationsByGroup(settingGroup, tenantDomain);
        if (retrievedSettingGroup == null) {
            if (log.isDebugEnabled()) {
                log.debug("No compatibility settings found for group: " + settingGroup + ", in tenant: " +
                        tenantDomain);
            }
            return null;
        }
        if (!retrievedSettingGroup.getSettings().containsKey(setting)) {
            if (log.isDebugEnabled()) {
                log.debug("No compatibility settings found for setting: " +
                        setting + ", for group: " + settingGroup + ", tenant: " + tenantDomain);
            }
            return null;
        }
        return retrievedSettingGroup.getSettings().get(setting);
    }

    @Override
    public CompatibilitySettingGroup updateConfigurationGroup(String settingGroup,
                                                              CompatibilitySettingGroup compatibilitySettingGroup,
                                                              String tenantDomain)
            throws CompatibilitySettingException {

        if (StringUtils.isBlank(settingGroup) || compatibilitySettingGroup == null) {
            if (log.isDebugEnabled()) {
                log.debug("Configuration update failed, Setting group or compatibility setting is empty.");
            }
            return null;
        }

        ConfigurationManager configurationManager = getConfigurationManager();
        if (configurationManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Configuration update failed, configurationManager is not available.");
            }
            return null;
        }

        if (isResourceExists(settingGroup, tenantDomain)) {
            updateResourceAttributes(settingGroup, compatibilitySettingGroup, tenantDomain);
        } else {
            addResourceToConfigStore(settingGroup, compatibilitySettingGroup, tenantDomain);
        }

        return compatibilitySettingGroup;
    }

    @Override
    public CompatibilitySetting updateConfiguration(CompatibilitySetting compatibilitySetting,
                                                    String tenantDomain) throws CompatibilitySettingException {

        if (compatibilitySetting == null || compatibilitySetting.getCompatibilitySettings().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Configuration update failed, Compatibility setting is empty.");
            }
            return null;
        }

        for (CompatibilitySettingGroup setting : compatibilitySetting.getCompatibilitySettings().values()) {
            updateConfigurationGroup(setting.getSettingGroup(), setting, tenantDomain);
        }

        return compatibilitySetting;
    }

    /**
     * Get compatibility settings for a specific setting group from the configuration store.
     *
     * @param settingGroup  The name of the setting group.
     * @param tenantDomain  The tenant domain.
     * @param getInherited  Flag to indicate if inherited resources should be fetched.
     * @return CompatibilitySettingGroup for the specified setting group, or null if not found.
     * @throws CompatibilitySettingException If an error occurs during retrieval.
     */
    private CompatibilitySettingGroup getConfigurationsByGroup(String settingGroup, String tenantDomain,
                                                               boolean getInherited)
            throws CompatibilitySettingException {

        if (StringUtils.isBlank(settingGroup)) {
            if (log.isDebugEnabled()) {
                log.debug("Empty setting group.");
            }
            return null;
        }

        try {
            ConfigurationManager configurationManager = getConfigurationManager();
            if (configurationManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("ConfigurationManager not available.");
                }
                return null;
            }

            // Inherited resources are fetched to ensure the root organization's behaviour is preserved in
            // sub organizations unless explicitly overridden.
            Resource resource = configurationManager.getResource(COMPATIBILITY_SETTINGS_RESOURCE_TYPE,
                    formatResourceName(settingGroup), getInherited);

            return IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(resource, settingGroup);

        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages
                    .ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                    ConfigurationConstants.ErrorMessages
                            .ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("No compatibility settings found for group: " + settingGroup + ", in tenant: " +
                            tenantDomain);
                }
                return null;
            }
            throw handleServerException(IdentityCompatibilitySettingsConstants
                    .ErrorMessages.ERROR_CODE_ERROR_READING_COMPATIBILITY_SETTINGS_CONFIG, e, tenantDomain);
        }
    }

    /**
     * Add a new resource to the configuration store.
     *
     * @param settingGroup               The setting group name.
     * @param compatibilitySettingGroup  The compatibility setting group.
     * @param tenantDomain               The tenant domain.
     * @throws CompatibilitySettingException If an error occurs during resource addition.
     */
    private void addResourceToConfigStore(String settingGroup, CompatibilitySettingGroup compatibilitySettingGroup,
                                          String tenantDomain) throws CompatibilitySettingException {

        ConfigurationManager configurationManager = getConfigurationManager();
        Resource newResource = IdentityCompatibilitySettingsUtil.buildResourceFromCompatibilitySettingGroup(
                COMPATIBILITY_SETTINGS_RESOURCE_TYPE, formatResourceName(settingGroup), compatibilitySettingGroup);
        if (newResource.getAttributes().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No attributes to add for setting group: " +
                        settingGroup + ", skipping resource addition.");
            }
            return;
        }
        try {
            if (configurationManager != null) {
                configurationManager.addResource(COMPATIBILITY_SETTINGS_RESOURCE_TYPE, newResource);
            }
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants
                    .ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                createCompatibilityResourceType(tenantDomain);
                addResourceToConfigStore(settingGroup, compatibilitySettingGroup, tenantDomain);
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Error while adding new resource for group: " + settingGroup, e);
            }
            throw handleServerException(IdentityCompatibilitySettingsConstants
                    .ErrorMessages.ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS, e, tenantDomain);
        }
    }

    /**
     * Update attributes of an existing resource in the configuration store.
     *
     * @param settingGroup               The setting group name.
     * @param compatibilitySettingGroup  The compatibility setting group.
     * @param tenantDomain               The tenant domain.
     * @throws CompatibilitySettingException If an error occurs during attribute update.
     */
    private void updateResourceAttributes(String settingGroup, CompatibilitySettingGroup compatibilitySettingGroup,
                                          String tenantDomain) throws CompatibilitySettingException {

        ConfigurationManager configurationManager = getConfigurationManager();
        try {
            for (Map.Entry<String, String> entry : compatibilitySettingGroup.getSettings().entrySet()) {
                Attribute attribute = new Attribute(entry.getKey(), entry.getValue());
                configurationManager.replaceAttribute(COMPATIBILITY_SETTINGS_RESOURCE_TYPE,
                        formatResourceName(settingGroup), attribute);
            }
        } catch (ConfigurationManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while updating resource attributes for group: " + settingGroup, e);
            }
            throw handleServerException(IdentityCompatibilitySettingsConstants
                    .ErrorMessages.ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS, e, tenantDomain);
        }
    }

    /**
     * Get the ConfigurationManager instance from the data holder.
     *
     * @return ConfigurationManager instance, or null if not available.
     */
    private ConfigurationManager getConfigurationManager() {

        return IdentityCompatibilitySettingsDataHolder.getInstance().getConfigurationManager();
    }

    /**
     * Creates the Resource Type for Compatibility Settings if it does not already exist.
     *
     * @param tenantDomain Tenant domain.
     * @throws CompatibilitySettingException If an error occurs while creating the resource type.
     */
    private void createCompatibilityResourceType(String tenantDomain) throws CompatibilitySettingException {

        try {
            ResourceTypeAdd resourceType = new ResourceTypeAdd();
            resourceType.setName(COMPATIBILITY_SETTINGS_RESOURCE_TYPE);
            resourceType.setDescription("Resource type for " + COMPATIBILITY_SETTINGS_RESOURCE_TYPE);
            getConfigurationManager().addResourceType(resourceType);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(
                    IdentityCompatibilitySettingsConstants
                        .ErrorMessages.ERROR_CODE_ERROR_CREATING_RESOURCE_TYPE,
                    e, COMPATIBILITY_SETTINGS_RESOURCE_TYPE, tenantDomain
            );
        }
    }

    /**
     * Check if a resource exists in the configuration store.
     *
     * @param settingGroup Setting group name.
     * @param tenantDomain Tenant domain.
     * @return True if the resource exists, false otherwise.
     * @throws CompatibilitySettingException If an error occurs during the check.
     */
    private boolean isResourceExists(String settingGroup, String tenantDomain)
            throws CompatibilitySettingException {

        // Fetch without inheritance to check existence in the specific tenant.
        CompatibilitySettingGroup resource = getConfigurationsByGroup(settingGroup, tenantDomain, false);
        return resource != null;
    }

    /**
     * Format the resource name by adding the predefined prefix.
     *
     * @param resourceName Original resource name.
     * @return Formatted resource name.
     */
    private String formatResourceName(String resourceName) {

        return IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_RESOURCE_PREFIX + resourceName;
    }

    /**
     * Remove the predefined prefix from the resource name.
     *
     * @param resourceName Resource name with prefix.
     * @return Resource name without prefix.
     */
    private String removeResourcePrefix(String resourceName) {

        return StringUtils.removeStart(resourceName,
                IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_RESOURCE_PREFIX);
    }
}
