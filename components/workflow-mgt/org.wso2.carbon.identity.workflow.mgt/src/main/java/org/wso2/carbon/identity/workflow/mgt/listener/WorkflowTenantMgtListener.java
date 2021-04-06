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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.listener;

import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.stratos.common.exception.StratosException;

/**
 * Workflow Tenant Management Listener.
 */
public class WorkflowTenantMgtListener extends AbstractIdentityTenantMgtListener {

    private static final int EXEC_ORDER = 12;

    /**
     * Get the execution order of the listener.
     *
     * @return int The order of the listener.
     */
    @Override
    public int getListenerOrder() {

        return EXEC_ORDER;
    }

    /**
     * Delete workflow data before tenant deletion.
     *
     * @param tenantId Id of the tenant
     * @throws StratosException
     */
    @Override
    public void onPreDelete(int tenantId) throws StratosException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService().removeWorkflows(tenantId);
        } catch (WorkflowException e) {
            throw new StratosException("Server error occurred when removing workflows");
        }
    }

}
