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

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementException;
import org.wso2.carbon.identity.org.resource.mgt.util.OrgResourceManagementUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MergeAllAggregationStrategy<T> implements AggregationStrategy<T> {

    private final BiFunction<T, T, T> resourceMerger;

    public MergeAllAggregationStrategy(BiFunction<T, T, T> resourceMerger) {

        this.resourceMerger = resourceMerger;
    }

    @Override
    public T aggregate(List<String> organizationHierarchy, Function<String, Optional<T>> resourceRetriever)
            throws OrgResourceManagementException {

        T aggregatedResource = null;
        if (CollectionUtils.isEmpty(organizationHierarchy) || organizationHierarchy.isEmpty()) {
            return aggregatedResource;
        }

        for (String orgId : organizationHierarchy) {
            if (OrgResourceManagementUtil.isMinOrgHierarchyDepthReached(orgId)) {
                break;
            }

            Optional<T> resource = resourceRetriever.apply(orgId);
            if (resource.isPresent()) {
                if (aggregatedResource == null) {
                    aggregatedResource = resource.get();
                } else {
                    aggregatedResource = resourceMerger.apply(aggregatedResource, resource.get());
                }
            }
        }
        return aggregatedResource;
    }

    @Override
    public T aggregate(List<String> organizationHierarchy, Map<String, String> applicationHierarchy,
                       BiFunction<String, String, Optional<T>> resourceRetriever)
            throws OrgResourceManagementException {

        T aggregatedResource = null;
        if (CollectionUtils.isEmpty(organizationHierarchy) || organizationHierarchy.isEmpty()) {
            return aggregatedResource;
        }

        for (String orgId : organizationHierarchy) {
            if (OrgResourceManagementUtil.isMinOrgHierarchyDepthReached(orgId)) {
                break;
            }

            String appId = applicationHierarchy.get(orgId);
            Optional<T> resource = resourceRetriever.apply(orgId, appId);
            if (resource.isPresent()) {
                if (aggregatedResource == null) {
                    aggregatedResource = resource.get();
                } else {
                    aggregatedResource = resourceMerger.apply(aggregatedResource, resource.get());
                }
            }
        }
        return aggregatedResource;
    }
}
