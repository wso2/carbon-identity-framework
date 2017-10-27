package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.AbstractIdentityMessageHandler;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class AuthenticationDataPublisherImpl extends AbstractIdentityMessageHandler implements AuthenticationDataPublisher {

    public boolean canHandle(MessageContext messageContext) {

        return true;
    }

    private static final Log log = LogFactory.getLog(AuthenticationDataPublisherImpl.class);

    private Event getEvent(HttpServletRequest request, AuthenticationContext context, SessionContext sessionContext, Map<String, Object> params, String eventName) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("request", request);
        eventProperties.put("context", context);
        if (sessionContext != null) {
            eventProperties.put("sessionContext", sessionContext);
        }
        eventProperties.put("params", params);

        Event event = new Event(eventName, eventProperties);
        return event;
    }

    @Override
    public void publishAuthenticationStepSuccess(HttpServletRequest request, AuthenticationContext context, Map<String, Object> params) {

        Event event = getEvent(request, context, null, params, "AUTHENTICATION_STEP_SUCCESS");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }

    }

    @Override
    public void publishAuthenticationStepFailure(HttpServletRequest request, AuthenticationContext context, Map<String, Object> params) {

        Event event = getEvent(request, context, null, params, "AUTHENTICATION_STEP_FAILURE");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }

    @Override
    public void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context, Map<String, Object> params) {

        Event event = getEvent(request, context, null, params, "AUTHENTICATION_SUCCESS");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }

    @Override
    public void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context, Map<String, Object> params) {

        Event event = getEvent(request, context, null, params, "AUTHENTICATION_FAILURE");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }

    @Override
    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context, SessionContext sessionContext, Map<String, Object> params) {

        Event event = getEvent(request, context, sessionContext, params, "SESSION_CREATE");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }

    @Override
    public void publishSessionUpdate(HttpServletRequest request, AuthenticationContext context, SessionContext sessionContext, Map<String, Object> params) {

        Event event = getEvent(request, context, sessionContext, params, "SESSION_UPDATE");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }

    @Override
    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context, SessionContext sessionContext, Map<String, Object> params) {

        Event event = getEvent(request, context, sessionContext, params, "SESSION_TERMINATE");
        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            if (log.isDebugEnabled()) {
                log.debug("there is an error while handling the event: " + event.getEventName() + ".", e);
            }
        }
    }
}
