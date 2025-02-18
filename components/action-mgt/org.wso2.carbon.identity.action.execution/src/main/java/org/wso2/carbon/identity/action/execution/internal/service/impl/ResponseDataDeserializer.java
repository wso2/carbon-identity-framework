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

package org.wso2.carbon.identity.action.execution.internal.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.ResponseData;

import java.io.IOException;

/**
 * Dynamic deserializer for the ResponseData class.
 */
public class ResponseDataDeserializer extends JsonDeserializer<ResponseData> {

    public static final String ACTION_TYPE_ATTR_NAME = "actionType";

    @Override
    public ResponseData deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        ActionType actionType = (ActionType) ctxt.getAttribute(ACTION_TYPE_ATTR_NAME);
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        return mapper.treeToValue(node,
                ActionInvocationResponseClassFactory.getInvocationSuccessResponseDataClass(actionType));
    }
}
