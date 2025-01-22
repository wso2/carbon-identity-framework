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
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Claim;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.UserClaim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsernameRecoveryApi {

  final String[] localVarAccepts = {"application/json"};
  final String[] localVarContentTypes = {"application/json"};
  String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(
          IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
  private ApiClient apiClient;

  public UsernameRecoveryApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UsernameRecoveryApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Return the recovery supported claims in the given tenant.
   *
   * @param tenantDomain tenant domain. Default &#x60;carbon.super&#x60; (optional)
   * @return List<Claim>
   * @throws ApiException if fails to make API call.
   */
  public List<Claim> claimsGet(String tenantDomain) throws ApiException {

    return claimsGet(tenantDomain, true);
  }

  /**
   * Return the recovery supported claims in the given tenant.
   *
   * @param tenantDomain          tenant domain. Default &#x60;carbon.super&#x60; (optional)
   * @param isEndpointTenantAware Is tenant aware endpoint.
   * @return List<Claim>
   * @throws ApiException if fails to make API call.
   */
  public List<Claim> claimsGet(String tenantDomain, boolean isEndpointTenantAware) throws ApiException {

    return claimsGet(tenantDomain, isEndpointTenantAware, null);
  }

  /**
   * Return the recovery supported claims in the given tenant for a given profile.
   *
   * @param tenantDomain          tenant domain Default carbon.super (optional).
   * @param isEndpointTenantAware Is tenant aware endpoint.
   * @param profileName           Profile name (eg. selfRegistration).
   * @return List<Claim> List of claims.
   * @throws ApiException if fails to make API call.
   */
  public List<Claim> claimsGet(String tenantDomain, boolean isEndpointTenantAware, String profileName)
          throws ApiException {

    Object localVarPostBody = null;

    if (StringUtils.isBlank(tenantDomain)) {
      tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    basePath = IdentityManagementEndpointUtil
            .getBasePath(tenantDomain, IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                    isEndpointTenantAware);
    apiClient.setBasePath(basePath);

    // create path and map variables.
    String localVarPath = "/claims".replaceAll("\\{format\\}", "json");

    // query params.
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));
    if (StringUtils.isNotBlank(profileName)) {
      localVarQueryParams.addAll(
              apiClient.parameterToPairs("", "profile-name", profileName));
    }

    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
    String[] localVarAuthNames = new String[]{};
    GenericType<List<Claim>> localVarReturnType = new GenericType<List<Claim>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams,
            localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
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

    recoverUsernamePost(claim, tenantDomain, notify, null);
  }

  /**
   * This API can be used to recover forgot username.
   *
   * @param claim        User answers for recovery claims. (required)
   * @param tenantDomain Tenant Domain which user belongs. Default &#x60;carbon.super&#x60; (optional)
   * @param notify       If notify&#x3D;true then, notifications will be internally managed. (optional)
   * @param headers      If reCaptcha respond is found, embedded in request header. (optional)
   * @throws ApiException if fails to make API call
   */
  public void recoverUsernamePost(List<UserClaim> claim, String tenantDomain, Boolean notify,
                                  Map<String, String> headers) throws ApiException {

    Object localVarPostBody = claim;
    
    // verify the required parameter 'claim' is set
    if (claim == null) {
      throw new ApiException(400, "Missing the required parameter 'claim' when calling recoverUsernamePost");
    }


    if (StringUtils.isBlank(tenantDomain)) {
      tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    basePath = IdentityManagementEndpointUtil
            .getBasePath(tenantDomain, IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
    apiClient.setBasePath(basePath);

    // create path and map variables
    String localVarPath = "/recover-username/".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();

    if (MapUtils.isNotEmpty(headers)) {
      localVarHeaderParams.putAll(headers);
    }

    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "notify", notify));

    

    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }

  /**
   * Return the user name recovery supported claims in the given tenant.
   *
   * @param tenantDomain          tenant domain. Default &#x60;carbon.super&#x60; (optional).
   * @param isEndpointTenantAware Is tenant aware endpoint.
   * @return List<Claim>
   * @throws ApiException if fails to make API call.
   */
  public List<Claim> getClaimsForUsernameRecovery(String tenantDomain, boolean isEndpointTenantAware)
          throws ApiException {

    if (StringUtils.isBlank(tenantDomain)) {
      tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    basePath = IdentityManagementEndpointUtil
            .getBasePath(tenantDomain, IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                    isEndpointTenantAware);
    apiClient.setBasePath(basePath);

    // Create path and map variables
    String localVarPath = "/claims".replaceAll("\\{format\\}", "json");

    // Query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tenant-domain", tenantDomain));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isUsernameRecovery", true));

    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    GenericType<List<Claim>> localVarReturnType = new GenericType<List<Claim>>() {
    };
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams, localVarFormParams,
            localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
