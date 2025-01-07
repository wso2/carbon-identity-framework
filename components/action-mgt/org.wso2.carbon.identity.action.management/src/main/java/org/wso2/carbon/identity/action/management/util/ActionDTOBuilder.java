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

package org.wso2.carbon.identity.action.management.util;

import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.ActionRule;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal Builder class for ActionDTO.
 */
public class ActionDTOBuilder {

    private String id;
    private Action.ActionTypes type;
    private String name;
    private String description;
    private Action.Status status;
    private EndpointConfig endpoint;
    private ActionRule rule;
    private Map<String, Object> properties;

    public ActionDTOBuilder() {

    }

    public ActionDTOBuilder(ActionDTO actionDTO) {

        this.id = actionDTO.getId();
        this.type = actionDTO.getType();
        this.name = actionDTO.getName();
        this.description = actionDTO.getDescription();
        this.status = actionDTO.getStatus();
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

    public ActionDTOBuilder setEndpointAndProperties(Map<String, String> properties) throws
            ActionMgtException {

        Authentication authentication;
        Authentication.Type authnType =
                Authentication.Type.valueOf(properties.remove(ActionMgtConstants.AUTHN_TYPE_PROPERTY));
        switch (authnType) {
            case BASIC:
                authentication = new Authentication.BasicAuthBuilder(
                        properties.remove(Authentication.Property.USERNAME.getName()),
                        properties.remove(Authentication.Property.PASSWORD.getName())).build();
                break;
            case BEARER:
                authentication = new Authentication.BearerAuthBuilder(
                        properties.remove(Authentication.Property.ACCESS_TOKEN.getName())).build();
                break;
            case API_KEY:
                authentication = new Authentication.APIKeyAuthBuilder(
                        properties.remove(Authentication.Property.HEADER.getName()),
                        properties.remove(Authentication.Property.VALUE.getName())).build();
                break;
            case NONE:
                authentication = new Authentication.NoneAuthBuilder().build();
                break;
            default:
                throw new ActionMgtServerException("Authentication type is not defined for the Action Endpoint.");
        }

        this.endpoint = new EndpointConfig.EndpointConfigBuilder()
                .uri(properties.remove(ActionMgtConstants.URI_PROPERTY))
                .authentication(authentication)
                .build();
        // Add remaining properties as action properties.
        this.properties = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return this;
    }

    public ActionDTOBuilder properties(Map<String, Object> properties) {

        this.properties = properties;
        return this;
    }

    public Map<String, Object> getProperties() {

        return this.properties;
    }

    public ActionDTOBuilder property(String propertyName, Object propertyValue) {

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
                .endpoint(this.endpoint)
                .rule(this.rule)
                .build();

        return new ActionDTO.Builder(action).properties(this.properties).build();
    }
}
