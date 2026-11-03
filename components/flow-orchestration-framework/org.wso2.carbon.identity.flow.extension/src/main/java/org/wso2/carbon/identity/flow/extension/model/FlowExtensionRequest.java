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

package org.wso2.carbon.identity.flow.extension.model;

import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * The request data of the flow sent to the Flow Extension action: the headers the flow defines, as last received.
 */
public class FlowExtensionRequest extends Request {

    public FlowExtensionRequest(List<Header> additionalHeaders) {

        this.additionalHeaders = additionalHeaders != null ? new ArrayList<>(additionalHeaders) : new ArrayList<>();
    }
}
