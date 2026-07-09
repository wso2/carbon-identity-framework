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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link OperationExecutionResult}.
 */
public class OperationExecutionResultTest {

    @Test
    public void testGettersCarryConstructorValues() {

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=http://wso2.org/claims/x]");

        OperationExecutionResult result = new OperationExecutionResult(
                operation, OperationExecutionResult.Status.SUCCESS, "applied");

        assertSame(result.getOperation(), operation);
        assertEquals(result.getStatus(), OperationExecutionResult.Status.SUCCESS);
        assertEquals(result.getMessage(), "applied");
    }

    @Test
    public void testStatusEnumValues() {

        assertEquals(OperationExecutionResult.Status.valueOf("SUCCESS"),
                OperationExecutionResult.Status.SUCCESS);
        assertEquals(OperationExecutionResult.Status.valueOf("FAILURE"),
                OperationExecutionResult.Status.FAILURE);
        assertEquals(OperationExecutionResult.Status.values().length, 2);
    }
}
