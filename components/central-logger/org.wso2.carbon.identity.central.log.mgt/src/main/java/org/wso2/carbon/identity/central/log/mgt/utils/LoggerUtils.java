/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.central.log.mgt.utils;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_AUDIT_LOG;

/**
 *
 */
public class LoggerUtils {

    public static void triggerAuditLogEvent(String clientComponent, String initiatorId,
                                             String initiatorName, String initiatorType, String evenType,
                                             String TargetId, String TargetName, String TargetType,
                                             String dataChange) {

        try {
            Map<String, Object> addAuditLogProperties = new HashMap<>();
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.CLIENT_COMPONENT, clientComponent);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_ID, initiatorId);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_NAME, initiatorName);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_TYPE, initiatorType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.EVENT_TYPE, evenType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_ID, TargetId);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_NAME, TargetName);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_TYPE, TargetType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.DATA_CHANGE, dataChange);

            IdentityEventService eventMgtService =
                    CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
            Event auditEvent = new Event(PUBLISH_AUDIT_LOG, addAuditLogProperties);
            eventMgtService.handleEvent(auditEvent);
        } catch (IdentityEventException e) {
            // TODO
        }
    }

}
