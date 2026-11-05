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
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.internal.util.RequestFilter;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link FlowExtensionHeader}.
 */
public class FlowExtensionHeaderTest {

    /**
     * An encrypted header is sent as its encrypted string, and another as its array of values.
     */
    @Test
    public void testSentValueForm() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        FlowExtensionHeader encrypted = new FlowExtensionHeader("x-device", "jwe");
        assertEquals(encrypted.getSentValue(), "jwe");
        assertEquals(mapper.writeValueAsString(encrypted), "{\"name\":\"x-device\",\"value\":\"jwe\"}");

        FlowExtensionHeader plain = new FlowExtensionHeader("x-device", new String[]{"mobile", "tablet"});
        assertEquals(mapper.writeValueAsString(plain), "{\"name\":\"x-device\",\"value\":[\"mobile\",\"tablet\"]}");
    }

    /**
     * A name given in any case is kept in lower case, so the action request filter returns the header as it is and an
     * encrypted header is still sent as its encrypted string, never as an array.
     */
    @Test
    public void testEncryptedHeaderKeepsItsFormThroughRequestFilter() throws Exception {

        FlowExtensionHeader encrypted = new FlowExtensionHeader("X-Device", "jwe");
        assertEquals(encrypted.getName(), "x-device");

        ActionExecutorConfig config = mock(ActionExecutorConfig.class);
        when(config.isCaseInsensitiveHeaderFilteringEnabled()).thenReturn(false);
        when(config.getExcludedHeadersInActionRequestForActionType(ActionType.FLOW_EXTENSION))
                .thenReturn(Collections.emptySet());
        List<Header> filteredHeaders;
        try (MockedStatic<ActionExecutorConfig> executorConfig = mockStatic(ActionExecutorConfig.class)) {
            executorConfig.when(ActionExecutorConfig::getInstance).thenReturn(config);
            filteredHeaders = RequestFilter.getFilteredHeaders(Collections.singletonList(encrypted),
                    Collections.singletonList("X-Device"), ActionType.FLOW_EXTENSION);
        }

        assertEquals(filteredHeaders.size(), 1);
        assertSame(filteredHeaders.get(0), encrypted);
        assertEquals(new ObjectMapper().writeValueAsString(filteredHeaders.get(0)),
                "{\"name\":\"x-device\",\"value\":\"jwe\"}");
    }
}
