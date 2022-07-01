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
package org.wso2.carbon.identity.event.services;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.internal.IdentityEventServiceComponent;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

public class IdentityEventServiceImplTest extends IdentityBaseTest {

    @Mock
    AbstractEventHandler abstractEventHandler;

    @Test
    public void testConstructor(){

        IdentityEventServiceImpl identityEventService = new IdentityEventServiceImpl(Collections.EMPTY_LIST,1);
        Assert.assertNotNull(identityEventService);
    }

    @Test
    public void testHandleEvent() throws IdentityEventException {

        Event event = new Event("eventName");
        event.addEventProperty("value","value");

        abstractEventHandler = mock(AbstractEventHandler.class);
        doReturn(true).when(abstractEventHandler).canHandle(any(MessageContext.class));
        doReturn(true).when(abstractEventHandler).isAssociationAsync(anyString());

        List list = new ArrayList();
        list.add(abstractEventHandler);
        IdentityEventServiceComponent.eventHandlerList = list;

        List abstractEventHandlerList = new ArrayList();
        abstractEventHandlerList.add(abstractEventHandler);
        IdentityEventService identityEventService = new IdentityEventServiceImpl(abstractEventHandlerList,1);
        identityEventService.handleEvent(event);

        Mockito.verify(abstractEventHandler).canHandle(any(MessageContext.class));
    }
}
