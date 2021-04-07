/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.Configuration;
import org.wso2.carbon.identity.mgt.endpoint.util.client.Pair;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.CodeIntrospectResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.CodeValidationRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API related to lite user registration functions.
 */
public class LiteRegisterApi {
    String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.USER_API_RELATIVE_PATH);
    private ApiClient apiClient;

    public LiteRegisterApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LiteRegisterApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * This API is used to validate code of self registered lite users.
     *
     * @param code Code retried after lite user registration and optional property parameters (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public CodeIntrospectResponse introspectCode(CodeValidationRequest code) throws ApiException {
        Object localVarPostBody = code;

        // verify the required parameter 'code' is set
        if (code == null) {
            throw new ApiException(400, "Missing the required parameter 'code' when introspecting code");
        }

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        List<Property> properties = code.getProperties();
        for (Property property : properties) {
            if (StringUtils.isNotEmpty(property.getKey()) && MultitenantConstants.TENANT_DOMAIN
                    .equals(property.getKey())) {
                tenantDomain = property.getValue();
            }
        }

        basePath = IdentityManagementEndpointUtil
                .getBasePath(tenantDomain, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH);
        apiClient.setBasePath(basePath);

        // create path and map variables
        String localVarPath = "/introspect-code".replaceAll("\\{format\\}", "json");

        List<Pair> localVarQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);


        String[] localVarAuthNames = new String[]{};

        GenericType<CodeIntrospectResponse> localVarReturnType = new GenericType<CodeIntrospectResponse>() {
        };

        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);

    }

}
