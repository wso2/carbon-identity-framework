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

import java.util.List;

@JsonDeserialize(builder = ActionInvocationSuccessResponse.Builder.class)
public class ActionInvocationSuccessResponse implements ActionInvocationResponse.APIResponse {

    private final List<PerformableOperation> operations;

    private ActionInvocationSuccessResponse(Builder builder) {

        this.operations = builder.operations;
    }

    public List<PerformableOperation> getOperations() {

        return operations;
    }

    public static class Builder {

        private List<PerformableOperation> operations;

        public Builder setOperations(@JsonProperty("operations") List<PerformableOperation> operations) {

            this.operations = operations;
            return this;
        }

        public ActionInvocationSuccessResponse build() {

            return new ActionInvocationSuccessResponse(this);
        }
    }
}
