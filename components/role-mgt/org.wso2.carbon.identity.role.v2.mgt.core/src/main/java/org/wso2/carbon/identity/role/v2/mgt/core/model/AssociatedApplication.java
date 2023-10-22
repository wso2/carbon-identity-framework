/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.model;

/**
 * Represents the Associated Application.
 */
public class AssociatedApplication {

    private String id;
    private String name;

    public AssociatedApplication(String id, String name) {

        this.id = id;
        this.name = name;
    }

    /**
     * Get associated application id.
     *
     * @return Application id.
     */
    public String getId() {

        return id;
    }

    /**
     * Set associated application id.
     *
     * @param id Application id
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get associated application name.
     *
     * @return Application name.
     */
    public String getName() {

        return name;
    }

    /**
     * Set associated application name.
     *
     * @param name Application name
     */
    public void setName(String name) {

        this.name = name;
    }
}
