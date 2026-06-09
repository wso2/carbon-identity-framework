/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.core.circuitbreaker;

/**
 * Class for globally extending the runtime policy as the final resolution step.
 */
public interface RuntimePolicyExtender {

    /**
     * Returns the final effective runtime policy for the given tenant and service.
     *
     * Note: This method is invoked on the hot path of the circuit breaker.
     * Implementations must cache their results to avoid latency impact on request processing.
     *
     * @param tenantDomain  The tenant domain.
     * @param service       The tenant service.
     * @param currentPolicy The policy after service-level loader overrides have been applied.
     * @return The updated {@link RuntimePolicy}, or {@code null} to keep the current policy.
     */
    public RuntimePolicy extend(String tenantDomain, TenantService service, RuntimePolicy currentPolicy);
}
