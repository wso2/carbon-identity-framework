/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter parameter representation.
 */
public class FilterData {

    private String filterString;
    private List<String> filterValues = new ArrayList<>();

    /**
     * Get where clause string for filtering.
     *
     * @return where clause string for filtering.
     */
    public String getFilterString() {

        return filterString;
    }

    /**
     * Set where clause string for filtering.
     *
     * @param filterString  where clause string for filtering.
     */
    public void setFilterString(String filterString) {

        this.filterString = filterString;
    }

    /**
     * Get list of filter values.
     *
     * @return list of filter values.
     */
    public List<String> getFilterValues() {

        return filterValues;
    }

    /**
     * Set list of filter values.
     *
     * @param filterValues  list of filter values.
     */
    public void setFilterValues(List<String> filterValues) {

        this.filterValues = filterValues;
    }

    /**
     * Add a filter value to list of filter values.
     *
     * @param filterValue   filter value.
     */
    public void addFilterValue(String filterValue) {

        filterValues.add(filterValue);
    }
}
