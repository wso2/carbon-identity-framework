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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.queue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.AsyncOperationStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.queue.AsyncOperationDataBuffer;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for AsyncOperationDataBufferTest.
 */
public class AsyncOperationDataBufferTest {
    private AsyncOperationStatusMgtDAO mockDAO;
    private AsyncOperationDataBuffer dataBuffer;

    private final int threshold = 100;
    private final int flushIntervalSeconds = 3;

    @BeforeClass
    void setUp() {

        mockDAO = mock(AsyncOperationStatusMgtDAO.class);
        dataBuffer = new AsyncOperationDataBuffer(mockDAO, threshold, flushIntervalSeconds);
    }

    @BeforeMethod
    void beforeEach() {

        emptyBuffer();
    }

    @Test
    void testAddBelowThreshold() throws Exception {

        UnitOperationInitDTO record = new UnitOperationInitDTO();
        dataBuffer.add(record);

        assertFalse(dataBuffer.isEmpty());
        verify(mockDAO, never()).registerAsyncStatusUnit(any());
    }

    @Test
    void testAddExceedThresholdTriggersPersistence() throws Exception {

        for (int i = 0; i < threshold; i++) {
            dataBuffer.add(new UnitOperationInitDTO());
        }

        TimeUnit.MILLISECONDS.sleep(2000);

        verify(mockDAO, times(1)).registerAsyncStatusUnit(any());
        assertTrue(dataBuffer.isEmpty());
    }
    @Test
    void testPeriodicFlush() throws Exception {

        UnitOperationInitDTO record = new UnitOperationInitDTO();
        dataBuffer.add(record);

        TimeUnit.SECONDS.sleep(flushIntervalSeconds + 1);

        verify(mockDAO, atLeastOnce()).registerAsyncStatusUnit(any());
        assertTrue(dataBuffer.isEmpty());
    }

    private void emptyBuffer() {

        while (!dataBuffer.isEmpty()) {
            dataBuffer.dequeue();
        }
    }

}
