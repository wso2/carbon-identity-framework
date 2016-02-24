/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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
package org.wso2.carbon.identity.sso.saml.processors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Response;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.builders.ErrorResponseBuilder;
import org.wso2.carbon.identity.sso.saml.builders.ResponseBuilder;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSORespDTO;
import org.wso2.carbon.identity.sso.saml.session.SSOSessionPersistenceManager;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.util.ArrayList;
import java.util.List;

public class SPInitSSOAuthnRequestProcessor implements SSOAuthnRequestProcessor{

    private static Log log = LogFactory.getLog(SPInitSSOAuthnRequestProcessor.class);

    public SAMLSSORespDTO process(SAMLSSOAuthnReqDTO authnReqDTO, String sessionId,
                                  boolean isAuthenticated, String authenticators, String authMode) throws Exception {

        try {
            SAMLSSOServiceProviderDO serviceProviderConfigs = getServiceProviderConfig(authnReqDTO);

            if (serviceProviderConfigs == null) {
                String msg =
                        "A Service Provider with the Issuer '" + authnReqDTO.getIssuer() +
                                "' is not registered." +
                                " Service Provider should be registered in advance.";
                log.warn(msg);
                return buildErrorResponse(authnReqDTO.getId(),
                        SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg, null);
            }

            // reading the service provider configs
            populateServiceProviderConfigs(serviceProviderConfigs, authnReqDTO);

            if (authnReqDTO.isDoValidateSignatureInRequests()) {


                List<String> idpUrlSet = SAMLSSOUtil.getDestinationFromTenantDomain(authnReqDTO.getTenantDomain());

                if (authnReqDTO.getDestination() == null
                        || !idpUrlSet.contains(authnReqDTO.getDestination())) {
                    String msg = "Destination validation for Authentication Request failed. " +
                            "Received: [" + authnReqDTO.getDestination() + "]." +
                            " Expected one in the list: [" + StringUtils.join(idpUrlSet, ',') + "]";
                    log.warn(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                            SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg, null);
                }

                // validate the signature
                boolean isSignatureValid = SAMLSSOUtil.validateAuthnRequestSignature(authnReqDTO);

                if (!isSignatureValid) {
                    String msg = "Signature validation for Authentication Request failed.";
                    log.warn(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                            SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg, null);
                }
            } else {
                //Validate the assertion consumer url,  only if request is not signed.
                String acsUrl = authnReqDTO.getAssertionConsumerURL();
                if (StringUtils.isBlank(acsUrl) || !serviceProviderConfigs.getAssertionConsumerUrlList().contains
                        (acsUrl)) {
                    String msg = "ALERT: Invalid Assertion Consumer URL value '" + acsUrl + "' in the " +
                            "AuthnRequest message from  the issuer '" + serviceProviderConfigs.getIssuer() +
                            "'. Possibly " + "an attempt for a spoofing attack";
                    log.error(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                            SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg, acsUrl);
                }
            }

            // if subject is specified in AuthnRequest only that user should be
            // allowed to logged-in
            if (authnReqDTO.getSubject() != null && authnReqDTO.getUser() != null) {
                String authenticatedSubjectIdentifier =
                        authnReqDTO.getUser().getAuthenticatedSubjectIdentifier();
                if (authenticatedSubjectIdentifier != null &&
                        !authenticatedSubjectIdentifier.equals(authnReqDTO.getSubject())) {
                    String msg = "Provided username does not match with the requested subject";
                    log.warn(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                            SAMLSSOConstants.StatusCodes.AUTHN_FAILURE, msg, authnReqDTO.getAssertionConsumerURL());
                }
            }

            // persist the session
            SSOSessionPersistenceManager sessionPersistenceManager = SSOSessionPersistenceManager.getPersistenceManager();

            SAMLSSORespDTO samlssoRespDTO = null;
            String sessionIndexId = null;

            if (isAuthenticated) {
                if (sessionId != null && sessionPersistenceManager.isExistingTokenId(sessionId)) {
                    sessionIndexId = sessionPersistenceManager.getSessionIndexFromTokenId(sessionId);
                } else {
                    sessionIndexId = UUIDGenerator.generateUUID();
                    sessionPersistenceManager.persistSession(sessionId, sessionIndexId);
                }

                //TODO check whether the same SP exists

                if (authMode.equals(SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD)) {
                    SAMLSSOServiceProviderDO spDO = new SAMLSSOServiceProviderDO();
                    spDO.setIssuer(authnReqDTO.getIssuer());
                    spDO.setAssertionConsumerUrl(authnReqDTO.getAssertionConsumerURL());
                    spDO.setCertAlias(authnReqDTO.getCertAlias());
                    spDO.setSloResponseURL(authnReqDTO.getSloResponseURL());
                    spDO.setSloRequestURL(authnReqDTO.getSloRequestURL());
                    spDO.setTenantDomain(authnReqDTO.getTenantDomain());
                    spDO.setNameIDFormat(authnReqDTO.getNameIDFormat());
                    spDO.setDoSingleLogout(authnReqDTO.isDoSingleLogout());
                    spDO.setIdPInitSLOEnabled(authnReqDTO.isIdPInitSLOEnabled());
                    spDO.setAssertionConsumerUrls(authnReqDTO.getAssertionConsumerURLs());
                    spDO.setIdpInitSLOReturnToURLs(authnReqDTO.getIdpInitSLOReturnToURLs());
                    spDO.setDoSignResponse(authnReqDTO.isDoSignResponse());
                    spDO.setSigningAlgorithmUri(authnReqDTO.getSigningAlgorithmUri());
                    spDO.setDigestAlgorithmUri(authnReqDTO.getDigestAlgorithmUri());
                    sessionPersistenceManager.persistSession(sessionIndexId,
                            authnReqDTO.getUser().getAuthenticatedSubjectIdentifier(),
                            spDO, authnReqDTO.getRpSessionId(),
                            authnReqDTO.getIssuer(),
                            authnReqDTO.getAssertionConsumerURL());
                }

                // Build the response for the successful scenario
                ResponseBuilder respBuilder = SAMLSSOUtil.getResponseBuilder();
                Response response = respBuilder.buildResponse(authnReqDTO, sessionIndexId);
                samlssoRespDTO = new SAMLSSORespDTO();
                String samlResp = SAMLSSOUtil.marshall(response);

                if (log.isDebugEnabled()) {
                    log.debug(samlResp);
                }

                samlssoRespDTO.setRespString(SAMLSSOUtil.encode(samlResp));
                samlssoRespDTO.setSessionEstablished(true);
                samlssoRespDTO.setAssertionConsumerURL(authnReqDTO.getAssertionConsumerURL());
                samlssoRespDTO.setLoginPageURL(authnReqDTO.getLoginPageURL());
                samlssoRespDTO.setSubject(authnReqDTO.getUser());
            }

            if (samlssoRespDTO.getRespString() != null) {
                if (log.isDebugEnabled()) {
                    log.debug(samlssoRespDTO.getRespString());
                }
            }

            return samlssoRespDTO;
        } catch (Exception e) {
            log.error("Error processing the authentication request", e);
            SAMLSSORespDTO errorResp =
                    buildErrorResponse(authnReqDTO.getId(),
                            SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                            "Authentication Failure, invalid username or password.", null);
            errorResp.setLoginPageURL(authnReqDTO.getLoginPageURL());
            return errorResp;
        }
    }


