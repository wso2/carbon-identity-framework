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

package org.wso2.carbon.identity.rule.evaluation.core;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.resolver.SymbolicValueResolver;
import org.wso2.carbon.identity.rule.evaluation.api.resolver.SymbolicValueResolverRegistry;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for SymbolicValueResolverRegistry.
 */
public class SymbolicValueResolverRegistryTest {

    private static final String FIELD = "osVersionTestField";

    @Test
    public void testRegisterAndGetResolver() {

        SymbolicValueResolverRegistry registry = SymbolicValueResolverRegistry.getInstance();
        SymbolicValueResolver resolver = symbolicValue -> new Value(Value.Type.NUMBER, "15");

        registry.register(FIELD, resolver);
        assertSame(registry.getResolver(FIELD), resolver);

        registry.deregister(FIELD);
        assertNull(registry.getResolver(FIELD));
    }

    @Test
    public void testGetResolverForUnregisteredField() {

        assertNull(SymbolicValueResolverRegistry.getInstance().getResolver("noSuchField"));
    }
}
