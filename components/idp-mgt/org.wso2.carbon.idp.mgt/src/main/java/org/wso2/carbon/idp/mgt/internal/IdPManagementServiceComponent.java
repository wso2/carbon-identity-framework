/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.idp.mgt.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.listener.IDPMgtAuditLogger;
import org.wso2.carbon.idp.mgt.listener.IdPMgtValidationListener;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="idp.mgt.dscomponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="identityCoreInitializedEventService"
 * interface="org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent" cardinality="1..1"
 * policy="dynamic" bind="setIdentityCoreInitializedEventService" unbind="unsetIdentityCoreInitializedEventService"
 * @scr.reference name="idp.mgt.event.listener.service"
 * interface="org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener"
 * cardinality="0..n" policy="dynamic"
 * bind="setIdentityProviderMgtListenerService"
 * unbind="unsetIdentityProviderMgtListenerService"
 * @scr.reference name="identity.provider.saml.service.component"
 * interface="org.wso2.carbon.idp.mgt.util.MetadataConverter"
 * cardinality="0..n" policy="dynamic"
 * bind="setMetadataConverterService"
 * unbind="unsetMetadataConverterService"
 */
public class IdPManagementServiceComponent {

    private static Log log = LogFactory.getLog(IdPManagementServiceComponent.class);



    private static Map<String, IdentityProvider> fileBasedIdPs = new HashMap<String, IdentityProvider>();

