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

package org.wso2.carbon.identity.extension.mgt.model;

import java.util.Objects;

/**
 * Key to uniquely identify an extension.
 */
public class ExtensionDataKey {

    private final String extensionType;

    private final String extensionId;

    public ExtensionDataKey(String extensionType, String extensionId) {

        this.extensionType = extensionType;
        this.extensionId = extensionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtensionDataKey)) {
            return false;
        }
        ExtensionDataKey that = (ExtensionDataKey) o;
        return Objects.equals(extensionType, that.extensionType) &&
                Objects.equals(extensionId, that.extensionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extensionType, extensionId);
    }
}
