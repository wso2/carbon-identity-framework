/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import java.io.Serializable;

/**
 * Authentication step or decision point node.
 */
public interface AuthGraphNode extends Serializable {

    /**
     * Name is for identification purpose and logging purposes only.
     * Should not be used to make any decision as it may not be unique.
     * 
     * @return
     */
    String getName();

    /**
     * Parent is the immediate upper level node of the AuthGraphNode.
     * This method can be called to get Parent of a particular node.
     *
     * @return
     */
    AuthGraphNode getParent();

    /**
     * This method should be used to assign a value to a instance variable
     * which refers to the parent of that particular AuthGraphNode object instance
     *
     * @param parentNode
     */
    void setParent(AuthGraphNode parentNode);
}
