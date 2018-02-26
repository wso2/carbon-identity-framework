/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.recorder;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests related to the UserDeletionEventRecorder.
 */
public class DefaultUserDeletionEventRecorderTest {

    private static final String DUMMY_USERNAME = "admin";
    private static final String DUMMY_DOMAIN = "PRIMARY";
    private static final String DUMMY_TENANT_DOMAIN = "carbon.super";
    private static final int DUMMY_TENANT_ID = -1234;

    @Test
    public void emptyPropertiesTest() {

        DefaultUserDeletionEventRecorder defaultUserDeleteEventRecorder = new DefaultUserDeletionEventRecorder();
        try {
            defaultUserDeleteEventRecorder.recordUserDeleteEvent(DUMMY_USERNAME, DUMMY_DOMAIN, DUMMY_TENANT_DOMAIN,
                    DUMMY_TENANT_ID, new Date(), new HashMap<>());
        } catch (RecorderException e) {
            assertEquals(e.getClass(), RecorderException.class);
        }
    }

}
