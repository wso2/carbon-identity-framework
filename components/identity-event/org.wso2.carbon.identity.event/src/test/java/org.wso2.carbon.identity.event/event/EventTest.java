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
package org.wso2.carbon.identity.event.event;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;

public class EventTest {

    Event event;
    Event eventOverload;
    Map<String,Object> mapForOverload;

    @BeforeMethod
    public void setUp(){

        setEvents();
    }

    private void setEvents() {

        event = new Event("name");
        mapForOverload =new HashMap();
        mapForOverload.put("key", "value");
        eventOverload = new Event("eventName", mapForOverload);
    }

    @Test
    public void testGetEventName(){

        Assert.assertEquals(event.getEventName(), "name");
    }

   @Test
    public void testEvent(){

        Assert.assertEquals(eventOverload.getEventName(), "eventName");
        Assert.assertEquals(eventOverload.getEventProperties(), mapForOverload);
   }

   @Test
    public void testAddEventProperty(){

        event.addEventProperty("key", "value");
        Map<String,Object> map =new HashMap();
        map.put("key", "value");
        Assert.assertEquals(event.getEventProperties(), map);
   }
}