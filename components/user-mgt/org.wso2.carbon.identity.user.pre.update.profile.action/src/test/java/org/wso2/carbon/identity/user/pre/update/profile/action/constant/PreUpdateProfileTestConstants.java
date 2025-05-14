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

package org.wso2.carbon.identity.user.pre.update.profile.action.constant;

public class PreUpdateProfileTestConstants {

    public static final String USER_ID = "user1";
    public static final String USER_STORE_DOMAIN = "PRIMARY";

    public enum Claims {
        CLAIM1("http://wso2.org/claims/claim1", false, "value11", "value10", "value10"),
        CLAIM2("http://wso2.org/claims/claim2", true, new String[]{"value21", "value22"}, new String[]{"value21"},
                "value21"),
        CLAIM3("http://wso2.org/claims/claim3", false, "", "value30", "value30"),
        CLAIM4("http://wso2.org/claims/claim4", true, new String[]{}, new String[]{"value40"}, "value40"),
        CLAIM5("http://wso2.org/claims/claim5", false, "value51,value52", "value51,value52,value53",
                "value51,value52,value53"),
        CLAIM6("http://wso2.org/claims/claim6", false, null, "value61", "value61"),
        CLAIM7("http://wso2.org/claims/claim7", true, null, new String[]{"value71", "value72"}, "value71,value72"),
        GROUPS("http://wso2.org/claims/groups", true, new String[]{"group1", "group2"},
                new String[]{"group2", "group3"},
                "group2,group3"),
        ROLES("http://wso2.org/claims/roles", true, new String[]{"role1", "role2"}, new String[]{"role1"}, "role1");

        private String claimURI;
        private boolean isMultiValued;
        private Object updatingValue;
        private Object existingValue;
        private String existingValueInUserStore;

        Claims(String claimURI, boolean isMultiValued, Object updatingValue, Object existingValue,
               String existingValueInUserStore) {

            this.claimURI = claimURI;
            this.isMultiValued = isMultiValued;
            this.updatingValue = updatingValue;
            this.existingValue = existingValue;
            this.existingValueInUserStore = existingValueInUserStore;
        }

        public String getClaimURI() {

            return claimURI;
        }

        public boolean isMultiValued() {

            return isMultiValued;
        }

        public Object getUpdatingValue() {

            return updatingValue;
        }

        public Object getExistingValue() {

            return existingValue;
        }

        public String getExistingValueInUserStore() {

            return existingValueInUserStore;
        }
    }

    public static final int TENANT_ID = -1234;
    public static final String TENANT_DOMAIN = "carbon.super";

    private PreUpdateProfileTestConstants() {

    }
}
