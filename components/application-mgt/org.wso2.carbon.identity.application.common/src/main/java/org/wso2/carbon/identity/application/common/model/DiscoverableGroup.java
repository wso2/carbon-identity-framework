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

package org.wso2.carbon.identity.application.common.model;

import java.util.List;
import java.util.Objects;

public class DiscoverableGroup  {

    private String userStore;
    private List<String> groups;

    public DiscoverableGroup(String userStore, List<String> groups) {
        this.userStore = userStore;
        this.groups = groups;
    }

    public String getUserStore() {
        return userStore;
    }

    public void setUserStore(String userStore) {
        this.userStore = userStore;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoverableGroup discoverableGroup = (DiscoverableGroup) o;
        return Objects.equals(this.userStore, discoverableGroup.userStore) &&
                Objects.equals(this.groups, discoverableGroup.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userStore, groups);
    }

    @Override
    public String toString() {

        return "class DiscoverableGroup {\n" +
                "    userStore: " + toIndentedString(userStore) + "\n" +
                "    groups: " + toIndentedString(groups) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}
