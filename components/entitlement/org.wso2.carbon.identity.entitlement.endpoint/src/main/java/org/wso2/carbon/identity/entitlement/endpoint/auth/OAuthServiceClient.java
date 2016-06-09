/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

public class OAuthServiceClient {
    public static final String BEARER_TOKEN_TYPE = "bearer";
    private static final Log log = LogFactory.getLog(OAuthServiceClient.class);
    private OAuth2TokenValidationServiceStub stub = null;

    /**
     * OAuth2TokenValidationService Admin Service Client
     *
     * @param backendServerURL
     * @param username
     * @param password
     * @param configCtx
     * @throws Exception
     */
    public OAuthServiceClient(String backendServerURL, String username, String password,
                              ConfigurationContext configCtx) throws Exception {
        String serviceURL = backendServerURL + "OAuth2TokenValidationService";
        try {
            stub = new OAuth2TokenValidationServiceStub(configCtx, serviceURL);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true, stub._getServiceClient());
        } catch (AxisFault e) {
            log.error("Error initializing OAuth2 Client");
            throw new Exception("Error initializing OAuth Client", e);
        }
    }

    /**
     * Validates the OAuth 2.0 request
     *
     * @param accessTokenIdentifier
     * @return
     * @throws Exception
     */
    public OAuth2TokenValidationResponseDTO validateAccessToken(String accessTokenIdentifier)
            throws Exception {
        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType(BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(accessTokenIdentifier);
        oauthReq.setAccessToken(accessToken);
        try {
            return stub.validate(oauthReq);
        } catch (RemoteException e) {
            log.error("Error while validating OAuth2 request");
            throw new Exception("Error while validating OAuth2 request", e);
        }
    }

    /**
     * @param accessTokenIdentifier
     * @return
     * @throws Exception
     */
    public OAuth2ClientApplicationDTO findOAuthConsumerIfTokenIsValid(String accessTokenIdentifier)
            throws Exception {
        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType(BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(accessTokenIdentifier);
        oauthReq.setAccessToken(accessToken);
        try {
            return stub.findOAuthConsumerIfTokenIsValid(oauthReq);
        } catch (RemoteException e) {
            log.error("Error while validating OAuth2 request");
            throw new Exception("Error while validating OAuth2 request", e);
        }
    }

}
