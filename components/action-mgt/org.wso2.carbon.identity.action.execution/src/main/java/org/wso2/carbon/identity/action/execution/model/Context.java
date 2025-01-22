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

/**
 * This interface defines the Context for Action Invocation Success Response.
 * Context is the class that is responsible for defining structure of the additional data coming from the
 * success invocation response received from the action execution.
 */
public interface Context {

    ActionType ACTION_TYPE = null;

    static ActionType getActionType() {
        return ACTION_TYPE;
    }

    /**
     * Default context implementation, which can be used when there are no extended Context class for the action type.
     */
    class DefaultContext implements Context {
    }

    /**
     * Dynamic deserializer for Context interface determined at runtime based on the action type.
     */
    class ContextDeserializer extends StdDeserializer<Context> {

        private static final long serialVersionUID = 6529685098267757690L;

        public static final String ACTION_TYPE_ATTR_NAME = "actionType";

        public ContextDeserializer() {

            super(Context.class);
        }

        @Override
        public Context deserialize(JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
                throws IOException {

            ActionType actionType = (ActionType) ctxt.getAttribute(ACTION_TYPE_ATTR_NAME);
            JsonNode node = p.getCodec().readTree(p);
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            return mapper.treeToValue(node, getInvocationSuccessResponseContextClass(actionType));
        }
    }
}
