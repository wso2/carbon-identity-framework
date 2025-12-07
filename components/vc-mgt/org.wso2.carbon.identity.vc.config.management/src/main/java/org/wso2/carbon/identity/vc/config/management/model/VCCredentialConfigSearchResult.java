/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.vc.config.management.model;

import java.util.List;

/**
 * VC credential configuration search result with pagination support.
 */
public class VCCredentialConfigSearchResult {

    private int totalCount;
    private List<VCCredentialConfiguration> configurations;

    /**
     * Get the total count of configurations.
     *
     * @return Total count.
     */
    public int getTotalCount() {

        return totalCount;
    }

    /**
     * Set the total count of configurations.
     *
     * @param totalCount Total count.
     */
    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }

    /**
     * Get the list of configurations.
     *
     * @return List of configurations.
     */
    public List<VCCredentialConfiguration> getConfigurations() {

        return configurations;
    }

    /**
     * Set the list of configurations.
     *
     * @param configurations List of configurations.
     */
    public void setConfigurations(List<VCCredentialConfiguration> configurations) {

        this.configurations = configurations;
    }
}
