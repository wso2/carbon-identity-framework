/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt.model;

/**
 * This is the function library entity object class.
 */
public class FunctionLibrary {

    private String functionLibraryName;
    private String description;
    private String content;

    /**
     * Get function library name.
     *
     * @return Function library name
     */
    public String getFunctionLibraryName() {

        return functionLibraryName;
    }

    /**
     * Set function library name.
     *
     * @param functionLibraryName Function library name
     */
    public void setFunctionLibraryName(String functionLibraryName) {

        this.functionLibraryName = functionLibraryName;
    }

    /**
     * Get function library description.
     *
     * @return Function library description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set function library description.
     *
     * @param description Function library description
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get function library script.
     *
     * @return Content
     */
    public String getFunctionLibraryScript() {

        return content;
    }

    /**
     * Set function library script.
     *
     * @param content Content
     */
    public void setFunctionLibraryScript(String content) {

        this.content = content;
    }

}


