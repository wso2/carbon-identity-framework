/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;

import javax.ws.rs.core.Response;

public abstract class ResourceApiService {

    public abstract Response resourceResourceTypePost(String resourceType, ResourceAddDTO resource);

    public abstract Response resourceResourceTypePut(String resourceType, ResourceAddDTO resource);

    public abstract Response resourceResourceTypeResourceNameAttributeKeyDelete(String resourceName,
                                                                                String resourceType,
                                                                                String attributeKey);

    public abstract Response resourceResourceTypeResourceNameAttributeKeyGet(String resourceName, String resourceType
            , String attributeKey);

    public abstract Response resourceResourceTypeResourceNameDelete(String resourceName, String resourceType);

    public abstract Response resourceResourceTypeResourceNameGet(String resourceName, String resourceType);

    public abstract Response resourceResourceTypeResourceNamePost(String resourceName, String resourceType,
                                                                  AttributeDTO attribute);

    public abstract Response resourceResourceTypeResourceNamePut(String resourceName, String resourceType,
                                                                 AttributeDTO attribute);
}

