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
package org.wso2.carbon.identity.fraud.detectors.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants;
import org.wso2.carbon.identity.fraud.detectors.core.exception.FraudDetectionConfigServerException;
import org.wso2.carbon.identity.fraud.detectors.core.exception.UnsupportedFraudDetectionEventException;
import org.wso2.carbon.identity.fraud.detectors.core.model.EventConfigDTO;
import org.wso2.carbon.identity.fraud.detectors.core.service.FraudDetectionConfigsService;
import org.wso2.carbon.identity.fraud.detectors.core.util.EventUtil;

import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.TENANT_DOMAIN;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.IS_LOGGING_ENABLED;
import static org.wso2.carbon.identity.fraud.detectors.core.util.EventUtil.resolveFraudDetectionEvent;

/**
 * Event handler for identity fraud detection events.
 */
public class IdentityFraudDetectorEventHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(IdentityFraudDetectorEventHandler.class);
    private static final String IDENTITY_FRAUD_DETECTOR_EVENT_HANDLER = "IdentityFraudDetectorEventHandler";

    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        if (!(messageContext instanceof IdentityEventMessageContext)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("MessageContext is not of type IdentityEventMessageContext. Cannot handle the event by " +
                        getName());
            }
            return false;
        }

        IdentityEventMessageContext identityContext = (IdentityEventMessageContext) messageContext;
        Event event = identityContext.getEvent();
        if (event == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Event is null in the MessageContext. Cannot handle the event by " + getName());
            }
            return false;
        }

        if (super.canHandle(messageContext)) {
            return isSupportedEvent(event);
        } else {
            // If here, the event is not subscribed to this handler. Hence, cannot handle.
            return false;
        }
    }

    @Override
    public String getName() {

        return IDENTITY_FRAUD_DETECTOR_EVENT_HANDLER;
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        boolean isLoggingEnabled = Boolean.parseBoolean(configs.getModuleProperties().getProperty(IS_LOGGING_ENABLED));
        String tenantDomain;
        if (event.getEventProperties().get(TENANT_DOMAIN) != null) {
            tenantDomain = (String) event.getEventProperties().get(TENANT_DOMAIN);
        } else {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        event.getEventProperties().put(TENANT_DOMAIN, tenantDomain);
        FraudDetectorConstants.FraudDetectionEvents fraudDetectionEvent;
        try {
            fraudDetectionEvent = resolveFraudDetectionEvent(event);
            EventUtil.handleEvent(event, fraudDetectionEvent, isLoggingEnabled);
        } catch (UnsupportedFraudDetectionEventException e) {
            // This should never happen as we have already checked in canHandle method.
            throw new IdentityEventException("Unsupported fraud detection event: " + event.getEventName() +
                    ". Hence, cannot handle the event.", e);
        }
    }

    /**
     * Check whether the event is a supported fraud detection event and publishing is enabled for the event.
     *
     * @param event Event.
     * @return true if the event is supported and publishing is enabled, false otherwise.
     */
    private boolean isSupportedEvent(Event event) {

        try {
            FraudDetectorConstants.FraudDetectionEvents fraudDetectionEvent = resolveFraudDetectionEvent(event);
            String tenantDomain = event.getEventProperties() != null &&
                    event.getEventProperties().get(TENANT_DOMAIN) != null ?
                    (String) event.getEventProperties().get(TENANT_DOMAIN) :
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();;
            return fraudDetectionEvent != null && isPublishingEnabledForEvent(fraudDetectionEvent, tenantDomain);
        } catch (UnsupportedFraudDetectionEventException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unsupported fraud detection event: " + event.getEventName() +
                        ". Hence, cannot handle the event.");
            }
            return false;
        }
    }

    /**
     * Check whether publishing is enabled for the given event in the tenant.
     *
     * @param event        Fraud detection event.
     * @param tenantDomain Tenant domain.
     * @return true if publishing is enabled, false otherwise.
     */
    private boolean isPublishingEnabledForEvent(FraudDetectorConstants.FraudDetectionEvents event,
                                                String tenantDomain) {

        String eventConfigKey = event.getEventConfigName();
        try {
            EventConfigDTO eventConfigDTO = FraudDetectionConfigsService.getInstance()
                    .getFraudDetectionConfigs(tenantDomain).getEventConfig(eventConfigKey);
            if (eventConfigDTO != null) {
                if (eventConfigDTO.isEnabled()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Fraud detection is enabled for event: " + event + " for tenant: "
                                + tenantDomain + ". Proceeding with event handling.");
                    }
                    return true;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Fraud detection is disabled for event: " + event + " for tenant: "
                                + tenantDomain + ". Hence, the event cannot be handled.");
                    }
                    return false;
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No fraud detection event config found for event: " + event + " for tenant: " +
                            tenantDomain + ". Hence, the event handling is interrupted.");
                }
                return false;
            }
        } catch (FraudDetectionConfigServerException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while retrieving fraud detection config for tenant: " + tenantDomain +
                        ". Hence, the event " + event + " cannot be handled.", e);
            }
            return false;
        }
    }
}
