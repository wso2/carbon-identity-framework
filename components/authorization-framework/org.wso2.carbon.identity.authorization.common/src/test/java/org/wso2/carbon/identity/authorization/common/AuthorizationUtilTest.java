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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.OperationScopeValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.model.OperationScopeSet;
import org.wso2.carbon.identity.authorization.common.exception.ForbiddenException;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for AuthorizationUtil class.
 */
public class AuthorizationUtilTest {

    @Test(description = "Validated Scopes will contain the scope mapped to the operation.")
    public void testValidateOperationScopesPositiveTest() {

        boolean validationRequired = true;
        List<String> validatedScopes = List.of("scope1", "scope2");
        Map<String, String> operationScopeMap = Map.of("operation1", "scope1", "operation2", "scope2");


        initPrivilegedCarbonContext();

        OperationScopeValidationContext operationScopeValidationContext =
                new OperationScopeValidationContext();
        operationScopeValidationContext.setValidationRequired(validationRequired);
        operationScopeValidationContext.setValidatedScopes(validatedScopes);
        OperationScopeSet operationScopeSet = new OperationScopeSet();
        operationScopeSet.setOperationScopeMap(operationScopeMap);
        operationScopeValidationContext.setOperationScopeSet(operationScopeSet);

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setOperationScopeValidationContext(operationScopeValidationContext);

        try {
            AuthorizationUtil.validateOperationScopes("operation2");
            Assert.assertTrue(true, "No ForbiddenException thrown"); // explicitly passes
        } catch (ForbiddenException e) {
            Assert.fail("Did not expect ForbiddenException, but got: " + e.getMessage());
        }

        PrivilegedCarbonContext.endTenantFlow();
    }

    @DataProvider
    public static Object[][] operationScopesNegativeTestDataProvider() {

        return new Object[][]{
                {"operation2"}, // Not in validatedScopes.
                {"operation3"}  // Not in operationScopeMap.
        };
    }

    @Test(expectedExceptions = ForbiddenException.class,
            description = "Validated Scopes will not contain the scope mapped to the operation.",
            dataProvider = "operationScopesNegativeTestDataProvider")
    public void testValidateOperationScopesNegativeTest(String operation) throws Exception {

        boolean validationRequired = true;
        List<String> validatedScopes = List.of("scope1");
        Map<String, String> operationScopeMap = Map.of("operation1", "scope1", "operation2", "scope2");

        initPrivilegedCarbonContext();

        OperationScopeValidationContext operationScopeValidationContext =
                new OperationScopeValidationContext();
        operationScopeValidationContext.setValidationRequired(validationRequired);
        operationScopeValidationContext.setValidatedScopes(validatedScopes);
        OperationScopeSet operationScopeSet = new OperationScopeSet();
        operationScopeSet.setOperationScopeMap(operationScopeMap);
        operationScopeValidationContext.setOperationScopeSet(operationScopeSet);

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setOperationScopeValidationContext(operationScopeValidationContext);

        AuthorizationUtil.validateOperationScopes(operation);

        PrivilegedCarbonContext.endTenantFlow();
    }

    public void initPrivilegedCarbonContext() {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
                          );
        PrivilegedCarbonContext.startTenantFlow();
    }
}
