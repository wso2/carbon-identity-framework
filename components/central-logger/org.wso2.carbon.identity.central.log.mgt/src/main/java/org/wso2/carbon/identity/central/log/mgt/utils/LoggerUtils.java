/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_AUDIT_LOG;

/**
 * Utils class of central logger.
 */
public class LoggerUtils {

    private static final Log log = LogFactory.getLog(LoggerUtils.class);

    /**
     * @param clientComponent Client component which initiates the request.
     * @param initiatorId     Request initiator's id.
     * @param initiatorName   Request initiator's name.
     * @param initiatorType   Request initiator's type.
     * @param evenType        State changing event name.
     * @param targetId        Target resource's id.
     * @param targetName      Target resource's name.
     * @param targetType      Target resource type.
     * @param dataChange      Changing data.
     */
    public static void triggerAuditLogEvent(Object clientComponent, String initiatorId,
                                            String initiatorName, String initiatorType, String evenType,
                                            String targetId, String targetName, String targetType,
                                            String dataChange) {

        try {
            Map<String, Object> addAuditLogProperties = new HashMap<>();
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.CLIENT_COMPONENT, clientComponent);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_ID, initiatorId);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_NAME, initiatorName);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.INITIATOR_TYPE, initiatorType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.EVENT_TYPE, evenType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_ID, targetId);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_NAME, targetName);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.TARGET_TYPE, targetType);
            addAuditLogProperties.put(CarbonConstants.LogEventConstants.DATA_CHANGE, dataChange);

            IdentityEventService eventMgtService =
                    CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
            Event auditEvent = new Event(PUBLISH_AUDIT_LOG, addAuditLogProperties);
            eventMgtService.handleEvent(auditEvent);
        } catch (IdentityEventException e) {
            String errorLog = "Error occurred when firing the event. Unable to audit the request.";
            log.error(errorLog, e);
        }
    }
}
