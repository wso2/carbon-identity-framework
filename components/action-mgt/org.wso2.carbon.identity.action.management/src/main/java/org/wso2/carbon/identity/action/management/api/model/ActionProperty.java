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

package org.wso2.carbon.identity.action.management.api.model;

/**
 * This class models the Action Property
 */
public abstract class ActionProperty {

    /**
     * Property type.
     */
    public enum Type {
        PRIMITIVE,
        OBJECT
    }

    private final ActionProperty.Type type;

    protected ActionProperty(Type type) {

        this.type = type;
    }

    public abstract Object getValue();

    public ActionProperty.Type getType() {

        return type;
    }

    public boolean isPrimitive() {

        return type == Type.PRIMITIVE;
    }

    public boolean isObject() {

        return type == Type.OBJECT;
    }
}
