/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.session.extender.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderClientException;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.request.SessionExtenderRequest;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.response.SessionExtenderResponse;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.AUDIT_MESSAGE_TEMPLATE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.EXTEND_SESSION_ACTION;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.SESSION_CONTEXT_ID;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.SUCCESS;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.TRACE_ID;

/**
 * Processes the Session Extender requests and extends session if the request is valid.
 */
public class SessionExtenderProcessor extends IdentityProcessor {

    private static final Log log = LogFactory.getLog(SessionExtenderProcessor.class);
    private static final Log audit = CarbonConstants.AUDIT_LOG;

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest)
            throws SessionExtenderClientException {

        if (log.isDebugEnabled()) {
            log.debug("Request processing started by SessionExtenderProcessor.");
        }

        SessionExtenderRequest sessionExtenderRequest = (SessionExtenderRequest) identityRequest;
        String tenantDomain = sessionExtenderRequest.getTenantDomain();
        String sessionKey = getSessionKey(sessionExtenderRequest);

        SessionContextCacheKey sessionContextCacheKey = new SessionContextCacheKey(sessionKey);
        SessionContextCacheEntry sessionContextCacheEntry =
                SessionContextCache.getInstance().getSessionContextCacheEntry(sessionContextCacheKey, tenantDomain);
        if (sessionContextCacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("No session available for requested session identifier: " + sessionKey);
            }
            throw new SessionExtenderClientException(
                    SessionExtenderConstants.Error.SESSION_NOT_AVAILABLE.getCode(),
                    SessionExtenderConstants.Error.SESSION_NOT_AVAILABLE.getMessage(),
                    "No session available for requested session identifier.");
        }
        SessionContext sessionContext = sessionContextCacheEntry.getContext();
        boolean isSessionExpired = SessionContextCache.getInstance().
                isSessionExpired(sessionContextCacheKey, sessionContextCacheEntry);
        if (isSessionExpired) {
            if (log.isDebugEnabled()) {
                log.debug("Session already expired for provided session cache entry");
            }
            throw new SessionExtenderClientException(
                    SessionExtenderConstants.Error.SESSION_NOT_AVAILABLE.getCode(),
                    SessionExtenderConstants.Error.SESSION_NOT_AVAILABLE.getMessage(),
                    "No session available for requested session identifier.");
        }

        long currentTime = System.currentTimeMillis();
        FrameworkUtils.updateSessionLastAccessTimeMetadata(sessionKey, currentTime);
        FrameworkUtils.addSessionContextToCache(sessionKey, sessionContext, tenantDomain, tenantDomain);

        String traceId = FrameworkUtils.getCorrelation();
        fireEvent(sessionKey, sessionContext, tenantDomain, traceId);
        addAuditLogs(sessionKey, tenantDomain, traceId);

        SessionExtenderResponse.SessionExtenderResponseBuilder responseBuilder =
                new SessionExtenderResponse.SessionExtenderResponseBuilder();
        responseBuilder.setTraceId(traceId);
        return responseBuilder;
    }

    private String getSessionKey(SessionExtenderRequest sessionExtenderRequest) throws SessionExtenderClientException {

        String sessionKeyFromParam = getSessionKeyFromParameters(sessionExtenderRequest);
        String sessionKeyFromCookie = getSessionKeyFromCookie(sessionExtenderRequest);

        // When both the cookie and parameter are present, check whether they match.
        if (sessionKeyFromParam != null && sessionKeyFromCookie != null) {
            if (!sessionKeyFromParam.equals(sessionKeyFromCookie)) {
                throw new SessionExtenderClientException(SessionExtenderConstants.Error.CONFLICT.getCode(),
                        SessionExtenderConstants.Error.CONFLICT.getMessage(),
                        "Session key mismatch between cookie and parameter values.");
            }
        }

        if (sessionKeyFromParam != null) {
            if (log.isDebugEnabled()) {
                log.debug("SessionExtenderProcessor proceeding with the sessionKey in the request. Identified session: "
                        + sessionKeyFromParam);
            }
            return sessionKeyFromParam;
        } else if (sessionKeyFromCookie != null) {
            if (log.isDebugEnabled()) {
                log.debug("SessionExtenderProcessor proceeding with the sessionCookie in the request. Identified " +
                        "session: " + sessionKeyFromCookie);
            }
            return sessionKeyFromCookie;
        } else {
            throw new SessionExtenderClientException(SessionExtenderConstants.Error.INVALID_REQUEST.getCode(),
                    SessionExtenderConstants.Error.INVALID_REQUEST.getMessage(),
                    "No session key or cookie available for processing.");
        }
    }

    private String getSessionKeyFromParameters(SessionExtenderRequest sessionExtenderRequest) {

        return sessionExtenderRequest.getSessionKey();
    }

    private String getSessionKeyFromCookie(SessionExtenderRequest sessionExtenderRequest) {

        return FrameworkUtils.getHashOfCookie(sessionExtenderRequest.getSessionCookie());
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {

        boolean canHandle = identityRequest instanceof SessionExtenderRequest;
        if (canHandle && log.isDebugEnabled()) {
            log.debug("canHandle evaluated as true for SessionExtenderProcessor.");
        }

        return canHandle;
    }

    @Override
    public String getCallbackPath(IdentityMessageContext context) {
        return null;
    }

    @Override
    public String getRelyingPartyId() {
        return null;
    }

    @Override
    public String getRelyingPartyId(IdentityMessageContext context) {
        return null;
    }

    private void fireEvent(String sessionId, SessionContext sessionContext, String tenantDomain, String traceId) {

        IdentityEventService eventService = FrameworkServiceDataHolder.getInstance().getIdentityEventService();
        try {
            Map<String, Object> eventProperties = new HashMap<>();
            eventProperties.put(IdentityEventConstants.EventProperty.SESSION_CONTEXT_ID, sessionId);
            eventProperties.put(IdentityEventConstants.EventProperty.SESSION_CONTEXT, sessionContext);
            eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
            eventProperties.put(IdentityEventConstants.EventProperty.TRACE_ID, traceId);
            Event event = new Event(IdentityEventConstants.Event.SESSION_EXTENSION, eventProperties);
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            String errorLog =  "Could not fire event " + IdentityEventConstants.Event.SESSION_EXTENSION +
                    " when extending the session with session ID " + sessionId + " in tenant domain " + tenantDomain;
            log.error(errorLog, e);
        }
    }

    private void addAuditLogs(String sessionKey, String tenantDomain, String traceId) {

        JSONObject auditData = new JSONObject();
        auditData.put(SESSION_CONTEXT_ID, sessionKey);
        auditData.put(TENANT_DOMAIN, tenantDomain);
        auditData.put(TRACE_ID, traceId);
        // Initiator identification not yet implemented.
        audit.info(String.format(AUDIT_MESSAGE_TEMPLATE, "Not implemented", EXTEND_SESSION_ACTION,
                auditData.toString(), SUCCESS));
    }
}
