/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth.ui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for OAuth FE functionality.
 */
public class OAuthUIUtil {

    private static final Log log = LogFactory.getLog(OAuthUIUtil.class);

    private OAuthUIUtil() {

    }

    /**
     * Returns the corresponding absolute endpoint URL. e.g. https://localhost:9443/oauth2/access-token
     *
     * @param endpointType It could be request-token endpoint, callback-token endpoint or access-token endpoint
     * @param oauthVersion OAuth version whether it is 1.0a or 2.0
     * @param request      HttpServletRequest coming to the FE jsp
     * @return Absolute endpoint URL.
     */
    public static String getAbsoluteEndpointURL(String endpointType, String oauthVersion, HttpServletRequest request) {

        String endpointURL = null;
        if(oauthVersion.equals(OAuthConstants.OAuthVersions.VERSION_1A)){
            endpointURL = IdentityUtil.getServerURL("/oauth" + endpointType, true, true);
        } else {
            endpointURL = IdentityUtil.getServerURL("/oauth2" + endpointType, true, false);
        }

        return endpointURL;
    }

    public static OAuthConsumerAppDTO[] doPaging(int pageNumber, OAuthConsumerAppDTO[] oAuthConsumerAppDTOSet) {

        int itemsPerPageInt = OAuthConstants.DEFAULT_ITEMS_PER_PAGE;
        OAuthConsumerAppDTO[] returnedOAuthConsumerSet;

        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        if (itemsPerPageInt < oAuthConsumerAppDTOSet.length) {
            returnedOAuthConsumerSet = new OAuthConsumerAppDTO[itemsPerPageInt];
        } else {
            returnedOAuthConsumerSet = new OAuthConsumerAppDTO[oAuthConsumerAppDTOSet.length];
        }
        for (int i = startIndex, j = 0; i < endIndex && i < oAuthConsumerAppDTOSet.length; i++, j++) {
            returnedOAuthConsumerSet[j] = oAuthConsumerAppDTOSet[i];
        }

        return returnedOAuthConsumerSet;
    }

}
