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

package org.wso2.carbon.identity.compatibility.settings.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.CompatibilitySettingsEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingClientException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingConfigurationProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingMetaDataProvider;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link CompatibilitySettingsManager}.
 * This class coordinates evaluators and providers to determine compatibility settings.
 */
public class CompatibilitySettingsManagerImpl implements CompatibilitySettingsManager {

    private static final Log LOG = LogFactory.getLog(CompatibilitySettingsManagerImpl.class);

    private final CompatibilitySettingMetaData metaData;
    private final Map<String, String[]> supportedSettings;

    /**
     * Constructor that initializes metadata from providers.
     * Metadata is static hence loaded once at construction time.
     */
    public CompatibilitySettingsManagerImpl() {

        this.metaData = loadMetaData();
        this.supportedSettings = buildSupportedSettings(this.metaData);
    }

    /**
     * Get the list of evaluators from data holder.
     *
     * @return List of evaluators sorted by execution order.
     */
    private List<CompatibilitySettingsEvaluator> getEvaluators() {

        return IdentityCompatibilitySettingsDataHolder.getInstance().getCompatibilitySettingsEvaluators();
    }

    /**
     * Get the list of metadata providers from data holder.
     *
     * @return List of metadata providers.
     */
    private List<CompatibilitySettingMetaDataProvider> getMetaDataProviders() {

        return IdentityCompatibilitySettingsDataHolder.getInstance().getMetaDataProviders();
    }

    /**
     * Get the list of configuration providers from data holder.
     *
     * @return List of configuration providers.
     */
    private List<CompatibilitySettingConfigurationProvider> getConfigurationProviders() {

        return IdentityCompatibilitySettingsDataHolder.getInstance().getConfigurationProviders();
    }

    /**
     * Get the cached metadata.
     *
     * @return Compatibility setting metadata.
     */
    public CompatibilitySettingMetaData getMetaData() {

        return metaData;
    }

    /**
     * Load metadata from all providers and merge them.
     *
     * @return Merged compatibility setting metadata.
     */
    private CompatibilitySettingMetaData loadMetaData() {

        List<CompatibilitySettingMetaDataProvider> metaDataProviders = getMetaDataProviders();
        CompatibilitySettingMetaData mergedMetaData = new CompatibilitySettingMetaData();

        for (CompatibilitySettingMetaDataProvider provider : metaDataProviders) {
            try {
                CompatibilitySettingMetaData providerMetaData = provider.getMetaData();
                if (providerMetaData != null) {
                    mergedMetaData.update(providerMetaData);
                }
            } catch (CompatibilitySettingException e) {
                // Log and Continue without interrupting server startup.
                LOG.error("Error loading metadata from provider " + provider.getName(), e);
            }
        }
        return mergedMetaData;
    }

    /**
     * Build supported settings map from metadata.
     *
     * @param metaData The metadata to build supported settings from.
     * @return Map of setting group names to their supported setting names.
     */
    private Map<String, String[]> buildSupportedSettings(CompatibilitySettingMetaData metaData) {

        Map<String, String[]> settings = new java.util.HashMap<>();

        if (metaData != null && metaData.getSettingsMetaData() != null) {
            for (Map.Entry<String, CompatibilitySettingMetaDataGroup> groupEntry :
                    metaData.getSettingsMetaData().entrySet()) {
                String groupName = groupEntry.getKey();
                CompatibilitySettingMetaDataGroup group = groupEntry.getValue();

                if (group.getSettingsMetaData() != null) {
                    String[] settingNames = group.getSettingsMetaData().keySet().toArray(new String[0]);
                    settings.put(groupName, settingNames);
                }
            }
        }
        return settings;
    }

    /**
     * Get the supported settings.
     * Returns a map where keys are setting group names and values are arrays of supported setting names.
     *
     * @return Map of setting group names to their supported setting names.
     */
    public Map<String, String[]> getSupportedSettings() {

        return supportedSettings;
    }


    @Override
    public CompatibilitySetting evaluate(CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting finalEvaluationResult = new CompatibilitySetting();
        for (CompatibilitySettingsEvaluator evaluator : getEvaluators()) {
            if (evaluator.canHandle(context)) {
                CompatibilitySetting evaluationResult = evaluator.evaluate(context);
                finalEvaluationResult.updateCompatibilitySetting(evaluationResult);
            }
        }
        return finalEvaluationResult;
    }

