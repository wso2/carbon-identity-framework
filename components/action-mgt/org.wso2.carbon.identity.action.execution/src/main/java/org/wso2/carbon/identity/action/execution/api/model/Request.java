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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class models the Request.
 * Request is the entity that represents the request that is sent to Action over Action Execution Request.
 * Request contains additional headers and additional parameters relevant to the trigger that are sent to the Action.
 * The abstraction allows to model requests with additional context based on the action type.
 */
public abstract class Request {

    protected List<Header> additionalHeaders = new ArrayList<>();
    protected List<Param> additionalParams = new ArrayList<>();

    public List<Header> getAdditionalHeaders() {

        return additionalHeaders != null ? Collections.unmodifiableList(additionalHeaders) : Collections.emptyList();
    }

    public void setAdditionalHeaders(List<Header> additionalHeaders) {

        this.additionalHeaders = additionalHeaders;
    }

    public List<Param> getAdditionalParams() {

        return additionalParams != null ? Collections.unmodifiableList(additionalParams) : Collections.emptyList();
    }

    public void setAdditionalParams(List<Param> additionalParams) {

        this.additionalParams = additionalParams;
    }
}
