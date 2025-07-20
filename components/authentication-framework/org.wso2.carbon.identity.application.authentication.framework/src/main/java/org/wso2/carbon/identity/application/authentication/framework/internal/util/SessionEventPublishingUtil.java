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

package org.wso2.carbon.identity.application.authentication.framework.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class to publish session events.
 * This class currently publishes V2 session termination events.
 */
public class SessionEventPublishingUtil {

    private static final Log LOG = LogFactory.getLog(SessionEventPublishingUtil.class);

    private SessionEventPublishingUtil() {
        // Prevent instantiation.
    }

    /**
     * Publishes {@link org.wso2.carbon.identity.event.IdentityEventConstants.Event#SESSION_TERMINATE_V2}
     * at a session termination.
     *
     * @param sessionId      - ID of the session to be terminated.
     * @param request        - HTTP request associated with the session termination.
     * @param context        - Authentication context associated with the session.
     * @param sessionContext - Session context associated with the session.
     */
    public static void publishSessionTerminationEvent(String sessionId, AuthenticatedUser authenticatedUser,
                                                      HttpServletRequest request,
                                                      AuthenticationContext context, SessionContext sessionContext) {

        if (StringUtils.isBlank(sessionId) || authenticatedUser == null || context == null) {
            LOG.debug("Session ID, authenticated user, or authentication context is null or empty. " +
                    "Skip publishing session termination event: " +
                    IdentityEventConstants.Event.SESSION_TERMINATE_V2);
            return;
        }

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.REQUEST, request);
        eventProperties.put(IdentityEventConstants.EventProperty.CONTEXT, context);
        if (sessionContext != null) {
            eventProperties.put(IdentityEventConstants.EventProperty.SESSION_CONTEXT, sessionContext);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, authenticatedUser);
        paramMap.put(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionId);
        eventProperties.put(IdentityEventConstants.EventProperty.PARAMS, paramMap);

        Event event = new Event(IdentityEventConstants.Event.SESSION_TERMINATE_V2, eventProperties);
        doPublishEvent(event);
    }

    /**
     * Publishes {@link org.wso2.carbon.identity.event.IdentityEventConstants.Event#SESSION_TERMINATE_V2}
     * at a session termination.
     * This method is used when the user ID is known, but the authenticated user object is not available.
     *
     * @param userId    - ID of the user whose session is being terminated.
     * @param sessionId - ID of the session to be terminated.
     */
    public static void publishSessionTerminationEvent(String userId, String sessionId) {

        if (StringUtils.isBlank(sessionId) || StringUtils.isBlank(userId)) {
            LOG.debug("Session ID or user ID is null or empty. " +
                    "Skip publishing session termination event: " +
                    IdentityEventConstants.Event.SESSION_TERMINATE_V2);
            return;
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionId);

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.USER_ID, userId);
        eventProperties.put(IdentityEventConstants.EventProperty.PARAMS, paramMap);

        Event event = new Event(IdentityEventConstants.Event.SESSION_TERMINATE_V2, eventProperties);
        doPublishEvent(event);
    }

    /**
     * Publishes {@link org.wso2.carbon.identity.event.IdentityEventConstants.Event#SESSION_TERMINATE_V2}
     * at a session termination for multiple sessions.
     *
     * @param userId     - ID of the user whose sessions are being terminated.
     * @param sessionIds - List of session IDs to be terminated.
     */
    public static void publishSessionTerminationEvent(String userId, List<String> sessionIds) {

        if (StringUtils.isBlank(userId) || sessionIds == null || sessionIds.isEmpty()) {
            LOG.debug("User ID or session IDs are null or empty. " +
                    "Skip publishing session termination event: " +
                    IdentityEventConstants.Event.SESSION_TERMINATE_V2);
            return;
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(IdentityEventConstants.EventProperty.SESSION_IDS, sessionIds);

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.USER_ID, userId);
        eventProperties.put(IdentityEventConstants.EventProperty.PARAMS, paramMap);

        Event event = new Event(IdentityEventConstants.Event.SESSION_TERMINATE_V2, eventProperties);
        doPublishEvent(event);
    }

    private static void doPublishEvent(Event event) {

        try {
            FrameworkServiceDataHolder.getInstance().getIdentityEventService().handleEvent(event);
        } catch (IdentityEventException e) {
            LOG.error("Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }
}
