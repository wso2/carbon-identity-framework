/*
 * Copyright (c) 2014-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.AbstractInboundAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.DiscoverableApplicationManager;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtService;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtServiceImpl;
import org.wso2.carbon.identity.application.mgt.inbound.protocol.ApplicationInboundAuthConfigHandler;
import org.wso2.carbon.identity.application.mgt.internal.impl.DiscoverableApplicationManagerImpl;
import org.wso2.carbon.identity.application.mgt.listener.AdminRoleListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationClaimMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationIdentityProviderMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtAuditLogger;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationResourceManagementListener;
import org.wso2.carbon.identity.application.mgt.listener.AuthorizedAPIManagementListener;
import org.wso2.carbon.identity.application.mgt.listener.ConsoleAuthorizedAPIListener;
import org.wso2.carbon.identity.application.mgt.listener.DefaultApplicationResourceMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.DefaultRoleManagementListener;
import org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider;
import org.wso2.carbon.identity.application.mgt.provider.RegistryBasedApplicationPermissionProvider;
import org.wso2.carbon.identity.application.mgt.validator.ApplicationValidator;
import org.wso2.carbon.identity.application.mgt.validator.DefaultApplicationValidator;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataMgtListener;
import org.wso2.carbon.identity.core.SAMLSSOServiceProviderManager;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManagementInitialize;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OSGI Service component for Application (aka Service Provider) management.
 */
@Component(
        name = "identity.application.management.component",
        immediate = true
)
public class ApplicationManagementServiceComponent {
    private static Log log = LogFactory.getLog(ApplicationManagementServiceComponent.class);
    private static BundleContext bundleContext;
    private static Map<String, ServiceProvider> fileBasedSPs = new HashMap<String, ServiceProvider>();

    public static Map<String, ServiceProvider> getFileBasedSPs() {
        return fileBasedSPs;
    }

