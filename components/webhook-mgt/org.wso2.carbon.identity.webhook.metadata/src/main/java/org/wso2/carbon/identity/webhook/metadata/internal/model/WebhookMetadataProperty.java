/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.webhook.metadata.internal.model;

/**
 * This class models the Webhook Metadata Property.
 * It can be used to represent both primitive and object types.
 */
public class WebhookMetadataProperty {

    private final WebhookMetadataProperty.Type type;
    private final Object value;

    /**
     * Property type.
     */
    public enum Type {
        PRIMITIVE,
        OBJECT
    }

    private WebhookMetadataProperty(WebhookMetadataProperty.Type type, Object value) {

        this.type = type;
        this.value = value;
    }

    public Object getValue() {

        return value;
    }

    public WebhookMetadataProperty.Type getType() {

        return type;
    }

    public boolean isPrimitive() {

        return type == Type.PRIMITIVE;
    }

    public boolean isObject() {

        return type == Type.OBJECT;
    }

    /**
     * This builder creates the WebhookMetadataProperty object to be used in the Service Layer.
     */
    public static class Builder {

        private Type type;
        private Object value;

        public Builder(String value) {

            this.type = Type.PRIMITIVE;
            this.value = value;
        }

        public Builder(Object value) {

            this.type = Type.OBJECT;
            this.value = value;
        }

        public WebhookMetadataProperty build() {

            return new WebhookMetadataProperty(type, value);
        }
    }
}
