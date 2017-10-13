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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;


import java.util.ArrayList;
import java.util.List;


import static org.mockito.Matchers.any;


public class EventDistributionTaskTest extends PowerMockTestCase{

    @Mock
    AbstractEventHandler module;

    @Test
    public void testRun() throws IdentityEventException {

       PowerMockito.when(module.isEnabled(any(MessageContext.class))).thenReturn(true);
        Event event = new Event("event");
        event.addEventProperty("value","value");

        List notificationModules = new ArrayList();
        notificationModules.add(module);

        EventDistributionTask eventDistributionTask = new EventDistributionTask(notificationModules , 1);
        eventDistributionTask.addEventToQueue(event);

        Thread thread = new Thread(eventDistributionTask);
        thread.start();

        eventDistributionTask.shutdown();
    }
}
