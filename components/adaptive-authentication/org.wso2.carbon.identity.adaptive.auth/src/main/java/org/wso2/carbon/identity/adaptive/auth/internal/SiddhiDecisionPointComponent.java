/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.adaptive.auth.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.adaptive.auth.EmbeddedSiddhiEngine;
import org.wso2.carbon.identity.adaptive.auth.QueryInterface;
import org.wso2.carbon.identity.adaptive.auth.SiddhiEventPublisher;
import org.wso2.carbon.identity.adaptive.auth.deployer.SiddhiAppDeployer;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component(
        name = "org.wso2.sample.siddhi.decision.point",
        immediate = true
)
public class SiddhiDecisionPointComponent {

    private static final Log log = LogFactory.getLog(SiddhiDecisionPointComponent.class);
    private SiddhiAppDeployer deployer = null;

    @Activate
    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("-------------- SiddhiDecisionPointComponent ACTIVATION STARTED ----------------");
        }

        SiddhiManager siddhiManager = new SiddhiManager();
        QueryInterface queryInterface = new QueryInterface(siddhiManager.getSiddhiAppRuntimeMap());
        SiddhiEventPublisher eventPublisher = new SiddhiEventPublisher(siddhiManager.getSiddhiAppRuntimeMap());
        EmbeddedSiddhiEngine siddhiEngine = new EmbeddedSiddhiEngine();
        AdaptiveDataHolder.getInstance().setSiddhiManager(siddhiManager);
        AdaptiveDataHolder.getInstance().setQueryInterface(queryInterface);
        AdaptiveDataHolder.getInstance().setEventPublisher(eventPublisher);
        AdaptiveDataHolder.getInstance().setSiddhiEngine(siddhiEngine);

        try {
            Path siddhiAppRootPath = Paths.get(CarbonUtils.getCarbonRepository(), "siddhiapps");

            deployer = new SiddhiAppDeployer(siddhiAppRootPath);
            deployer.start();
        } catch (Throwable throwable) {
            log.error("Error while activating bundle.", throwable);
        }

        if (log.isDebugEnabled()) {
            log.debug("-------------- SiddhiDecisionPointComponent ACTIVATION COMPLETED ----------------");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.info("SiddhiDecisionPointComponent bundle is deactivated");
        if (deployer != null) {
            deployer.stop();
        }
    }

    /**
     * This bind method will be called when Siddhi ComponentActivator OSGi service is registered.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    @Reference(
            name = "siddhi.component.activator.service",
            service = SiddhiComponentActivator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiComponentActivator"
    )
    protected void setSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    protected void unsetSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }
}
