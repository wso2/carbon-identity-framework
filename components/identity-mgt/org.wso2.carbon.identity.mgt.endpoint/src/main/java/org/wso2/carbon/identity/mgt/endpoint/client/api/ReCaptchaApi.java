/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.mgt.endpoint.client.model.ReCaptchaProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generating and invoking the Captcha api to display and the ReCaptcha.
 */
public class ReCaptchaApi {

    private static final String TENANTDOMAIN = "tenant-domain";
    private static final String CAPTCHA_TYPE = "captcha-type";
    private static final String RECOVERY_TYPE = "recovery-type";
    private final String[] localVarAccepts = {"application/json"};
    private final String[] localVarContentTypes = {"application/json"};
    private String basePath = IdentityManagementEndpointUtil.buildEndpointUrl(IdentityManagementEndpointConstants
            .UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
    private ApiClient apiClient;

    public ReCaptchaApi() {

        this(Configuration.getDefaultApiClient());
    }

    private ReCaptchaApi(ApiClient apiClient) {

        this.apiClient = apiClient;
    }

    /**
     * return the reCaptchaGet details in the headers for the given tenant.
     *
     * @param tenantDomain tenant domain. Default &#x60;carbon.super&#x60; (optional)
     * @throws ApiException if fails to make API call
     */
    public ReCaptchaProperties getReCaptcha(String tenantDomain, boolean isEndpointTenantAware, String captchaType,
                                            String recoveryType) throws ApiException {

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (isEndpointTenantAware && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            basePath = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH);
        }

        apiClient.setBasePath(basePath);

        String localVarPath = "/captcha";

        // Query params
        List<Pair> localVarQueryParams = new ArrayList<>();
        Map<String, String> localVarHeaderParams = new HashMap<>();
        Map<String, Object> localVarFormParams = new HashMap<>();

        localVarQueryParams.addAll(apiClient.parameterToPairs(StringUtils.EMPTY, TENANTDOMAIN, tenantDomain));
        localVarQueryParams.addAll(apiClient.parameterToPairs(StringUtils.EMPTY, CAPTCHA_TYPE, captchaType));
        localVarQueryParams.addAll(apiClient.parameterToPairs(StringUtils.EMPTY, RECOVERY_TYPE, recoveryType));

        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<ReCaptchaProperties> localVarReturnType = new GenericType<ReCaptchaProperties>() {
        };

        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams,
                localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
