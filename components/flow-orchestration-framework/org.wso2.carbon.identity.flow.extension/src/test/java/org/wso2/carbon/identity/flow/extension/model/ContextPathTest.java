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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ContextPath}.
 */
public class ContextPathTest {

    @Test
    public void testGettersCarryConstructorValues() {

        ContextPath encrypted = new ContextPath("/user/credentials/password", true);
        assertEquals(encrypted.getPath(), "/user/credentials/password");
        assertTrue(encrypted.isEncrypted());

        ContextPath plain = new ContextPath("/user/id", false);
        assertEquals(plain.getPath(), "/user/id");
        assertFalse(plain.isEncrypted());
    }

    @Test
    public void testTwoArgumentConstructorHasNoFlowType() {

        assertNull(new ContextPath("/user/id", false).getFlowType());
        assertEquals(new ContextPath("/runtimeValues[name=plan]", true, "REGISTRATION").getFlowType(),
                "REGISTRATION");
    }

    /**
     * An entry without a flow type is stored as before, so stored access configs read back unchanged.
     */
    @Test
    public void testJsonRoundTripWithoutFlowType() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(new ContextPath("/user/id", true));
        assertFalse(json.contains("flowType"), json);

        ContextPath read = mapper.readValue(json, ContextPath.class);
        assertEquals(read.getPath(), "/user/id");
        assertTrue(read.isEncrypted());
        assertNull(read.getFlowType());

        ContextPath legacy = mapper.readValue("{\"path\":\"/user/id\",\"encrypted\":false}", ContextPath.class);
        assertEquals(legacy.getPath(), "/user/id");
        assertNull(legacy.getFlowType());
    }

    @Test
    public void testJsonRoundTripWithFlowType() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(new ContextPath("/runtimeValues[name=plan]", false, "REGISTRATION"));
        assertTrue(json.contains("\"flowType\":\"REGISTRATION\""), json);

        ContextPath read = mapper.readValue(json, ContextPath.class);
        assertEquals(read.getPath(), "/runtimeValues[name=plan]");
        assertFalse(read.isEncrypted());
        assertEquals(read.getFlowType(), "REGISTRATION");
    }
}