    /**
     * Returns the configured service provider configurations. The
     * configurations are taken from the user registry or from the
     * sso-idp-config.xml configuration file. In Stratos deployment the
     * configurations are read from the sso-idp-config.xml file.
     *
     * @param authnReqDTO
     * @return
     * @throws IdentityException
     */
    private SAMLSSOServiceProviderDO getServiceProviderConfig(SAMLSSOAuthnReqDTO authnReqDTO)
            throws IdentityException {
        try {
            SSOServiceProviderConfigManager stratosIdpConfigManager = SSOServiceProviderConfigManager
                    .getInstance();
            SAMLSSOServiceProviderDO ssoIdpConfigs = stratosIdpConfigManager
                    .getServiceProvider(authnReqDTO.getIssuer());
            if (ssoIdpConfigs == null) {
                IdentityTenantUtil.initializeRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getTenantId(), PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
                IdentityPersistenceManager persistenceManager = IdentityPersistenceManager.getPersistanceManager();
                Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry
                        (RegistryType.SYSTEM_CONFIGURATION);
                ssoIdpConfigs = persistenceManager.getServiceProvider(registry, authnReqDTO.getIssuer());
                authnReqDTO.setStratosDeployment(false); // not stratos
            } else {
                authnReqDTO.setStratosDeployment(true); // stratos deployment
            }
            return ssoIdpConfigs;
        } catch (Exception e) {
            throw IdentityException.error("Error while reading Service Provider configurations", e);
        }
    }

