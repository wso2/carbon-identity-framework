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

package org.wso2.carbon.identity.topic.management.internal.component;

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
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.topic.management.internal.service.impl.TopicManagementServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

/**
 * TopicManagementServiceComponent is responsible for registering the topic management service
 * in the OSGi runtime.
 */
@Component(
        name = "topic.management.service.component",
        immediate = true
)
public class TopicManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(TopicManagementServiceComponent.class);

    /**
     * Activate the component.
     *
     * @param context Component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            Adapter adapter = TopicManagementComponentServiceHolder.getInstance().getEventAdapterMetadataService()
                    .getCurrentActiveAdapter();
            TopicManagementComponentServiceHolder.getInstance()
                    .setWebhookAdapter(adapter);

            // Register the TopicManagementService
            bundleContext.registerService(TopicManagementService.class.getName(),
                    TopicManagementServiceImpl.getInstance(), null);

            LOG.debug("TopicManagementService is activated");
        } catch (Throwable e) {
            LOG.debug("Error while activating TopicManagementService", e);
        }
    }

    /**
     * Deactivate the component.
     *
     * @param context Component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            LOG.debug("TopicManagementService is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating TopicManagementService", e);
        }
    }

    /**
     * Add topic manager.
     *
     * @param manager TopicManager implementation.
     */
    @Reference(
            name = "topic.manager",
            service = TopicManager.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterTopicManager"
    )
    protected void registerTopicManager(TopicManager manager) {

        LOG.debug("Registering topic manager: " + manager.getAssociatedAdapter());
        TopicManagementComponentServiceHolder.getInstance().addTopicManager(manager);
    }

    /**
     * Remove topic manager.
     *
     * @param manager TopicManager implementation.
     */
    protected void unregisterTopicManager(TopicManager manager) {

        LOG.debug("Unregistering topic manager: " + manager.getAssociatedAdapter());
        TopicManagementComponentServiceHolder.getInstance().removeTopicManager(manager);
    }

    @Reference(
            name = "identity.webhook.adapter.metadata.component",
            service = EventAdapterMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventAdapterMetadataService"
    )
    protected void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        TopicManagementComponentServiceHolder.getInstance()
                .setEventAdapterMetadataService(eventAdapterMetadataService);
        LOG.debug("EventAdapterMetadataService set in TopicManagementComponentServiceHolder bundle.");
    }

    protected void unsetEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        TopicManagementComponentServiceHolder.getInstance().setEventAdapterMetadataService(null);
        LOG.debug("EventAdapterMetadataService unset in TopicManagementComponentServiceHolder bundle.");
    }
}
