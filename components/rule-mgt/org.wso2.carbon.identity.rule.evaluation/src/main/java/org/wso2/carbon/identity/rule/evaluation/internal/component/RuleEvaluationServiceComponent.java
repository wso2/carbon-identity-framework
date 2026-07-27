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

package org.wso2.carbon.identity.rule.evaluation.internal.component;

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
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluationDataManager;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluationServiceImpl;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

/**
 * Rule evaluation service component.
 */
@Component(
        name = "rule.evaluation.service.component",
        immediate = true
)
public class RuleEvaluationServiceComponent {

    private static final Log LOG = LogFactory.getLog(RuleEvaluationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(RuleEvaluationService.class, new RuleEvaluationServiceImpl(), null);
            LOG.debug("Rule evaluation bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing rule evaluation service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(RuleEvaluationService.class));
            LOG.debug("Rule evaluation bundle is deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating rule evaluation service component.", e);
        }
    }

    @Reference(
            name = "rule.metadata.service.component",
            service = RuleMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleMetadataService"
    )
    protected void setRuleMetadataService(RuleMetadataService ruleMetadataService) {

        LOG.debug("Registering a reference for RuleMetadataService in the rule evaluation service component.");
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
    }

    protected void unsetRuleMetadataService(RuleMetadataService ruleMetadataService) {

        LOG.debug("Unregistering reference for RuleMetadataService in the rule evaluation service component.");
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(null);
    }

    @Reference(
            name = "rule.management.service.component",
            service = RuleManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleManagementService"
    )
    protected void setRuleManagementService(RuleManagementService ruleManagementService) {

        LOG.debug("Registering a reference for RuleManagementService in the rule evaluation service component.");
        RuleEvaluationComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);
    }

    protected void unsetRuleManagementService(RuleManagementService ruleManagementService) {

        LOG.debug("Unregistering reference for RuleManagementService in the rule evaluation service component.");
        RuleEvaluationComponentServiceHolder.getInstance().setRuleManagementService(null);
    }

    @Reference(
            name = "rule.evaluation.data.provider",
            service = RuleEvaluationDataProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleEvaluationDataProvider"
    )
    protected void setRuleEvaluationProvider(RuleEvaluationDataProvider ruleEvaluationDataProvider) {

        LOG.debug("Registering RuleEvaluationDataProvider: " + ruleEvaluationDataProvider.getClass().getName() +
                " in rule evaluation service component.");
        RuleEvaluationDataManager.getInstance().registerRuleEvaluationDataProvider(ruleEvaluationDataProvider);
    }

    protected void unsetRuleEvaluationDataProvider(RuleEvaluationDataProvider ruleEvaluationDataProvider) {

        LOG.debug("Unregistering RuleEvaluationDataProvider: " + ruleEvaluationDataProvider.getClass().getName() +
                " in rule evaluation service component.");
        RuleEvaluationDataManager.getInstance().unregisterRuleEvaluationDataProvider(ruleEvaluationDataProvider);
    }
}
