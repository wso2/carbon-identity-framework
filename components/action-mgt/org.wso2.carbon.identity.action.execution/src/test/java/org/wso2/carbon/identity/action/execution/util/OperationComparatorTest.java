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

package org.wso2.carbon.identity.action.execution.util;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.internal.util.OperationComparator;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OperationComparatorTest {

    @Test
    public void testCompareMatchingOperationAndBasePathForArrayOrObjectTypeClaims() {

        AllowedOperation allowedOp = new AllowedOperation();
        allowedOp.setOp(Operation.ADD);
        allowedOp.setPaths(Arrays.asList("/accessToken/claims/", "/accessToken/scopes/"));

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.ADD);
        performableOp.setPath("/accessToken/claims/-");
        performableOp.setValue("testValue");

        assertTrue(OperationComparator.compare(allowedOp, performableOp));
    }

    @Test
    public void testCompareMatchingOperationAndBasePathForRedirectOperation() {

        AllowedOperation allowedOp = new AllowedOperation();
        allowedOp.setOp(Operation.REDIRECT);

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REDIRECT);
        performableOp.setUrl("https://test.com");

        assertTrue(OperationComparator.compare(allowedOp, performableOp));
    }

    @Test
    public void testCompareMatchingOperationAndPathForPrimitiveTypeClaims() {

        AllowedOperation allowedOp = new AllowedOperation();
        allowedOp.setOp(Operation.REPLACE);
        allowedOp.setPaths(Arrays.asList("/accessToken/claims/testKey", "/accessToken/scopes/"));

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REPLACE);
        performableOp.setPath("/accessToken/claims/testKey");
        performableOp.setValue("testValue");

        assertTrue(OperationComparator.compare(allowedOp, performableOp));
    }

    @Test
    public void testCompareNonMatchingOperation() {

        AllowedOperation allowedOp = new AllowedOperation();
        allowedOp.setOp(Operation.ADD);
        allowedOp.setPaths(Arrays.asList("/accessToken/claims/", "/accessToken/scopes/"));

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REMOVE);
        performableOp.setPath("/accessToken/claims/testKey");

        assertFalse(OperationComparator.compare(allowedOp, performableOp));
    }

    @Test
    public void testCompareNonMatchingPath() {

        AllowedOperation allowedOp = new AllowedOperation();
        allowedOp.setOp(Operation.REPLACE);
        allowedOp.setPaths(Arrays.asList("/accessToken/claims/testKey", "/accessToken/scopes/"));

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REPLACE);
        performableOp.setPath("/accessToken/claims/0");
        performableOp.setValue("testValue");

        assertFalse(OperationComparator.compare(allowedOp, performableOp));
    }
}
