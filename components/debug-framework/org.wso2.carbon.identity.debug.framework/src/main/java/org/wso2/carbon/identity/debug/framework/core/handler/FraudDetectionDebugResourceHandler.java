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

package org.wso2.carbon.identity.debug.framework.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.extension.DebugResourceHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for debugging Fraud Detection resources.
 * Placeholder implementation for future fraud detection debugging capabilities.
 */
public class FraudDetectionDebugResourceHandler implements DebugResourceHandler {

    private static final Log LOG = LogFactory.getLog(FraudDetectionDebugResourceHandler.class);

    @Override
    public Map<String, Object> handleDebugRequest(Map<String, Object> debugRequest) {
        LOG.info("Fraud Detection debug handler invoked");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "NOT_IMPLEMENTED");
        result.put("message", "Fraud Detection debugging not yet implemented");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }
}
