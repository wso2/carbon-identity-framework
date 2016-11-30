/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.common.base.handler;

import org.wso2.carbon.identity.common.base.message.MessageContext;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for sorting message handler collection.
 */
public class MessageHandlerComparator implements Comparator<MessageHandler>, Serializable {

    private static final long serialVersionUID = -4006101351105308862L;

    private MessageContext messageContext = null;

    public MessageHandlerComparator(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public MessageHandlerComparator() {
    }

    @Override
    public int compare(MessageHandler o1, MessageHandler o2) {

        if (o1.getPriority(messageContext) > o2.getPriority(messageContext)) {
            return 1;
        } else if (o1.getPriority(messageContext) == o2.getPriority(messageContext)) {
            return 0;
        } else {
            return -1;
        }
    }


    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {

        // TODO: Remove this if this class should be really serializable.
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {

        // TODO: Remove this if this class should be really serializable.
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
