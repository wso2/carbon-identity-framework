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

package org.wso2.carbon.identity.user.pre.update.profile.action.api.model;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;

import java.util.List;

/**
 * Pre Update Profile Action.
 */
public class PreUpdateProfileAction extends Action {

    private final List<String> attributes;

    public PreUpdateProfileAction(ResponseBuilder responseBuilder) {

        super(responseBuilder);
        this.attributes = responseBuilder.attributes;
    }

    public PreUpdateProfileAction(RequestBuilder requestBuilder) {

        super(requestBuilder);
        this.attributes = requestBuilder.attributes;
    }

    public List<String> getAttributes() {

        return attributes;
    }

    /**
     * Response Builder for PreUpdateProfileAction.
     */
    public static class ResponseBuilder extends ActionResponseBuilder {

        private List<String> attributes;

        @Override
        public ResponseBuilder id(String id) {

            super.id(id);
            return this;
        }

        @Override
        public ResponseBuilder type(ActionTypes type) {

            super.type(type);
            return this;
        }

        @Override
        public ResponseBuilder name(String name) {

            super.name(name);
            return this;
        }

        @Override
        public ResponseBuilder description(String description) {

            super.description(description);
            return this;
        }

        @Override
        public ResponseBuilder status(Status status) {

            super.status(status);
            return this;
        }

        @Override
        public ResponseBuilder endpoint(EndpointConfig endpoint) {

            super.endpoint(endpoint);
            return this;
        }

        public ResponseBuilder attributes(List<String> attributes) {

            this.attributes = attributes;
            return this;
        }

        @Override
        public PreUpdateProfileAction build() {

            return new PreUpdateProfileAction(this);
        }
    }

    /**
     * Request Builder for PreUpdatePasswordAction.
     */
    public static class RequestBuilder extends ActionRequestBuilder {

        private List<String> attributes;

        public RequestBuilder(Action action) {

            name(action.getName());
            description(action.getDescription());
            endpoint(action.getEndpoint());
            rule(action.getActionRule());
        }

        @Override
        public RequestBuilder name(String name) {

            super.name(name);
            return this;
        }

        @Override
        public RequestBuilder description(String description) {

            super.description(description);
            return this;
        }

        @Override
        public RequestBuilder endpoint(EndpointConfig endpoint) {

            super.endpoint(endpoint);
            return this;
        }

        public RequestBuilder attributes(List<String> attributes) {

            this.attributes = attributes;
            return this;
        }

        @Override
        public PreUpdateProfileAction build() {

            return new PreUpdateProfileAction(this);
        }
    }

}
