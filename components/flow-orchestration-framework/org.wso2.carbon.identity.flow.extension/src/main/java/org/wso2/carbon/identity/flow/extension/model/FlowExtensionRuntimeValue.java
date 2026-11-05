/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A runtime value of the flow, as sent to the Flow Extension action. The value has the form kept for its data type,
 * or is a JWE compact string when the runtime value is exposed encrypted.
 */
public class FlowExtensionRuntimeValue {

    private final String name;
    private final String dataType;
    private final Object value;

    public FlowExtensionRuntimeValue(String name, String dataType, Object value) {

        this.name = name;
        this.dataType = dataType;
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public String getDataType() {

        return dataType;
    }

    /**
     * NON_NULL overrides the ObjectMapper-level NON_EMPTY so that an empty text or array value is sent rather than
     * omitted.
     *
     * @return The value.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getValue() {

        return value;
    }
}
