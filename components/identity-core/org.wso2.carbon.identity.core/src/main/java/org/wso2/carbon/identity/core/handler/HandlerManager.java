/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.identity.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.bean.context.MessageContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;

/**
 * HandlerManager class can be used to get the correct handlers just before execute it either
 * list or first priority one
 */
public class HandlerManager {

    private static Log log = LogFactory.getLog(AbstractIdentityMessageHandler.class);
    private static HandlerManager handlerManager = new HandlerManager();

    private HandlerManager(){

    }

    public static HandlerManager getInstance(){
        if(log.isDebugEnabled()){
            log.debug("Created singleton instance for " + HandlerManager.class.getName());
        }
        return HandlerManager.handlerManager ;
    }

    /**
     * Get the first priority handler after sort and filter the enabled handlers.
     *
     * @param identityHandlers
     * @param isEnableHandlersOnly
     * @return IdentityHandler
     */
    public <T extends IdentityHandler> T getFirstPriorityHandler(List<T> identityHandlers,
                                                   boolean isEnableHandlersOnly) {
        if(log.isDebugEnabled()){
            log.debug("Get first priority handler for the given handler list.");
        }
        if(identityHandlers == null || identityHandlers.isEmpty()){
            return null ;
        }
        T identityHandler = null;

        sort(identityHandlers, new HandlerComparator());

        for (T identityHandlerTmp : identityHandlers) {
            if (isEnableHandlersOnly) {
                if (identityHandlerTmp.isEnabled()) {
                    identityHandler = identityHandlerTmp;
                    break;
                }
            } else {
                identityHandler = identityHandlerTmp;
                break;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Get first priority handler : " + identityHandler.getName() + "(" +
                    identityHandler.getClass().getName() + ")");
        }
        return identityHandler;
    }

    /**
     * Sort and filter enabled handlers.
     *
     * @param identityHandlers
     * @param isEnableHandlersOnly
     * @return List<IdentityHandler>
     */
    public <T extends IdentityHandler> List<T> sortHandlers(List<T> identityHandlers,
                                              boolean isEnableHandlersOnly) {
        if(log.isDebugEnabled()){
            log.debug("Sort the handler list.");
        }
        if(identityHandlers == null || identityHandlers.isEmpty()){
            return new ArrayList<T>()  ;
        }
        List<T> identityHandlersList = identityHandlers;
        sort(identityHandlersList, new HandlerComparator());
        if (isEnableHandlersOnly) {
            identityHandlersList = new ArrayList<>();
            for (IdentityHandler identityHandler : identityHandlers) {
                if (identityHandler.isEnabled()) {
                    identityHandlersList.add((T)identityHandler);
                }
            }
        }
        return identityHandlersList;
    }

    /**
     * Get the first priority handler after sort and filter the enabled handlers.
     *
     * @param identityMessageHandlers
     * @param isEnableHandlersOnly
     * @param messageContext
     * @return IdentityMessageHandler
     */
    public <T1 extends IdentityMessageHandler, T2 extends MessageContext> T1
            getFirstPriorityHandler(List<T1> identityMessageHandlers, boolean isEnableHandlersOnly, T2 messageContext) {
        if(log.isDebugEnabled()){
            log.debug("Get first priority handler for the given handler list and the context");
        }
        if(identityMessageHandlers == null || identityMessageHandlers.isEmpty()){
            return null ;
        }
        T1 identityMessageHandler = null;

        sort(identityMessageHandlers, new MessageHandlerComparator(messageContext));

        for (T1 identityHandlerTmp : identityMessageHandlers) {
            if (isEnableHandlersOnly) {
                if (identityHandlerTmp.isEnabled(messageContext)) {
                    if (identityHandlerTmp.canHandle(messageContext)) {
                        identityMessageHandler = identityHandlerTmp;
                        break;
                    }
                }
            } else {
                if (identityHandlerTmp.canHandle(messageContext)) {
                    identityMessageHandler = identityHandlerTmp;
                    break;
                }
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Get first priority handler : " + identityMessageHandler.getName() + "(" +
                    identityMessageHandler.getClass().getName() + ")");
        }
        return identityMessageHandler;
    }


    /**
     * Sort and filter enabled handlers.
     *
     * @param identityMessageHandlers
     * @param isEnableHandlersOnly
     * @param messageContext
     * @return List<IdentityMessageHandler>
     */
    public <T1 extends IdentityMessageHandler, T2 extends MessageContext> List<T1> sortHandlers
                        (List<T1> identityMessageHandlers, boolean isEnableHandlersOnly, T2 messageContext) {

        if(log.isDebugEnabled()){
            log.debug("Sort the handler list with the context.");
        }
        if(identityMessageHandlers == null || identityMessageHandlers.isEmpty()){
            return new ArrayList<T1>()  ;
        }
        List<T1> identityMessageHandlerList = identityMessageHandlers;
        sort(identityMessageHandlerList, new MessageHandlerComparator(messageContext));
        if (isEnableHandlersOnly) {
            identityMessageHandlerList = new ArrayList<>();
            for (T1 identityMessageHandler : identityMessageHandlers) {
                if (identityMessageHandler.isEnabled(messageContext)) {
                    identityMessageHandlerList.add(identityMessageHandler);
                }
            }
        }
        return identityMessageHandlerList;
    }


}
