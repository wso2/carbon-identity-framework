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

import java.io.InputStream;

/**
 * A model class representing resource file.
 */
public class ResourceFile {

    private String path;
    @Deprecated
    private String value;
    private String id;
    private String name;
    private InputStream inputStream;

    public ResourceFile() {

    }

    public ResourceFile(String id) {

        this.id = id;
    }

    public ResourceFile(String id, String value, String name) {

        this.id = id;
        this.path = value;
        this.name = name;
    }

    public ResourceFile(String fileId, String fileName) {

        this.id = fileId;
        this.name = fileName;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String value) {

        this.path = value;
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

    @Deprecated
    public String getValue() {

        return value;
    }

    @Deprecated
    public void setValue(String value) {

        this.value = value;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
