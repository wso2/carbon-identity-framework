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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.compatibility.settings.core.CompatibilitySettingsManager;
import org.wso2.carbon.identity.compatibility.settings.core.CompatibilitySettingsManagerImpl;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.CompatibilitySettingsEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.ConfigBasedEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.evaluator.OrganizationalCreationTimeBasedEvaluator;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingConfigurationProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.CompatibilitySettingMetaDataProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.ConfigStoreBasedConfigurationProvider;
import org.wso2.carbon.identity.compatibility.settings.core.provider.FileBasedStaticMetaDataProvider;
import org.wso2.carbon.identity.compatibility.settings.core.service.CompatibilitySettingsService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

/**
 * OSGi component to initialize and expose the compatibility settings service.
 */
@Component(
        name = "org.wso2.carbon.identity.compatibility.settings.component",
        immediate = true
)
public class IdentityCompatibilitySettingsServiceComponent {

    private static final Log LOG = LogFactory.getLog(IdentityCompatibilitySettingsServiceComponent.class);

    /**
     * Activate the identity compatibility settings component.
     *
     * @param context OSGi component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(
                CompatibilitySettingsService.class.getName(), new CompatibilitySettingsService(), null);

        registerDefaultEvaluators();
        registerMetaDataProviders();
        registerConfigurationProviders();

        CompatibilitySettingsManagerImpl manager = new CompatibilitySettingsManagerImpl();
        IdentityCompatibilitySettingsDataHolder.getInstance().setCompatibilitySettingsManager(manager);

        context.getBundleContext().registerService(
                CompatibilitySettingsManager.class.getName(), manager, null);
    }

    /**
     * Deactivate the identity compatibility settings component.
     *
     * @param context OSGi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Identity compatibility settings core bundle deactivated.");
        }
    }

    @Reference(
            name = "organization.management.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        IdentityCompatibilitySettingsDataHolder.getInstance().setOrganizationManager(organizationManager);
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        IdentityCompatibilitySettingsDataHolder.getInstance().setOrganizationManager(null);
    }

    @Reference(
            name = "configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager")
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        IdentityCompatibilitySettingsDataHolder.getInstance().setConfigurationManager(configurationManager);
    }

    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        IdentityCompatibilitySettingsDataHolder.getInstance().setConfigurationManager(null);
    }

    @Reference(
            name = "compatibility.settings.evaluator",
            service = CompatibilitySettingsEvaluator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCompatibilitySettingsEvaluator")
    protected void setCompatibilitySettingsEvaluator(CompatibilitySettingsEvaluator evaluator) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility settings evaluator: " + evaluator.getName() + " is registered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().addCompatibilitySettingsEvaluator(evaluator);
    }

    protected void unsetCompatibilitySettingsEvaluator(CompatibilitySettingsEvaluator evaluator) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility settings evaluator: " + evaluator.getName() + " is unregistered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().removeCompatibilitySettingsEvaluator(evaluator);
    }

    @Reference(
            name = "compatibility.setting.configuration.provider",
            service = CompatibilitySettingConfigurationProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationProvider")
    protected void setConfigurationProvider(CompatibilitySettingConfigurationProvider provider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility setting configuration provider with priority: " + provider.getPriority() +
                    " is registered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().addConfigurationProvider(provider);
    }

    protected void unsetConfigurationProvider(CompatibilitySettingConfigurationProvider provider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility setting configuration provider with priority: " + provider.getPriority() +
                    " is unregistered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().removeConfigurationProvider(provider);
    }

    @Reference(
            name = "compatibility.setting.metadata.provider",
            service = CompatibilitySettingMetaDataProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetaDataProvider")
    protected void setMetaDataProvider(CompatibilitySettingMetaDataProvider provider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility setting metadata provider with priority: " + provider.getPriority() +
                    " is registered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().addMetaDataProvider(provider);
    }

    protected void unsetMetaDataProvider(CompatibilitySettingMetaDataProvider provider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Compatibility setting metadata provider with priority: " + provider.getPriority() +
                    " is unregistered.");
        }
        IdentityCompatibilitySettingsDataHolder.getInstance().removeMetaDataProvider(provider);
    }

    private void registerDefaultEvaluators() {

        ConfigBasedEvaluator configBasedEvaluator = new ConfigBasedEvaluator();
        OrganizationalCreationTimeBasedEvaluator orgCreationTimeBasedEvaluator =
                new OrganizationalCreationTimeBasedEvaluator();

        IdentityCompatibilitySettingsDataHolder.getInstance().addCompatibilitySettingsEvaluator(configBasedEvaluator);
        IdentityCompatibilitySettingsDataHolder.getInstance()
                .addCompatibilitySettingsEvaluator(orgCreationTimeBasedEvaluator);
    }

    private void registerMetaDataProviders() {


        try {
            CompatibilitySettingMetaDataProvider metaDataProvider = getMetaDataProvider();
            IdentityCompatibilitySettingsDataHolder.getInstance()
                    .addMetaDataProvider(metaDataProvider);
        } catch (CompatibilitySettingServerException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error loading compatibility setting metadata from file-based provider. " +
                        "Ignoring file-based metadata provider.", e);
            }
        }
    }

    private static CompatibilitySettingMetaDataProvider getMetaDataProvider()
            throws CompatibilitySettingServerException {

        return new FileBasedStaticMetaDataProvider(
                IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_FILE_NAME,
                IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_FILE_PATH,
                IdentityCompatibilitySettingsConstants.COMPATIBILITY_SETTINGS_FILE_PATH_SEPARATOR);
    }

    private void registerConfigurationProviders() {

        CompatibilitySettingConfigurationProvider configStoreBasedProvider =
                new ConfigStoreBasedConfigurationProvider();
        IdentityCompatibilitySettingsDataHolder.getInstance()
                .addConfigurationProvider(configStoreBasedProvider);
    }
}
