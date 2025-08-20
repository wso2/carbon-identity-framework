/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.internal;

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
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.flow.mgt.FlowAIService;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;

/**
 * OSGi declarative services component which handles registration and un-registration of flow management service.
 */
@Component(
        name = "flow.mgt.component",
        immediate = true)
public class FlowMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(FlowMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(FlowMgtService.class.getName(),
                    FlowMgtService.getInstance(), null);
            bundleContext.registerService(FlowAIService.class, FlowAIService.getInstance(), null);
            LOG.debug("Flow Management bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error occurred while activating Flow Management bundle.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        BundleContext bundleCtx = context.getBundleContext();
        bundleCtx.ungetService(bundleCtx.getServiceReference(FlowMgtService.class));
        LOG.debug("Flow Management bundle is deactivated.");
    }

    @Reference(
            name = "organization.management.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        FlowMgtServiceDataHolder.getInstance().setOrganizationManager(organizationManager);
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        FlowMgtServiceDataHolder.getInstance().setOrganizationManager(null);
    }

    @Reference(
            name = "org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service",
            service = OrgResourceResolverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrgResourceResolverService")
    protected void setOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        FlowMgtServiceDataHolder.getInstance().setOrgResourceResolverService(orgResourceResolverService);
    }

    protected void unsetOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        FlowMgtServiceDataHolder.getInstance().setOrgResourceResolverService(null);
    }

    @Reference(
            name = "configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager")
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        FlowMgtServiceDataHolder.getInstance().setConfigurationManager(configurationManager);
    }

    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        FlowMgtServiceDataHolder.getInstance().setConfigurationManager(null);
    }
}
