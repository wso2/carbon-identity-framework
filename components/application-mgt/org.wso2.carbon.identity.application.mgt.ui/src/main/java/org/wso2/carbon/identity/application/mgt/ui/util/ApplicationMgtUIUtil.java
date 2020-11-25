/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.ui.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ui.ApplicationBean;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.DEFAULT_FILTER;

/**
 * Few utility functions related to Application management UI.
 */
public class ApplicationMgtUIUtil {

    private static final String SP_UNIQUE_ID_MAP = "spUniqueIdMap";
    public static final String JWKS_URI = IdentityApplicationConstants.JWKS_URI_SP_PROPERTY_NAME;
    public static final String JWKS_DISPLAYNAME = "JWKS Endpoint";
    public static final String APP_NAME_JAVASCRIPT_VALIDATING_REGEX = "^[a-zA-Z0-9\\s.+_-]*$";
    private static final String SERVICE_PROVIDERS_NAME_JAVASCRIPT_REGEX = "ServiceProviders.SPNameJavascriptRegex";

    /**
     * Get related application bean from the session.
     *
     * @param session HTTP Session.
     * @param spName  Service provider name.
     * @return ApplicationBean
     */
    public static ApplicationBean getApplicationBeanFromSession(HttpSession session, String spName) {

        Map<String, UUID> spUniqueIdMap;

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            spUniqueIdMap = new HashMap<>();
            session.setAttribute(SP_UNIQUE_ID_MAP, spUniqueIdMap);
        } else {
            spUniqueIdMap = (HashMap<String, UUID>) session.getAttribute(SP_UNIQUE_ID_MAP);
        }

        if (spUniqueIdMap.get(spName) == null) {
            ApplicationBean applicationBean = new ApplicationBean();
            UUID uuid = UUID.randomUUID();
            spUniqueIdMap.put(spName, uuid);
            session.setAttribute(uuid.toString(), applicationBean);
        }
        return (ApplicationBean) session.getAttribute(spUniqueIdMap.get(spName).toString());
    }

    /**
     * Remove related application bean from the session.
     *
     * @param session Http Session.
     * @param spName  Service provider name.
     */
    public static void removeApplicationBeanFromSession(HttpSession session, String spName) {

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            return;
        }
        Map<String, UUID> spUniqueIdMap = (HashMap<String, UUID>) session.getAttribute(SP_UNIQUE_ID_MAP);

        if (spUniqueIdMap.get(spName) == null) {
            return;
        }
        session.removeAttribute(spUniqueIdMap.get(spName).toString());
        spUniqueIdMap.remove(spName);
    }

    /**
     * Resolves the filter string for search the application.
     *
     * @param filterString String to resolve.
     * @return filterString, If the filterString is null then use the default filter.
     */
    public static String resolveFilterString(String filterString) {

        if (!StringUtils.isNotBlank(filterString)) {
            return DEFAULT_FILTER;
        } else {
            return filterString.trim();
        }
    }

    /**
     * Resolves the pagination value.
     *
     * @param filterString String to resolve.
     * @return paginationValue.
     */
    public static String resolvePaginationValue(String filterString, String region, String item) {

        if (filterString != null) {
            return String.format(ApplicationMgtUIConstants.PAGINATION_VALUE_WITH_FILTER, region, item, filterString);
        } else {
            return String.format(ApplicationMgtUIConstants.PAGINATION_VALUE, region, item);
        }
    }

    /**
     * Return the Service Provider javascript validation regex if configured in the deployment.toml.
     *
     * @return regex.
     */
    public static String getSPValidatorJavascriptRegex() {

        String spValidatorJavascriptRegex = IdentityUtil.getProperty(SERVICE_PROVIDERS_NAME_JAVASCRIPT_REGEX);
        if (StringUtils.isBlank(spValidatorJavascriptRegex)) {
            spValidatorJavascriptRegex = APP_NAME_JAVASCRIPT_VALIDATING_REGEX;
        }
        return spValidatorJavascriptRegex;
    }
}
