/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds parameterized SQL filter fragments (with {@code ?} placeholders) and their bound
 * parameter values, partitioned by {@link SessionMgtConstants.FilterType}.
 */
public class SessionFilterQueryBuilder {

    private final Map<SessionMgtConstants.FilterType, String> filterQueries = new HashMap<>();
    private final Map<SessionMgtConstants.FilterType, List<Object>> filterParams = new HashMap<>();

    /**
     * Sets the SQL fragment (containing {@code ?} placeholders) for a given filter type.
     */
    public void setFilterQuery(SessionMgtConstants.FilterType type, String query) {

        filterQueries.put(type, query);
    }

    /**
     * Returns the SQL fragment for the given filter type, or an empty string if none was set.
     */
    public String getFilterQuery(SessionMgtConstants.FilterType type) {

        return filterQueries.getOrDefault(type, "");
    }

    /**
     * Appends a bound parameter value for the given filter type.
     * Values must be added in the same order as the corresponding {@code ?} placeholders.
     * Use {@link String} for character columns and {@link Long} for numeric columns.
     */
    public void addFilterParam(SessionMgtConstants.FilterType type, Object value) {

        filterParams.computeIfAbsent(type, k -> new ArrayList<>()).add(value);
    }

    /**
     * Returns the ordered list of bound parameter values for the given filter type.
     */
    public List<Object> getFilterParams(SessionMgtConstants.FilterType type) {

        return filterParams.getOrDefault(type, Collections.emptyList());
    }
}
