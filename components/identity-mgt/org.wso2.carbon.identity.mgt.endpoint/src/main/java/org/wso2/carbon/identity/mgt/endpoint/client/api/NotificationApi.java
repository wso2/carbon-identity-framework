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
import org.wso2.carbon.identity.mgt.endpoint.client.model.CodeValidationRequest;
import org.wso2.carbon.identity.mgt.endpoint.client.model.Property;
import org.wso2.carbon.identity.mgt.endpoint.client.model.RecoveryInitiatingRequest;
import org.wso2.carbon.identity.mgt.endpoint.client.model.ResetPasswordRequest;
import org.wso2.carbon.identity.mgt.endpoint.client.model.UserClaim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationApi {

    private ApiClient apiClient;

    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(
            IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);

    public NotificationApi() {

        this(Configuration.getDefaultApiClient());
    }

    public NotificationApi(ApiClient apiClient) {

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
     * @param recoveryInitiatingRequest It can be sent optional property parameters over email based on email template.
     *                                  (required)
     * @param type                      Notification Type (optional)
     * @param notify                    If notify&#x3D;true then, notifications will be internally managed. (optional)
     * @return String
     * @throws ApiException if fails to make API call
     */
    public String recoverPasswordPost(RecoveryInitiatingRequest recoveryInitiatingRequest, String type, Boolean notify)
            throws ApiException {

        return recoverPasswordPost(recoveryInitiatingRequest, type, notify, null);

    }

    /**
     * This API is used to send password recovery confirmation over defined channels like email/sms
     *
     * @param recoveryInitiatingRequest It can be sent optional property parameters over email based on email template.
     *                                  (required)
     * @param type                      Notification Type (optional)
     * @param notify                    If notify&#x3D;true then, notifications will be internally managed. (optional)
     * @param headers                   Adding headers for request recover password api. (optional)
     * @return String
     * @throws ApiException if fails to make API call
     */
    public String recoverPasswordPost(RecoveryInitiatingRequest recoveryInitiatingRequest, String type, Boolean notify,
                                      Map<String, String> headers) throws ApiException {

        Object localVarPostBody = recoveryInitiatingRequest;

        // verify the required parameter 'recoveryInitiatingRequest' is set
        if (recoveryInitiatingRequest == null) {
            throw new ApiException(400, "Missing the required parameter 'recoveryInitiatingRequest' when calling recoverPasswordPost");
        }

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (StringUtils.isNotBlank(recoveryInitiatingRequest.getUser().getTenantDomain())) {
            tenantDomain = recoveryInitiatingRequest.getUser().getTenantDomain();
        }

        setBasePath(tenantDomain);

        // create path and map variables
        String localVarPath = "/recover-password".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(headers)) {
            localVarHeaderParams.putAll(headers);
        }

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
     * This API can be used to recover forgot username.
     *
     * @param claim        User answers for recovery claims. (required)
     * @param tenantDomain Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
     * @param notify       If notify&#x3D;true then, notifications will be internally managed. (optional)
     * @throws ApiException if fails to make API call
     */
    public void recoverUsernamePost(List<UserClaim> claim, String tenantDomain, Boolean notify) throws ApiException {

        Object localVarPostBody = claim;

        // verify the required parameter 'claim' is set
        if (claim == null) {
            throw new ApiException(400, "Missing the required parameter 'claim' when calling recoverUsernamePost");
        }

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        setBasePath(tenantDomain);

        // create path and map variables
        String localVarPath = "/recover-username/".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));
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

        apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
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

        String userTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        List<Property> properties = resetPasswordRequest.getProperties();
        for (Property property : properties) {
            if (StringUtils.equalsIgnoreCase(IdentityManagementEndpointConstants.TENANT_DOMAIN, property.getKey())) {
                userTenantDomain = property.getValue();
                properties.remove(property);
                break;
            }
        }

        setBasePath(userTenantDomain);

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
     * This API is used to validate code of self reigstered users
     *
     * @param code Code retried after user self registration and optional property parameters (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public void validateCodePostCall(CodeValidationRequest code) throws ApiException {

        // verify the required parameter 'code' is set
        if (code == null) {
            throw new ApiException(400, "Missing the required parameter 'code' when calling validateCodePost(Async)");
        }
        final String[] contentType = {
                "application/json"
        };
        final String headerAccept = apiClient.selectHeaderAccept(contentType);
        String tenantDomain = getTenantDomain(code);
        setBasePath(tenantDomain);

        Map<String, String> headerParams = new HashMap<>();
        if (headerAccept != null) {
            headerParams.put("Accept", headerAccept);
        }
        final String headerContentType = apiClient.selectHeaderContentType(contentType);
        headerParams.put("Content-Type", headerContentType);
        String[] authNames = new String[]{};
        GenericType<String> localVarReturnType = new GenericType<String>() {};
        // create path and map variables
        String path = "/validate-code".replaceAll("\\{format\\}", "json");
        List<Pair> queryParams = new ArrayList<>();
        Map<String, Object> formParams = new HashMap<>();
        apiClient.invokeAPI(path, "POST", queryParams, code, headerParams,
                formParams, headerAccept, headerContentType, authNames, localVarReturnType);
    }

    private void setBasePath(String tenantDomain) {

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            basePath = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
        }

        apiClient.setBasePath(basePath);
    }

    private String getTenantDomain(CodeValidationRequest code) {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        List<Property> properties = code.getProperties();
        for (Property property : properties) {
            if (StringUtils.isNotEmpty(property.getKey()) && MultitenantConstants.TENANT_DOMAIN.equals(property.getKey())) {
                tenantDomain = property.getValue();
            }
        }
        return tenantDomain;
    }
}
