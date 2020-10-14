/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.mgt.endpoint.util.client.api;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.Configuration;
import org.wso2.carbon.identity.mgt.endpoint.util.client.Pair;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.AccountRecoveryType;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.ConfirmRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.RecoveryInitRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.RecoveryRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.RecoveryResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.ResendConfirmationCodeResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.ResendConfirmationRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1.ResetCodeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API related to password recovery with user preferred channel.
 */
public class PasswordRecoveryApiV1 {

    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.RECOVERY_API_V1_RELATIVE_PATH);
    private ApiClient apiClient;

    public PasswordRecoveryApiV1() {

        this(Configuration.getDefaultApiClient());
    }

    public PasswordRecoveryApiV1(ApiClient apiClient) {

        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {

        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {

        this.apiClient = apiClient;
    }

    /**
     * This API can be used to initiate recovering forgotten password.
     *
     * @param recoveryInitRequest Password recovery initiating request. (required)
     * @param tenantDomain        Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers             If reCaptcha respond is found, embedded in request header. (optional)
     * @return Account recovery options response object.
     * @throws ApiException if fails to make API call
     */
    public List<AccountRecoveryType> initiatePasswordRecovery(RecoveryInitRequest recoveryInitRequest, String tenantDomain,
                                                              Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/init".replaceAll("\\{format\\}", "json");
        return initiateRecovery(recoveryInitRequest, tenantDomain, null, localVarPath);
    }

    /**
     * This API is used to recover password via selected recovery option.
     *
     * @param recoveryRequest   Recovery request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @return Recovery response
     * @throws ApiException if fails to make API call
     */
    public RecoveryResponse recoverPassword(RecoveryRequest recoveryRequest, String tenantDomain,
                                            Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/recover".replaceAll("\\{format\\}", "json");
        return recover(recoveryRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * This API can be used to initiate recovering forgotten password/username.
     *
     * @param recoveryInitRequest Recovery initiating request. (required)
     * @param tenantDomain        Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers             If reCaptcha respond is found, embedded in request header. (optional)
     * @param localVarPath        Endpoint path
     * @return Account recovery options response object.
     * @throws ApiException if fails to make API call.
     */
    private List<AccountRecoveryType> initiateRecovery(RecoveryInitRequest recoveryInitRequest, String tenantDomain,
                                                       Map<String, String> headers, String localVarPath)
            throws ApiException {

        Object localVarPostBody = recoveryInitRequest;
        // Verify the required parameter 'recoveryInitRequest' is set
        if (recoveryInitRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryInitRequest' when calling initializeRecovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V1_RELATIVE_PATH);
        apiClient.setBasePath(basePath);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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
        GenericType<List<AccountRecoveryType>> localVarReturnType = new GenericType<List<AccountRecoveryType>>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * This API is used to recover username/password via selected recovery option.
     *
     * @param recoveryRequest   Recovery request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @param localVarPath      Endpoint path
     * @return Recovery response
     * @throws ApiException if fails to make API call
     */
    private RecoveryResponse recover(RecoveryRequest recoveryRequest, String tenantDomain,
                                     Map<String, String> headers, String localVarPath) throws ApiException {

        Object localVarPostBody = recoveryRequest;
        // Verify if the required parameter 'recoveryRequest' is set
        if (recoveryRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryRequest' when requesting recovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V1_RELATIVE_PATH);
        apiClient.setBasePath(basePath);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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
        GenericType<RecoveryResponse> localVarReturnType = new GenericType<RecoveryResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * This API is used to request to resend the password recovery confirmation code via requested notification channel.
     *
     * @param resentPasswordRequest     Resend request. (required)
     * @param tenantDomain              Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers                   Any additional headers to be embedded. (optional)
     * @return Resend confirmation response
     * @throws ApiException if fails to make API call
     */
    public ResendConfirmationCodeResponse resendPasswordConfirmationCode(
            ResendConfirmationRequest resentPasswordRequest, String tenantDomain, Map<String, String> headers)
            throws ApiException {

        Object localVarPostBody = resentPasswordRequest;
        // verify the required parameter 'resentPasswordRequest' is set
        if (resentPasswordRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'resentPasswordRequest' " +
                    "when calling resendPasswordConfirmationCode");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V1_RELATIVE_PATH);
        apiClient.setBasePath(basePath);
        // Create path and map variables
        String localVarPath = "/password/resend".replaceAll("\\{format\\}","json");
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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
        GenericType<ResendConfirmationCodeResponse> localVarReturnType = new
                GenericType<ResendConfirmationCodeResponse>() {
                };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * This API is used to request to validate the password recovery confirmation code.
     *
     * @param confirmRequest            Confirmation request. (required)
     * @param tenantDomain              Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param headers                   Any additional headers to be embedded. (optional)
     * @return Resent code response
     * @throws ApiException if fails to make API call
     */
    public ResetCodeResponse validateConfirmationCode(ConfirmRequest confirmRequest, String tenantDomain,
                                                      Map<String, String> headers) throws ApiException {

        Object localVarPostBody = confirmRequest;
        // verify the required parameter 'confirmRequest' is set
        if (confirmRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'confirmRequest' " +
                    "when calling validateConfirmationCode");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V1_RELATIVE_PATH);
        apiClient.setBasePath(basePath);
        // Create path and map variables
        String localVarPath = "/password/confirm".replaceAll("\\{format\\}","json");
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
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
        GenericType<ResetCodeResponse> localVarReturnType = new GenericType<ResetCodeResponse>() {};
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }
}
