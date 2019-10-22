/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended property model for IDP REST API.
 */
public class ExtendedProperty extends Property {

    private String regex;

    private List<String> options;

    private List<ExtendedProperty> subProperties;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<ExtendedProperty> getSubProperties() {

        return subProperties;
    }

    public void setSubProperties(List<ExtendedProperty> subProperties) {

        this.subProperties = subProperties;
    }

    public void addSubProperty(ExtendedProperty property) {

        if (CollectionUtils.isNotEmpty(this.subProperties)) {
            this.subProperties.add(property);
        } else {
            this.subProperties = new ArrayList<>();
            this.subProperties.add(property);
        }
    }

    public void addOption(String option) {

        if (CollectionUtils.isNotEmpty(this.options)) {
            this.options.add(option);
        } else {
            this.options = new ArrayList<>();
            this.options.add(option);
        }
    }
}
