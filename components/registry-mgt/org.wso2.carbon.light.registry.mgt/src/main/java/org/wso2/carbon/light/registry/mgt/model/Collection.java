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
package org.wso2.carbon.light.registry.mgt.model;

import org.wso2.carbon.light.registry.mgt.LightRegistryException;

public interface Collection extends Resource {

    /**
     * Method to return the relative paths of the children of the collection
     *
     * @return the array of relative paths of the children
     * @throws LightRegistryException if the operation fails.
     */
    String[] getChildren() throws LightRegistryException;

    /**
     * Method to return the number of children.
     *
     * @return the number of children.
     * @throws LightRegistryException if the operation fails.
     */
    int getChildCount() throws LightRegistryException;

    /**
     * Method to set the relative paths of the children belonging to this collection. Relative paths
     * begin from the parent collection.
     *
     * @param paths the array of relative paths of the children
     *
     * @throws LightRegistryException if the operation fails.
     */
    void setChildren(String[] paths) throws LightRegistryException;

}
