/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.thrift.authentication.internal;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.dao.ThriftSessionDAO;
import org.wso2.carbon.identity.thrift.authentication.internal.generatedCode.AuthenticationException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.ThriftSession;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a utility class that performs authentication related functionality
 * by talking to back end authentication service.
 */
public class ThriftAuthenticatorServiceImpl implements ThriftAuthenticatorService {

    private static final Log log = LogFactory.getLog(ThriftAuthenticatorServiceImpl.class);
    //session timeout in milli seconds
    private static long thriftSessionTimeOut;
    private RealmService realmService;
    private Map<String, ThriftSession> authenticatedSessions =
            new ConcurrentHashMap<String, ThriftSession>();
    private ThriftSessionDAO thriftSessionDAO;

    public ThriftAuthenticatorServiceImpl(RealmService realmService, ThriftSessionDAO thriftSessionDAO, long thriftSessionTimeOut) {
        this.realmService = realmService;
        setThriftSessionTimeOut(thriftSessionTimeOut);
        this.thriftSessionDAO = thriftSessionDAO.getInstance();
    }

    private void addThriftSession(ThriftSession thriftSession) throws IdentityException {
        //add to cache
        authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
        //add to database
        ThriftSessionDAO sessionDAO = this.thriftSessionDAO.getInstance();
        sessionDAO.addSession(thriftSession);
    }

    private void removeThriftSession(String thriftSessionId) throws IdentityException {
        //remove from cache
        authenticatedSessions.remove(thriftSessionId);
        //remove from db
        ThriftSessionDAO sessionDAO = this.thriftSessionDAO.getInstance();
        sessionDAO.removeSession(thriftSessionId);
    }

    public String authenticate(String userName, String password) throws AuthenticationException {

        if (userName == null) {
            logAndAuthenticationException("Authentication request was missing the user name ");
        }

        if (userName.indexOf("@") > 0) {
            String domainName = userName.substring(userName.indexOf("@") + 1);
            if (domainName == null || ("").equals(domainName.trim())) {
                logAndAuthenticationException("Authentication request was missing the domain name of" +
                        " the user");
            }
        }

        if (password == null) {
            logAndAuthenticationException("Authentication request was missing the required password");
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        int tenantId = 0;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            logAndAuthenticationException("Tenant domain tenantDomain does not exist");
        }
        //every thread should has its own populated CC. During the deployment time we assume super tenant
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain);
        carbonContext.setTenantId(tenantId);

        boolean isSuccessful;
        String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(userName);

        try {
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            // User not found in the UserStoreManager
            if (!userRealm.getUserStoreManager().isExistingUser(tenantLessUsername)) {
                throw new AuthenticationException("Invalid User : " + tenantLessUsername);
            }

            // Check if the user is authenticated
            isSuccessful = userRealm.getUserStoreManager().authenticate(tenantLessUsername, password);

            // Let the engine know if the user is authenticated or not
        } catch (UserStoreException e) {
            throw new AuthenticationException("User not authenticated for the given username : " + tenantLessUsername);
        }

        if (log.isDebugEnabled()) {
            if (isSuccessful) {
                log.debug("User: " + userName + " was successfully authenticated..");
            } else {
                log.debug("Authentication failed for user: " + userName + " Hence, returning null for session id.");
            }
        }

