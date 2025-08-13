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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for WorkflowErrorConstants.
 */
public class WorkflowErrorConstantsTest {

    @Test
    public void testErrorMessages() {

        // Test if the error messages are initialized correctly.
        assertEquals("WFM-10001", WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ALREADY_EXISTS.getCode());
        assertEquals("There is a pending workflow already defined for the user.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ALREADY_EXISTS.getMessage());

        assertEquals("WFM-10002",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_ACCOUNT_PENDING_APPROVAL.getCode());
        assertEquals("The user authentication failed due to pending approval of the user: %s",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_ACCOUNT_PENDING_APPROVAL.getMessage());

        assertEquals("WFM-10003",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_ACCOUNT_PENDING_DELETION.getCode());
        assertEquals("The user account is pending in the deletion workflow.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_ACCOUNT_PENDING_DELETION.getMessage());

        assertEquals("WFM-10004", WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_NOT_FOUND.getCode());
        assertEquals("The user is not found in the system.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_NOT_FOUND.getMessage());

        assertEquals("WFM-10005",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_ALREADY_EXISTS.getCode());
        assertEquals("The user already exists in the system.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_USER_ALREADY_EXISTS.getMessage());

        assertEquals("WFM-10006", WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ROLE_NOT_FOUND.getCode());
        assertEquals("The role %s is not found in the system for assign the user.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ROLE_NOT_FOUND.getMessage());

        assertEquals("WFM-10007",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ROLE_PENDING_DELETION.getCode());
        assertEquals("There is the pending deletion workflow for the role: %s",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ROLE_PENDING_DELETION.getMessage());

        assertEquals("WFM-10008",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_PENDING_ALREADY_EXISTS.getCode());
        assertEquals("There is a pending workflow already defined for the role.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_PENDING_ALREADY_EXISTS.getMessage());

        assertEquals("WFM-10009",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_ROLE_ALREADY_EXISTS.getCode());
        assertEquals("The role already exist in the system.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_ROLE_ALREADY_EXISTS.getMessage());

        assertEquals("WFM-10010", WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_NOT_FOUND.getCode());
        assertEquals("The user %s is not found in the system for assign the role.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_NOT_FOUND.getMessage());

        assertEquals("WFM-10011",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_PENDING_DELETION.getCode());
        assertEquals("There is the pending deletion workflow for the user: %s",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_PENDING_DELETION.getMessage());

        assertEquals("WFM-10012", WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_ROLE_NOT_FOUND.getCode());
        assertEquals("The role is not found in the system.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_ROLE_NOT_FOUND.getMessage());

        assertEquals("WFM-10013",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_PENDING_APPROVAL_FOR_ROLE.getCode());
        assertEquals("The user %s is already pending approval for the role.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_PENDING_APPROVAL_FOR_ROLE.getMessage());

        // Test the toString method of the enum.
        assertEquals("WFM-10001 - There is a pending workflow already defined for the user.",
                WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_WF_ALREADY_EXISTS.toString());
    }
}
