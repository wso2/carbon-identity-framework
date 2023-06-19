/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.hanlder;

import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_AUDIT_LOG;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_DIAGNOSTIC_LOG;

/**
 * Central log event handler for audit, and diagnostic logs.
 */
public class CentralLogger extends AbstractEventHandler {

    @Override
    public void handleEvent(Event event) {

        String eventName = event.getEventName();
        Map<String, Object> eventProperties = event.getEventProperties();

        // This central log event handler handles only audit logs and diagnostic logs.
        switch (eventName) {
            case PUBLISH_AUDIT_LOG:
                // Publish new audit logs only if the old audit log publishing is disabled.
                CarbonUtils.publishAuditLogs(eventProperties);
                break;
            case PUBLISH_DIAGNOSTIC_LOG:
                CarbonUtils.publishDiagnosticLog(eventProperties);
                break;
            default:
                break;
        }
    }
}
