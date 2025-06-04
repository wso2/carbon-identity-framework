/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.execution.engine.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;

/**
 * Cache entry for FlowExecutionContext.
 */
public class FlowExecCtxCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 9071624974136245647L;

    FlowExecutionContext context;

    public FlowExecCtxCacheEntry(FlowExecutionContext context) {

        this.context = context;
    }

    public FlowExecutionContext getContext() {

        return context;
    }

    public void setContext(FlowExecutionContext context) {

        this.context = context;
    }
}
