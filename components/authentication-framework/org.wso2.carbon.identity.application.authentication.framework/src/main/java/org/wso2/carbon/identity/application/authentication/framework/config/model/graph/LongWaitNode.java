/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Node which does long wait in UI and calls backend service.
 * May provide feedback to the UI via websocket or HTTP Polling.
 */
public class LongWaitNode extends DynamicDecisionNode implements AuthGraphNode {

    private static final long serialVersionUID = -6709928014403661699L;
    private transient AsyncProcess asyncProcess;

    public LongWaitNode(AsyncProcess asyncProcess) {

        this.asyncProcess = asyncProcess;
    }

    public AsyncProcess getAsyncProcess() {

        return asyncProcess;
    }
}