    private static Set<String> sharedIdps = new HashSet<String>();



    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Registry service in Identity idp-mgt bundle");
        }
        try {
            IdpMgtServiceComponentHolder.getInstance().setRegistryService(registryService);
        } catch (Throwable e) {
            log.error("Failed to get a reference to the Registry in idp-mgt bundle", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in idp.mgt bundle");
        }
        IdpMgtServiceComponentHolder.getInstance().setRegistryService(null);
    }

    protected void setMetadataConverterService(MetadataConverter converter) {
        if (log.isDebugEnabled()) {
            log.debug("Metadata converter set in Identity idp-mgt bundle");
        }
        try {
            IdpMgtServiceComponentHolder.getInstance().addMetadataConverter(converter);
        } catch (Throwable e) {
            log.error("Failed to get a reference to the Metadata Converter in idp-mgt bundle", e);
        }
    }

    protected void unsetMetadataConverterService(MetadataConverter metadataConverter) {
        if (log.isDebugEnabled()) {
            log.debug("org.wso2.carbon.idp.mgt.util.MetadataConverter unset in idp-mgt");
        }
        IdpMgtServiceComponentHolder.getInstance().removeMetadataConverter(metadataConverter);
    }

    /**
     * @return
     */
    public static Map<String, IdentityProvider> getFileBasedIdPs() {
        return fileBasedIdPs;
    }

    /**
     * @return
     */
    public static RealmService getRealmService() {
        return IdpMgtServiceComponentHolder.getInstance().getRealmService();
    }

    /**
     * @param rlmService
     */
    protected void setRealmService(RealmService rlmService) {
        IdpMgtServiceComponentHolder.getInstance().setRealmService(rlmService);
    }

    /**
     * @return
     */
    public static ConfigurationContextService getConfigurationContextService() {
        return IdpMgtServiceComponentHolder.getInstance().getConfigurationContextService();
    }

    /**
     * @param service
     */
    protected void setConfigurationContextService(ConfigurationContextService service) {
        IdpMgtServiceComponentHolder.getInstance().setConfigurationContextService(service);
    }

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleCtx = ctxt.getBundleContext();

            TenantManagementListener idPMgtTenantMgtListener = new TenantManagementListener();
            ServiceRegistration tenantMgtListenerSR = bundleCtx.registerService(
                    TenantMgtListener.class.getName(), idPMgtTenantMgtListener, null);
            if (tenantMgtListenerSR != null) {
                log.debug("Identity Provider Management - TenantMgtListener registered");
            } else {
                log.error("Identity Provider Management - TenantMgtListener could not be registered");
            }

            ServiceRegistration userOperationListenerSR = bundleCtx.registerService(
                    UserOperationEventListener.class.getName(), new UserStoreListener(), null);
            if (userOperationListenerSR != null) {
                log.debug("Identity Provider Management - UserOperationEventListener registered");
            } else {
                log.error("Identity Provider Management - UserOperationEventListener could not be registered");
            }

            ServiceRegistration auditLoggerSR = bundleCtx.registerService(IdentityProviderMgtListener.class.getName()
                    , new IDPMgtAuditLogger(), null);

            if (auditLoggerSR != null) {
                log.debug("Identity Provider Management - Audit Logger registered");
            } else {
                log.error("Identity Provider Management - Error while registering Audit Logger");
            }
            setIdentityProviderMgtListenerService(new IdPMgtValidationListener());

            CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());
            if (dao.getIdPByName(null,
                    IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                    IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) == null) {
                addSuperTenantIdp();
            }
            bundleCtx.registerService(IdpManager.class, IdentityProviderManager.getInstance(), null);

            buildFileBasedIdPList();
            cleanUpRemovedIdps();

            log.debug("Identity Provider Management bundle is activated");

        } catch (Throwable e) {

            log.error("Error while activating Identity Provider Management bundle", e);

        }
    }

    /**
     *
     */
    private void buildFileBasedIdPList() {

        String spConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity"
                + File.separator + "identity-providers";
        FileInputStream fileInputStream = null;
        File spConfigDir = new File(spConfigDirPath);
        OMElement documentElement = null;

        if (spConfigDir.exists()) {

            for (final File fileEntry : spConfigDir.listFiles()) {
                try {
                    if (!fileEntry.isDirectory()) {
                        fileInputStream = new FileInputStream(new File(fileEntry.getAbsolutePath()));
                        documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();
                        IdentityProvider idp = IdentityProvider.build(documentElement);
                        if (idp != null) {
                            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
                            String superTenantDN = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                            if (isSharedIdP(idp)) {
                                IdentityProvider currentIdp = idpManager.getIdPByName(idp.getIdentityProviderName(),
                                        superTenantDN);
                                if (currentIdp != null && !IdentityApplicationConstants.DEFAULT_IDP_CONFIG.equals(
                                        currentIdp.getIdentityProviderName())) {
                                    idpManager.updateIdP(idp.getIdentityProviderName(), idp, superTenantDN);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Shared IdP " + idp.getIdentityProviderName() + " updated");
                                    }
                                } else {
                                    idpManager.addIdP(idp, superTenantDN);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Shared IdP " + idp.getIdentityProviderName() + " added");
                                    }
                                }
                                sharedIdps.add(idp.getIdentityProviderName());
                            } else {
                                fileBasedIdPs.put(idp.getIdentityProviderName(), idp);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while loading idp from file system.", e);
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    private void cleanUpRemovedIdps() {
        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
        String superTenantDN = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        List<IdentityProvider> idPs;
        try {
            idPs = idpManager.getIdPs(superTenantDN);
        } catch (IdentityProviderManagementException e) {
            log.error("Error loading IDPs", e);
            return;
        }
        for (IdentityProvider idp : idPs) {
            if (isSharedIdP(idp) && !sharedIdps.contains(idp.getIdentityProviderName())) {
                //IDP config file has been deleted from filesystem
                try {
                    idpManager.deleteIdP(idp.getIdentityProviderName(), superTenantDN);
                    if (log.isDebugEnabled()) {
                        log.debug("Deleted shared IdP with the name : " + idp.getIdentityProviderName());
                    }
                } catch (IdentityProviderManagementException e) {
                    log.error("Error when deleting IdP " + idp.getIdentityProviderName(), e);
                }
            }
        }
    }

    private boolean isSharedIdP(IdentityProvider idp) {
        return idp != null && idp.getIdentityProviderName() != null && idp.getIdentityProviderName().startsWith
                (IdPManagementConstants.SHARED_IDP_PREFIX);
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Identity Provider Management bundle is deactivated");
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        IdpMgtServiceComponentHolder.getInstance().setRealmService(null);
    }

    /**
     * @param service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        IdpMgtServiceComponentHolder.getInstance().setConfigurationContextService(null);
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityProviderMgtListenerService(
            IdentityProviderMgtListener identityProviderMgtListenerService) {

        IdpMgtServiceComponentHolder.getInstance().getIdpMgtListeners().add(identityProviderMgtListenerService);
        Collections.sort(IdpMgtServiceComponentHolder.getInstance().getIdpMgtListeners(), idpMgtListenerComparator);
    }

    protected void unsetIdentityProviderMgtListenerService(
            IdentityProviderMgtListener identityProviderMgtListenerService) {

        IdpMgtServiceComponentHolder.getInstance().getIdpMgtListeners().remove(identityProviderMgtListenerService);
    }

    public static Collection<IdentityProviderMgtListener> getIdpMgtListeners() {
        return IdpMgtServiceComponentHolder.getInstance().getIdpMgtListeners();
    }

    private static Comparator<IdentityProviderMgtListener> idpMgtListenerComparator =
            new Comparator<IdentityProviderMgtListener>() {

                @Override
                public int compare(IdentityProviderMgtListener identityProviderMgtListener1,
                                   IdentityProviderMgtListener identityProviderMgtListener2) {

                    if (identityProviderMgtListener1.getExecutionOrderId() > identityProviderMgtListener2.getExecutionOrderId()) {
                        return 1;
                    } else if (identityProviderMgtListener1.getExecutionOrderId() < identityProviderMgtListener2.getExecutionOrderId()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

    private static void addSuperTenantIdp() throws Exception {

        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME);
            identityProvider.setHomeRealmId(IdentityUtil.getHostName());
            identityProvider.setPrimary(true);
            IdentityProviderManager.getInstance()
                    .addResidentIdP(identityProvider, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (Throwable e) {
            throw new Exception("Error when adding Resident Identity Provider entry for super tenant ", e);
        }
    }
}
