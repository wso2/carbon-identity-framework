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
package org.wso2.carbon.identity.event.Bean;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;

public class IdentityEventMessageContextTest {

    Event event;

    @BeforeMethod
    public void setUp(){

        setEvent();
    }

    private void setEvent() {

        event = new Event("event");
    }

    @Test
    public void testIdentityEventMessageContext(){

        IdentityEventMessageContextTest identityEventMessageContextTest = new IdentityEventMessageContextTest();
        Assert.assertNotNull(identityEventMessageContextTest);
    }

    @Test
    public void testGetEvent(){

        IdentityEventMessageContext identityEventMessageContext = new IdentityEventMessageContext(event);
        Assert.assertNotNull(identityEventMessageContext.getEvent());
    }

    @Test
    public void testSetEvent(){

        Event setEvent = new Event("setEvent");
        IdentityEventMessageContext identityEventMessageContext = new IdentityEventMessageContext(event);
        identityEventMessageContext.setEvent(setEvent);
        Assert.assertEquals(identityEventMessageContext.getEvent(), setEvent);
    }
}
