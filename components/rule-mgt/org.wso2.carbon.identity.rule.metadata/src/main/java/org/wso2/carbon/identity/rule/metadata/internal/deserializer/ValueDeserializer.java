/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.internal.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.io.IOException;

/**
 * Deserializer for Value, which reads the value meta as the subclass its input type and value type call for.
 */
public class ValueDeserializer extends JsonDeserializer<Value> {

    @Override
    public Value deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {

        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        Value.InputType inputType = readEnum(jsonParser, node, "inputType", Value.InputType.class);
        Value.ValueType valueType = readEnum(jsonParser, node, "valueType", Value.ValueType.class);

        if (inputType == Value.InputType.OPTIONS && valueType == Value.ValueType.REFERENCE) {
            return codec.treeToValue(node, OptionsReferenceValue.class);
        }
        if (inputType == Value.InputType.OPTIONS) {
            return codec.treeToValue(node, OptionsInputValue.class);
        }
        return codec.treeToValue(node, InputValue.class);
    }

    private static <T extends Enum<T>> T readEnum(JsonParser jsonParser, JsonNode node, String property,
                                                  Class<T> type) throws JsonMappingException {

        JsonNode text = node.get(property);
        if (text == null || !text.isTextual()) {
            throw JsonMappingException.from(jsonParser, "A value needs a " + property + ".");
        }
        try {
            return Enum.valueOf(type, text.asText().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(jsonParser, "Unsupported " + property + ": " + text.asText(), e);
        }
    }
}
