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

package org.wso2.carbon.identity.flow.execution.engine.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.OperationExecutionResult;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link OperationExecutionResult}.
 */
public class OperationExecutionResultTest {

    private PerformableOperation createOperation(Operation op, String path, Object value) {

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(op);
        operation.setPath(path);
        operation.setValue(value);
        return operation;
    }

    @Test
    public void testSuccessResult() {

        PerformableOperation operation = createOperation(
                Operation.REPLACE, "/user/claims/email", "test@example.com");
        OperationExecutionResult result = new OperationExecutionResult(
                operation, OperationExecutionResult.Status.SUCCESS, "Claim updated");

        assertEquals(result.getOperation(), operation);
        assertEquals(result.getStatus(), OperationExecutionResult.Status.SUCCESS);
        assertEquals(result.getMessage(), "Claim updated");
    }

    @Test
    public void testFailureResult() {

        PerformableOperation operation = createOperation(
                Operation.REPLACE, "/user/credentials/password", "newPass");
        OperationExecutionResult result = new OperationExecutionResult(
                operation, OperationExecutionResult.Status.FAILURE, "Path not allowed");

        assertEquals(result.getStatus(), OperationExecutionResult.Status.FAILURE);
        assertEquals(result.getMessage(), "Path not allowed");
        assertNotNull(result.getOperation());
    }

    @Test
    public void testStatusEnumValues() {

        OperationExecutionResult.Status[] values = OperationExecutionResult.Status.values();
        assertEquals(values.length, 2);
        assertEquals(OperationExecutionResult.Status.valueOf("SUCCESS"),
                OperationExecutionResult.Status.SUCCESS);
        assertEquals(OperationExecutionResult.Status.valueOf("FAILURE"),
                OperationExecutionResult.Status.FAILURE);
    }
}
