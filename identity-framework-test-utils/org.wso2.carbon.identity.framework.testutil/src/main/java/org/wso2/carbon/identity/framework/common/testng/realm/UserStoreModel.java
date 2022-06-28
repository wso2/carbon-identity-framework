/*
 * Copyright (c) 2022. WSO2 Inc. (http://www.wso2.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.common.testng.realm;

import java.util.HashMap;
import java.util.Map;

/**
 * User store model is used to build the users in Mock User store.
 * It is easy to supply the mockable users using this.
 * The test class need to be annotated with {@code @WithRealmService annotation}
 *
 * <pre>{@code
 *         UserStoreModel userStoreModel = createUserStoreModel();
 *         userStoreModel.bindToRealm();
 *         //Test the operation using mock user store
 *         userStoreModel.unBindFromRealm();
 * }</pre>
 *
 * The user store model can be created similar to following code
 * <pre>{@code
 *     private UserStoreModel createUserStoreModel() {
 *
 *         UserStoreModel userStoreModel = new UserStoreModel();
 *         userStoreModel.newUserBuilder()
 *                 .withUserId(TEST_USER_1_ID)
 *                 .withClaim("http://wso2.org/claims/givenname", "FName")
 *                 .withClaim("http://wso2.org/claims/lastname", "Lname")
 *                 .build();
 *         return userStoreModel;
 *     }
 * }</pre>
 */
public class UserStoreModel {
    private Map<String, Map<String, String>> userModel = new HashMap<>();

    /**
     * Use the user builder to build a user object for test purpose.
     * Syntax to build a user in the user store is made easy.
     */
    public class UserBuilder {
        private String userId;
        private  Map<String, String> claims = new HashMap<>();

        /**
         * Attach the user id for the builder
         * @param userId
         * @return
         */
        public UserBuilder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Attach a local attribute and value to the user
         * @param dialect
         * @param value
         * @return
         */
        public UserBuilder withClaim(String dialect, String value) {
            claims.put(dialect, value);
            return this;
        }

        /**
         * Builds the user and add to the user store model.
         */
        public void build() {
            UserStoreModel.this.userModel.put(userId, claims);
        }
    }

    /**
     * Create a new user builder object
     * @return
     */
    public UserBuilder newUserBuilder() {
        return new UserBuilder();
    }

    public Map<String, String > getClaimValues(String userId) {
        return userModel.get(userId);
    }

    /**
     * Binds this user store model to the current test realm.
     */
    public void bindToRealm() {
        MockUserStoreManager.bindUserStoreModel(this);
    }

    /**
     * Unbinds this user store modem from the current test realm
     */
    public void unBindFromRealm() {
        MockUserStoreManager.unbindUserStoreModel();
    }
}
