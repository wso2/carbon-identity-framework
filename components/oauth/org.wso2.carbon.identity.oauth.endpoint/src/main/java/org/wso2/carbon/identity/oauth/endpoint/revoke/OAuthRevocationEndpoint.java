/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth.endpoint.revoke;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.identity.oauth.endpoint.OAuthRequestWrapper;
import org.wso2.carbon.identity.oauth.endpoint.util.EndpointUtil;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Enumeration;

@Path("/revoke")
public class OAuthRevocationEndpoint {

    private static final Log log = LogFactory.getLog(OAuthRevocationEndpoint.class);
    private static final String TOKEN_PARAM = "token";
    private static final String TOKEN_TYPE_HINT_PARAM = "token_type_hint";
    private static final String CALLBACK_PARAM = "callback";

    @POST
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    public Response revokeAccessToken(@Context HttpServletRequest request,
                                      MultivaluedMap<String, String> paramMap) throws OAuthSystemException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            HttpServletRequestWrapper httpRequest = new OAuthRequestWrapper(request, paramMap);

            String token = httpRequest.getParameter(TOKEN_PARAM);
            if (StringUtils.isBlank(token) && paramMap.get(TOKEN_PARAM) != null &&
                    !paramMap.get(TOKEN_PARAM).isEmpty()) {
                token = paramMap.get(TOKEN_PARAM).get(0);
            }
            String tokenType = httpRequest.getParameter(TOKEN_TYPE_HINT_PARAM);
            if (StringUtils.isBlank(tokenType) && paramMap.get(TOKEN_TYPE_HINT_PARAM) != null && !paramMap
                    .get(TOKEN_TYPE_HINT_PARAM).isEmpty()) {
                tokenType = paramMap.get(TOKEN_TYPE_HINT_PARAM).get(0);
            }
            String callback = httpRequest.getParameter(CALLBACK_PARAM);
            if (StringUtils.isBlank(callback) && paramMap.get(CALLBACK_PARAM) != null && !paramMap.get
                    (CALLBACK_PARAM).isEmpty()) {
                callback = paramMap.get(CALLBACK_PARAM).get(0);
            }

