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
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;

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
            bundleContext.registerService(WebhookManagementService.class.getName(),
                    WebhookManagementServiceImpl.getInstance(), null);
            WebhookManagementComponentServiceHolder.getInstance().setEventSubscriberService(
                    new EventSubscriberService());

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

    /**
     * Add webhook subscriber.
     *
     * @param subscriber WebhookSubscriber implementation.
     */
    @Reference(
            name = "event.subscriber",
            service = EventSubscriber.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterSubscriber"
    )
    protected void registerSubscriber(EventSubscriber subscriber) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering event subscriber: " + subscriber.getName());
        }
        WebhookManagementComponentServiceHolder.getInstance().addEventSubscriber(subscriber);
    }

    /**
     * Remove webhook subscriber.
     *
     * @param subscriber WebhookSubscriber implementation.
     */
    protected void unregisterSubscriber(EventSubscriber subscriber) {

        LOG.debug("Unregistering event subscriber: " + subscriber.getName());
        WebhookManagementComponentServiceHolder.getInstance().removeEventSubscriber(subscriber);
    }
}
