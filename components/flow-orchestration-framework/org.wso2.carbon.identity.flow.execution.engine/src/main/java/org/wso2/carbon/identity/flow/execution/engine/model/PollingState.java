/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

/**
 * State of the flow's polling, set by executors through the flow execution context.
 */
public enum PollingState {

    /**
     * An executor started polling. The engine creates its entry after the node runs.
     */
    REQUESTED,

    /**
     * The entry exists and a client waits for something outside the flow.
     */
    IN_PROGRESS,

    /**
     * The wait ended successfully and the waiting client can continue the flow.
     */
    COMPLETED,

    /**
     * The wait ended without success. The engine deletes the entry after the node runs.
     */
    FAILED
}
