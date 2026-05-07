/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.idp.core;

import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

/**
 * Abstract base for IdP-specific debug processors.
 * Owns the connectionId concept on behalf of all IdP protocol processors.
 */
public abstract class IdpDebugProcessor extends DebugProcessor {

    @Override
    protected String extractResourceIdentifier(DebugContext debugContext) {
        String connectionId = (String) debugContext.getProperty(
                IdpDebugConstants.CONNECTION_ID);
        return connectionId != null ? connectionId : "";
    }
}
