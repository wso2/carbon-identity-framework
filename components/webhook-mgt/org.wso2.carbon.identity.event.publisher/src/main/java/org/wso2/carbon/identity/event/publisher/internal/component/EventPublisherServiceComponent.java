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

package org.wso2.carbon.identity.event.publisher.internal.component;

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
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService;
import org.wso2.carbon.identity.event.publisher.internal.service.impl.EventPublisherServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

/**
 * EventPublisherServiceComponent is responsible for registering the event publisher service
 * in the OSGi runtime.
 */
@Component(
        name = "event.publisher.service.component",
        immediate = true
)
public class EventPublisherServiceComponent {

    private static final Log LOG = LogFactory.getLog(EventPublisherServiceComponent.class);

    /**
     * Activate the component.
     *
     * @param context Component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            Adapter adapter = EventPublisherComponentServiceHolder.getInstance().getEventAdapterMetadataService()
                    .getCurrentActiveAdapter();
            EventPublisherComponentServiceHolder.getInstance()
                    .setWebhookAdapter(adapter);

            // Register the EventPublisherService
            bundleContext.registerService(EventPublisherService.class.getName(),
                    EventPublisherServiceImpl.getInstance(), null);

            LOG.debug("EventPublisherService is activated");
        } catch (Throwable e) {
            LOG.debug("Error while activating EventPublisherService", e);
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
            LOG.debug("EventPublisherService is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating EventPublisherService", e);
        }
    }

    @Reference(
            name = "identity.event.publisher",
            service = EventPublisher.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeEventPublisher"
    )
    protected void addEventPublisher(EventPublisher eventPublisher) {

        LOG.debug("Adding the event publisher service : " +
                eventPublisher.getClass().getName());
        EventPublisherComponentServiceHolder.getInstance().addEventPublisher(eventPublisher);
    }

    protected void removeEventPublisher(EventPublisher eventPublisher) {

        LOG.debug("Removing the event publisher service : " +
                eventPublisher.getClass().getName());
        EventPublisherComponentServiceHolder.getInstance().removeEventPublisher(eventPublisher);
    }

    @Reference(
            name = "identity.webhook.adapter.metadata.component",
            service = EventAdapterMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventAdapterMetadataService"
    )
    protected void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        EventPublisherComponentServiceHolder.getInstance()
                .setEventAdapterMetadataService(eventAdapterMetadataService);
        LOG.debug("EventAdapterMetadataService set in EventPublisherComponentServiceHolder bundle.");
    }

    protected void unsetEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        EventPublisherComponentServiceHolder.getInstance().setEventAdapterMetadataService(null);
        LOG.debug("EventAdapterMetadataService unset in EventPublisherComponentServiceHolder bundle.");
    }
}