    /**
     * Populate the configurations of the service provider
     *
     * @param ssoIdpConfigs
     * @param authnReqDTO
     * @throws IdentityException
     */
    private void populateServiceProviderConfigs(SAMLSSOServiceProviderDO ssoIdpConfigs,
                                                SAMLSSOAuthnReqDTO authnReqDTO)
            throws IdentityException {

        // load the ACS url, if it is not defined in the request. If it is sent in request,  if must owner it.
        String acsUrl = authnReqDTO.getAssertionConsumerURL();
        if (StringUtils.isBlank(acsUrl)) {
            authnReqDTO.setAssertionConsumerURL(ssoIdpConfigs.getDefaultAssertionConsumerUrl());
        }
        authnReqDTO.setLoginPageURL(ssoIdpConfigs.getLoginPageURL());
        authnReqDTO.setCertAlias(ssoIdpConfigs.getCertAlias());
        authnReqDTO.setNameIdClaimUri(ssoIdpConfigs.getNameIdClaimUri());
        authnReqDTO.setNameIDFormat(ssoIdpConfigs.getNameIDFormat());
        authnReqDTO.setDoSingleLogout(ssoIdpConfigs.isDoSingleLogout());
        authnReqDTO.setSloResponseURL(ssoIdpConfigs.getSloResponseURL());
        authnReqDTO.setSloRequestURL(ssoIdpConfigs.getSloRequestURL());
        authnReqDTO.setDoSignResponse(ssoIdpConfigs.isDoSignResponse());
        authnReqDTO.setDoSignAssertions(ssoIdpConfigs.isDoSignAssertions());
        authnReqDTO.setRequestedClaims(ssoIdpConfigs.getRequestedClaims());
        authnReqDTO.setRequestedAudiences(ssoIdpConfigs.getRequestedAudiences());
        authnReqDTO.setRequestedRecipients(ssoIdpConfigs.getRequestedRecipients());
        authnReqDTO.setDoEnableEncryptedAssertion(ssoIdpConfigs.isDoEnableEncryptedAssertion());
        authnReqDTO.setDoValidateSignatureInRequests(ssoIdpConfigs.isDoValidateSignatureInRequests());
        authnReqDTO.setIdPInitSLOEnabled(ssoIdpConfigs.isIdPInitSLOEnabled());
        authnReqDTO.setAssertionConsumerURLs(ssoIdpConfigs.getAssertionConsumerUrls());
        authnReqDTO.setIdpInitSLOReturnToURLs(ssoIdpConfigs.getIdpInitSLOReturnToURLs());
        authnReqDTO.setSigningAlgorithmUri(ssoIdpConfigs.getSigningAlgorithmUri());
        authnReqDTO.setDigestAlgorithmUri(ssoIdpConfigs.getDigestAlgorithmUri());
    }

    /**
     * @param id
     * @param status
     * @param statMsg
     * @return
     * @throws Exception
     */
    private SAMLSSORespDTO buildErrorResponse(String id, String status,
                                              String statMsg, String destination) throws Exception {
        SAMLSSORespDTO samlSSORespDTO = new SAMLSSORespDTO();
        ErrorResponseBuilder errRespBuilder = new ErrorResponseBuilder();
        List<String> statusCodeList = new ArrayList<String>();
        statusCodeList.add(status);
        Response resp = errRespBuilder.buildResponse(id, statusCodeList, statMsg, destination);
        String encodedResp = SAMLSSOUtil.compressResponse(SAMLSSOUtil.marshall(resp));
        samlSSORespDTO.setRespString(encodedResp);
        samlSSORespDTO.setSessionEstablished(false);
        return samlSSORespDTO;
    }
}
