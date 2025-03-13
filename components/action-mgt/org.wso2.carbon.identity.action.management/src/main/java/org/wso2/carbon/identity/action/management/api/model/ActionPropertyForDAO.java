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
 * This class models the Action Property model to be used in DAO Layer
 * {@link org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOImpl}
 *
 * - Primitive Type: Represents simple data types such as String, boolean, int and
 *   String references of any object values. These values are stored directly and
 *   do not represent complex objects.
 * - Object Type: Represents complex types such as properties having binary stream .
 *   These values are stored as instances of {@link BinaryObject} objects.
 */
public class ActionPropertyForDAO extends ActionProperty {

    private final Object value;

    public ActionPropertyForDAO(String value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForDAO(boolean value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForDAO(int value) {

        super(Type.PRIMITIVE);
        this.value = value;
    }

    public ActionPropertyForDAO(BinaryObject value) {

        super(Type.OBJECT);
        this.value = value;
    }

    @Override
    public Object getValue() {

        return value;
    }
}
