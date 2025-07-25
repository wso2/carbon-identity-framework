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

package org.wso2.carbon.identity.webhook.management.internal.component;

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
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

/**
 * WebhookManagementServiceComponent is responsible for registering the webhook management service
 * in the OSGi runtime.
 */
@Component(
        name = "webhook.management.service.component",
        immediate = true
)
public class WebhookManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(WebhookManagementServiceComponent.class);

    /**
     * Activate the component.
     *
     * @param context Component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            Adapter adapter = WebhookManagementComponentServiceHolder.getInstance().getEventAdapterMetadataService()
                    .getCurrentActiveAdapter();
            WebhookManagementComponentServiceHolder.getInstance()
                    .setWebhookAdapter(adapter);

            bundleContext.registerService(WebhookManagementService.class.getName(),
                    WebhookManagementServiceImpl.getInstance(), null);
            LOG.debug("WebhookManagementService is activated");
        } catch (Throwable e) {
            LOG.error("Error while activating WebhookManagementService", e);
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
            LOG.debug("WebhookManagementService is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating WebhookManagementService", e);
        }
    }

    @Reference(
            name = "topic.management.service.component",
            service = TopicManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTopicManagementService"
    )
    protected void setTopicManagementService(TopicManagementService topicManagementService) {

        WebhookManagementComponentServiceHolder.getInstance().setTopicManagementService(topicManagementService);
        LOG.debug("TopicManagementService set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetTopicManagementService(TopicManagementService topicManagementService) {

        WebhookManagementComponentServiceHolder.getInstance().setTopicManagementService(null);
        LOG.debug("TopicManagementService unset in WebhookManagementComponentServiceHolder bundle.");
    }

    @Reference(
            name = "subscription.management.service.component",
            service = SubscriptionManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSubscriptionManagementService"
    )
    protected void setSubscriptionManagementService(SubscriptionManagementService subscriptionManagementService) {

        WebhookManagementComponentServiceHolder.getInstance()
                .setSubscriptionManagementService(subscriptionManagementService);
        LOG.debug("SubscriptionManagementService set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetSubscriptionManagementService(SubscriptionManagementService subscriptionManagementService) {

        WebhookManagementComponentServiceHolder.getInstance().setSubscriptionManagementService(null);
        LOG.debug("SubscriptionManagementService unset in WebhookManagementComponentServiceHolder bundle.");
    }

    @Reference(
            name = "identity.webhook.event.metadata.component",
            service = WebhookMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetWebhookMetadataService"
    )
    protected void setWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        WebhookManagementComponentServiceHolder.getInstance().setWebhookMetadataService(webhookMetadataService);
        LOG.debug("WebhookMetadataService set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        WebhookManagementComponentServiceHolder.getInstance().setWebhookMetadataService(null);
        LOG.debug("WebhookMetadataService unset in WebhookManagementComponentServiceHolder bundle.");
    }

    @Reference(
            name = "identity.webhook.adapter.metadata.component",
            service = EventAdapterMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventAdapterMetadataService"
    )
    protected void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        WebhookManagementComponentServiceHolder.getInstance()
                .setEventAdapterMetadataService(eventAdapterMetadataService);
        LOG.debug("EventAdapterMetadataService set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        WebhookManagementComponentServiceHolder.getInstance().setEventAdapterMetadataService(null);
        LOG.debug("EventAdapterMetadataService unset in WebhookManagementComponentServiceHolder bundle.");
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretManager",
            service = SecretManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretManager"
    )
    protected void setSecretManager(SecretManager secretManager) {

        WebhookManagementComponentServiceHolder.getInstance().setSecretManager(secretManager);
        LOG.debug("SecretManager set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetSecretManager(SecretManager secretManager) {

        WebhookManagementComponentServiceHolder.getInstance().setSecretManager(null);
        LOG.debug("SecretManager unset in WebhookManagementComponentServiceHolder bundle.");
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager",
            service = SecretResolveManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretResolveManager"
    )
    protected void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        WebhookManagementComponentServiceHolder.getInstance().setSecretResolveManager(secretResolveManager);
        LOG.debug("SecretResolveManager set in WebhookManagementComponentServiceHolder bundle.");
    }

    protected void unsetSecretResolveManager(SecretResolveManager secretResolveManager) {

        WebhookManagementComponentServiceHolder.getInstance().setSecretResolveManager(null);
        LOG.debug("SecretResolveManager unset in WebhookManagementComponentServiceHolder bundle.");
    }
}
