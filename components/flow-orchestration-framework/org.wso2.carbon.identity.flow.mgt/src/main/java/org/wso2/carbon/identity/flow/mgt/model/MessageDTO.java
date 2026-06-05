/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;

/**
 * Model class to represent a message surfaced to the client as part of a flow execution step.
 * A message carries a {@link MessageType}, a human-readable message and an i18n key that the
 * client can use to render a localized message.
 */
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 6473859283746591023L;

    private MessageType type;
    private String message;
    private String i18nKey;

    public MessageDTO() {

    }

    private MessageDTO(Builder builder) {

        this.type = builder.type;
        this.message = builder.message;
        this.i18nKey = builder.i18nKey;
    }

    public MessageType getType() {

        return type;
    }

    public void setType(MessageType type) {

        this.type = type;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getI18nKey() {

        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
    }

    /**
     * Builder class to build {@link MessageDTO} objects.
     */
    public static class Builder {

        private MessageType type;
        private String message;
        private String i18nKey;

        public Builder type(MessageType type) {

            this.type = type;
            return this;
        }

        public Builder message(String message) {

            this.message = message;
            return this;
        }

        public Builder i18nKey(String i18nKey) {

            this.i18nKey = i18nKey;
            return this;
        }

        public MessageDTO build() {

            return new MessageDTO(this);
        }
    }

    /**
     * Type of a {@link MessageDTO} surfaced in a flow execution step.
     * <p>
     * The constant name is what gets serialized to the client (e.g. {@code "ERROR"}), so the
     * client uses it to decide how to render the message.
     */
    public enum MessageType {

        ERROR,
        WARNING,
        INFO
    }
}
