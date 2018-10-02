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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import java.io.InputStream;

/**
 * Holds Stream for SP file configuration.
 */
public class SpFileStream {

    private InputStream fileStream;
    private String fileName;

    /**
     * @param fileStream
     * @param fileName
     */
    public SpFileStream(InputStream fileStream, String fileName) {

        this.fileStream = fileStream;
        this.fileName = fileName;
    }

    /**
     * @return
     */
    public InputStream getFileStream() {

        return fileStream;
    }

    /**
     * @param fileStream
     */
    public void setFileStream(InputStream fileStream) {

        this.fileStream = fileStream;
    }

    /**
     * @return
     */
    public String getFileName() {

        return fileName;
    }

    /**
     * @param fileName
     */
    public void setFileName(String fileName) {

        this.fileName = fileName;
    }
}
