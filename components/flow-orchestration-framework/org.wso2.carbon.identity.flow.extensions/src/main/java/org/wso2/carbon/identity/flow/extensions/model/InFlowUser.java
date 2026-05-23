/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extensions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.action.execution.api.model.User;

/**
 * In-Flow Extension specific view of {@link User} that serializes the user identifier as
 * {@code "userId"} rather than the shared model's {@code "id"} field name.
 */
public class InFlowUser extends User {

    public InFlowUser(User.Builder builder) {

        super(builder);
    }

    @Override
    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getId() {

        return super.getId();
    }
}
