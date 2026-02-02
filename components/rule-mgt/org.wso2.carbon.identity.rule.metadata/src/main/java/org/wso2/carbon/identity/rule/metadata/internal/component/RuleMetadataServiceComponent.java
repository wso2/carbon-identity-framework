/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.internal.component;

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
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;
import org.wso2.carbon.identity.rule.metadata.internal.provider.impl.StaticRuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.internal.service.impl.RuleMetadataManager;
import org.wso2.carbon.identity.rule.metadata.internal.service.impl.RuleMetadataServiceImpl;

/**
 * Rule metadata service component.
 */
@Component(
        name = "rule.metadata.service.component",
        immediate = true
)
public class RuleMetadataServiceComponent {

    private static final Log LOG = LogFactory.getLog(RuleMetadataServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            RuleMetadataManager ruleMetadataManager = RuleMetadataManager.getInstance();
            ruleMetadataManager.registerMetadataProvider(StaticRuleMetadataProvider.loadStaticMetadata());
            bundleCtx.registerService(RuleMetadataService.class.getName(),
                    new RuleMetadataServiceImpl(ruleMetadataManager),
                    null);
            LOG.debug("Rule metadata bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing rule metadata service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(RuleMetadataService.class));
            LOG.debug("Rule metadata bundle is deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating rule metadata service component.", e);
        }
    }

    @Reference(
            name = "rule.metadata.provider",
            service = RuleMetadataProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleMetadataProvider"
    )
    protected void setRuleMetadataProvider(RuleMetadataProvider ruleMetadataProvider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering RuleMetadataProvider: " + ruleMetadataProvider.getClass().getName() +
                    " in the RuleMetadataComponent.");
        }
        RuleMetadataManager.getInstance().registerMetadataProvider(ruleMetadataProvider);
    }

    protected void unsetRuleMetadataProvider(RuleMetadataProvider ruleMetadataProvider) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering RuleMetadataProvider: " + ruleMetadataProvider.getClass().getName() +
                    " in the RuleMetadataComponent.");
        }
        RuleMetadataManager.getInstance().unregisterMetadataProvider(ruleMetadataProvider);
    }
}
