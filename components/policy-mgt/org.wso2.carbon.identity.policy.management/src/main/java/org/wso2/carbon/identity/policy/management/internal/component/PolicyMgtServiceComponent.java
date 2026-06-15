/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.internal.component;

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
import org.wso2.carbon.identity.policy.management.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.policy.management.internal.service.impl.PolicyEvaluationServiceImpl;
import org.wso2.carbon.identity.policy.management.internal.service.impl.PolicyManagementServiceImpl;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

/**
 * OSGi DS component that registers the PolicyManagementService.
 */
@Component(
        name = "policy.management.service.component",
        immediate = true
)
public class PolicyMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(PolicyMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            PolicyManagementServiceImpl policyManagementService = PolicyManagementServiceImpl.getInstance();
            bundleCtx.registerService(PolicyManagementService.class.getName(), policyManagementService, null);
            PolicyMgtComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);

            PolicyEvaluationServiceImpl policyEvaluationService = PolicyEvaluationServiceImpl.getInstance();
            bundleCtx.registerService(PolicyEvaluationService.class.getName(), policyEvaluationService, null);
            LOG.debug("Policy management bundle activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing policy management service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Policy management bundle deactivated.");
    }

    @Reference(
            name = "rule.management.service",
            service = RuleManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleManagementService"
    )
    protected void setRuleManagementService(RuleManagementService ruleManagementService) {

        PolicyMgtComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);
        LOG.debug("RuleManagementService set in Policy Management component.");
    }

    protected void unsetRuleManagementService(RuleManagementService ruleManagementService) {

        PolicyMgtComponentServiceHolder.getInstance().setRuleManagementService(null);
        LOG.debug("RuleManagementService unset in Policy Management component.");
    }

    @Reference(
            name = "rule.evaluation.service",
            service = RuleEvaluationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleEvaluationService"
    )
    protected void setRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        PolicyMgtComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
        LOG.debug("RuleEvaluationService set in Policy Management component.");
    }

    protected void unsetRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        PolicyMgtComponentServiceHolder.getInstance().setRuleEvaluationService(null);
        LOG.debug("RuleEvaluationService unset in Policy Management component.");
    }
}