            // extract the basic auth credentials if present in the request and use for
            // authentication.
            if (request.getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ) != null) {
                try {
                    String[] clientCredentials = EndpointUtil.extractCredentialsFromAuthzHeader(
                            request.getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ));

                    // The client MUST NOT use more than one authentication method in each request
                    if (paramMap.containsKey(OAuth.OAUTH_CLIENT_ID) &&
                            paramMap.containsKey(OAuth.OAUTH_CLIENT_SECRET)) {
                        return handleBasicAuthFailure(callback);
                    }

                    if(clientCredentials.length != 2){
                        handleBasicAuthFailure(callback);
                    }

                    // add the credentials available in Authorization to the parameter map
                    paramMap.add(OAuth.OAUTH_CLIENT_ID, clientCredentials[0]);
                    paramMap.add(OAuth.OAUTH_CLIENT_SECRET, clientCredentials[1]);
                } catch (OAuthClientException e) {
                    // malformed credential string is considered as an auth failure.
                    log.error("Error while extracting credentials from authorization header", e);
                    return handleBasicAuthFailure(callback);
                }
            }

            try {
                OAuthRevocationRequestDTO revokeRequest = new OAuthRevocationRequestDTO();
                if (paramMap.get(OAuth.OAUTH_CLIENT_ID) != null) {
                    revokeRequest.setConsumerKey(paramMap.get(OAuth.OAUTH_CLIENT_ID).get(0));
                }
                if (paramMap.get(OAuth.OAUTH_CLIENT_SECRET) != null) {
                    revokeRequest.setConsumerSecret(paramMap.get(OAuth.OAUTH_CLIENT_SECRET).get(0));
                }
                if (StringUtils.isNotEmpty(token)) {
                    revokeRequest.setToken(token);
                } else {
                    handleClientFailure(callback);
                }
                if (StringUtils.isNotEmpty(tokenType)) {
                    revokeRequest.setToken_type(tokenType);
                }
                OAuthRevocationResponseDTO oauthRevokeResp = revokeTokens(revokeRequest);
                // if there BE has returned an error
                if (oauthRevokeResp.getErrorMsg() != null) {
                    // if there is an auth failure, HTTP 401 Status Code should be sent back to the client.
                    if (OAuth2ErrorCodes.INVALID_CLIENT.equals(oauthRevokeResp.getErrorCode())) {
                        return handleBasicAuthFailure(callback);
                    } else if (OAuth2ErrorCodes.UNAUTHORIZED_CLIENT.equals(oauthRevokeResp.getErrorCode())) {
                        return handleAuthorizationFailure(callback);
                    }
                    // Otherwise send back HTTP 400 Status Code
                    return handleClientFailure(callback, oauthRevokeResp);
                } else {
                    OAuthResponse response;
                    if (StringUtils.isNotEmpty(callback)) {
                        response = CarbonOAuthASResponse.revokeResponse(HttpServletResponse.SC_OK).buildBodyMessage();
                        response.setBody(callback + "();");
                    } else {
                        response = CarbonOAuthASResponse.revokeResponse(HttpServletResponse.SC_OK).buildBodyMessage();
                    }
                    ResponseHeader[] headers = oauthRevokeResp.getResponseHeaders();
                    ResponseBuilder respBuilder = Response
                            .status(response.getResponseStatus())
                            .header(OAuthConstants.HTTP_RESP_HEADER_CACHE_CONTROL,
                                    OAuthConstants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE)
                            .header(HTTPConstants.HEADER_CONTENT_LENGTH,
                                    "0")
                            .header(OAuthConstants.HTTP_RESP_HEADER_PRAGMA,
                                    OAuthConstants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);
                    if (headers != null && headers.length > 0) {
                        for (int i = 0; i < headers.length; i++) {
                            if (headers[i] != null) {
                                respBuilder.header(headers[i].getKey(), headers[i].getValue());
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(callback)) {
                        respBuilder.header("Content-Type", "application/javascript");
                    } else {
                        respBuilder.header("Content-Type", "text/html");
                    }

                    return respBuilder.entity(response.getBody()).build();
                }

            } catch (OAuthClientException e) {
                return handleServerFailure(callback, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    private Response handleBasicAuthFailure(String callback)
            throws OAuthSystemException {
        if (callback == null || "".equals(callback)) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.INVALID_CLIENT)
                    .setErrorDescription("Client Authentication failed.").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.INVALID_CLIENT).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "application/javascript")
                    .entity(callback + "(" + response.getBody() + ");").build();
        }
    }

    private Response handleAuthorizationFailure(String callback)
            throws OAuthSystemException {
        if (callback == null || "".equals(callback)) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT)
                    .setErrorDescription("Client Authentication failed.").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "application/javascript")
                    .entity(callback + "(" + response.getBody() + ");").build();
        }
    }

    private Response handleServerFailure(String callback, Exception e)
            throws OAuthSystemException {
        if (callback == null || "".equals(callback)) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .setError(OAuth2ErrorCodes.SERVER_ERROR)
                    .setErrorDescription(e.getMessage()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .setError(OAuth2ErrorCodes.SERVER_ERROR).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback + "(" + response.getBody() + ");").build();
        }
    }

    private Response handleClientFailure(String callback)
            throws OAuthSystemException {
        if (callback == null || "".equals(callback)) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuth2ErrorCodes.INVALID_REQUEST)
                    .setErrorDescription("Invalid revocation request").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuth2ErrorCodes.INVALID_REQUEST).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback + "(" + response.getBody() + ");").build();
        }
    }

    private Response handleClientFailure(String callback, OAuthRevocationResponseDTO dto)
            throws OAuthSystemException {
        if (callback == null || "".equals(callback)) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(dto.getErrorCode())
                    .setErrorDescription(dto.getErrorMsg()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(dto.getErrorCode()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback + "(" + response.getBody() + ");").build();
        }
    }

    private OAuthRevocationResponseDTO revokeTokens(OAuthRevocationRequestDTO oauthRequest)
            throws OAuthClientException {

        OAuthRevocationRequestDTO revokeReqDTO = new OAuthRevocationRequestDTO();

        revokeReqDTO.setConsumerKey(oauthRequest.getConsumerKey());
        revokeReqDTO.setConsumerSecret(oauthRequest.getConsumerSecret());
        revokeReqDTO.setToken(oauthRequest.getToken());
        revokeReqDTO.setToken_type(oauthRequest.getToken_type());

        return EndpointUtil.getOAuth2Service().revokeTokenByOAuthClient(revokeReqDTO);
    }
}
