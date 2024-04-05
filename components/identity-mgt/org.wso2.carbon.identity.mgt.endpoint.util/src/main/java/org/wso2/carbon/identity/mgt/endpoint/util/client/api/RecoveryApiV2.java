/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.AccountRecoveryType;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ConfirmRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ConfirmResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryInitRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResendRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResendResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResetRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResetResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API related to recovery with user preferred channel.
 */
public class RecoveryApiV2 {

    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
    private ApiClient apiClient;

    public RecoveryApiV2() {

        this(Configuration.getDefaultApiClient());
    }

    public RecoveryApiV2(ApiClient apiClient) {

        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {

        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {

        this.apiClient = apiClient;
    }

    /**
     * Initiate recovering the forgotten password.
     *
     * @param recoveryInitRequest Password recovery initiating request. (required)
     * @param tenantDomain        Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers             If reCaptcha respond is found, embedded in request header. (optional)
     * @return Account recovery options response object.
     * @throws ApiException If fails to make API call.
     */
    public List<AccountRecoveryType> initiatePasswordRecovery(RecoveryInitRequest recoveryInitRequest,
                                                              String tenantDomain, Map<String, String> headers)
            throws ApiException {

        String localVarPath = "/password/init".replaceAll("\\{format\\}", "json");
        return initiateRecovery(recoveryInitRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * Recover password via selected recovery option.
     *
     * @param recoveryRequest   Recovery request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @return Recovery response.
     * @throws ApiException If fails to make API call.
     */
    public RecoveryResponse recoverPassword(RecoveryRequest recoveryRequest, String tenantDomain,
                                            Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/recover".replaceAll("\\{format\\}", "json");
        return recover(recoveryRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * Resend password recovery confirmation details.
     *
     * @param resendRequest   Resend request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @return Resend response.
     * @throws ApiException If fails to make API call.
     */
    public ResendResponse resendPasswordNotification(ResendRequest resendRequest, String tenantDomain,
                                                     Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/resend".replaceAll("\\{format\\}", "json");
        return resend(resendRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * Confirm password recovery.
     *
     * @param confirmRequest   Password recovery confirm request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @return Confirm response.
     * @throws ApiException If fails to make API call.
     */
    public ConfirmResponse confirmPasswordRecovery(ConfirmRequest confirmRequest, String tenantDomain,
                                                   Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/confirm".replaceAll("\\{format\\}", "json");
        return confirm(confirmRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * Reset user password.
     *
     * @param resetRequest   Password reset request. (required)
     * @param tenantDomain      Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @return reset response.
     * @throws ApiException If fails to make API call.
     */
    public ResetResponse resetUserPassword(ResetRequest resetRequest, String tenantDomain,
                                           Map<String, String> headers) throws ApiException {

        String localVarPath = "/password/reset".replaceAll("\\{format\\}", "json");
        return reset(resetRequest, tenantDomain, headers, localVarPath);
    }

    /**
     * This API can be used to initiate recovering forgotten password/username.
     *
     * @param recoveryInitRequest Recovery initiating request. (required)
     * @param tenantDomain        Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers             If reCaptcha respond is found, embedded in request header. (optional)
     * @param localVarPath        Endpoint path.
     * @return Account recovery options response object.
     * @throws ApiException If fails to make API call.
     */
    private List<AccountRecoveryType> initiateRecovery(RecoveryInitRequest recoveryInitRequest, String tenantDomain,
                                                       Map<String, String> headers, String localVarPath)
            throws ApiException {

        Object localVarPostBody = recoveryInitRequest;
        // Verify the required parameter 'recoveryInitRequest' is set.
        if (recoveryInitRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryInitRequest' when calling " +
                    "initializeRecovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
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
     * @param tenantDomain      Tenant Domain which user belongs. Default "carbon.super" (optional)
     * @param headers           Any additional headers to be embedded. (optional)
     * @param localVarPath      Endpoint path.
     * @return Recovery response.
     * @throws ApiException If fails to make API call.
     */
    private RecoveryResponse recover(RecoveryRequest recoveryRequest, String tenantDomain,
                                     Map<String, String> headers, String localVarPath) throws ApiException {

        Object localVarPostBody = recoveryRequest;
        // Verify if the required parameter 'recoveryRequest' is set.
        if (recoveryRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryRequest' when requesting recovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
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

    private ResendResponse resend(ResendRequest resendRequest, String tenantDomain, Map<String, String> headers,
                                  String localVarPath) throws ApiException {

        // Verify if the required parameter 'recoveryRequest' is set.
        if (resendRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'resendRequest' when requesting recovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
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
        GenericType<ResendResponse> localVarReturnType = new GenericType<ResendResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, resendRequest,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    private ConfirmResponse confirm(ConfirmRequest confirmRequest, String tenantDomain, Map<String, String> headers,
                                    String localVarPath) throws ApiException {

        // Verify if the required parameter 'recoveryRequest' is set.
        if (confirmRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'confirmRequest' when requesting recovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
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
        GenericType<ConfirmResponse> localVarReturnType = new GenericType<ConfirmResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, confirmRequest,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    private ResetResponse reset(ResetRequest resetRequest, String tenantDomain, Map<String, String> headers,
                                    String localVarPath) throws ApiException {

        // Verify if the required parameter 'recoveryRequest' is set.
        if (resetRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'resetRequest' when requesting recovery");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        basePath = IdentityManagementEndpointUtil.getBasePath(tenantDomain,
                IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_V2_RELATIVE_PATH);
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
        GenericType<ResetResponse> localVarReturnType = new GenericType<ResetResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, resetRequest,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }
}
