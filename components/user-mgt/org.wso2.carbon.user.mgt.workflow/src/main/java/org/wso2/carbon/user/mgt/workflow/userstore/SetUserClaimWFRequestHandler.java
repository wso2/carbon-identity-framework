/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.workflow.userstore;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.extension.AbstractWorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowDataType;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.workflow.internal.IdentityWorkflowDataHolder;
import org.wso2.carbon.user.mgt.workflow.util.UserStoreWFConstants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SetUserClaimWFRequestHandler extends AbstractWorkflowRequestHandler {

    private static final String FRIENDLY_NAME = "Update User Claim";
    private static final String FRIENDLY_DESCRIPTION = "Triggered when a user update one of his/her claim.";

    private static final String USERNAME = "Username";
    private static final String USER_STORE_DOMAIN = "User Store Domain";
    private static final String CLAIM_URI = "Claim URI";
    private static final String CLAIM_VALUE = "Claim Value";
    private static final String PROFILE_NAME = "Profile";

    private static final Map<String, String> PARAM_DEFINITION;
    private static Log log = LogFactory.getLog(AddUserWFRequestHandler.class);

    static {
        PARAM_DEFINITION = new LinkedHashMap<>();
        PARAM_DEFINITION.put(USERNAME, WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put(USER_STORE_DOMAIN, WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put(CLAIM_URI, WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put(CLAIM_VALUE, WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put(PROFILE_NAME, WorkflowDataType.STRING_TYPE);
    }

    public boolean startSetClaimWorkflow(String userStoreDomain, String userName, String claimURI, String claimValue,
                                         String profileName) throws WorkflowException {

        WorkflowManagementService workflowService = IdentityWorkflowDataHolder.getInstance().getWorkflowService();

        int tenant = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fullyQualifiedName = UserCoreUtil.addDomainToName(userName, userStoreDomain);


        Map<String, Object> wfParams = new HashMap<>();
        Map<String, Object> nonWfParams = new HashMap<>();
        wfParams.put(USERNAME, userName);
        wfParams.put(USER_STORE_DOMAIN, userStoreDomain);
        wfParams.put(CLAIM_URI, claimURI);
        wfParams.put(CLAIM_VALUE, claimValue);
        wfParams.put(PROFILE_NAME, profileName);
        String uuid = UUID.randomUUID().toString();
        if (workflowService.isEventAssociated(UserStoreWFConstants.SET_USER_CLAIM_EVENT) && !Boolean.TRUE.equals
                (getWorkFlowCompleted()) && !isValidOperation(new Entity[]{new Entity
                (fullyQualifiedName, UserStoreWFConstants.ENTITY_TYPE_USER, tenant), new Entity(claimURI,
                UserStoreWFConstants.ENTITY_TYPE_CLAIM, tenant)})) {
            throw new WorkflowException("Operation is not valid.");
        }
        boolean state = startWorkFlow(wfParams, nonWfParams, uuid).getExecutorResultState().state();

        //WF_REQUEST_ENTITY_RELATIONSHIP table has foreign key to WF_REQUEST, so need to run this after WF_REQUEST is
        // updated
        if (!Boolean.TRUE.equals(getWorkFlowCompleted()) && !state) {
            try {
                workflowService.addRequestEntityRelationships(uuid, new Entity[]{new Entity(fullyQualifiedName,
                        UserStoreWFConstants.ENTITY_TYPE_USER, tenant), new Entity(claimURI, UserStoreWFConstants
                        .ENTITY_TYPE_CLAIM, tenant)});

            } catch (InternalWorkflowException e) {
                //debug exception which occurs at DB level since no workflows associated with event
                if (log.isDebugEnabled()) {
                    log.debug("No workflow associated with the operation.", e);
                }
            }
        }
        return state;
    }

    @Override
    public void onWorkflowCompletion(String status, Map<String, Object> requestParams,
                                     Map<String, Object> responseAdditionalParams, int tenantId)
            throws WorkflowException {
        String userName;
        Object requestUsername = requestParams.get(USERNAME);
        if (requestUsername == null || !(requestUsername instanceof String)) {
            throw new WorkflowException("Callback request for Set User Claim received without the mandatory " +
                    "parameter 'username'");
        }
        String userStoreDomain = (String) requestParams.get(USER_STORE_DOMAIN);
        if (StringUtils.isNotBlank(userStoreDomain)) {
            userName = userStoreDomain + "/" + requestUsername;
        } else {
            userName = (String) requestUsername;
        }

        String claimURI = (String) requestParams.get(CLAIM_URI);
        String claimValue = (String) requestParams.get(CLAIM_VALUE);
        String profile = (String) requestParams.get(PROFILE_NAME);

        if (WorkflowRequestStatus.APPROVED.toString().equals(status) ||
                WorkflowRequestStatus.SKIPPED.toString().equals(status)) {
            try {
                RealmService realmService = IdentityWorkflowDataHolder.getInstance().getRealmService();
                UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                userRealm.getUserStoreManager().setUserClaimValue(userName, claimURI, claimValue, profile);
            } catch (UserStoreException e) {
                // Sending e.getMessage() since it is required to give error message to end user.
                throw new WorkflowException(e.getMessage(), e);
            }
        } else {
            if (retryNeedAtCallback()) {
                //unset threadlocal variable
                unsetWorkFlowCompleted();
            }
            if (log.isDebugEnabled()) {
                log.debug("Setting User Claim is aborted for user '" + userName + "', ClaimURI:" + claimURI + " " +
                        "ClaimValue:" + claimValue + ", Reason: Workflow response was " + status);
            }
        }
    }

    @Override
    public boolean retryNeedAtCallback() {
        return true;
    }

    @Override
    public String getEventId() {
        return UserStoreWFConstants.SET_USER_CLAIM_EVENT;
    }

    @Override
    public Map<String, String> getParamDefinitions() {
        return PARAM_DEFINITION;
    }

    @Override
    public String getFriendlyName() {
        return FRIENDLY_NAME;
    }

    @Override
    public String getDescription() {
        return FRIENDLY_DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return UserStoreWFConstants.CATEGORY_USERSTORE_OPERATIONS;
    }

    @Override
    public boolean isValidOperation(Entity[] entities) throws WorkflowException {

        WorkflowManagementService workflowService = IdentityWorkflowDataHolder.getInstance().getWorkflowService();
        RealmService realmService = IdentityWorkflowDataHolder.getInstance().getRealmService();
        UserRealm userRealm;
        AbstractUserStoreManager userStoreManager;
        try {
            userRealm = realmService.getTenantUserRealm(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getTenantId());
            userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        } catch (UserStoreException e) {
            throw new WorkflowException("Error while retrieving user realm.", e);
        }
        for (int i = 0; i < entities.length; i++) {
            try {
                if (UserStoreWFConstants.ENTITY_TYPE_USER.equals(entities[i].getEntityType()) && workflowService
                        .entityHasPendingWorkflowsOfType(entities[i], UserStoreWFConstants.DELETE_USER_EVENT)) {
                    throw new WorkflowException("User has a delete operation pending.");
                } else if (UserStoreWFConstants.ENTITY_TYPE_USER.equals(entities[i].getEntityType()) &&
                        !userStoreManager.isExistingUser(entities[i].getEntityId())) {
                    throw new WorkflowException("User " + entities[i].getEntityId() + " does not exist.");
                }
                if (UserStoreWFConstants.ENTITY_TYPE_USER.equals(entities[i].getEntityType())) {
                    for (int j = 0; j < entities.length; j++) {
                        if (UserStoreWFConstants.ENTITY_TYPE_CLAIM.equals(entities[j].getEntityType()) &&
                                workflowService.areTwoEntitiesRelated(entities[i], entities[j])) {
                            throw new WorkflowException(entities[j].getEntityId() + " of user is already in a " +
                                    "workflow to delete or update.");
                        }
                    }
                }
            } catch (InternalWorkflowException | org.wso2.carbon.user.core.UserStoreException e) {
                throw new WorkflowException(e.getMessage(), e);
            }
        }
        return true;
    }
}
