/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.bean.context;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityRuntimeException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for {@link MessageContext}.
 */
public class MessageContextTest {

    /**
     * Concrete stand-in, since MessageContext is abstract.
     */
    private static class TestMessageContext extends MessageContext<String, String> {
    }

    private TestMessageContext messageContext;

    @BeforeMethod
    public void setUp() {

        messageContext = new TestMessageContext();
    }

    @Test
    public void testAddParameterRejectsAnExistingKey() {

        messageContext.addParameter("key", "first");

        assertThrows(IdentityRuntimeException.class, () -> messageContext.addParameter("key", "second"));
        assertEquals(messageContext.getParameter("key"), "first",
                "A rejected addParameter() should leave the existing value untouched.");
    }

    @Test
    public void testSetParameterReplacesAnExistingKey() {

        messageContext.setParameter("key", "first");
        assertEquals(messageContext.getParameter("key"), "first");

        // Unlike addParameter(), this must not throw on an existing key.
        messageContext.setParameter("key", "second");

        assertEquals(messageContext.getParameter("key"), "second");
        assertEquals(messageContext.getParameters().size(), 1,
                "Replacing a value should not add a second entry.");
    }

    @Test
    public void testSetParameterAddsANewKey() {

        messageContext.setParameter("key", "value");

        assertEquals(messageContext.getParameter("key"), "value");
        assertEquals(messageContext.getParameters().size(), 1);
    }
}
