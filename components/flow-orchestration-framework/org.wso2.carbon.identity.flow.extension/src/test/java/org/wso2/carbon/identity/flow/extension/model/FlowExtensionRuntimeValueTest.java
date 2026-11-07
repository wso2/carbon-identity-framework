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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link FlowExtensionRuntimeValue}.
 */
public class FlowExtensionRuntimeValueTest {

    /**
     * The action request mapper leaves out empty values, but an empty runtime value is still sent, so the extension
     * can tell it from one that is not exposed.
     */
    @Test
    public void testEmptyValuesAreSerialized() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("plan", "STRING", "")),
                "{\"name\":\"plan\",\"dataType\":\"STRING\",\"value\":\"\"}");
        assertEquals(mapper.writeValueAsString(
                        new FlowExtensionRuntimeValue("tags", "STRING_ARRAY", Collections.emptyList())),
                "{\"name\":\"tags\",\"dataType\":\"STRING_ARRAY\",\"value\":[]}");
    }

    /**
     * A number is sent without an exponent, and a decimal keeps its decimal point, whether it is a value or an array
     * entry.
     */
    @Test
    public void testNumbersAreSerializedAsTheyRead() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("score", "DECIMAL", 10.0)),
                "{\"name\":\"score\",\"dataType\":\"DECIMAL\",\"value\":10.0}");
        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("score", "DECIMAL", 0.0000001)),
                "{\"name\":\"score\",\"dataType\":\"DECIMAL\",\"value\":0.0000001}");
        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("attempts", "INTEGER", 3L)),
                "{\"name\":\"attempts\",\"dataType\":\"INTEGER\",\"value\":3}");
        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("scores", "NUMBER_ARRAY",
                        Arrays.asList(10.0, 2.5, 999999999999999.0))),
                "{\"name\":\"scores\",\"dataType\":\"NUMBER_ARRAY\",\"value\":[10.0,2.5,999999999999999.0]}");
        assertEquals(mapper.writeValueAsString(new FlowExtensionRuntimeValue("plan", "STRING", "gold")),
                "{\"name\":\"plan\",\"dataType\":\"STRING\",\"value\":\"gold\"}");
    }
}
