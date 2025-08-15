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

import org.wso2.carbon.context.OperationScopeValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.authorization.common.exception.ForbiddenException;

import java.util.List;
import java.util.Map;

/**
 * Utility class for authorization operations.
 */
public class AuthorizationUtil {

    public static void validateOperationScopes(String operationName) throws ForbiddenException {

        OperationScopeValidationContext operationScopeValidationContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getOperationScopeValidationContext();

        List<String> allowedScopes = operationScopeValidationContext.getValidatedScopes();
        Map<String, String> operationScopeMap = operationScopeValidationContext.getOperationScopeMap();
        String operationScope = operationScopeMap.get(operationName);
        String generalScope = operationScopeMap.get("BULK_CREATE_RESOURCE_OP");

        if (!(allowedScopes.contains(operationScope) || allowedScopes.contains(generalScope))) {
            throw new ForbiddenException("Operation is not permitted. You do not have permissions to make " +
                    "this request.");
        }
    }
}
