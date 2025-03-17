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
 * This class models the Action Property model to be used in Service Layer
 * {@link org.wso2.carbon.identity.action.management.api.service.ActionConverter}
 *
 * - Primitive Type: Represents simple data types such as String, boolean, and int.
 *  These values are stored directly and do not represent complex objects.
 * - Object Type: Represents complex types such as action property objects.
 * {@link org.wso2.carbon.identity.certificate.management.model.Certificate}
 */
public class ActionPropertyForService extends ActionProperty {

    private final Object value;

    public ActionPropertyForService(String value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForService(boolean value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForService(int value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForService(Object value) {

        super(Type.OBJECT);
        this.value = value;
    }

    @Override
    public Object getValue() {

        return value;
    }
}
