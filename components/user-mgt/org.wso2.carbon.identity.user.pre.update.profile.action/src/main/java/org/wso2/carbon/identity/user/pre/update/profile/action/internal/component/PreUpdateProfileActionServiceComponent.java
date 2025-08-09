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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.component;

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
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution.PreUpdateProfileRequestBuilder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution.PreUpdateProfileResponseProcessor;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionConverter;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionDTOModelResolver;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.rule.PreUpdateProfileActionRuleEvaluationDataProvider;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGI Service component for the Pre Update Profile Action.
 */
@Component(
        name = "pre.update.profile.action.service.component",
        immediate = true
)
public class PreUpdateProfileActionServiceComponent {

    private static final Log LOG = LogFactory.getLog(PreUpdateProfileActionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            bundleCtx.registerService(ActionConverter.class, new PreUpdateProfileActionConverter(), null);
            LOG.debug("Pre Update Profile Action Converter is enabled");

            bundleCtx.registerService(ActionDTOModelResolver.class, new PreUpdateProfileActionDTOModelResolver(), null);
            LOG.debug("Pre Update Profile Action DTO Model Resolver is enabled");

            bundleCtx.registerService(ActionExecutionRequestBuilder.class, new PreUpdateProfileRequestBuilder(), null);
            LOG.debug("Pre Update Profile Action Request Builder is registered");

            bundleCtx.registerService(
                    ActionExecutionResponseProcessor.class, new PreUpdateProfileResponseProcessor(), null);
            LOG.debug("Pre Update Profile Action Response Processor is registered");

            bundleCtx.registerService(RuleEvaluationDataProvider.class,
                    new PreUpdateProfileActionRuleEvaluationDataProvider(), null);
            LOG.debug("User Pre Update Profile Action Rule Evaluation Data Provider is enabled");

            LOG.debug("Pre Update Profile Action bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Pre Update Profile Action service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Pre Update Profile Action bundle is deactivated");
    }

    @Reference(
            name = "action.executor.service",
            service = ActionExecutorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutorService"
    )
    protected void setActionExecutorService(ActionExecutorService actionExecutorService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setActionExecutorService(actionExecutorService);
        LOG.debug("ActionExecutorService set in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    protected void unsetActionExecutorService(ActionExecutorService actionExecutorService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setActionExecutorService(null);
        LOG.debug("ActionExecutorService unset in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setRealmService(realmService);
        LOG.debug("RealmService set in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    protected void unsetRealmService(RealmService realmService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setRealmService(null);
        LOG.debug("RealmService unset in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    @Reference(
            name = "claim.metadata.management.service",
            service = org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetadataManagementService")
    protected void setClaimMetadataManagementService(ClaimMetadataManagementService claimManagementService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setClaimManagementService(claimManagementService);
        LOG.debug("ClaimMetadataManagementService set in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    protected void unsetClaimMetadataManagementService(ClaimMetadataManagementService claimManagementService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setClaimManagementService(null);
        LOG.debug("ClaimMetadataManagementService unset in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    @Reference(
            name = "organization.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager"
    )
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        LOG.debug("Organization management service set in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setOrganizationManager(null);
        LOG.debug("Organization management service unset in PreUpdateProfileActionServiceComponentHolder bundle.");
    }
}