    @Activate
    protected void activate(ComponentContext context) {
        try {
            buildFileBasedSPList();
            if (log.isDebugEnabled()) {
                log.debug("File based SP building completed");
            }

            loadAuthenticationTemplates();
            if (log.isDebugEnabled()) {
                log.debug("Authentication templates are loaded");
            }

            bundleContext = context.getBundleContext();
            // Registering Application management service as a OSGIService
            bundleContext.registerService(ApplicationManagementService.class.getName(),
                    ApplicationManagementServiceImpl.getInstance(), null);
            bundleContext.registerService(IdentityProviderMgtListener.class.getName(),
                    new ApplicationIdentityProviderMgtListener(), null);
            ApplicationMgtSystemConfig.getInstance();
            bundleContext.registerService(ApplicationMgtListener.class.getName(), new ApplicationMgtAuditLogger(),
                    null);
            bundleContext.registerService(DefaultAuthSeqMgtService.class.getName(),
                    DefaultAuthSeqMgtServiceImpl.getInstance(), null);
            // Register the DefaultApplicationResourceMgtListener.
            context.getBundleContext().registerService(ApplicationResourceManagementListener.class,
                    new DefaultApplicationResourceMgtListener(), null);

            bundleContext.registerService(DiscoverableApplicationManager.class.getName(),
                    new DiscoverableApplicationManagerImpl(), null);

            bundleContext.registerService(ClaimMetadataMgtListener.class.getName(), new ApplicationClaimMgtListener(),
                    null);

            bundleContext.registerService(AuthorizedAPIManagementService.class,
                    new AuthorizedAPIManagementServiceImpl(), null);

            bundleContext.registerService(RoleManagementListener.class, new DefaultRoleManagementListener(), null);
            bundleContext.registerService(ApplicationMgtListener.class, new DefaultRoleManagementListener(), null);

            // Register the ApplicationValidator.
            context.getBundleContext().registerService(ApplicationValidator.class,
                    new DefaultApplicationValidator(), null);
            if (ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider() == null) {
                ApplicationManagementServiceComponentHolder.getInstance()
                        .setApplicationPermissionProvider(new RegistryBasedApplicationPermissionProvider());
            }

            // Register the Admin Role Listener.
            bundleContext.registerService(RoleManagementListener.class, new AdminRoleListener(), null);
            // Register the Authorized API Management Listener.
            bundleContext.registerService(AuthorizedAPIManagementListener.class, new ConsoleAuthorizedAPIListener(),
                    null);

            if (log.isDebugEnabled()) {
                log.debug("Identity ApplicationManagementComponent bundle is activated");
            }
        } catch (Exception e) {
            log.error("Error while activating ApplicationManagementComponent bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity ApplicationManagementComponent bundle is deactivated");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "configuration.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService configContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Configuration Context Service");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setConfigContextService(configContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Configuration Context Service");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setConfigContextService(null);
    }

    @Reference(
            name = "application.mgt.authenticator",
            service = AbstractInboundAuthenticatorConfig.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetInboundAuthenticatorConfig"
    )
    protected void setInboundAuthenticatorConfig(AbstractInboundAuthenticatorConfig authenticator) {
        ApplicationManagementServiceComponentHolder.addInboundAuthenticatorConfig(authenticator);
    }

    protected void unsetInboundAuthenticatorConfig(AbstractInboundAuthenticatorConfig authenticator) {
        ApplicationManagementServiceComponentHolder.removeInboundAuthenticatorConfig(authenticator.getName());
    }

    @Reference(
            name = "consent.mgt.service",
            service = ConsentManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentMgtService"
    )
    protected void setConsentMgtService(ConsentManager consentManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setConsentManager(consentManager);
    }

    protected void unsetConsentMgtService(ConsentManager consentManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setConsentManager(null);
    }

    private void buildFileBasedSPList() {
        String spConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity"
                + File.separator + "service-providers";
        FileInputStream fileInputStream = null;
        File spConfigDir = new File(spConfigDirPath);
        OMElement documentElement;
        File[] fileList;

        if (spConfigDir.exists() && ArrayUtils.isNotEmpty(fileList = spConfigDir.listFiles())) {
            for (final File fileEntry : fileList) {
                try {
                    if (!fileEntry.isDirectory()) {
                        fileInputStream = new FileInputStream(new File(fileEntry.getAbsolutePath()));
                        documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();
                        ServiceProvider sp = ServiceProvider.build(documentElement);
                        if (sp != null) {
                            if (StringUtils.isBlank(sp.getApplicationVersion())) {
                                sp.setApplicationVersion(ApplicationConstants.ApplicationVersion.BASE_APP_VERSION);
                            }
                            fileBasedSPs.put(sp.getApplicationName(), sp);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while loading idp from file system.", e);
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            log.error("Error occurred while closing file input stream for file " + spConfigDirPath, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Load the authentication template files from [IS_HOME]/repository/resources/identity/authntemplates/ . Files
     * need to have .json as the extension to be read as an template. Will be ignored otherwise.
     */
    private void loadAuthenticationTemplates() {

        File templatesDir = new File(ApplicationConstants.TEMPLATES_DIR_PATH);
        if (!templatesDir.exists() || !templatesDir.isDirectory()) {
            log.warn("Templates directory not found at " + templatesDir.getPath());
            ApplicationManagementServiceComponentHolder.getInstance().setAuthenticationTemplatesJson("{}");
            return;
        }
        File categoriesFile = new File(templatesDir, ApplicationConstants.CATEGORIES_METADATA_FILE);
        JSONObject categoriesObj = parseCategoryMetadata(categoriesFile);
        File[] jsonFiles = templatesDir.listFiles((d, name) -> name.endsWith(ApplicationConstants
                .FILE_EXT_JSON) && !ApplicationConstants.CATEGORIES_METADATA_FILE.equals(name));
        if (jsonFiles != null) {
            for (File jsonFile : jsonFiles) {
                if (jsonFile.isFile()) {
                    try {
                        String templateJsonString = FileUtils.readFileToString(jsonFile);
                        JSONObject templateObj = new JSONObject(templateJsonString);
                        if (templateObj.has(ApplicationConstants.RUN_TIME) && isExcludeFromTemplates(templateObj)) {
                            continue;
                        }
                        if (templateObj.has(ApplicationConstants.TEMPLATE_CATEGORY)) {
                            String category = templateObj.getString(ApplicationConstants.TEMPLATE_CATEGORY);
                            if (!categoriesObj.has(category)) {
                                log.warn(String.format("No category defined as %s for template at %s. Proceeding with" +
                                        " uncategorized.", category, jsonFile.getName()));
                                category = ApplicationConstants.UNCATEGORIZED;
                            }
                            JSONObject categoryObj = categoriesObj.getJSONObject(category);
                            if (!categoryObj.has(ApplicationConstants.CATEGORY_TEMPLATES)) {
                                categoryObj.put(ApplicationConstants.CATEGORY_TEMPLATES, Collections.emptyList());
                            }
                            JSONArray categoryTemplateArray = categoryObj.getJSONArray(ApplicationConstants
                                    .CATEGORY_TEMPLATES);
                            categoryTemplateArray.put(templateObj);
                        } else {
                            log.warn(String.format("Script template in file %s is missing category information, or " +
                                    "using an undefined category. Hence it will be ignored.", jsonFile.getName()));
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Authentication template file loaded from: " + jsonFile.getName());
                        }
                    } catch (JSONException e) {
                        log.error("Error when parsing json content from file " + jsonFile.getName(), e);
                    } catch (IOException e) {
                        log.error("Error when reading authentication template file " + jsonFile.getName(), e);
                    }
                }
            }
        } else {
            log.warn("Authentication template files could not be read from " + ApplicationConstants
                    .TEMPLATES_DIR_PATH);
        }
        ApplicationManagementServiceComponentHolder.getInstance().setAuthenticationTemplatesJson(categoriesObj
                .toString());
    }

    private static boolean isExcludeFromTemplates(JSONObject templateObj) {

        String runtime = templateObj.getString(ApplicationConstants.RUN_TIME);

        if (StringUtils.isBlank(runtime) || StringUtils.equalsIgnoreCase(ApplicationConstants.RUN_TIME_ANY, runtime)) {
            return false;
        }
        if (StringUtils.equalsIgnoreCase(ApplicationConstants.RUN_TIME_NEW, runtime)) {
            return CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME;
        }
        if (StringUtils.equalsIgnoreCase(ApplicationConstants.RUN_TIME_LEGACY, runtime)) {
            return !CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME;
        }
        return false;
    }

    private JSONObject parseCategoryMetadata(File categoryMetadataFile) {

        JSONObject categoriesObj = null;
        try {
            String categoryMetadataString = FileUtils.readFileToString(categoryMetadataFile);
            try {
                categoriesObj = new JSONObject(categoryMetadataString);
            } catch (JSONException e) {
                log.error("Invalid syntax for authentication template category metadata file: " +
                        categoryMetadataFile.getName() + " . Hence ignoring and proceeding with defaults.", e);
            }
        } catch (IOException e) {
            log.error("Error when reading authentication template category metadata file: " + categoryMetadataFile
                    .getName(), e);
        }
        if (categoriesObj == null) {
            categoriesObj = new JSONObject();
        }
        JSONObject objForUncategorized = new JSONObject();
        objForUncategorized.put(ApplicationConstants.CATEGORY_DISPLAY_NAME, ApplicationConstants
                .DISPLAY_NAME_FOR_UNCATEGORIZED);
        objForUncategorized.put(ApplicationConstants.CATEGORY_ORDER, ApplicationConstants.ORDER_FOR_UNCATEGORIZED);
        categoriesObj.put(ApplicationConstants.UNCATEGORIZED, objForUncategorized);
        return categoriesObj;
    }

    @Reference(
            name = "claim.meta.mgt.service",
            service = ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetaMgtService"
    )
    protected void setClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        ApplicationManagementServiceComponentHolder.getInstance().setClaimMetadataManagementService(
                claimMetaMgtService);
    }

    protected void unsetClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        ApplicationManagementServiceComponentHolder.getInstance().setClaimMetadataManagementService(null);
    }

    @Reference(
            name = "identity.organization.management.component",
            service = OrganizationUserResidentResolverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationUserResidentResolverService"
    )
    protected void setOrganizationUserResidentResolverService(
            OrganizationUserResidentResolverService organizationUserResidentResolverService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the organization management service.");
        }
        ApplicationManagementServiceComponentHolder.getInstance()
                .setOrganizationUserResidentResolverService(organizationUserResidentResolverService);
    }

    protected void unsetOrganizationUserResidentResolverService(
            OrganizationUserResidentResolverService organizationUserResidentResolverService) {

        if (log.isDebugEnabled()) {
            log.debug("Unset organization management service.");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setOrganizationUserResidentResolverService(null);
    }

    @Reference(
            name = "organization.mgt.initialize.service",
            service = OrganizationManagementInitialize.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManagementEnablingService"
    )
    protected void setOrganizationManagementEnablingService(
            OrganizationManagementInitialize organizationManagementInitializeService) {

        ApplicationManagementServiceComponentHolder.getInstance()
                .setOrganizationManagementEnable(organizationManagementInitializeService);
    }

    protected void unsetOrganizationManagementEnablingService(
            OrganizationManagementInitialize organizationManagementInitializeInstance) {

        ApplicationManagementServiceComponentHolder.getInstance().setOrganizationManagementEnable(null);
    }

    @Reference(
            name = "application.permission.provider",
            service = ApplicationPermissionProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationPermissionProvider"
    )
    protected void setApplicationPermissionProvider(ApplicationPermissionProvider applicationPermissionProvider) {

        ApplicationPermissionProvider existingApplicationPermissionProvider =
                ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider();

        if (existingApplicationPermissionProvider != null) {
            log.warn("Multiple Application Permission Providers are registered. Permission Provider:"
                    + existingApplicationPermissionProvider.getClass().getName() + " will be replaced with "
                    + applicationPermissionProvider.getClass().getName());
        }
        ApplicationManagementServiceComponentHolder.getInstance()
                .setApplicationPermissionProvider(applicationPermissionProvider);
        log.info("Application permission provider got registered: " +
                applicationPermissionProvider.getClass().getName());
    }

    protected void unsetApplicationPermissionProvider(ApplicationPermissionProvider applicationPermissionProvider) {

        ApplicationManagementServiceComponentHolder.getInstance()
                .setApplicationPermissionProvider(new RegistryBasedApplicationPermissionProvider());

        if (log.isDebugEnabled()) {
            log.debug("Removed application permission provider.");
        }
    }

    @Reference(
            name = "saml.sso.service.provider.manager",
            service = org.wso2.carbon.identity.core.SAMLSSOServiceProviderManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSAMLSSOServiceProviderManager")
    protected void setSAMLSSOServiceProviderManager(SAMLSSOServiceProviderManager samlSSOServiceProviderManager) {

        ApplicationManagementServiceComponentHolder.getInstance()
                .setSAMLSSOServiceProviderManager(samlSSOServiceProviderManager);
        if (log.isDebugEnabled()) {
            log.debug("SAMLSSOServiceProviderManager set in to bundle");
        }
    }

    protected void unsetSAMLSSOServiceProviderManager(SAMLSSOServiceProviderManager samlSSOServiceProviderManager) {

        ApplicationManagementServiceComponentHolder.getInstance()
                .setSAMLSSOServiceProviderManager(null);
        if (log.isDebugEnabled()) {
            log.debug("SAMLSSOServiceProviderManager unset in to bundle");
        }
    }

    @Reference(
            name = "application.mgt.inbound.config.service",
            service = ApplicationInboundAuthConfigHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationInboundAuthConfigService"
    )
    protected void setApplicationInboundAuthConfigService(ApplicationInboundAuthConfigHandler
                                                                  applicationInboundAuthConfigHandler) {

        ApplicationManagementServiceComponentHolder.getInstance().addApplicationInboundAuthConfigHandler(
                applicationInboundAuthConfigHandler);
    }

    protected void unsetApplicationInboundAuthConfigService(ApplicationInboundAuthConfigHandler
                                                                    applicationInboundAuthConfigHandler) {

        ApplicationManagementServiceComponentHolder.getInstance().removeApplicationInboundConfigHandler(
                applicationInboundAuthConfigHandler);
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        ApplicationManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        log.debug("IdentityEventService set in Identity Application Management bundle");
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        ApplicationManagementServiceComponentHolder.getInstance().setIdentityEventService(null);
        log.debug("IdentityEventService unset in Identity Application Management bundle");
    }

    @Reference(
            name = "api.resource.mgt.service.component",
            service = APIResourceManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIResourceManager")
    protected void setAPIResourceManager(APIResourceManager apiResourceManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(apiResourceManager);
        log.debug("APIResourceManager set in to bundle");
    }

    protected void unsetAPIResourceManager(APIResourceManager apiResourceManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(null);
        log.debug("APIResourceManager unset in to bundle");
    }

    @Reference(
            name = "org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService",
            service = org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRoleManagementServiceV2")
    protected void setRoleManagementServiceV2(RoleManagementService roleManagementService) {

        ApplicationManagementServiceComponentHolder.getInstance().setRoleManagementServiceV2(roleManagementService);
        log.debug("RoleManagementServiceV2 set in ApplicationManagementServiceComponent bundle.");
    }

    protected void unsetRoleManagementServiceV2(RoleManagementService roleManagementService) {

        ApplicationManagementServiceComponentHolder.getInstance().setRoleManagementServiceV2(null);
        log.debug("RoleManagementServiceV2 unset in ApplicationManagementServiceComponent bundle.");
    }

    @Reference(name = "org.wso2.carbon.identity.organization.management.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        log.debug("OrganizationManager set in ApplicationManagementServiceComponent bundle.");
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setOrganizationManager(null);
        log.debug("OrganizationManager unset in ApplicationManagementServiceComponent bundle.");
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl",
            service = SecretManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretManagerService"
    )
    private void setSecretManagerService(SecretManager secretManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setSecretManager(secretManager);
    }

    private void unsetSecretManagerService(SecretManager secretManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setSecretManager(null);
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl",
            service = SecretResolveManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretResolveManagerService"
    )
    private void setSecretResolveManagerService(SecretResolveManager secretResolveManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
    }

    private void unsetSecretResolveManagerService(SecretResolveManager secretResolveManager) {

        ApplicationManagementServiceComponentHolder.getInstance().setSecretResolveManager(null);
    }
}
