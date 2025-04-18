package org.wso2.carbon.identity.framework.async.status.mgt.queue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.queue.AsyncOperationDataBuffer;

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
    private AsyncStatusMgtDAO mockDAO;
    private AsyncOperationDataBuffer dataBuffer;

    private final int threshold = 100;
    private final int flushIntervalSeconds = 3;

    @BeforeClass
    void setUp() {

        mockDAO = mock(AsyncStatusMgtDAO.class);
        dataBuffer = new AsyncOperationDataBuffer(mockDAO, threshold, flushIntervalSeconds);
    }

    @BeforeMethod
    void beforeEach() {

        emptyBuffer();
    }

    @AfterClass
    void tearDown() {

        dataBuffer.shutdown();
    }

    @Test
    void testAddBelowThreshold() throws Exception {

        UnitOperationRecord record = new UnitOperationRecord();
        dataBuffer.add(record);

        assertFalse(dataBuffer.isEmpty());
        verify(mockDAO, never()).registerAsyncStatusUnit(any());
    }

    @Test
    void testAddExceedThresholdTriggersPersistence() throws Exception {

        for (int i = 0; i < threshold; i++) {
            dataBuffer.add(new UnitOperationRecord());
        }

        TimeUnit.MILLISECONDS.sleep(2000);

        verify(mockDAO, times(1)).registerAsyncStatusUnit(any());
        assertTrue(dataBuffer.isEmpty());
    }
    @Test
    void testPeriodicFlush() throws Exception {

        UnitOperationRecord record = new UnitOperationRecord();
        dataBuffer.add(record);

        TimeUnit.SECONDS.sleep(flushIntervalSeconds + 1);

        verify(mockDAO, atLeastOnce()).registerAsyncStatusUnit(any());
        assertTrue(dataBuffer.isEmpty());
    }

    private void emptyBuffer(){

        while (!dataBuffer.isEmpty()) {
            dataBuffer.dequeue();
        }
    }

}
