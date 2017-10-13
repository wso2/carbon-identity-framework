/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.event;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EventDistributionTaskTest {

    @Mock
    AbstractEventHandler module;

    @Test
    public void testRun() throws IdentityEventException {

        module = mock(AbstractEventHandler.class);
        doReturn(true).when(module).isEnabled(any(MessageContext.class));

        Event event = new Event("event");
        event.addEventProperty("value","value");

        List notificationModules = new ArrayList();
        notificationModules.add(module);

        final EventDistributionTask eventDistributionTask = new EventDistributionTask(notificationModules , 1);
        eventDistributionTask.addEventToQueue(event);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = new Runnable() {
            public void run() {
                eventDistributionTask.run();
                Mockito.verify(module).isEnabled(any(MessageContext.class));
            }
        };

        int delay = 5;
        scheduler.schedule(task, delay, TimeUnit.SECONDS);
        scheduler.shutdown();

        eventDistributionTask.shutdown();

    }
}
