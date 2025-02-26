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

package org.wso2.carbon.identity.action.management.util;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for Action Management Tests.
 */
public class TestUtil {

    public static final int TENANT_ID = 2;
    public static final String TENANT_DOMAIN = "carbon.super";

    public static final String PRE_ISSUE_ACCESS_TOKEN_TYPE = Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType();
    public static final String PRE_UPDATE_PASSWORD_TYPE = Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType();

    public static final String PRE_ISSUE_ACCESS_TOKEN_PATH = Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getPathParam();

    public static final String PRE_ISSUE_ACCESS_TOKEN_ACTION_ID = String.valueOf(UUID.randomUUID());
    public static final String PRE_UPDATE_PASSWORD_ACTION_ID = String.valueOf(UUID.randomUUID());

    public static final String TEST_SECRET_TYPE_ID = "fcaf81a9-0d58-4cf4-98c8-fde2f3ba8df2";

    public static final String TEST_ACTION_NAME = "Test Action Name";
    public static final String TEST_ACTION_NAME_UPDATED = "Updated Test Action Name";
    public static final String TEST_INVALID_ACTION_NAME = "PreIssueAccessToken_#1";
    public static final String TEST_ACTION_DESCRIPTION = "Test Action description";
    public static final String TEST_ACTION_DESCRIPTION_UPDATED = "Updated Test Action description";
    public static final String TEST_ACTION_URI = "https://example.com";
    public static final String TEST_ACTION_URI_UPDATED = "https://sample.com";

    public static final String TEST_USERNAME = "sampleUsername";
    public static final String TEST_USERNAME_SECRET_REFERENCE = buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
            Authentication.Type.BASIC, Authentication.Property.USERNAME);
    public static final String TEST_PASSWORD = "samplePassword";
    public static final String TEST_PASSWORD_SECRET_REFERENCE = buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
            Authentication.Type.BASIC, Authentication.Property.PASSWORD);
    public static final String TEST_ACCESS_TOKEN = "5e482c2a-e83a-3afe-bc6a-ff79e1fdaaba";
    public static final String TEST_ACCESS_TOKEN_UPDATED = "fe326c2a-e83a-41fe-bc6a-ee79e1feabba";
    public static final String TEST_ACCESS_TOKEN_SECRET_REFERENCE = buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
            Authentication.Type.BEARER, Authentication.Property.ACCESS_TOKEN);
    public static final String TEST_API_KEY_HEADER = "sampleHeader";
    public static final String TEST_API_KEY_HEADER_UPDATED = "UpdatedSampleHeader";
    public static final String TEST_INVALID_API_KEY_HEADER = "-test-header";
    public static final String TEST_API_KEY_VALUE = "sampleValue";
    public static final String TEST_API_KEY_VALUE_UPDATED = "UpdatedSampleValue";
    public static final String TEST_API_KEY_VALUE_SECRET_REFERENCE = buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
            Authentication.Type.API_KEY, Authentication.Property.VALUE);

    public static final String TEST_ACTION_PROPERTY_NAME_1 = "samplePropertyName";
    public static final String TEST_ACTION_PROPERTY_VALUE_1 = "samplePropertyValue";
    public static final String TEST_ACTION_PROPERTY_VALUE_1_UPDATED = "UpdatedSamplePropertyValue";
    public static final String TEST_ACTION_PROPERTY_NAME_2 = "samplePropertyName2";
    public static final String TEST_ACTION_PROPERTY_VALUE_2 = "samplePropertyValue2";
    public static final String TEST_ACTION_PROPERTY_VALUE_2_UPDATED = "UpdatedSamplePropertyValue2";
    public static final String PASSWORD_SHARING_TYPE_PROPERTY_NAME = "passwordSharingType";
    public static final String TEST_PASSWORD_SHARING_TYPE = "PLAIN_TEXT";
    public static final String TEST_PASSWORD_SHARING_TYPE_UPDATED = "SHA256_HASHED";
    public static final String CERTIFICATE_PROPERTY_NAME = "certificate";
    public static final String TEST_CERTIFICATE = "sampleCertificate";
    public static final String TEST_CERTIFICATE_UPDATED = "UpdatedSampleCertificate";
    public static final String CERTIFICATE_ID = String.valueOf(UUID.randomUUID());
    public static final String CERTIFICATE_NAME = "ACTIONS:" + PRE_UPDATE_PASSWORD_ACTION_ID;

    public static Action buildMockAction(String name, String description, String uri, Authentication authentication) {

        return new Action.ActionRequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authentication))
                .build();
    }

    public static Action buildMockActionWithRule(String name, String description, String uri,
                                                 Authentication authentication, Rule rule) {

        return new Action.ActionRequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authentication))
                .rule(ActionRule.create(rule))
                .build();
    }

    public static String buildSecretName(String actionId, Authentication.Type authType,
                                         Authentication.Property authProperty) {

        return TEST_SECRET_TYPE_ID + ":" + actionId + ":" + authType.getName() + ":" + authProperty.getName();
    }

    public static Authentication buildMockBasicAuthentication(String username, String password) {

        return new Authentication.BasicAuthBuilder(username, password).build();
    }

    public static Authentication buildMockBearerAuthentication(String accessToken) {

        return new Authentication.BearerAuthBuilder(accessToken).build();
    }

    public static Authentication buildMockAPIKeyAuthentication(String header, String value) {

        return new Authentication.APIKeyAuthBuilder(header, value).build();
    }

    public static EndpointConfig buildMockEndpointConfig(String uri, Authentication authentication) {

        if (uri == null && authentication == null) {
            return null;
        }

        return new EndpointConfig.EndpointConfigBuilder()
                .uri(uri)
                .authentication(authentication)
                .build();
    }

    public static Rule buildMockRule(String ruleId, boolean isActive) {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        when(rule.isActive()).thenReturn(isActive);
        return rule;
    }
}
