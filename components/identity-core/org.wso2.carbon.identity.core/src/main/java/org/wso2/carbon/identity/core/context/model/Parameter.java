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

package org.wso2.carbon.identity.core.context.model;

/**
 * This class models the Parameter.
 * Parameter is the entity that represents additional parameters sent in the request.
 */
public class Parameter {

    private final String name;
    private final String[] value;

    public Parameter(String name, String[] value) {

        this.name = name;
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public String[] getValue() {

        return value;
    }
}
