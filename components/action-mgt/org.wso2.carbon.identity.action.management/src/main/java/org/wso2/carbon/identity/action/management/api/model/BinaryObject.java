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

package org.wso2.carbon.identity.action.management.api.model;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtRuntimeException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class models the object typed action property values
 */
public class BinaryObject {

    private final InputStream value;

    public BinaryObject(InputStream value) {

        this.value = value;
    }

    public InputStream getInputStream() {

        return value;
    }

    public int getLength() {

        try {
            return value.available();
        } catch (IOException e) {
            throw new ActionMgtRuntimeException("Error occurred while reading the input stream", e);
        }
    }
}
