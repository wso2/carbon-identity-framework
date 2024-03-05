/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.user.mgt.listeners;

import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractGroupOperationEventListener;
import org.wso2.carbon.user.core.common.Claim;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.listener.GroupOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.isEnableV2AuditLogs;

/**
 * This v2 audit logger logs the User Management success activities.
 */
public class GroupManagementV2AuditLogger extends AbstractGroupOperationEventListener {

    public boolean isEnable() {
        return isEnableV2AuditLogs();
    }

    @Override
    public int getExecutionOrderId() {
        int orderId = 0;
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 1;
    }


}
