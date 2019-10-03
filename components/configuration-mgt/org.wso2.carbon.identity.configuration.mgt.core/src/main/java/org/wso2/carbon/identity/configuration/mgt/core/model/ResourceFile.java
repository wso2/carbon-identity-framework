/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.model;

/**
 * A model class representing resource file.
 */
public class ResourceFile {

    private String value;
    private String id;
    private String name;

    public ResourceFile() {

    }

    public ResourceFile(String value) {

        this.value = value;
    }

    public ResourceFile(String id, String value, String name) {

        this.id = id;
        this.value = value;
        this.name = name;
    }


    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
