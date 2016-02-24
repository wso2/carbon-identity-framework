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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth2.token.handlers.grant.iwa.ntlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractAuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import waffle.util.Base64;
import waffle.windows.auth.IWindowsSecurityContext;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

public class NTLMAuthenticationGrantHandlerWithHandshake extends AbstractAuthorizationGrantHandler  {

    private static Log log = LogFactory.getLog(NTLMAuthenticationGrantHandlerWithHandshake.class);

    private static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String SCHEME_NTLM = "NTLM";
    private static final String SERVER_CONNECTION = "server-connection";
    private static final String SECURITY_PACKAGE = "Negotiate";
    private static final String RESPONSE_HEADERS_PROPERTY = "RESPONSE_HEADERS";

    //This is the index of the byte which represents the NTLM token type.
    private static final int MESSAGE_TYPE_BYTE_INDEX = 8;

    //NTLM Token types
    private static final int NTLM_TYPE_1_TOKEN = 1;
    private static final int NTLM_TYPE_3_TOKEN = 3;

    //'provider' stores the current state of the handshake. i.e. when it receives TYPE-1 token and validate it, it 
    //  generates the challenge for the client and stores it in its context. When the TYPE3 token is received as the final
    //  step, this uses the stored challenge to validate the TYPE-3 token (response to the challenge). As this maintains the
    //  status of the handshake, there should be only one instance of 'provider', hence it is made static.
    private static WindowsAuthProviderImpl provider = new WindowsAuthProviderImpl();

    public int getNLTMMessageType(byte[] decodedNLTMMessage) throws IdentityOAuth2Exception {
        int messageType;
        if (decodedNLTMMessage.length > MESSAGE_TYPE_BYTE_INDEX) {
            messageType = decodedNLTMMessage[MESSAGE_TYPE_BYTE_INDEX];
        } else {
            throw new IdentityOAuth2Exception(
                    "Cannot extract message type from NLTM Token. Decoded token length is less than 8.");
        }

        //NLTM token type must be one of 1,2 or 3
        if (messageType < NTLM_TYPE_1_TOKEN || messageType > NTLM_TYPE_3_TOKEN) {
            throw new IdentityOAuth2Exception(
                    "Invalid NLTM message type:" + messageType + ". Should be one of 1,2 or 3.");
        }

        return messageType;
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        boolean validGrant = super.validateGrant(tokReqMsgCtx);

        if (!validGrant) {
            return false;
        }

        String token = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getWindowsToken();

        IWindowsSecurityContext serverContext = null;
        if (token != null) {

            byte[] bytesToken = Base64.decode(token);
            int tokenType = getNLTMMessageType(bytesToken);

            if (log.isDebugEnabled()) {
                log.debug("Received NTLM token Type " + tokenType + ":" + token);
            }

            if (tokenType == NTLM_TYPE_1_TOKEN) {
                serverContext = provider.acceptSecurityToken(SERVER_CONNECTION, bytesToken, SECURITY_PACKAGE);
                String type2Token = Base64.encode(serverContext.getToken());
                if (log.isDebugEnabled()) {
                    log.debug("Sent NTLM token Type 2:" + type2Token);
                }
                ResponseHeader[] responseHeaders = new ResponseHeader[1];
                responseHeaders[0] = new ResponseHeader();
                responseHeaders[0].setKey(HEADER_WWW_AUTHENTICATE);
                responseHeaders[0].setValue(SCHEME_NTLM + " " + type2Token);
                tokReqMsgCtx.addProperty(RESPONSE_HEADERS_PROPERTY, responseHeaders);
                return false;
            } else if (tokenType == NTLM_TYPE_3_TOKEN) {
                serverContext = provider.acceptSecurityToken(SERVER_CONNECTION, bytesToken, SECURITY_PACKAGE);
                String resourceOwnerUserNameWithDomain = serverContext.getIdentity().getFqn();
                String resourceOwnerUserName = resourceOwnerUserNameWithDomain.split("\\\\")[1];
                tokReqMsgCtx.setAuthorizedUser(OAuth2Util.getUserFromUserName(resourceOwnerUserName));
                return true;
            } else {
                log.error("Unknown NTLM token, Type " + tokenType + ":" + token);
                return false;
            }

        } else {
            throw new IdentityOAuth2Exception("Received NTLM token is null");
        }
    }
}