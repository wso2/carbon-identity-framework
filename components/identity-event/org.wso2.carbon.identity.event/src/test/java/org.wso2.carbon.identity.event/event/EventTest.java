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
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;

public class EventTest {

    @Test
    public void testGetEventName(){

        Event event = new Event("name");
        Assert.assertEquals(event.getEventName(),"name");
    }

    @Test
    public void testGetEventProperties(){

       Event event = new Event("name");
        event.addEventProperty("value","value");

        Map<String,Object> map =new HashMap<String,Object>();
        map.put("value","value");

        Assert.assertEquals(event.getEventProperties(),map);
    }

   @Test
    public void testEvent(){

        Event event = new Event("name");
        Assert.assertEquals(event.getEventName(),"name");
   }

   @Test
    public void testAddEventProperty(){
        Event event = new Event("name");
        event.addEventProperty("key","value");

        Map<String,Object> map =new HashMap<String,Object>();
        map.put("key","value");

        Assert.assertEquals(event.getEventProperties(),map);

   }


}