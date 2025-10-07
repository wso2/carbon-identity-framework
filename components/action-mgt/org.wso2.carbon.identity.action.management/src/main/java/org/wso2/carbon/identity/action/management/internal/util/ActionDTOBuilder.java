/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.internal.util;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal Builder class for ActionDTO.
 */
public class ActionDTOBuilder {

    private String id;
    private Action.ActionTypes type;
    private String name;
    private String description;
    private Action.Status status;
    private String actionVersion;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private EndpointConfig endpoint;
    private ActionRule rule;
    private Map<String, ActionProperty> properties;

    public ActionDTOBuilder() {

    }

    public ActionDTOBuilder(ActionDTO actionDTO) {

        this.id = actionDTO.getId();
        this.type = actionDTO.getType();
        this.name = actionDTO.getName();
        this.description = actionDTO.getDescription();
        this.status = actionDTO.getStatus();
        this.actionVersion = actionDTO.getActionVersion();
        this.createdAt = actionDTO.getCreatedAt();
        this.updatedAt = actionDTO.getUpdatedAt();
        this.endpoint = actionDTO.getEndpoint();
        this.rule = actionDTO.getActionRule();
        this.properties = actionDTO.getProperties();
    }

    public ActionDTOBuilder(Action action) {

        this.id = action.getId();
        this.type = action.getType();
        this.name = action.getName();
        this.description = action.getDescription();
        this.status = action.getStatus();
        this.actionVersion = action.getActionVersion();
        this.createdAt = action.getCreatedAt();
        this.updatedAt = action.getUpdatedAt();
        this.endpoint = action.getEndpoint();
        this.rule = action.getActionRule();
    }

    public ActionDTOBuilder id(String id) {

        this.id = id;
        return this;
    }

    public String getId() {

        return this.id;
    }

    public ActionDTOBuilder type(Action.ActionTypes type) {

        this.type = type;
        return this;
    }

    public Action.ActionTypes getType() {

        return this.type;
    }

    public ActionDTOBuilder name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return this.name;
    }

    public ActionDTOBuilder description(String description) {

        this.description = description;
        return this;
    }

    public String getDescription() {

        return this.description;
    }

    public ActionDTOBuilder status(Action.Status status) {

        this.status = status;
        return this;
    }

    public Action.Status getStatus() {

        return this.status;
    }

    public ActionDTOBuilder actionVersion(String actionVersion) {

        this.actionVersion = actionVersion;
        return this;
    }

    public ActionDTOBuilder createdAt(Timestamp createdAt) {

        this.createdAt = createdAt;
        return this;
    }

    public ActionDTOBuilder updatedAt(Timestamp updatedAt) {

        this.updatedAt = updatedAt;
        return this;
    }

    public ActionDTOBuilder endpoint(EndpointConfig endpoint) {

        this.endpoint = endpoint;
        return this;
    }

    public EndpointConfig getEndpoint() {

        return this.endpoint;
    }

    public ActionDTOBuilder rule(ActionRule rule) {

        this.rule = rule;
        return this;
    }

    public ActionRule getActionRule() {

        return rule;
    }

    public ActionDTOBuilder properties(Map<String, ActionProperty> properties) {

        this.properties = properties;
        return this;
    }

    public Map<String, ActionProperty> getProperties() {

        return this.properties;
    }

    public ActionDTOBuilder property(String propertyName, ActionProperty propertyValue) {

        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(propertyName, propertyValue);
        return this;
    }

    public ActionDTO build() {

        Action action = new Action.ActionResponseBuilder()
                .id(this.id)
                .type(this.type)
                .name(this.name)
                .description(this.description)
                .status(this.status)
                .actionVersion(this.actionVersion)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .endpoint(this.endpoint)
                .rule(this.rule)
                .build();

        return new ActionDTO.Builder(action).properties(this.properties).build();
    }
}
