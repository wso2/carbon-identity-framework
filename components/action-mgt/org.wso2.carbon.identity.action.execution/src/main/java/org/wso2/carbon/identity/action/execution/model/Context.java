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

package org.wso2.carbon.identity.action.execution.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

import static org.wso2.carbon.identity.action.execution.impl.InvocationSuccessResponseContextFactory.getInvocationSuccessResponseContextClass;

public interface Context {

    public static final ActionType ACTION_TYPE = null;

    public static ActionType getActionType() {
        return ACTION_TYPE;
    }

    public static class DefaultContext implements Context {
    }

    public static class ContextDeserializer extends StdDeserializer<Context> {

        private final ActionType actionType;

        public ContextDeserializer(ActionType actionType) {

            super(Context.class);
            this.actionType = actionType;
        }

        @Override
        public Context deserialize(JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
                throws IOException {

            JsonNode node = p.getCodec().readTree(p);
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            return mapper.treeToValue(node, getInvocationSuccessResponseContextClass(actionType));
        }
    }
}
