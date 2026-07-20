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

package org.wso2.carbon.identity.policy.evaluation.internal.component;

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
import org.wso2.carbon.identity.policy.evaluation.api.evaluator.PolicyResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.evaluation.internal.evaluator.RuleResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.internal.service.impl.PolicyEvaluationServiceImpl;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

/**
 * Policy evaluation service component.
 */
@Component(
        name = "policy.evaluation.service.component",
        immediate = true
)
public class PolicyEvaluationServiceComponent {

    private static final Log LOG = LogFactory.getLog(PolicyEvaluationServiceComponent.class);
    private static final String SERVICE_COMPONENT = "service component.";

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(PolicyEvaluationService.class, new PolicyEvaluationServiceImpl(), null);
            bundleCtx.registerService(PolicyResourceEvaluator.class, new RuleResourceEvaluator(), null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy evaluation bundle is activated.");
            }
        } catch (Throwable e) {
            LOG.error("Error while initializing policy evaluation service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(PolicyEvaluationService.class));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy evaluation bundle is deactivated.");
            }
        } catch (Throwable e) {
            LOG.error("Error while deactivating policy evaluation service component.", e);
        }
    }

    @Reference(
            name = "policy.management.service.component",
            service = PolicyManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPolicyManagementService"
    )
    protected void setPolicyManagementService(PolicyManagementService policyManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering a reference for PolicyManagementService in the policy evaluation " +
                    SERVICE_COMPONENT);
        }
        PolicyEvaluationComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);
    }

    protected void unsetPolicyManagementService(PolicyManagementService policyManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering reference for PolicyManagementService in the policy evaluation " +
                    SERVICE_COMPONENT);
        }
        PolicyEvaluationComponentServiceHolder holder = PolicyEvaluationComponentServiceHolder.getInstance();
        if (holder.getPolicyManagementService() == policyManagementService) {
            holder.setPolicyManagementService(null);
        }
    }

    @Reference(
            name = "rule.evaluation.service.component",
            service = RuleEvaluationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleEvaluationService"
    )
    protected void setRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering a reference for RuleEvaluationService in the policy evaluation " +
                    SERVICE_COMPONENT);
        }
        PolicyEvaluationComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
    }

    protected void unsetRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering reference for RuleEvaluationService in the policy evaluation " +
                    SERVICE_COMPONENT);
        }
        PolicyEvaluationComponentServiceHolder holder = PolicyEvaluationComponentServiceHolder.getInstance();
        if (holder.getRuleEvaluationService() == ruleEvaluationService) {
            holder.setRuleEvaluationService(null);
        }
    }

    @Reference(
            name = "policy.resource.evaluator",
            service = PolicyResourceEvaluator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPolicyResourceEvaluator"
    )
    protected void setPolicyResourceEvaluator(PolicyResourceEvaluator policyResourceEvaluator) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering PolicyResourceEvaluator: " + policyResourceEvaluator.getClass().getName() +
                    " in policy evaluation service component.");
        }
        PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(policyResourceEvaluator);
    }

    protected void unsetPolicyResourceEvaluator(PolicyResourceEvaluator policyResourceEvaluator) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering PolicyResourceEvaluator: " + policyResourceEvaluator.getClass().getName() +
                    " in policy evaluation service component.");
        }
        PolicyEvaluationComponentServiceHolder.getInstance().removePolicyResourceEvaluator(policyResourceEvaluator);
    }
}
