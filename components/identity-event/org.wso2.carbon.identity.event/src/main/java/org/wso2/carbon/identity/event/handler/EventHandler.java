///*
// * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.wso2.carbon.identity.event.handler;
//
//import org.wso2.carbon.identity.core.handler.IdentityHandler;
//import org.wso2.carbon.identity.event.EventMgtException;
//import org.wso2.carbon.identity.event.event.Event;
//
//import java.util.Map;
//
//public interface EventHandler extends IdentityHandler {
//
//    boolean handleEvent(Event event) throws EventMgtException;
//
//    /**
//     * Initiate the EventHandler
//     */
//    void init() throws EventMgtException;
//
//    /**
//     * This method returns whether this EventHandler can handle the given type of event or not. ie Whether
//     * this handler is registered to the passed event or not.
//     *
//     * @param event Event which is to be executed
//     * @return True if the module can handle or registered to the event.
//     */
//    boolean isRegistered(Event event) throws EventMgtException;
//
//
//    String getModuleName();
//
//    boolean isAssociationAsync(String eventName) throws EventMgtException;
//
//    Map<String, String> getTenantConfigurations (int tenantId) throws EventMgtException;
//
//}
