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
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

/**
 * Converts a CORSOrigin to a ConfigurationManagement Resource attribute.
 */
public class CORSOriginToAttribute implements CheckedFunction<CORSOrigin, Attribute> {

    private static final Log log = LogFactory.getLog(CORSOriginToAttribute.class);

    @Override
    public Attribute apply(CORSOrigin corsOrigin) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        Attribute attribute = new Attribute();
        attribute.setKey(String.valueOf(corsOrigin.hashCode()));
        String corsOriginString = mapper.writeValueAsString(corsOrigin);
        attribute.setValue(corsOriginString);
        return attribute;
    }
}
