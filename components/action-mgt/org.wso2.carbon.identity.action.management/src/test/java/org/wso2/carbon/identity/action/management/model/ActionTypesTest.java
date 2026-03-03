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

package org.wso2.carbon.identity.action.management.model;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;

/**
 * Test class for ActionTypes.
 */
public class ActionTypesTest {

    @DataProvider
    public Object[][] actionTypesProvider() {

        return new Object[][]{
                {Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN, "preIssueAccessToken", "PRE_ISSUE_ACCESS_TOKEN",
                        "Pre Issue Access Token",
                        "Configure an extension point for modifying access token via a custom service.",
                        Action.ActionTypes.Category.PRE_POST},
                {Action.ActionTypes.PRE_UPDATE_PASSWORD, "preUpdatePassword", "PRE_UPDATE_PASSWORD",
                        "Pre Update Password",
                        "Configure an extension point for modifying user password via a custom service.",
                        Action.ActionTypes.Category.PRE_POST},
                {Action.ActionTypes.PRE_UPDATE_PROFILE, "preUpdateProfile", "PRE_UPDATE_PROFILE", "Pre Update Profile",
                        "Configure an extension point for modifying user profile via a custom service.",
                        Action.ActionTypes.Category.PRE_POST},
                {Action.ActionTypes.PRE_REGISTRATION, "preRegistration", "PRE_REGISTRATION", "Pre Registration",
                        "Configure an extension point for modifying user registration via a custom service.",
                        Action.ActionTypes.Category.PRE_POST},
                {Action.ActionTypes.AUTHENTICATION, "authentication", "AUTHENTICATION", "Authentication",
                        "Configure an extension point for user authentication via a custom service.",
                        Action.ActionTypes.Category.IN_FLOW},
                {Action.ActionTypes.PRE_ISSUE_ID_TOKEN, "preIssueIdToken", "PRE_ISSUE_ID_TOKEN",
                        "Pre Issue ID Token",
                        "Configure an extension point for modifying ID token via a custom service.",
                        Action.ActionTypes.Category.PRE_POST}
        };
    }

    @Test(dataProvider = "actionTypesProvider")
    public void testActionTypes(Action.ActionTypes actionType, String expectedPathParam, String expectedActionType,
                                String expectedDisplayName, String expectedDescription,
                                Action.ActionTypes.Category expectedCategory) {

        Assert.assertEquals(actionType.getPathParam(), expectedPathParam);
        Assert.assertEquals(actionType.getActionType(), expectedActionType);
        Assert.assertEquals(actionType.getDisplayName(), expectedDisplayName);
        Assert.assertEquals(actionType.getDescription(), expectedDescription);
        Assert.assertEquals(actionType.getCategory(), expectedCategory);
    }

    @DataProvider
    public Object[][] filterByCategoryProvider() {

        return new Object[][]{
                {Action.ActionTypes.Category.PRE_POST,
                        new Action.ActionTypes[]{Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN,
                                Action.ActionTypes.PRE_UPDATE_PASSWORD, Action.ActionTypes.PRE_UPDATE_PROFILE,
                                Action.ActionTypes.PRE_REGISTRATION, Action.ActionTypes.PRE_ISSUE_ID_TOKEN}},
                {Action.ActionTypes.Category.IN_FLOW, new Action.ActionTypes[]{Action.ActionTypes.AUTHENTICATION}}
        };
    }

    @Test(dataProvider = "filterByCategoryProvider")
    public void testFilterByCategory(Action.ActionTypes.Category category, Action.ActionTypes[] expectedActionTypes) {

        Assert.assertEquals(Action.ActionTypes.filterByCategory(category), expectedActionTypes);
    }
}

