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

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link FlowExtensionHeader}.
 */
public class FlowExtensionHeaderTest {

    /**
     * Normalizing the name keeps the header's form: an encrypted header is still sent as its encrypted string, and
     * another as its array of values.
     */
    @Test
    public void testNormalizedNameKeepsTheForm() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        FlowExtensionHeader encrypted = new FlowExtensionHeader("X-Device", "jwe").withNormalizedName();
        assertEquals(encrypted.getName(), "x-device");
        assertEquals(encrypted.getSentValue(), "jwe");
        assertEquals(mapper.writeValueAsString(encrypted), "{\"name\":\"x-device\",\"value\":\"jwe\"}");

        FlowExtensionHeader plain =
                new FlowExtensionHeader("X-Device", new String[]{"mobile", "tablet"}).withNormalizedName();
        assertEquals(plain.getName(), "x-device");
        assertEquals(mapper.writeValueAsString(plain), "{\"name\":\"x-device\",\"value\":[\"mobile\",\"tablet\"]}");
    }
}
