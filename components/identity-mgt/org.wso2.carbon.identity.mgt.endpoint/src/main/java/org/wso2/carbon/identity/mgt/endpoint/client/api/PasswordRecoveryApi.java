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
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.client.Configuration;
import org.wso2.carbon.identity.mgt.endpoint.client.Pair;
import org.wso2.carbon.identity.mgt.endpoint.client.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordRecoveryApi {
    private ApiClient apiClient;

    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);

    public PasswordRecoveryApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PasswordRecoveryApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * This API is used to send password recovery confirmation over defined channels like email/sms
     *
     * @param recoveryInitiatingRequest It can be sent optional property parameters over email based on email template. (required)
     * @param type                      Notification Type (optional)
     * @param notify                    If notify&#x3D;true then, notifications will be internally managed. (optional)
     * @return String
     * @throws ApiException if fails to make API call
     */
    public String recoverPasswordPost(RecoveryInitiatingRequest recoveryInitiatingRequest, String type, Boolean notify) throws ApiException {
        Object localVarPostBody = recoveryInitiatingRequest;

        // verify the required parameter 'recoveryInitiatingRequest' is set
        if (recoveryInitiatingRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryInitiatingRequest' when calling recoverPasswordPost");
        }

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (StringUtils.isNotBlank(recoveryInitiatingRequest.getUser().getTenantDomain())) {
            tenantDomain = recoveryInitiatingRequest.getUser().getTenantDomain();
        }

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            basePath = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
        }


        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/recover-password".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "notify", notify));


        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<String> localVarReturnType = new GenericType<String>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * This API is used to initiate password recovery using user challenge questions. Response will be a random challenge question with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified, it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified, it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @return InitiateQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateQuestionResponse securityQuestionGet(String username, String realm, String tenantDomain) throws ApiException {
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
     * This API is used to initiate password recovery using user challenge questions at once. Response will be a random challenge questions with a confirmation key.
     *
     * @param username     username of the user (required)
     * @param realm        &#x60;User Store Domain&#x60; which user belongs. If not specified, it will be &#x60;PRIMARY&#x60; domain.  (optional)
     * @param tenantDomain &#x60;Tenant Domain&#x60; which user belongs. If not specified, it will be &#x60;carbon.super&#x60; domain.  (optional)
     * @return InitiateAllQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateAllQuestionResponse securityQuestionsGet(String username, String realm, String tenantDomain) throws ApiException {
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
     * This API will be used to reset user password using the confirmatin key recieved through recovery process. Need to input &#x60;key&#x60;  and the new &#x60;password&#x60;.
     *
     * @param resetPasswordRequest key, password and optional metadata properties (required)
     * @throws ApiException if fails to make API call
     */
    public void setPasswordPost(ResetPasswordRequest resetPasswordRequest) throws ApiException {
        Object localVarPostBody = resetPasswordRequest;

        // verify the required parameter 'resetPasswordRequest' is set
        if (resetPasswordRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'resetPasswordRequest' when calling setPasswordPost");
        }

        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/set-password".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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


        apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    }

    /**
     * This is used to validate user challenge answer. If user challenge answer is valid, it will send another challenge question to answer until the status become &#x60;COMPLETE&#x60;. If the answer is wrong, user can retry the answer.
     *
     * @param answerVerificationRequest User answers verification with key returned in privious step. (required)
     * @return InitiateQuestionResponse
     * @throws ApiException if fails to make API call
     */
    public InitiateQuestionResponse validateAnswerPost(AnswerVerificationRequest answerVerificationRequest) throws ApiException {
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
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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
