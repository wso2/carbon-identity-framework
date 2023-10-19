/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter query builder class.
 */
public class FilterQueryBuilder {

    private Map<Integer, String> stringParameters;
    private String filter;

    /**
     * Get filter query builder attributes.
     *
     * @return Map of filter query builder attributes.
     */
    public Map<Integer, String> getFilterAttributeValue() {

        return stringParameters;
    }

    /**
     * Add a filter query builder attribute.
     *
     * @param value attribute value.
     */
    public void setFilterAttributeValue(int count, String value) {

        if (stringParameters == null) {
            stringParameters = new HashMap<>();
        }
        stringParameters.put(count, value);
    }

    /**
     * Set filter query.
     *
     * @param filter filter query.
     */
    public void setFilterQuery(String filter) {

        this.filter = filter;
    }

    /**
     * Get filter query.
     *
     * @return Filter query string.
     */
    public String getFilterQuery() {

        return filter;
    }
}
