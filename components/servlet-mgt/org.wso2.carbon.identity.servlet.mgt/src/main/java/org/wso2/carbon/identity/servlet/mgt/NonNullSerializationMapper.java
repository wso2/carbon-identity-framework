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

package org.wso2.carbon.identity.servlet.mgt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class extends ObjectMapper to set the serialization inclusion to NON_NULL.
 */
public class NonNullSerializationMapper extends ObjectMapper {

    // Add serial version UID to the class.
    private static final long serialVersionUID = 1L;

    public NonNullSerializationMapper() {

        super();
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
