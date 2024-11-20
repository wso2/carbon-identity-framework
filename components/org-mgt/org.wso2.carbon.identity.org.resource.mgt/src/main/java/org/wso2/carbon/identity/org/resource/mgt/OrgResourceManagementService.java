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

package org.wso2.carbon.identity.org.resource.mgt;

import org.wso2.carbon.identity.org.resource.mgt.exceptions.NotImplementedException;
import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementException;
import org.wso2.carbon.identity.org.resource.mgt.strategy.AggregationStrategy;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Service interface for organization resource management.
 */
public interface OrgResourceManagementService {

    /**
     * Get resources from the organization hierarchy.
     *
     * @param organizationId      Organization ID.
     * @param resourceRetriever   Function to retrieve the resource.
     * @param aggregationStrategy Aggregation strategy.
     * @param <T>                 Type of the resource.
     * @return Resolved resources.
     * @throws OrgResourceManagementException If an error occurs while retrieving the resources.
     */
    default <T> T getResourcesFromOrgHierarchy(String organizationId,
                                               Function<String, Optional<T>> resourceRetriever,
                                               AggregationStrategy<T> aggregationStrategy)
            throws OrgResourceManagementException {

        throw new NotImplementedException("getResourcesFromOrgHierarchy method is not implemented in " +
                this.getClass());
    }

    /**
     * Get resources from the organization and application hierarchy.
     *
     * @param organizationId      Organization ID.
     * @param applicationId       Application ID.
     * @param resourceRetriever   Function to retrieve the resource.
     * @param aggregationStrategy Aggregation strategy.
     * @param <T>                 Type of the resource.
     * @return Resolved resources.
     * @throws OrgResourceManagementException If an error occurs while retrieving the resources.
     */
    default <T> T getResourcesFromOrgHierarchy(String organizationId, String applicationId,
                                               BiFunction<String, String, Optional<T>> resourceRetriever,
                                               AggregationStrategy<T> aggregationStrategy)
            throws OrgResourceManagementException {

        throw new NotImplementedException("getResourcesFromOrgAppHierarchy method is not implemented in " +
                this.getClass());
    }
}
