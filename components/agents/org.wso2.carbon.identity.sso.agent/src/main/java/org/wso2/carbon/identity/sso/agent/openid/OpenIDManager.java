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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.identity.sso.agent.openid;

import org.apache.commons.collections.MapUtils;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.yadis.YadisException;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentDataHolder;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.bean.LoggedInSessionBean;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenIDManager {

    // Smart OpenID Consumer Manager
    AttributesRequestor attributesRequestor = null;
    private SSOAgentConfig ssoAgentConfig = null;

    public OpenIDManager(SSOAgentConfig ssoAgentConfig) throws SSOAgentException {
        SSOAgentDataHolder.getInstance().setConsumerManager(getConsumerManagerInstance());
        this.ssoAgentConfig = ssoAgentConfig;
    }

    private ConsumerManager getConsumerManagerInstance() throws SSOAgentException {

        HttpFetcherFactory httpFetcherFactory = null;
        try {
            httpFetcherFactory = new HttpFetcherFactory(SSLContext.getDefault(), null);
        } catch (NoSuchAlgorithmException e) {
            throw new SSOAgentException("Error while getting default SSL Context", e);
        }
        return new ConsumerManager(
                new RealmVerifierFactory(new YadisResolver(httpFetcherFactory)),
                new Discovery(), httpFetcherFactory);
    }

    public String doOpenIDLogin(HttpServletRequest request, HttpServletResponse response) throws SSOAgentException {

        String claimedId = ssoAgentConfig.getOpenId().getClaimedId();

        try {
            ConsumerManager manager = SSOAgentDataHolder.getInstance().getConsumerManager();

            if (ssoAgentConfig.getOpenId().isDumbModeEnabled()) {
                // Switch the consumer manager to dumb mode
                manager.setMaxAssocAttempts(0);
            }

            // Discovery on the user supplied ID
            List discoveries = manager.discover(claimedId);

            // Associate with the OP and share a secret
            DiscoveryInformation discovered = manager.associate(discoveries);

            // Keeping necessary parameters to verify the AuthResponse
            LoggedInSessionBean sessionBean = new LoggedInSessionBean();
            sessionBean.setOpenId(sessionBean.new OpenID());
            sessionBean.getOpenId().setDiscoveryInformation(discovered); // set the discovery information
            request.getSession().setAttribute(SSOAgentConstants.SESSION_BEAN_NAME, sessionBean);

            manager.setImmediateAuth(true);
            AuthRequest authReq = manager.authenticate(discovered,
                    ssoAgentConfig.getOpenId().getReturnToURL());


            // Request subject attributes using Attribute Exchange extension specification if AttributeExchange is enabled
            if (ssoAgentConfig.getOpenId().isAttributeExchangeEnabled() &&
                    ssoAgentConfig.getOpenId().getAttributesRequestor() != null) {

                attributesRequestor = ssoAgentConfig.getOpenId().getAttributesRequestor();
                attributesRequestor.init();

                String[] requestedAttributes = attributesRequestor.getRequestedAttributes(claimedId);

                // Getting required attributes using FetchRequest
                FetchRequest fetchRequest = FetchRequest.createFetchRequest();

                for (String requestedAttribute : requestedAttributes) {
                    fetchRequest.addAttribute(requestedAttribute,
                            attributesRequestor.getTypeURI(claimedId, requestedAttribute),
                            attributesRequestor.isRequired(claimedId, requestedAttribute),
                            attributesRequestor.getCount(claimedId, requestedAttribute));
                }

                // Adding the AX extension to the AuthRequest message
                authReq.addExtension(fetchRequest);
            }

            // Returning OP Url
            SSOAgentDataHolder.getInstance().setConsumerManager(manager);
            StringBuilder destinationUrl = new StringBuilder(authReq.getDestinationUrl(true));

            if (MapUtils.isNotEmpty(ssoAgentConfig.getQueryParams())) {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, String[]> entry : ssoAgentConfig.getQueryParams().entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null && entry.getValue().length > 0) {
                        for (String param : entry.getValue()) {
                            builder.append("&").append(entry.getKey()).append("=").append(param);
                        }
                    }
                }
                destinationUrl.append(builder);
            }
            return destinationUrl.toString();

        } catch (YadisException e) {
            if (e.getErrorCode() == 1796) {
                throw new SSOAgentException(e.getMessage(), e);
            }
            throw new SSOAgentException("Error while creating FetchRequest", e);
        } catch (MessageException e) {
            throw new SSOAgentException("Error while creating FetchRequest", e);
        } catch (DiscoveryException e) {
            throw new SSOAgentException("Error while doing OpenID Discovery", e);
        } catch (ConsumerException e) {
            throw new SSOAgentException("Error while doing OpenID Authentication", e);
        }
    }

    public void processOpenIDLoginResponse(HttpServletRequest request, HttpServletResponse response) throws SSOAgentException {

        try {
            // Getting all parameters in request including AuthResponse
            ParameterList authResponseParams = new ParameterList(request.getParameterMap());

            // Get previously saved session bean
            LoggedInSessionBean loggedInSessionBean = (LoggedInSessionBean) request.getSession(false).
                    getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
            if (loggedInSessionBean == null) {
                throw new SSOAgentException("Error while verifying OpenID response. " +
                        "Cannot find valid session for user");
            }

            // Previously discovered information
            DiscoveryInformation discovered = loggedInSessionBean.getOpenId().getDiscoveryInformation();

            // Verify return-to, discoveries, nonce & signature
            // Signature will be verified using the shared secret
            VerificationResult verificationResult = SSOAgentDataHolder.getInstance().getConsumerManager().verify(
                    ssoAgentConfig.getOpenId().getReturnToURL(), authResponseParams, discovered);

            Identifier verified = verificationResult.getVerifiedId();

            // Identifier will be NULL if verification failed
            if (verified != null) {

                AuthSuccess authSuccess = (AuthSuccess) verificationResult.getAuthResponse();

                loggedInSessionBean.getOpenId().setClaimedId(authSuccess.getIdentity());

                // Get requested attributes using AX extension
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    Map<String, List<String>> attributesMap = new HashMap<String, List<String>>();
                    if (ssoAgentConfig.getOpenId().getAttributesRequestor() != null) {
                        attributesRequestor = ssoAgentConfig.getOpenId().getAttributesRequestor();
                        String[] attrArray = attributesRequestor.getRequestedAttributes(authSuccess.getIdentity());
                        FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                        for (String attr : attrArray) {
                            List attributeValues = fetchResp.getAttributeValuesByTypeUri(attributesRequestor.getTypeURI(authSuccess.getIdentity(), attr));
                            if (attributeValues.get(0) instanceof String && ((String) attributeValues.get(0)).split(",").length > 1) {
                                String[] splitString = ((String) attributeValues.get(0)).split(",");
                                for (String part : splitString) {
                                    attributeValues.add(part);
                                }
                            }
                            if (attributeValues.get(0) != null) {
                                attributesMap.put(attr, attributeValues);
                            }
                        }
                    }
                    loggedInSessionBean.getOpenId().setSubjectAttributes(attributesMap);
                }

            } else {
                throw new SSOAgentException("OpenID verification failed");
            }

        } catch (AssociationException e) {
            throw new SSOAgentException("Error while verifying OpenID response", e);
        } catch (MessageException e) {
            throw new SSOAgentException("Error while verifying OpenID response", e);
        } catch (DiscoveryException e) {
            throw new SSOAgentException("Error while verifying OpenID response", e);
        }

    }



}
