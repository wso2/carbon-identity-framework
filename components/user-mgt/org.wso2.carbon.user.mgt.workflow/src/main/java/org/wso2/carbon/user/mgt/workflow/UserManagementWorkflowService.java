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

package org.wso2.carbon.user.mgt.workflow;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.user.mgt.workflow.internal.IdentityWorkflowDataHolder;

import java.util.List;

public class UserManagementWorkflowService {

    private static Log log = LogFactory.getLog(UserManagementWorkflowService.class);

    WorkflowManagementService workflowService = IdentityWorkflowDataHolder.getInstance().getWorkflowService();

    /**
     * Retrieve List of associated Entity-types of the workflow requests.
     *
     * @param wfOperationType Operation Type of the Work-flow.
     * @param wfStatus        Current Status of the Work-flow.
     * @param entityType      Entity Type of the Work-flow.
     * @param entityIdFilter        Entity ID filter to search
     * @return
     * @throws WorkflowException
     */

    public List<String> listAllEntityNames(String wfOperationType, String wfStatus, String entityType, String
            entityIdFilter) throws WorkflowException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<String> entityNames = workflowService.listEntityNames(wfOperationType, wfStatus, entityType, tenantID,
                entityIdFilter);
        return entityNames;

    }

}
