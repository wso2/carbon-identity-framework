/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.model;

import java.io.Serializable;

/**
 * Represents the metadata of a attribute mapping.
 */
public class AttributeMapping implements Serializable {
    private String userStoreDomain;
    private String attributeName;

    public AttributeMapping(String userStoreDomain, String attributeName) {
        this.userStoreDomain = userStoreDomain.toUpperCase();
        this.attributeName = attributeName;
    }

    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
