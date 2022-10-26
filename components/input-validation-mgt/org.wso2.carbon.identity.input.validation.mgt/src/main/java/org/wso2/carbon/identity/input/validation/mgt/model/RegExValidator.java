/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.model;

/**
 * Configuration object to regex validator.
 */
public class RegExValidator {

    private String jsRegExPattern;
    private String javaRegExPattern;

    /**
     * Method to set JavaScript regex pattern.
     *
     * @param jsRegExPattern    JavaScript pattern.
     */
    public void setJsRegExPattern(String jsRegExPattern) {

        this.jsRegExPattern = jsRegExPattern;
    }

    /**
     * Method to set Java regex pattern.
     *
     * @param javaRegExPattern Java regex pattern.
     */
    public void setJavaRegExPattern(String javaRegExPattern) {

        this.javaRegExPattern = javaRegExPattern;
    }

    /**
     * Method to get JavaScript regex pattern.
     *
     * @return  JavaScript regex pattern.
     */
    public String getJsRegExPattern() {

        return this.jsRegExPattern;
    }

    /**
     * Method to get Java regex pattern.
     *
     * @return  Java regex pattern.
     */
    public String getJavaRegExPattern() {

        return this.javaRegExPattern;
    }
}
