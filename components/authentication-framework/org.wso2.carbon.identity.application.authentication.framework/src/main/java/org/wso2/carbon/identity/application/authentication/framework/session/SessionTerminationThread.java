/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authentication.framework.session;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Dedicated thread class to carry out session terminations in a structured manner while maintaining proper logging.
 */
public class SessionTerminationThread implements Callable<Boolean> {

    private SessionManagementService sessionManagementService;
    private String tenantDomain;
    private String jobId;
    private List<String> sessionIdList;
    private static final Log log = LogFactory.getLog(SessionTerminationThread.class);

    /**
     * Constructor to initialize the required variables.
     * @param sessionManagementService  instance of the class which holds the implementation for session removal.
     * @param tenantDomain  tenant domain of the caller/application. Required for succeeding methods called in the
     *                     class.
     * @param jobId job id to track the session termination tasks
     * @param sessionIdList the list of sessions to be terminated
     */
    public SessionTerminationThread(
            SessionManagementService sessionManagementService, String tenantDomain, String jobId,
            List<String> sessionIdList) {

        super();
        this.tenantDomain = tenantDomain;
        this.sessionManagementService = sessionManagementService;
        this.jobId = jobId;
        this.sessionIdList = sessionIdList;
    }

    /**
     * Overrides the generic call method in the Callable interface. A new tenant stack for the thread is initiated
     * and sessions initialized are successively terminated while logging each session against the job id initialized
     * @return success if Future.get() is used to track the result of the parallel processing task. Returns false by
     * default and true if all is executed within this method without encountering any exceptions
     */
    @Override
    public Boolean call() {

        boolean success = false;
        String tenantDomain = this.tenantDomain;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(getTenantIdFromDomain(tenantDomain));

            for (String sessionId : sessionIdList) {
                sessionManagementService.removeSession(sessionId);
                UserSessionStore.getInstance().removeTerminatedSessionRecords(Collections.singletonList(sessionId));
                if (log.isDebugEnabled()) {
                    log.debug("Session terminated. JobId: " + this.getJobId() + " | Session: " + sessionId);
                }
            }
            success = true;
        } catch (Exception e) {
            String errMsg = "Terminating session for tenant domain = " + tenantDomain;
            log.error(errMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return success;
    }

    /**
     * Retrieves tenant id based on the domain.
     * @param tenantDomain tenant domain of which the tenant id is required
     * @return tenant if of the given tenant domain
     * @throws UserSessionException if the tenant domain is invalid
     */
    private int getTenantIdFromDomain(String tenantDomain) throws UserSessionException {

        if (StringUtils.isBlank(tenantDomain)) {
            throw new UserSessionException("Provided tenant domain is invalid: " + tenantDomain);
        }

        try {
            return IdPManagementUtil.getTenantIdOfDomain(tenantDomain);
        } catch (UserStoreException e) {
            throw new UserSessionException(
                    "Error occurred while resolving tenant Id from tenant domain :" + tenantDomain, e);
        }
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getJobId() {
        return jobId;
    }

    public List<String> getSessionIdList() {
        return sessionIdList;
    }
}
