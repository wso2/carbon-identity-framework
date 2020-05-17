/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.cors.mgt.core.internal.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_NAME;

/**
 * Converts a list of CORSOrigins to a ConfigurationManagement ResourceAdd object.
 */
public class CORSOriginToResourceAdd implements CheckedFunction<List<CORSOrigin>, ResourceAdd> {

    private static final Log log = LogFactory.getLog(CORSOriginToResourceAdd.class);

    @Override
    public ResourceAdd apply(List<CORSOrigin> corsOrigins) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(CORS_ORIGIN_RESOURCE_NAME);

        List<Attribute> attributes = new ArrayList<>();
        for (CORSOrigin corsOrigin : corsOrigins) {
            String corsOriginString = mapper.writeValueAsString(corsOrigin);
            addAttribute(attributes, String.valueOf(corsOrigin.hashCode()), corsOriginString);
        }
        resourceAdd.setAttributes(attributes);
        return resourceAdd;
    }

    private void addAttribute(List<Attribute> attributeList, String key, String value) {

        if (value != null) {
            Attribute attribute = new Attribute();
            attribute.setKey(key);
            attribute.setValue(value);
            attributeList.add(attribute);
        }
    }
}
