/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.org.resource.mgt.strategy;

import org.wso2.carbon.identity.org.resource.mgt.exceptions.NotImplementedException;
import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Interface for aggregation strategies.
 *
 * @param <T> Type of the resource to be aggregated
 */
public interface AggregationStrategy<T> {

    /**
     * Aggregate resources from the organization hierarchy.
     *
     * @param organizationHierarchy Organization hierarchy.
     * @param resourceRetriever     Function to retrieve the resource.
     * @return Aggregated resource.
     * @throws OrgResourceManagementException If an error occurs while aggregating the resources.
     */
    default T aggregate(List<String> organizationHierarchy, Function<String, Optional<T>> resourceRetriever)
            throws OrgResourceManagementException {

        throw new NotImplementedException("aggregate method is not implemented in " + this.getClass());
    }

    /**
     * Aggregate resources from the organization and application hierarchy.
     *
     * @param organizationHierarchy Organization hierarchy.
     * @param applicationHierarchy  Application hierarchy.
     * @param resourceRetriever     Function to retrieve the resource.
     * @return Aggregated resource.
     * @throws OrgResourceManagementException If an error occurs while aggregating the resources.
     */
    default T aggregate(List<String> organizationHierarchy, Map<String, String> applicationHierarchy,
                        BiFunction<String, String, Optional<T>> resourceRetriever)
            throws OrgResourceManagementException {

        throw new NotImplementedException("aggregate method is not implemented in " + this.getClass());
    }
}
