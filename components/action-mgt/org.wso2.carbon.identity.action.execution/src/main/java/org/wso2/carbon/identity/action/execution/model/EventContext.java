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

package org.wso2.carbon.identity.action.execution.model;

import java.util.Map;

public class EventContext {

    private String actionId;
    Map<String, Object> eventContext;

    public EventContext(Map<String, Object> eventContext) {

        this.eventContext = eventContext;
    }

    public void setEventContext(Map<String, Object> eventContext) {

        this.eventContext = eventContext;
    }

    public Map<String, Object> getEventContext() {

        return eventContext;
    }

    public void setAction(String actionId) {

        this.actionId = actionId;
    }

    public String getAction() {

        return actionId;
    }
}
