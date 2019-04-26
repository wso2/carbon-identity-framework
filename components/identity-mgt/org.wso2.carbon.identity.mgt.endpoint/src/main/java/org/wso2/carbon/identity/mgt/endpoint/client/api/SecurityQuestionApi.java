/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client.api;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.client.Configuration;
import org.wso2.carbon.identity.mgt.endpoint.client.Pair;
import org.wso2.carbon.identity.mgt.endpoint.client.model.AnswerVerificationRequest;
import org.wso2.carbon.identity.mgt.endpoint.client.model.InitiateAllQuestionResponse;
import org.wso2.carbon.identity.mgt.endpoint.client.model.InitiateQuestionResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityQuestionApi {
    private ApiClient apiClient;

    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);

    public SecurityQuestionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SecurityQuestionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * This API is used to initiate password recovery using user challenge questions. Response will be a random
     * challenge question with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @return InitiateQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateQuestionResponse securityQuestionGet(String username, String realm, String tenantDomain)
            throws ApiException {

        return securityQuestionGet(username, realm, tenantDomain, null);
    }

    /**
     * This API is used to initiate password recovery using user challenge questions. Response will be a
     * random challenge question with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @param headers      Adding headers for request recover password api. (optional)
     * @return InitiateQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateQuestionResponse securityQuestionGet(String username, String realm, String tenantDomain,
                                                        Map<String, String> headers) throws ApiException {

        Object localVarPostBody = null;

        // verify the required parameter 'username' is set
        if (username == null) {
            throw new ApiException(400, "Missing the required parameter 'username' when calling securityQuestionGet");
        }

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            basePath = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
        }

        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/security-question".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(headers)) {
            localVarHeaderParams.putAll(headers);
        }

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "username", username));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "realm", realm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));


        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<InitiateQuestionResponse> localVarReturnType = new GenericType<InitiateQuestionResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * This API is used to initiate password recovery using user challenge questions at once. Response will be a random
     * challenge questions with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @return InitiateAllQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateAllQuestionResponse securityQuestionsGet(String username, String realm, String tenantDomain)
            throws ApiException {

        return securityQuestionsGet(username, realm, tenantDomain, null);
    }

    /**
     * This API is used to initiate password recovery using user challenge questions at once. Response will be a
     * random challenge questions with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified,
     *                     it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @param headers      Adding headers for request recover password api. (optional)
     * @return InitiateAllQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateAllQuestionResponse securityQuestionsGet(String username, String realm, String tenantDomain,
                                                            Map<String, String> headers) throws ApiException {

        Object localVarPostBody = null;

        // verify the required parameter 'username' is set
        if (username == null) {
            throw new ApiException(400, "Missing the required parameter 'username' when calling securityQuestionsGet");
        }

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            basePath = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
        }

        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/security-questions".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(headers)) {
            localVarHeaderParams.putAll(headers);
        }

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "username", username));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "realm", realm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));


        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<InitiateAllQuestionResponse> localVarReturnType = new GenericType<InitiateAllQuestionResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * This is used to validate user challenge answer. If user challenge answer is valid, it will send another challenge question to answer until the status become &#x60;COMPLETE&#x60;. If the answer is wrong, user can retry the answer.
     *
     * @param answerVerificationRequest User answers verification with key returned in privious step. (required)
     * @return InitiateQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateQuestionResponse validateAnswerPost(AnswerVerificationRequest answerVerificationRequest,
                                                       Map<String, String> headers ) throws ApiException {
        Object localVarPostBody = answerVerificationRequest;

        // verify the required parameter 'answerVerificationRequest' is set
        if (answerVerificationRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'answerVerificationRequest' when calling validateAnswerPost");
        }

        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/validate-answer".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>() ;

        if (MapUtils.isNotEmpty(headers)) {
            localVarHeaderParams.putAll(headers);
        }

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<InitiateQuestionResponse> localVarReturnType = new GenericType<InitiateQuestionResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
