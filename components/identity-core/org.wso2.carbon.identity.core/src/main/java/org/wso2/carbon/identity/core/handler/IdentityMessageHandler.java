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

import org.wso2.carbon.identity.core.bean.context.MessageContext;

/**
 * This interface needs to be implemented by any identity handler.
 */
public interface IdentityMessageHandler {

    /**
     * Initializes the handler
     *
     */
    public void init(InitConfig initConfig);

    /**
     * Name of the handler.
     *
     * @return Name of the handler
     */
    public String getName();

    /**
     * Tells if the handler is enabled or not. Based on the result {@Code canHandle()} and {@code handle()} may be
     * called.
     *
     * @param messageContext The runtime message context
     */
    public boolean isEnabled(MessageContext messageContext);

    /**
     * Used to sort the set of handlers
     *
     * @param messageContext The runtime message context
     * @return The priority value of the handler
     */
    public int getPriority(MessageContext messageContext);

    /**
     * Tells if this request can be handled by this handler
     *
     * @param messageContext The runtime message context
     * @return {@code true} if the message can be handled by this handler
     */
    public boolean canHandle(MessageContext messageContext);
}