    @Override
    public CompatibilitySetting evaluate(String settingGroup, CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting finalEvaluationResult = new CompatibilitySetting();
        for (CompatibilitySettingsEvaluator evaluator : getEvaluators()) {
            if (evaluator.canHandle(context)) {
                CompatibilitySetting evaluationResult = evaluator.evaluate(settingGroup, context);
                finalEvaluationResult.updateCompatibilitySetting(evaluationResult);
            }
        }
        return finalEvaluationResult;
    }

    @Override
    public CompatibilitySetting evaluate(String settingGroup, String setting,
                                        CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting finalEvaluationResult = new CompatibilitySetting();
        for (CompatibilitySettingsEvaluator evaluator : getEvaluators()) {
            if (evaluator.canHandle(context)) {
                CompatibilitySetting evaluationResult = evaluator.evaluate(settingGroup, setting, context);
                finalEvaluationResult.updateCompatibilitySetting(evaluationResult);
            }
        }
        return finalEvaluationResult;
    }

    @Override
    public CompatibilitySetting getCompatibilitySettings(String tenantDomain)
            throws CompatibilitySettingException {

        CompatibilitySettingContext context = buildContext(tenantDomain);
        return evaluate(context);
    }

    @Override
    public CompatibilitySetting getCompatibilitySettings(String tenantDomain, String settingGroup)
            throws CompatibilitySettingException {

        validateSettingGroupRequest(settingGroup, getSupportedSettings());
        CompatibilitySettingContext context = buildContext(tenantDomain);
        return evaluate(settingGroup, context);
    }

    @Override
    public CompatibilitySetting getCompatibilitySettings(String tenantDomain, String settingGroup, String setting)
            throws CompatibilitySettingException {

        validateSettingRequest(settingGroup, setting, getSupportedSettings());
        CompatibilitySettingContext context = buildContext(tenantDomain);
        return evaluate(settingGroup, setting, context);
    }

    @Override
    public CompatibilitySetting updateCompatibilitySettings(String tenantDomain,
                                                            CompatibilitySetting compatibilitySetting)
            throws CompatibilitySettingException {

        validateSetting(compatibilitySetting, getSupportedSettings());
        List<CompatibilitySettingConfigurationProvider> configurationProviders = getConfigurationProviders();
        for (CompatibilitySettingConfigurationProvider provider : configurationProviders) {
            provider.updateConfiguration(compatibilitySetting, tenantDomain);
        }
        return getCompatibilitySettings(tenantDomain);
    }

    @Override
    public CompatibilitySetting updateCompatibilitySettings(String tenantDomain, String settingGroup,
                                                                 CompatibilitySettingGroup compatibilitySettingGroup)
            throws CompatibilitySettingException {

        validateSettingGroup(settingGroup, compatibilitySettingGroup, getSupportedSettings());
        List<CompatibilitySettingConfigurationProvider> configurationProviders = getConfigurationProviders();
        for (CompatibilitySettingConfigurationProvider provider : configurationProviders) {
            provider.updateConfiguration(settingGroup, compatibilitySettingGroup, tenantDomain);
        }
        return getCompatibilitySettings(tenantDomain, settingGroup);
    }

    /**
     * Build compatibility setting context for the given tenant.
     * This method uses cached metadata (static) and loads configuration from providers.
     * Providers are executed in priority order (highest first) and results are merged.
     *
     * @param tenantDomain Tenant domain.
     * @return Compatibility setting context.
     * @throws CompatibilitySettingException If an error occurs during context building.
     */
    private CompatibilitySettingContext buildContext(String tenantDomain) throws CompatibilitySettingException {

        CompatibilitySettingContext context = CompatibilitySettingContext.create();
        context.setTenantDomain(tenantDomain);
        context.setCompatibilitySettings(new CompatibilitySetting());

        if (metaData != null) {
            context.setCompatibilitySettingMetaData(metaData);
        }

        List<CompatibilitySettingConfigurationProvider> configurationProviders = getConfigurationProviders();
        if (!configurationProviders.isEmpty()) {
            CompatibilitySetting mergedConfiguration = new CompatibilitySetting();

            for (CompatibilitySettingConfigurationProvider provider : configurationProviders) {
                CompatibilitySetting providerConfiguration = provider.getConfigurations(tenantDomain);
                if (providerConfiguration != null) {
                    for (Map.Entry<String, CompatibilitySettingGroup> entry :
                            providerConfiguration.getCompatibilitySettings().entrySet()) {
                        mergedConfiguration.addCompatibilitySetting(entry.getValue());
                    }
                }
            }

            if (!mergedConfiguration.getCompatibilitySettings().isEmpty()) {
                context.setCompatibilitySettings(mergedConfiguration);
            }
        }
        return context;
    }

