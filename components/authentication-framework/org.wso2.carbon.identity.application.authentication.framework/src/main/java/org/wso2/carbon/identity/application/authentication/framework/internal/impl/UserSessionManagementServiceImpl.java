/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CURRENT_SESSION_IDENTIFIER;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE;

/**
 * This a service class used to manage user sessions.
 */
public class UserSessionManagementServiceImpl implements UserSessionManagementService {

    private static final Log log = LogFactory.getLog(UserSessionManagementServiceImpl.class);
    private SessionManagementService sessionManagementService = new SessionManagementService();

    @Override
    public void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException {

        validate(username, userStoreDomain, tenantDomain);

        String userId = resolveUserIdFromUsername(getTenantId(tenantDomain), userStoreDomain, username);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all the active sessions of user: " + username + " of userstore domain: " +
                        userStoreDomain + " in tenant: " + tenantDomain);
            }
            terminateSessionsByUserId(userId);
        } catch (SessionManagementException e) {
            throw new UserSessionException("Error while terminating sessions of user:" + username +
                    " of userstore domain: " + userStoreDomain + " in tenant: " + tenantDomain, e);
        }
    }

    /**
     * Retrieves the unique user id of the given username.
     *
     * @param tenantId          id of the tenant domain of the user
     * @param userStoreDomain   userstore of the user
     * @param username          username
     * @return                  unique user id of the user
     * @throws UserSessionException
     */
    private String resolveUserIdFromUsername(int tenantId, String userStoreDomain, String username) throws
            UserSessionException {

        try {
            if (userStoreDomain == null) {
                userStoreDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }
            UserStoreManager userStoreManager = getUserStoreManager(tenantId, userStoreDomain);
            try {
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(username);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Provided user store manager for the user: " + username + " of userstore domain: " +
                            userStoreDomain + ", is not an instance of the AbstractUserStore manager");
                }
                throw new UserSessionException("Unable to get the unique id of the user: " + username + ".");
            } catch (org.wso2.carbon.user.core.UserStoreException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while resolving Id for the user: " + username, e);
                }
                throw new UserSessionException("Error occurred while resolving Id for the user: " + username, e);
            }
        } catch (UserStoreException e) {
            throw new UserSessionException("Error occurred while retrieving the userstore manager to resolve Id for " +
                    "the user: " + username, e);
        }
    }

    private static UserStoreManager getUserStoreManager(int tenantId, String userStoreDomain)
            throws UserStoreException {

        UserStoreManager userStoreManager = FrameworkServiceComponent.getRealmService().getTenantUserRealm(tenantId)
                .getUserStoreManager();
        if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
            return ((org.wso2.carbon.user.core.UserStoreManager) userStoreManager).getSecondaryUserStoreManager(
                    userStoreDomain);
        }
        if (log.isDebugEnabled()) {
            log.debug("Unable to resolve the corresponding user store manager for the domain: " + userStoreDomain
                    + ", as the provided user store manager: " + userStoreManager.getClass() + ", is not an instance " +
                    "of org.wso2.carbon.user.core.UserStoreManager. Therefore returning the user store " +
                    "manager: " + userStoreManager.getClass() + ", from the realm.");
        }
        return userStoreManager;
    }

    private void validate(String username, String userStoreDomain, String tenantDomain) throws UserSessionException {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(userStoreDomain)
                || StringUtils.isBlank(tenantDomain)) {
            throw new UserSessionException("Username, userstore domain or tenant domain cannot be empty");
        }

        int tenantId = getTenantId(tenantDomain);
        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            throw new UserSessionException("Invalid tenant domain: " + tenantDomain + " provided.");
        }
    }

    private void terminateSessionsOfUser(List<String> sessionList) {

        for (String session : sessionList) {
            sessionManagementService.removeSession(session);
        }
    }

    private int getTenantId(String tenantDomain) throws UserSessionException {

        try {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new UserSessionException("Failed to retrieve tenant id for tenant domain: " + tenantDomain);
        }
    }

    @Override
    public List<UserSession> getSessionsByUserId(String userId) throws SessionManagementException {

        if (userId == null || userId.isEmpty()) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all the active sessions of user: " + userId + ".");
        }
        return getActiveSessionList(getSessionIdListByUserId(userId));
    }

    @Override
    public boolean terminateSessionsByUserId(String userId) throws SessionManagementException {

        if (userId == null || userId.isEmpty()) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        List<String> sessionIdList = getSessionIdListByUserId(userId);

        boolean isSessionPreservingAtPasswordUpdateEnabled =
                Boolean.parseBoolean(IdentityUtil.getProperty(PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE));
        String currentSessionId = "";
        boolean isSessionTerminationSkipped = false;
        if (isSessionPreservingAtPasswordUpdateEnabled) {
            if (IdentityUtil.threadLocalProperties.get().get(CURRENT_SESSION_IDENTIFIER) != null) {
                currentSessionId = (String) IdentityUtil.threadLocalProperties.get().get(CURRENT_SESSION_IDENTIFIER);
            }
            // Remove current sessionId from the list so that its termination is bypassed.
            if (sessionIdList.remove(currentSessionId)) {
                isSessionTerminationSkipped = true;
            }
        }

        if (log.isDebugEnabled()) {
            if (isSessionTerminationSkipped) {
                log.debug("Terminating the active sessions of user: " + userId + "except the current session.");
            } else {
                log.debug("Terminating all the active sessions of user: " + userId + ".");
            }
        }
        terminateSessionsOfUser(sessionIdList);
        if (!sessionIdList.isEmpty()) {
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
        }
        return true;
    }

    @Override
    public boolean terminateSessionBySessionId(String userId, String sessionId) throws SessionManagementException {

        if (userId == null || userId.isEmpty()) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        if (sessionId == null || sessionId.isEmpty()) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_SESSION,
                    null);
        }
        if (isUserSessionMappingExist(userId, sessionId)) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating the session: " + sessionId + " which belongs to the user: " + userId + ".");
            }
            sessionManagementService.removeSession(sessionId);
            List<String> sessionIdList = new ArrayList<>();
            sessionIdList.add(sessionId);
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
            return true;
        } else {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_FORBIDDEN_ACTION, userId);
        }
    }

    @Override
    public List<UserSession> getSessionsByUser(User user, int idpId) throws SessionManagementException {

        if (user == null) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all the active sessions of user: " + user.getLoggableUserId() + " of user store " +
                    "domain: " + user.getUserStoreDomain() + ".");
        }
        return getActiveSessionList(getSessionIdListByUser(user, idpId));
    }

    @Override
    public boolean terminateSessionsByUser(User user, int idpId) throws SessionManagementException {

        if (user == null) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        List<String> sessionIdList = getSessionIdListByUser(user, idpId);
        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of user: " + user.getLoggableUserId() + " of user store " +
                    "domain: " + user.getUserStoreDomain() + ".");
        }
        terminateSessionsOfUser(sessionIdList);
        if (!sessionIdList.isEmpty()) {
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
        }
        return true;
    }

    @Override
    public boolean terminateSessionBySessionId(User user, int idpId, String sessionId) throws
            SessionManagementException {

        if (user == null) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    null);
        }
        if (sessionId == null || sessionId.isEmpty()) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_SESSION,
                    null);
        }

        if (isUserSessionMappingExist(user, idpId, sessionId)) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating the session: " + sessionId + " which belongs to the user: " +
                        user.getLoggableUserId() + " of user store domain: " + user.getUserStoreDomain() + ".");
            }
            sessionManagementService.removeSession(sessionId);
            List<String> sessionIdList = new ArrayList<>();
            sessionIdList.add(sessionId);
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
            return true;
        } else {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_FORBIDDEN_ACTION, user.getUserName());
        }
    }

    /**
     * Returns the user session of the given session id.
     *
     * @param sessionId session id.
     * @return user session of the given session id.
     * @throws SessionManagementClientException if the session cannot be found for the given session id.
     * @throws SessionManagementServerException if an error occurred while retrieving federated
     *                                          authentication session information.
     */
    @Override
    public Optional<UserSession> getUserSessionBySessionId(String sessionId) throws SessionManagementClientException,
            SessionManagementServerException {

        if (StringUtils.isBlank(sessionId)) {
            throw handleSessionManagementClientException(SessionMgtConstants.ErrorMessages
                            .ERROR_CODE_INVALID_SESSION_ID, null);
        }
        UserSessionDAO userSessionDTO = new UserSessionDAOImpl();
        return Optional.ofNullable(userSessionDTO.getSession(sessionId));
    }

    /**
     * Returns the session id list for a given user id.
     *
     * @param userId user id for which the sessions should be retrieved.
     * @return the list of session ids
     * @throws SessionManagementServerException if session Ids can not be retrieved from the database.
     */
    private List<String> getSessionIdListByUserId(String userId) throws SessionManagementServerException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving the list of sessions owned by the user: " + userId + ".");
            }
            return UserSessionStore.getInstance().getSessionId(userId);
        } catch (UserSessionException e) {
            throw handleSessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSIONS, userId, e);
        }
    }

    /**
     * Returns the session id list for a given user.
     *
     * @param user  user object
     * @param idpId id of the user's identity provider
     * @throws SessionManagementServerException if session Ids can not be retrieved from the database.
     */
    private List<String> getSessionIdListByUser(User user, int idpId) throws SessionManagementServerException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving the list of sessions owned by the user: " + user.getLoggableUserId()
                        + " of user store domain: " + user.getUserStoreDomain() + ".");
            }
            return UserSessionStore.getInstance().getSessionId(user, idpId);
        } catch (UserSessionException e) {
            throw handleSessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSIONS, user.getUserName(), e);
        }
    }

    /**
     * Returns the active sessions from given list of session IDs.
     *
     * @param sessionIdList list of sessionIds
     * @return list of user sessions
     * @throws SessionManagementServerException if an error occurs when retrieving the UserSessions.
     */
    private List<UserSession> getActiveSessionList(List<String> sessionIdList) throws SessionManagementServerException {

        List<UserSession> sessionsList = new ArrayList<>();
        for (String sessionId : sessionIdList) {
            if (sessionId != null) {
                SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId,
                        FrameworkUtils.getLoginTenantDomainFromContext());
                if (sessionContext != null) {
                    UserSessionDAO userSessionDTO = new UserSessionDAOImpl();
                    UserSession userSession = userSessionDTO.getSession(sessionId);
                    if (userSession != null) {
                        sessionsList.add(userSession);
                    }
                }
            }
        }
        return sessionsList;
    }

    /**
     * This method validates the session that is going to be deleted actually belongs to the provided user identified
     * by the user id.
     *
     * @param userId    id of the user
     * @param sessionId id of the session
     * @return true/false whether the user is authorized for the action
     * @throws SessionManagementServerException if an error occurs while validating the user.
     */
    private boolean isUserSessionMappingExist(String userId, String sessionId) throws SessionManagementServerException {

        boolean isUserAuthorized;
        try {
            isUserAuthorized = UserSessionStore.getInstance().isExistingMapping(userId, sessionId);
        } catch (UserSessionException e) {
            throw handleSessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, userId, e);
        }
        return isUserAuthorized;
    }

    /**
     * This method validates the session that is going to be deleted actually belongs to the provided user.
     *
     * @param user      user object
     * @param idpId     id of the user's identity provider
     * @param sessionId id of the session
     * @return true/false whether the user is authorized for the action
     * @throws SessionManagementServerException if an error occurs while validating the user.
     */
    private boolean isUserSessionMappingExist(User user, int idpId, String sessionId) throws
            SessionManagementServerException {

        boolean isUserAuthorized;
        try {
            isUserAuthorized = UserSessionStore.getInstance().isExistingMapping(user, idpId, sessionId);
        } catch (UserSessionException e) {
            throw handleSessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, user.getUserName(), e);
        }
        return isUserAuthorized;
    }

    private SessionManagementServerException handleSessionManagementServerException(
            SessionMgtConstants.ErrorMessages error, String data, Throwable e) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new SessionManagementServerException(error, description, e);
    }

    private SessionManagementClientException handleSessionManagementClientException(
            SessionMgtConstants.ErrorMessages error, String data) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new SessionManagementClientException(error, description);
    }
}