        if (isSuccessful) {

            //if not, create a new session
            String sessionId = null;
            ThriftSession session = null;
            try {
                sessionId = UUID.randomUUID().toString();
                //populate thrift session
                session = new ThriftSession();
                session.setSessionId(sessionId);
                session.setUserName(userName);
                session.setCreatedAt(System.currentTimeMillis());
                session.setLastAccess(System.currentTimeMillis());

                callOnSuccessAdminLogin(session);
                addThriftSession(session);

            } catch (Exception e) {
                String errorMsg = "Error occurred while authenticating the user: " + userName;
                log.error(errorMsg, e);
                throw new AuthenticationException(errorMsg);
            }
            return sessionId;
        } else {
            //TODO:call onFailedLogin: just for logging purposes
            throw new AuthenticationException("User '" + userName + "' not authenticated.");

        }

    }

    public boolean isAuthenticated(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        //if cache empty, try to populate from db
        if (authenticatedSessions.isEmpty()) {
            try {
                populateSessionsFromDB();
            } catch (IdentityException e) {
                String error = "Error while populating thrift sessions from cache";
                log.error(error, e);
            } catch (Exception e) {
                String error = "Error while populating thrift sessions from cache";
                log.error(error, e);
            }
        }
        //if cache not empty, check if session id existing and valid, if so, update last access time and return it.
        if (!authenticatedSessions.isEmpty()) {
            ThriftSessionDAO sessionDAO = this.thriftSessionDAO.getInstance();
            if (authenticatedSessions.containsKey(sessionId)) {
                ThriftSession thriftSessionInCache = authenticatedSessions.get(sessionId);
                if (isSessionValid(thriftSessionInCache)) {
                    //update the last access time in cache and d
                    long lastAccessTime = System.currentTimeMillis();
                    (authenticatedSessions.get(sessionId)).setLastAccess(lastAccessTime);
                    try {
                        //if carbon context in the thrift session is not initialized, should do that now.
                        onSuccessLogin(thriftSessionInCache);
                        //put the thrift session filled with carbon context info
                        authenticatedSessions.put(sessionId, thriftSessionInCache);
                        sessionDAO.updateLastAccessTime(sessionId, lastAccessTime);
                    } catch (IdentityException e) {
                        String error = "Error while updating last access time in DB";
                        log.error(error, e);
                    } catch (Exception e) {
                        String error = "Error in calling on success admin login for the thrift session.";
                        log.error(error, e);
                    }
                    return true;
                } else {
                    //if not valid in cache, check if valid in db
                    try {
                        ThriftSession thriftSession = sessionDAO.getSession(sessionId);
                        if (isSessionValid(thriftSession)) {
                            //update cache and return true
                            thriftSession.setLastAccess(System.currentTimeMillis());
                            onSuccessLogin(thriftSession);
                            authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
                            sessionDAO.updateLastAccessTime(sessionId, thriftSession.getLastAccess());
                            return true;
                        } else {
                            //remove from cache and db and return false
                            removeThriftSession(sessionId);
                            return false;
                        }
                    } catch (IdentityException e) {
                        String error = "Error while obtaining thrift session from database.";
                        log.error(error, e);
                    } catch (Exception e) {
                        String error = "Error in calling on success admin login for the thrift session.";
                        log.error(error, e);
                    }
                }
            } else {
                //if session id not found, check in db as well, if exist in db, populate cache
                try {
                    if (sessionDAO.isSessionExisting(sessionId)) {
                        ThriftSession thriftSession = sessionDAO.getSession(sessionId);
                        if (isSessionValid(thriftSession)) {
                            thriftSession.setLastAccess(System.currentTimeMillis());
                            onSuccessLogin(thriftSession);
                            authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
                            sessionDAO.updateLastAccessTime(sessionId, thriftSession.getLastAccess());
                            return true;
                        } else {
                            sessionDAO.removeSession(sessionId);
                            return false;
                        }
                    }
                } catch (IdentityException e) {
                    String error = "Error while obtaining thrift session from database.";
                    log.error(error, e);
                } catch (Exception e) {
                    String error = "Error in calling on success admin login for the thrift session obtained from DB.";
                    log.error(error, e);
                }
            }
        }

        return false;
    }

    public ThriftSession getSessionInfo(String sessionId) {
        return authenticatedSessions.get(sessionId);
    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }

    private boolean isSessionValid(ThriftSession thriftSession) {
        //check whether the session is expired.
        return (System.currentTimeMillis() - thriftSession.getLastAccess()) < getThriftSessionTimeOut();
    }

    private void populateSessionsFromDB() throws Exception {
        //first clear the cache
        if (!authenticatedSessions.isEmpty()) {
            authenticatedSessions.clear();
        }
        //get all sessions from db
        ThriftSessionDAO sessionDAO = this.thriftSessionDAO.getInstance();
        List<ThriftSession> thriftSessions = sessionDAO.getAllSessions();
        //add to cache
        if (CollectionUtils.isNotEmpty(thriftSessions)) {
            for (ThriftSession thriftSession : thriftSessions) {
                authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
            }
        }
    }

    private void callOnSuccessAdminLogin(ThriftSession session) throws Exception {
        if (realmService != null) {
            String tenantDomain = MultitenantUtils.getTenantDomain(session.getUserName());
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            CarbonAuthenticationUtil.onSuccessAdminLogin(session, session.getUserName(), tenantId,
                    tenantDomain, "");
        }
    }

    private void onSuccessLogin(ThriftSession authSession) throws IdentityException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        try {
            carbonContext.setUsername((String) (authSession.getAttribute(ServerConstants.AUTHENTICATION_SERVICE_USERNAME)));
            carbonContext.setTenantDomain((String) (authSession.getAttribute(MultitenantConstants.TENANT_DOMAIN)));
            carbonContext.setTenantId((Integer) (authSession.getAttribute(MultitenantConstants.TENANT_ID)));
        } catch (Exception e) {
            String authErrorMsg = "Error populating current carbon context from thrift auth session: " + e.getMessage();
            throw IdentityException.error(authErrorMsg);
        }
    }

    public static long getThriftSessionTimeOut() {
        return thriftSessionTimeOut;
    }

    public static void setThriftSessionTimeOut(long thriftSessionTimeOut) {
        ThriftAuthenticatorServiceImpl.thriftSessionTimeOut = thriftSessionTimeOut;
    }

}
