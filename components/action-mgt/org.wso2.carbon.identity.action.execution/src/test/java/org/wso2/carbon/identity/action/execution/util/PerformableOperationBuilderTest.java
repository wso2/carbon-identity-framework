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
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;

import java.util.ArrayList;

public class PerformableOperationBuilderTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidRedirectOperationWithPath() {

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REDIRECT);
        performableOp.setPath("/testPath");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidRedirectOperationWithValue() {

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REDIRECT);
        performableOp.setValue(new ArrayList<>());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAddOperationWithValue() {

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.ADD);
        performableOp.setUrl("DummyUrl");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidReplaceOperationWithValue() {

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.REPLACE);
        performableOp.setUrl("DummyUrl");
    }
}
