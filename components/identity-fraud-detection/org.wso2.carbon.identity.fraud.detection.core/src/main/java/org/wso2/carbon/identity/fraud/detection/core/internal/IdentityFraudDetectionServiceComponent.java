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
package org.wso2.carbon.identity.fraud.detection.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.fraud.detection.core.IdentityFraudDetector;
import org.wso2.carbon.identity.fraud.detection.core.handler.IdentityFraudDetectionEventHandler;
import org.wso2.carbon.identity.fraud.detection.core.http.HttpClientConnectionConfig;
import org.wso2.carbon.identity.fraud.detection.core.http.HttpClientManager;
import org.wso2.carbon.identity.fraud.detection.core.service.FraudDetectionConfigsService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi service component for Identity Fraud Detector.
 */
@Component(
        name = "org.wso2.carbon.identity.fraud.detector",
        immediate = true
)
public class IdentityFraudDetectionServiceComponent {

    private static final Log LOG = LogFactory.getLog(IdentityFraudDetectionServiceComponent.class);
    private CloseableHttpClient httpClient;

    @Activate
    protected void activate(ComponentContext context) {

        try {
            HttpClientConnectionConfig connectionConfig = new HttpClientConnectionConfig.Builder().build();
            httpClient = HttpClientManager.getInstance().getHttpClient(connectionConfig);
            IdentityFraudDetectionDataHolder.getInstance().setHttpClient(httpClient);

            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(FraudDetectionConfigsService.class.getName(),
                    FraudDetectionConfigsService.getInstance(), null);
            bundleContext.registerService(AbstractEventHandler.class.getName(),
                    new IdentityFraudDetectionEventHandler(), null);
            LOG.debug("Identity Fraud Detector Service Component bundle activated successfully.");
        } catch (Throwable e) {
            LOG.error("Error occurred while activating IdentityFraudDetectionServiceComponent.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (httpClient != null) {
            HttpClientManager.getInstance().closeHttpClient(httpClient);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Identity Fraud Detector Service Component bundle is deactivated.");
        }
    }

    @Reference(
            name = "org.wso2.carbon.identity.fraud.detection.core",
            service = IdentityFraudDetector.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetFraudDetectors"
    )
    protected void setFraudDetectors(IdentityFraudDetector fraudDetector) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Fraud detector is set: " + fraudDetector.getName());
        }
        IdentityFraudDetectionDataHolder.getInstance().addIdentityFraudDetector(fraudDetector.getName(), fraudDetector);
    }

    protected void unsetFraudDetectors(IdentityFraudDetector fraudDetector) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Fraud detector is unset: " + fraudDetector.getName());
        }
        IdentityFraudDetectionDataHolder.getInstance().removeIdentityFraudDetector(fraudDetector.getName());
    }

    @Reference(
            name = "configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager")
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        IdentityFraudDetectionDataHolder.getInstance().setConfigurationManager(configurationManager);
    }

    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        IdentityFraudDetectionDataHolder.getInstance().setConfigurationManager(null);
    }

    @Reference(
            name = "RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        IdentityFraudDetectionDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        IdentityFraudDetectionDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "organization.management.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        IdentityFraudDetectionDataHolder.getInstance().setOrganizationManager(organizationManager);
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        IdentityFraudDetectionDataHolder.getInstance().setOrganizationManager(null);
    }
}
