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

import org.wso2.carbon.identity.rule.management.api.model.Value;

/**
 * Resolves a symbolic field value to a concrete {@link Value} at rule evaluation time.
 * Implementations are registered per field name via {@link SymbolicValueResolverRegistry}.
 */
public interface SymbolicValueResolver {

    /**
     * Resolves the given symbolic string to a concrete Value.
     * The returned Value must have type NUMBER or LIST.
     *
     * @param symbolicValue Symbolic token or comma-separated list of tokens to resolve.
     * @return Resolved Value with type NUMBER or LIST.
     */
    Value resolve(String symbolicValue);
}
