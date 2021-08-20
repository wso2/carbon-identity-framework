/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Util class for application management.
 */
public class ApplicationMgtUtils {

    private static final Log log = LogFactory.getLog(ApplicationMgtUtils.class);
    public static final String MASKING_CHARACTER = "*";
    public static final String MASKING_REGEX = "(?<!^.?).(?!.?$)";

    /**
     * Get the application id from service provider object.
     *
     * @param serviceProvider Service provider object.
     * @return Id of the service provider.
     */
    public static String getAppId(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationResourceId();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the application name from service provider object.
     *
     * @param serviceProvider Service provider object.
     * @return Name of the service provider.
     */
    public static String getApplicationName(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationName();
        }
        return "Undefined";
    }

    /**
     * Get the initiator id.
     *
     * @param userName     Username of the initiator.
     * @param tenantDomain Tenant domain of the initiator.
     * @return User id of the initiator.
     */
    public static String getInitiatorId(String userName, String tenantDomain) {

        String userId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserId();
        if (userId == null) {
            AuthenticatedUser user = new AuthenticatedUser();
            user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(userName));
            user.setUserName(UserCoreUtil.removeDomainFromName(userName));
            user.setTenantDomain(tenantDomain);
            userId = user.getLoggableUserId();
        }
        return userId;
    }

    /**
     * Build the service provider JSON string masking the sensitive information.
     *
     * @param serviceProvider Service provider object.
     * @return JSON string of the service provider object.
     */
    public static String buildSPData(ServiceProvider serviceProvider) {

        if (serviceProvider == null) {
            return StringUtils.EMPTY;
        }
        try {
            JSONObject serviceProviderJSONObject =
                    new JSONObject(new ObjectMapper().writeValueAsString(serviceProvider));
            JSONObject inboundAuthenticationConfig =
                    serviceProviderJSONObject.optJSONObject("inboundAuthenticationConfig");
            if (inboundAuthenticationConfig != null) {
                JSONArray inboundAuthenticationRequestConfigsArray =
                        inboundAuthenticationConfig.optJSONArray("inboundAuthenticationRequestConfigs");
                if (inboundAuthenticationRequestConfigsArray != null) {
                    for (int i = 0; i < inboundAuthenticationRequestConfigsArray.length(); i++) {
                        JSONObject requestConfig = inboundAuthenticationRequestConfigsArray.getJSONObject(i);
                        JSONArray properties = requestConfig.optJSONArray("properties");
                        if (properties != null) {
                            for (int j = 0; j < properties.length(); j++) {
                                JSONObject property = properties.optJSONObject(j);
                                if (property != null && StringUtils.equalsIgnoreCase("oauthConsumerSecret",
                                        (String) property.get("name"))) {
                                    String secret = (String) property.get("value");
                                    String maskedSecret = secret.replaceAll(MASKING_REGEX, MASKING_CHARACTER);
                                    property.put("value", maskedSecret);
                                }
                            }
                        }
                    }
                }
            }
            return serviceProviderJSONObject.toString();
        } catch (JsonProcessingException e) {
            log.error("Error while converting service provider object to json.");
        }
        return StringUtils.EMPTY;
    }
}
