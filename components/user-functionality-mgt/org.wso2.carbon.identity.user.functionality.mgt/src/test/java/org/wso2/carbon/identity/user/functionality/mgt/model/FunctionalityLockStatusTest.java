/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.functionality.mgt.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for functionality lock status model.
 */
public class FunctionalityLockStatusTest {

    private final FunctionalityLockStatus
            functionalityLockStatus = new FunctionalityLockStatus(true, System.currentTimeMillis() + 300000,
            "Functionality Lock code", "Functionality Lock Reason");

    @Test
    public void testGetLockStatus() {

        functionalityLockStatus.setLockStatus(false);
        assertEquals(functionalityLockStatus.getLockStatus(), false);
    }

    @Test
    public void testGetFunctionalityUnlockTime() {

        functionalityLockStatus.setUnlockTime(300000);
        assertEquals(functionalityLockStatus.getUnlockTime(), 300000);
    }

    @Test
    public void testGetFunctionalityLockReasonCode() {

        functionalityLockStatus.setLockReasonCode("Functionality Lock Code 2");
        assertEquals(functionalityLockStatus.getLockReasonCode(), "Functionality Lock Code 2");
    }

    @Test
    public void testGetFunctionalityLockReason() {

        functionalityLockStatus.setLockReason("Functionality Lock Reason 2");
        assertEquals(functionalityLockStatus.getLockReason(), "Functionality Lock Reason 2");
    }
}
