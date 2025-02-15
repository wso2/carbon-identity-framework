/*
 * Copyright (c) 2018-2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
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
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CURRENT_SESSION_IDENTIFIER;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants.ErrorMessages.ERROR_CODE_FORBIDDEN_ACTION;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_SESSION;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_USER;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_AUTHORIZE_USER;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSIONS;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IS_FRAGMENT_APP;

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
            if (StringUtils.isBlank(userStoreDomain)) {
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

    /**
     * Retrieves the username of the given userId.
     *
     * @param userId Id of the user.
     * @param tenantId Id of the tenant domain of the user.
     * @return username.
     * @throws UserSessionException
     */
    private String getUsernameFromUserId(String userId, int tenantId) throws
            UserSessionException {

        try {
            UserStoreManager userStoreManager = FrameworkServiceComponent.getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager();
            try {
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userStoreManager).getUserNameFromUserID(userId);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Provided user store manager for the tenantId: " + tenantId + ", is not an instance " +
                            "of the AbstractUserStore manager.");
                }
                throw new UserSessionException("Unable to get the username for the userId: " + userId + ".");
            } catch (org.wso2.carbon.user.core.UserStoreException e) {
                String message = String.format("Error occurred while retrieving username for the userId: %s of " +
                        "tenantId: %s", userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserSessionException(message, e);
            }
        } catch (UserStoreException e) {
            throw new UserSessionException("Error occurred while retrieving the userstore manager to resolve " +
                    "username for the userId: " + userId, e);
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
                    + ", as the provided user store manager: " + userStoreManager.getClass() + ", is not an " +
                    "instance of org.wso2.carbon.user.core.UserStoreManager. Therefore returning the user store " +
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

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return getSessionsByUserId(userId, tenantDomain);
    }

    @Override
    public List<UserSession> getSessionsByUserId(String userId, String tenantDomain) throws SessionManagementException {

        if (StringUtils.isBlank(userId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all the active sessions of user: " + userId + ".");
        }

        List<UserSession> userSessions;
        // First check whether a federated association exists for the userId.
        try {
            int tenantId = getTenantId(tenantDomain);
            Map<SessionMgtConstants.AuthSessionUserKeys, String> authSessionUserMap =
                    getAuthSessionUserMapFromFedAssociationMapping(tenantId, userId);
            if (authSessionUserMap != null && !authSessionUserMap.isEmpty()) {
                String fedAssociatedUserId = authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.USER_ID);
                if (StringUtils.isNotEmpty(fedAssociatedUserId)) {
                    userSessions = getActiveSessionList(getSessionIdListByUserId(fedAssociatedUserId),
                            authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.IDP_ID),
                            authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.IDP_NAME));
                    userSessions.addAll(getActiveSessionList(getSessionIdListByUserId(userId), null, null));
                } else {
                    userSessions = getActiveSessionList(getSessionIdListByUserId(userId), null, null);
                }
            } else {
                userSessions = getActiveSessionList(getSessionIdListByUserId(userId), null, null);
            }
        } catch (UserSessionException e) {
            String msg = "Error occurred while retrieving federated associations for the userId: " + userId;
            throw new SessionManagementServerException(ERROR_CODE_UNABLE_TO_GET_SESSIONS, msg, e);
        }
        return userSessions;
    }

    @Override
    public boolean terminateSessionsByUserId(String userId) throws SessionManagementException {

        List<String> sessionIdList = null;

        if (StringUtils.isBlank(userId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        String userIdToSearch = userId;

        // First check whether a federated association exists for the userId.
        try {
            int tenantId = getTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            Map<SessionMgtConstants.AuthSessionUserKeys, String> authSessionUserMap =
                    getAuthSessionUserMapFromFedAssociationMapping(tenantId, userId);
            if (authSessionUserMap != null && !authSessionUserMap.isEmpty()) {
                String fedAssociatedUserId = authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.USER_ID);
                if (StringUtils.isNotEmpty(fedAssociatedUserId)) {
                    userIdToSearch = fedAssociatedUserId;
                }
            }
        } catch (UserSessionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while retrieving federated associations for the userId: " + userId);
            }
        }
        sessionIdList = getSessionIdListByUserId(userIdToSearch);

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
    public Optional<UserSession> getSessionBySessionId(String userId, String sessionId)
            throws SessionManagementException {

        if (StringUtils.isBlank(userId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        if (StringUtils.isBlank(sessionId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_SESSION, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving session: " + sessionId + " of user: " + userId + ".");
        }

        Optional<UserSession> userSession;
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId,
                FrameworkUtils.getLoginTenantDomainFromContext());
        if (sessionContext != null) {
            UserSessionDAO userSessionDAO = new UserSessionDAOImpl();
            try {
                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                int tenantId = getTenantId(tenantDomain);

                // First check whether a federated association exists for the userId.
                Map<SessionMgtConstants.AuthSessionUserKeys, String> authSessionUserMap =
                        getAuthSessionUserMapFromFedAssociationMapping(tenantId, userId);
                if (authSessionUserMap != null && !authSessionUserMap.isEmpty()) {
                    String fedAssociatedUserId = authSessionUserMap.get(
                            SessionMgtConstants.AuthSessionUserKeys.USER_ID);
                    if (StringUtils.isNotEmpty(fedAssociatedUserId)) {
                        userSession = userSessionDAO.getSession(fedAssociatedUserId, sessionId);
                        userSession.ifPresent(session -> session.setIdpName(
                                authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.IDP_NAME)));
                    } else {
                        userSession = userSessionDAO.getSession(userId, sessionId);
                    }
                } else {
                    userSession = userSessionDAO.getSession(userId, sessionId);
                }

                return userSession;
            } catch (UserSessionException e) {
                String msg = SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION.getDescription();
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                throw new SessionManagementServerException(
                        SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION, msg, e);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean terminateSessionBySessionId(String userId, String sessionId) throws SessionManagementException {

        if (StringUtils.isBlank(userId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        if (StringUtils.isBlank(sessionId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_SESSION, null);
        }
        String userIdToSearch = userId;

        // First check whether a federated association exists for the userId.
        try {
            int tenantId = getTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            Map<SessionMgtConstants.AuthSessionUserKeys, String> authSessionUserMap =
                    getAuthSessionUserMapFromFedAssociationMapping(tenantId, userId);
            if (authSessionUserMap != null && !authSessionUserMap.isEmpty()) {
                String fedAssociatedUserId = authSessionUserMap.get(SessionMgtConstants.AuthSessionUserKeys.USER_ID);
                if (StringUtils.isNotEmpty(fedAssociatedUserId)) {
                    userIdToSearch = fedAssociatedUserId;
                }
            }
        } catch (UserSessionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while retrieving federated associations for the userId: " + userId);
            }
        }

        if (isUserSessionMappingExist(userIdToSearch, sessionId)) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating the session: " + sessionId + " which belongs to the user: " + userId + ".");
            }
            sessionManagementService.removeSession(sessionId);
            List<String> sessionIdList = new ArrayList<>();
            sessionIdList.add(sessionId);
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
            return true;
        } else {
            throw handleSessionManagementClientException(ERROR_CODE_FORBIDDEN_ACTION, userId);
        }
    }

    @Override
    public List<UserSession> getSessionsByUser(User user, int idpId) throws SessionManagementException {

        if (user == null) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all the active sessions of user: " + user.getLoggableUserId() + " of user store " +
                    "domain: " + user.getUserStoreDomain() + ".");
        }

        return getActiveSessionList(getSessionIdListByUser(user, idpId),
                Integer.toString(idpId), null);
    }

    @Override
    public List<UserSession> getSessions(String tenantDomain, List<ExpressionNode> filter, Integer limit,
                                         String sortOrder) throws SessionManagementException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Searching active sessions on the system.");
            }
            UserSessionDAO userSessionDAO = new UserSessionDAOImpl();

            List<UserSession> sessionsList = userSessionDAO.getSessions(getTenantId(tenantDomain),
                    filter, limit, sortOrder);

            // Add identity provider information to the session list.
            try {
                parseIdpInfoToSessionsResponse(tenantDomain, sessionsList, null);

                return sessionsList;
            } catch (UserSessionException e) {
                String msg = "Error while parsing idp information to the session objects.";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                throw new SessionManagementServerException(
                        ERROR_CODE_UNABLE_TO_GET_SESSIONS, msg, e);
            }
        } catch (UserSessionException e) {
            throw new SessionManagementServerException(ERROR_CODE_UNABLE_TO_GET_SESSIONS, e.getMessage(), e);
        }
    }

    @Override
    public boolean terminateSessionsByUser(User user, int idpId) throws SessionManagementException {

        if (user == null) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
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
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_USER, null);
        }
        if (StringUtils.isBlank(sessionId)) {
            throw handleSessionManagementClientException(ERROR_CODE_INVALID_SESSION, null);
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
            throw handleSessionManagementClientException(ERROR_CODE_FORBIDDEN_ACTION, user.getUserName());
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
            throw handleSessionManagementClientException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_INVALID_SESSION_ID, null);
        }
        UserSessionDAO userSessionDTO = new UserSessionDAOImpl();
        UserSession userSession = userSessionDTO.getSession(sessionId);

        return Optional.ofNullable(userSession);
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
            throw handleSessionManagementServerException(ERROR_CODE_UNABLE_TO_GET_SESSIONS, userId, e);
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
            throw handleSessionManagementServerException(ERROR_CODE_UNABLE_TO_GET_SESSIONS, user.getUserName(), e);
        }
    }

    /**
     * Returns the active sessions from given list of session IDs.
     *
     * @param sessionIdList list of sessionIds.
     * @param idpId Id of the authenticated idp.
     * @param idpName Name of the authenticated idp.
     * @return list of user sessions.
     * @throws SessionManagementServerException if an error occurs when retrieving the UserSessions.
     */
    private List<UserSession> getActiveSessionList(List<String> sessionIdList, String idpId, String idpName)
            throws SessionManagementServerException {

        List<UserSession> sessionsList = new ArrayList<>();
        for (String sessionId : sessionIdList) {
            if (sessionId != null) {
                SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId,
                        FrameworkUtils.getLoginTenantDomainFromContext());
                if (sessionContext != null) {
                    UserSessionDAO userSessionDAO = new UserSessionDAOImpl();
                    UserSession userSession = userSessionDAO.getSession(sessionId);
                    if (userSession != null) {
                        if (!isEffectiveSession(sessionContext, userSession)) {
                            continue;
                        }
                        if (StringUtils.isNotBlank(idpId)) {
                            userSession.setIdpId(idpId);
                        }
                        if (StringUtils.isNotBlank(idpName)) {
                            userSession.setIdpName(idpName);
                        }
                        sessionsList.add(userSession);
                    }
                }
            }
        }
        return sessionsList;
    }

    private boolean isEffectiveSession(SessionContext sessionContext, UserSession userSession) {

        try {
            String sessionTenantDomain = null;
            if (sessionContext.getProperties() != null &&
                    sessionContext.getProperties().get(FrameworkUtils.TENANT_DOMAIN) instanceof String) {
                sessionTenantDomain = (String) sessionContext.getProperties().get(FrameworkUtils.TENANT_DOMAIN);
            }
            if (StringUtils.isEmpty(sessionTenantDomain)) {
                return true;
            }
            String loginTenantDomain = FrameworkUtils.getLoginTenantDomainFromContext();
            boolean isOrganization = OrganizationManagementUtil.isOrganization(loginTenantDomain);
            if (StringUtils.equals(sessionTenantDomain, loginTenantDomain)) {
                return !(isOrganization & areAllFragmentAppsInUserSession(userSession));
            } else {
                if (isOrganization &&
                        validatePrimaryOrganization(sessionTenantDomain, loginTenantDomain)) {
                    return true;
                }
                return isSaaSAppAvailableInUserSession(userSession);
            }
        } catch (OrganizationManagementException | IdentityApplicationManagementException e) {
            log.error("Error occurred while validating the effective sessions.", e);
            return true;
        }
    }

    private boolean areAllFragmentAppsInUserSession(UserSession userSession)
            throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManager =
                FrameworkServiceDataHolder.getInstance().getApplicationManagementService();
        for (Application app: userSession.getApplications()) {
            ServiceProvider serviceProvider = applicationManager
                    .getServiceProvider(Integer.parseInt(app.getAppId()));
            if (serviceProvider == null) {
                return false;
            }
            boolean isFragmentApp = Arrays.stream(serviceProvider.getSpProperties())
                    .anyMatch(property -> IS_FRAGMENT_APP.equals(property.getName()) &&
                    Boolean.parseBoolean(property.getValue()));
            if (!isFragmentApp) {
                return false;
            }
        }
        return true;
    }

    private boolean validatePrimaryOrganization(String sessionTenantDomain, String loginTenantDomain)
            throws OrganizationManagementException {

        OrganizationManager organizationManager = FrameworkServiceDataHolder.getInstance().getOrganizationManager();
        String loginTenantDomainOrgId = organizationManager.resolveOrganizationId(loginTenantDomain);
        String sessionTenantDomainOrgId = organizationManager.resolveOrganizationId(sessionTenantDomain);
        String primaryOrgId = organizationManager.getPrimaryOrganizationId(loginTenantDomainOrgId);
        return StringUtils.equals(primaryOrgId, sessionTenantDomainOrgId);
    }

    private boolean isSaaSAppAvailableInUserSession(UserSession userSession)
            throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManager =
                FrameworkServiceDataHolder.getInstance().getApplicationManagementService();
        for (Application app: userSession.getApplications()) {
            ServiceProvider serviceProvider = applicationManager
                    .getServiceProvider(Integer.parseInt(app.getAppId()));
            if (serviceProvider == null) {
                continue;
            }
            if (serviceProvider.isSaasApp()) {
                return true;
            }
        }
        return false;
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
            throw handleSessionManagementServerException(ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, userId, e);
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
            throw handleSessionManagementServerException(ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, user.getUserName(), e);
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

    /**
     * This method checks whether federated associations exist for the given userId and if so returns the
     * internal userId stored in IDN_AUTH_USER table.
     *
     * @param tenantId Tenant Id.
     * @param userId User Id.
     * @return A map keyed by AuthSessionUserKeys.
     */
    private Map<SessionMgtConstants.AuthSessionUserKeys, String> getAuthSessionUserMapFromFedAssociationMapping(
            int tenantId, String userId) throws UserSessionException {

        if (log.isDebugEnabled()) {
            log.debug("Searching federated association for the userId.");
        }

        // Retrieve the username for the userId.
        String username = getUsernameFromUserId(userId, tenantId);
        if (StringUtils.isEmpty(username)) {
            // Return null if the userId is existing in the IDN_AUTH_USER table. No need to check federated
            // associations. Otherwise, throw an exception.
            if (UserSessionStore.getInstance().isExistingUser(userId)) {
                return null;
            }
            throw new UserSessionException(String.format("Error while retrieving federated associations. " +
                    "Username not found for the userId: %s of tenantId: %s", userId, tenantId));
        }
        String userDomain = UserCoreUtil.extractDomainFromName(username);
        username = UserCoreUtil.removeDomainFromName(username);

        try {
            // Retrieve the federated associations for the username.
            List<AssociatedAccountDTO> federatedAssociations = getFederatedAssociationManager()
                    .getFederatedAssociationsOfUser(tenantId, userDomain, username);

            // Get IDP_USER_ID for the retrieved idpId, username and tenant.
            if (!federatedAssociations.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("A federated association found for the userId: %s of tenantId: %s.",
                            userId, tenantId));
                }
                String authSessionUserId = UserSessionStore.getInstance().getUserId(
                        username, tenantId, null, federatedAssociations.get(0).getIdentityProviderId());
                if (StringUtils.isNotEmpty(authSessionUserId)) {
                    Map<SessionMgtConstants.AuthSessionUserKeys, String> resultMap = new HashMap<>();
                    resultMap.put(SessionMgtConstants.AuthSessionUserKeys.USER_ID, authSessionUserId);
                    resultMap.put(SessionMgtConstants.AuthSessionUserKeys.IDP_ID,
                            Integer.toString(federatedAssociations.get(0).getIdentityProviderId()));
                    resultMap.put(SessionMgtConstants.AuthSessionUserKeys.IDP_NAME,
                            federatedAssociations.get(0).getIdentityProviderName());

                    return resultMap;
                }
            }
        } catch (FederatedAssociationManagerException e) {
            throw new UserSessionException("Error while retrieving federated associations.", e);
        }

        return null;
    }

    private FederatedAssociationManager getFederatedAssociationManager() throws UserSessionException {

        FederatedAssociationManager federatedAssociationManager =
                FrameworkServiceDataHolder.getInstance().getFederatedAssociationManager();
        if (federatedAssociationManager == null) {
            String messge = "Error while retrieving federated associations. FederatedAssociationManager is not " +
                    "available in the OSGi framework.";
            if (log.isDebugEnabled()) {
                log.debug(messge);
            }
            throw new UserSessionException(messge);
        }

        return federatedAssociationManager;
    }

    /**
     * Retrieve and set identity provider name to each session object.
     *
     * @param tenantDomain Tenant domain of the user.
     * @param userSessions List of user sessions containing idpId.
     * @param userId Optional userId. If passed, will be set to each session if userId is not found.
     * @throws UserSessionException Exception is thrown if any error occurred.
     */
    private void parseIdpInfoToSessionsResponse(String tenantDomain, List<UserSession> userSessions, String userId)
            throws UserSessionException {

        if (userSessions.isEmpty()) {
            return;
        }
        Set<String> idpIdList = userSessions.stream().map(UserSession::getIdpId).collect(Collectors.toSet());
        if (idpIdList.isEmpty()) {
            return;
        }
        try {
            Map<String, String> idpNameMap = getIDPManagementService().getIdPNamesById(tenantDomain, idpIdList);
            if (idpNameMap == null || idpNameMap.isEmpty()) {
                return;
            }
            for (UserSession userSession : userSessions) {
                String idpName = idpNameMap.get(userSession.getIdpId());
                if (StringUtils.isNotEmpty(idpName)) {
                    userSession.setIdpName(idpName);
                }
                if (StringUtils.isEmpty(userSession.getUserId()) && StringUtils.isNotEmpty(userId)) {
                    userSession.setUserId(userId);
                }
            }
        } catch (IdentityProviderManagementException e) {
            throw new UserSessionException(
                    "Error when retrieving identity provider information for the sessions list", e);
        }
    }

    private IdpManager getIDPManagementService() throws UserSessionException {

        IdpManager idpManagementService = FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
        if (idpManagementService == null) {
            String messge = "Error while retrieving idp management service. IdpManager is not available in the " +
                    "OSGi framework.";
            if (log.isDebugEnabled()) {
                log.debug(messge);
            }
            throw new UserSessionException(messge);
        }

        return idpManagementService;
    }
}
