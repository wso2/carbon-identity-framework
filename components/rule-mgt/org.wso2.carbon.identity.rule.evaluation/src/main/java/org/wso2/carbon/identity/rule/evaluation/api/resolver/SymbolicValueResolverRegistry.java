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

package org.wso2.carbon.identity.rule.evaluation.api.resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton registry for {@link SymbolicValueResolver} instances, keyed by field name.
 * Bundles register resolvers on OSGi bundle activation and deregister on deactivation.
 */
public class SymbolicValueResolverRegistry {

    private static final SymbolicValueResolverRegistry INSTANCE = new SymbolicValueResolverRegistry();
    private final Map<String, SymbolicValueResolver> resolvers = new ConcurrentHashMap<>();

    private SymbolicValueResolverRegistry() {

    }

    /**
     * Returns the singleton instance of this registry.
     *
     * @return Singleton SymbolicValueResolverRegistry.
     */
    public static SymbolicValueResolverRegistry getInstance() {

        return INSTANCE;
    }

    /**
     * Registers a resolver for the given field name.
     *
     * @param fieldName Field name to associate the resolver with.
     * @param resolver  Resolver implementation.
     */
    public void register(String fieldName, SymbolicValueResolver resolver) {

        resolvers.put(fieldName, resolver);
    }

    /**
     * Removes the resolver registered for the given field name.
     *
     * @param fieldName Field name whose resolver should be removed.
     */
    public void deregister(String fieldName) {

        resolvers.remove(fieldName);
    }

    /**
     * Returns the resolver for the given field name, or null if none is registered.
     *
     * @param fieldName Field name.
     * @return Registered resolver, or null if not found.
     */
    public SymbolicValueResolver getResolver(String fieldName) {

        return resolvers.get(fieldName);
    }
}
