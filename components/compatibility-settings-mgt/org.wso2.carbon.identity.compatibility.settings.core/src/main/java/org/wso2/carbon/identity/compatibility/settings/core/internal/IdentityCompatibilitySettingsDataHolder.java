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

package org.wso2.carbon.identity.compatibility.settings.core.internal;

import org.wso2.carbon.identity.compatibility.settings.core.CompatibilitySettingsManager;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.CompatibilitySettingsEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingConfigurationProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingMetaDataProvider;
import org.wso2.carbon.identity.compatibility.settings.core.service.CompatibilitySettingsService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Data holder for identity compatibility settings component.
 */
public final class IdentityCompatibilitySettingsDataHolder {

    private static final IdentityCompatibilitySettingsDataHolder INSTANCE =
            new IdentityCompatibilitySettingsDataHolder();
    private final CompatibilitySettingsService service = new CompatibilitySettingsService();
    private final List<CompatibilitySettingsEvaluator> evaluators = new ArrayList<>();
    private final List<CompatibilitySettingConfigurationProvider> configurationProviders = new ArrayList<>();
    private final List<CompatibilitySettingMetaDataProvider> metaDataProviders = new ArrayList<>();
    private CompatibilitySettingsManager compatibilitySettingsManager;
    private OrganizationManager organizationManager;
    private ConfigurationManager configurationManager;

    private IdentityCompatibilitySettingsDataHolder() {
    }

    /**
     * Get the data holder instance.
     *
     * @return IdentityCompatibilitySettingsDataHolder instance.
     */
    public static IdentityCompatibilitySettingsDataHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the compatibility settings service instance.
     *
     * @return Compatibility settings service instance.
     */
    public CompatibilitySettingsService getService() {


        return service;
    }

    /**
     * Get the CompatibilitySettingsManager instance.
     *
     * @return CompatibilitySettingsManager instance.
     */
    public CompatibilitySettingsManager getCompatibilitySettingsManager() {

        return compatibilitySettingsManager;
    }

    /**
     * Set the CompatibilitySettingsManager.
     *
     * @param compatibilitySettingsManager CompatibilitySettingsManager instance.
     */
    public void setCompatibilitySettingsManager(CompatibilitySettingsManager compatibilitySettingsManager) {

        this.compatibilitySettingsManager = compatibilitySettingsManager;
    }

    /**
     * Set the OrganizationManager.
     *
     * @param organizationManager OrganizationManager instance.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Get the OrganizationManager.
     *
     * @return OrganizationManager instance.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Get the ConfigurationManager.
     *
     * @return ConfigurationManager instance.
     */
    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager ConfigurationManager instance.
     */
    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    /**
     * Get the list of compatibility settings evaluators.
     *
     * @return List of CompatibilitySettingsEvaluator instances.
     */
    public List<CompatibilitySettingsEvaluator> getCompatibilitySettingsEvaluators() {

        return evaluators;
    }

    /**
     * Add a compatibility settings evaluator.
     *
     * @param evaluator CompatibilitySettingsEvaluator instance to be added.
     */
    public void addCompatibilitySettingsEvaluator(CompatibilitySettingsEvaluator evaluator) {

        evaluators.add(evaluator);
        evaluators.sort(Comparator.comparingInt(CompatibilitySettingsEvaluator::getExecutionOrderId));
    }

    /**
     * Remove a compatibility settings evaluator.
     *
     * @param evaluator CompatibilitySettingsEvaluator instance to be removed.
     */
    public void removeCompatibilitySettingsEvaluator(CompatibilitySettingsEvaluator evaluator) {

        evaluators.remove(evaluator);
    }

    /**
     * Get the list of compatibility setting configuration providers.
     *
     * @return List of CompatibilitySettingConfigurationProvider instances.
     */
    public List<CompatibilitySettingConfigurationProvider> getConfigurationProviders() {

        return configurationProviders;
    }

    /**
     * Add a compatibility setting configuration provider.
     *
     * @param provider CompatibilitySettingConfigurationProvider instance to be added.
     */
    public void addConfigurationProvider(CompatibilitySettingConfigurationProvider provider) {

        configurationProviders.add(provider);
        configurationProviders.sort(
                Comparator.comparingInt(CompatibilitySettingConfigurationProvider::getPriority).reversed()
        );
    }

    /**
     * Remove a compatibility setting configuration provider.
     *
     * @param provider CompatibilitySettingConfigurationProvider instance to be removed.
     */
    public void removeConfigurationProvider(CompatibilitySettingConfigurationProvider provider) {

        configurationProviders.remove(provider);
    }

    /**
     * Get the list of compatibility setting metadata providers.
     *
     * @return List of CompatibilitySettingMetaDataProvider instances.
     */
    public List<CompatibilitySettingMetaDataProvider> getMetaDataProviders() {

        return metaDataProviders;
    }

    /**
     * Add a compatibility setting metadata provider.
     *
     * @param provider CompatibilitySettingMetaDataProvider instance to be added.
     */
    public void addMetaDataProvider(CompatibilitySettingMetaDataProvider provider) {

        metaDataProviders.add(provider);
        metaDataProviders.sort(
                Comparator.comparingInt(CompatibilitySettingMetaDataProvider::getPriority).reversed()
        );
    }

    /**
     * Remove a compatibility setting metadata provider.
     *
     * @param provider CompatibilitySettingMetaDataProvider instance to be removed.
     */
    public void removeMetaDataProvider(CompatibilitySettingMetaDataProvider provider) {

        metaDataProviders.remove(provider);
    }
}
