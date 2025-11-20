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

package org.wso2.carbon.identity.authorization.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.OperationScopeValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.authorization.common.exception.ForbiddenException;

import java.util.List;
import java.util.Map;

/**
 * Utility class for authorization operations.
 */
public class AuthorizationUtil {

    private static final Log LOG = LogFactory.getLog(AuthorizationUtil.class);

    /**
     * Validates the operation scopes for the given operation name.
     *
     * @param operationName Name of the operation to validate.
     * @throws ForbiddenException If the operation is not permitted.
     */
    public static void validateOperationScopes(String operationName) throws ForbiddenException {

        LOG.debug("Validating operation scopes for operation: " + operationName);

        OperationScopeValidationContext operationScopeValidationContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getOperationScopeValidationContext();

        if (operationScopeValidationContext != null && operationScopeValidationContext.isValidationRequired()) {
            LOG.debug("Operation scope validation is required for operation: " + operationName);
            List<String> allowedScopes = operationScopeValidationContext.getValidatedScopes();
            Map<String, String> operationScopeMap = operationScopeValidationContext.getOperationScopeSet()
                    .getOperationScopeMap();
            String operationScope = operationScopeMap.get(operationName);

            if (operationScope == null) {
                LOG.error("Operation '" + operationName + "' does not have a defined scope. Please check the " +
                        " server configuration for operation scope mapping.");
                throw new ForbiddenException("Operation is not permitted.");
            }

            if (!allowedScopes.contains(operationScope)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Operation '" + operationName + "' requires scope '" + operationScope +
                            "' which is not in allowed scopes.");
                }
                throw new ForbiddenException("Operation is not permitted. You do not have permissions to make " +
                        "this request.");
            }
        }
    }
}