    /**
     * Validate that the setting group name is supported.
     *
     * @param groupName         The name of the setting group to validate.
     * @param supportedSettings Map of supported setting groups to their supported settings.
     * @throws CompatibilitySettingClientException If the setting group is not supported.
     */
    private void validateSettingGroupRequest(String groupName, Map<String, String[]> supportedSettings)
            throws CompatibilitySettingException {

        if (supportedSettings == null || supportedSettings.isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    ErrorMessages.ERROR_CODE_SUPPORTED_SETTINGS_NOT_CONFIGURED);
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP);
        }
        if (!supportedSettings.containsKey(groupName)) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP, groupName);
        }
    }

    /**
     * Validate that the setting group and setting name are supported.
     *
     * @param groupName         The name of the setting group.
     * @param settingName       The name of the setting to validate.
     * @param supportedSettings Map of supported setting groups to their supported settings.
     * @throws CompatibilitySettingClientException If the setting group or setting is not supported.
     */
    private void validateSettingRequest(String groupName, String settingName, Map<String, String[]> supportedSettings)
            throws CompatibilitySettingException {

        validateSettingGroupRequest(groupName, supportedSettings);

        if (settingName == null || settingName.trim().isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING);
        }

        String[] supportedValues = supportedSettings.get(groupName);
        Set<String> supportedValuesSet = new HashSet<>(Arrays.asList(supportedValues));
        if (!supportedValuesSet.contains(settingName)) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING, settingName, groupName);
        }
    }

    /**
     * Validate that the setting group and its settings are supported.
     *
     * @param groupName         The name of the setting group.
     * @param settingGroup       The setting group to validate.
     * @param supportedSettings  Map of supported setting groups to their supported settings.
     * @throws CompatibilitySettingClientException If the setting group or any setting is not supported.
     */
    private void validateSettingGroup(String groupName, CompatibilitySettingGroup settingGroup, Map<String,
            String[]> supportedSettings) throws CompatibilitySettingException {

        if (supportedSettings == null || supportedSettings.isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_SUPPORTED_SETTINGS_NOT_CONFIGURED);
        }
        if (settingGroup == null || settingGroup.getSettingGroup() == null
                || settingGroup.getSettings().isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP);
        }

        String[] supportedValues = supportedSettings.get(groupName);
        if (supportedValues == null) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP, groupName);
        }
        Set<String> supportedValuesSet = new HashSet<>(Arrays.asList(supportedValues));
        for (String setting : settingGroup.getSettings().keySet()) {
            if (!supportedValuesSet.contains(setting)) {
                throw IdentityCompatibilitySettingsUtil.handleClientException(
                        ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING, setting, groupName);
            }
        }
    }

    /**
     * Validate that the compatibility setting and its groups/settings are supported.
     *
     * @param setting            The compatibility setting to validate.
     * @param supportedSettings  Map of supported setting groups to their supported settings.
     * @throws CompatibilitySettingClientException If any setting group or setting is not supported.
     */
    private void validateSetting(CompatibilitySetting setting, Map<String, String[]> supportedSettings)
            throws CompatibilitySettingException {

        if (supportedSettings == null || supportedSettings.isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    ErrorMessages.ERROR_CODE_SUPPORTED_SETTINGS_NOT_CONFIGURED);
        }
        if (setting == null || setting.getCompatibilitySettings() == null
                || setting.getCompatibilitySettings().isEmpty()) {
            throw IdentityCompatibilitySettingsUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_COMPATIBILITY_SETTING);
        }
        for (CompatibilitySettingGroup settingGroup : setting.getCompatibilitySettings().values()) {
            validateSettingGroup(settingGroup.getSettingGroup(), settingGroup, supportedSettings);
        }
    }
}

