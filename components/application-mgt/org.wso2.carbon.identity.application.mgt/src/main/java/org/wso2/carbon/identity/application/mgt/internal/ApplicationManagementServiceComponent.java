/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.AbstractInboundAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationIdentityProviderMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtAuditLogger;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtValidationListener;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
            bundleContext = context.getBundleContext();
            // Registering Application management service as a OSGIService
            bundleContext.registerService(ApplicationManagementService.class.getName(),
                    ApplicationManagementServiceImpl.getInstance(), null);
            bundleContext.registerService(IdentityProviderMgtListener.class.getName(), new ApplicationIdentityProviderMgtListener(), null);
            bundleContext.registerService(ApplicationMgtListener.class.getName(), new ApplicationMgtValidationListener(), null);
            ApplicationMgtSystemConfig.getInstance();
            bundleContext.registerService(ApplicationMgtListener.class.getName(), new ApplicationMgtAuditLogger(),
                    null);
            buildFileBasedSPList();

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
            name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in Identity ApplicationManagementComponent bundle");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in Identity ApplicationManagementComponent bundle");
        }
        ApplicationManagementServiceComponentHolder.getInstance().setRegistryService(null);
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

}
