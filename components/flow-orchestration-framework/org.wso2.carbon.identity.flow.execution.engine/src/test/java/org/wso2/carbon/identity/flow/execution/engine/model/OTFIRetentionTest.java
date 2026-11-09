/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.RETAIN_OTFI;

/**
 * Unit tests for {@link OTFIRetention}.
 */
public class OTFIRetentionTest {

    private static final String EXECUTOR_NAME = "DeviceRegistrationExecutor";

    @Test
    public void testFromContextReadsInstance() {

        OTFIRetention retention = new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build();
        FlowExecutionContext context = new FlowExecutionContext();
        context.setProperty(RETAIN_OTFI, retention);

        assertSame(OTFIRetention.fromContext(context), retention);
    }

    @Test
    public void testFromContextReadsMapAfterJsonRoundTrip() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> properties = new HashMap<>();
        properties.put(RETAIN_OTFI, new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build());

        String json = objectMapper.writeValueAsString(properties);
        assertEquals(json, "{\"RETAIN_OTFI\":{\"executorName\":\"" + EXECUTOR_NAME + "\"}}");
        Map<String, Object> restored = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        assertTrue(restored.get(RETAIN_OTFI) instanceof Map);
        FlowExecutionContext context = new FlowExecutionContext();
        context.addProperties(restored);

        assertEquals(OTFIRetention.fromContext(context).getExecutorName(), EXECUTOR_NAME);
    }

    @DataProvider
    public Object[][] invalidRetentions() {

        return new Object[][]{{null}, {"true"}, {Boolean.TRUE}, {Collections.emptyMap()},
                {Collections.singletonMap("executorName", " ")}, {Collections.singletonMap("executorName", 1)}};
    }

    @Test(dataProvider = "invalidRetentions")
    public void testFromContextIgnoresInvalidValues(Object value) {

        FlowExecutionContext context = new FlowExecutionContext();
        if (value != null) {
            context.setProperty(RETAIN_OTFI, value);
        }

        assertNull(OTFIRetention.fromContext(context));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildWithoutExecutorNameFails() {

        new OTFIRetention.Builder().executorName(" ").build();
    }
}
