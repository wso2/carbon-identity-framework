/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.mgt.model;

/**
 * Action Type Item.
 */
public class ActionType {

    /**
     * Action Type Enum.
     */
    public enum TypeEnum {

        PRE_ISSUE_ACCESS_TOKEN("PRE_ISSUE_ACCESS_TOKEN"),
        PRE_UPDATE_PASSWORD("PRE_UPDATE_PASSWORD"),
        PRE_UPDATE_PROFILE("PRE_UPDATE_PROFILE"),
        PRE_REGISTRATION("PRE_REGISTRATION"),
        POST_LOGIN("POST_LOGIN"),
        PRE_LOGIN("PRE_LOGIN");

        private final String value;

        TypeEnum(String v) {
            this.value = v;
        }

        public String value() {
            return value;
        }

        public static TypeEnum fromValue(String value) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private TypeEnum type;
    private String displayName;
    private String description;
    private Integer count = 0;
    private String self;

    public ActionType() {
    }

    public ActionType(ActionTypeBuilder actionTypeBuilder) {

        this.type = actionTypeBuilder.type;
        this.displayName = actionTypeBuilder.displayName;
        this.description = actionTypeBuilder.description;
        this.count = actionTypeBuilder.count;
        this.self = actionTypeBuilder.self;
    }

    public TypeEnum getType() {

        return type;
    }

    public void setType(TypeEnum type) {

        this.type = type;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Integer getCount() {

        return count;
    }

    public void setCount(Integer count) {

        this.count = count;
    }

    public String getSelf() {

        return self;
    }

    public void setSelf(String self) {

        this.self = self;
    }

    /**
     * Action Type Item Builder.
     */
    public static class ActionTypeBuilder {

        private TypeEnum type;
        private String displayName;
        private String description;
        private Integer count = 0;
        private String self;

        public ActionTypeBuilder() {
        }

        public ActionTypeBuilder type(TypeEnum type) {

            this.type = type;
            return this;
        }

        public ActionTypeBuilder displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        public ActionTypeBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ActionTypeBuilder count(Integer count) {

            this.count = count;
            return this;
        }

        public ActionTypeBuilder self(String self) {

            this.self = self;
            return this;
        }

        public ActionType build() {

            return new ActionType(this);
        }
    }
}
