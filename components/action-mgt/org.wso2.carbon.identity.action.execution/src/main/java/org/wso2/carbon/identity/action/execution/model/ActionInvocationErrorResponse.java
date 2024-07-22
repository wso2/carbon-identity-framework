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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ActionInvocationErrorResponse.Builder.class)
public class ActionInvocationErrorResponse implements ActionInvocationResponse.APIResponse {

    private final String error;
    private final String errorDescription;
    private final String errorUri;

    private ActionInvocationErrorResponse(Builder builder) {

        this.error = builder.error;
        this.errorDescription = builder.errorDescription;
        this.errorUri = builder.errorUri;
    }

    public String getError() {

        return error;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public String getErrorUri() {

        return errorUri;
    }

    public static class Builder {

        private String error;
        private String errorDescription;
        private String errorUri;

        public ActionInvocationErrorResponse.Builder setError(@JsonProperty("error") String error) {

            this.error = error;
            return this;
        }

        public ActionInvocationErrorResponse.Builder setErrorDescription(
                @JsonProperty("errorDescription") String errorDescription) {

            this.errorDescription = errorDescription;
            return this;
        }

        public ActionInvocationErrorResponse.Builder setErrorUri(@JsonProperty("errorUri") String errorUri) {

            this.errorUri = errorUri;
            return this;
        }

        public ActionInvocationErrorResponse build() {

            return new ActionInvocationErrorResponse(this);
        }
    }
}
