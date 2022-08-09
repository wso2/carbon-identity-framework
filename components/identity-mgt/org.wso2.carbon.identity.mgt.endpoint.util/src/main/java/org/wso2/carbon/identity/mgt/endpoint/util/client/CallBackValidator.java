/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;

import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceStub;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.URLEncoder;

/**
 * Validates the call back URL passed in the recovery request.
 */
public class CallBackValidator {

    private static final Log log = LogFactory.getLog(CallBackValidator.class);

    /**
     * This method is to validate the callback URL in the request with the configured one.
     *
     * @param callbackURL  CallbackURL Passed in the request.
     * @param tenantDomain TenantDomain of the user.
     * @return The status of the validation.
     * @throws IdentityRecoveryException IdentityRecoveryException.
     */
    public boolean isValidCallbackURL(String callbackURL, String tenantDomain)
            throws IdentityRecoveryException {

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain is considered as super tenant domain: " + tenantDomain);
            }
        }

        IdentityProvider residentIdP;

        // Build the service URL of idp management admin service
        StringBuilder builder = new StringBuilder();
        String serviceURL = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL())
                .append(IdentityManagementEndpointConstants.ServiceEndpoints.IDENTITY_PROVIDER_MANAGEMENT_SERVICE)
                .toString().replaceAll("(?<!(http:|https:))//", "/");
        try {
            IdentityProviderMgtServiceStub idPMgtStub = new IdentityProviderMgtServiceStub(serviceURL);
            ServiceClient idpClient = idPMgtStub._getServiceClient();
            IdentityManagementEndpointUtil.authenticate(idpClient);
            residentIdP = idPMgtStub.getResidentIdP();
        } catch (AxisFault axisFault) {
            if (log.isDebugEnabled()) {
                log.debug(axisFault);
            }
            throw new IdentityRecoveryException("Error while authenticating the user or getting residentIDP configurations.", axisFault);
        } catch (Exception e) {
            throw new IdentityRecoveryException("Error occurred when getting residentIDP configurations.", e);
        }

        IdentityProviderProperty[] idpProperties = null;
        if (residentIdP != null) {
            idpProperties = residentIdP.getIdpProperties();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Resident identity provider is not found for the tenant domain: " + tenantDomain);
            }
        }

        String callbackRegex = null;
        if (idpProperties != null) {
            for (IdentityProviderProperty property : idpProperties) {
                if (IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_CALLBACK_REGEX.equals(property
                        .getValue())) {
                    callbackRegex = property.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("Configured recovery callback URL regex: " + callbackRegex);
                    }
                    break;
                }
            }
        }

        if (StringUtils.isNotBlank(callbackURL)) {
            try {
                String encodeURL = URLEncoder.encode(callbackURL, IdentityManagementEndpointConstants.UTF_8);
                URI uri = new URI(encodeURL);
                callbackURL = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null)
                        .toString();
                if (log.isDebugEnabled()) {
                    log.debug("Callback URL in the username recovery request: " + callbackURL);
                }
            } catch (URISyntaxException e) {
                throw new IdentityRecoveryException("Error occurred while formatting the provided callback URL. ", e);
            } catch (UnsupportedEncodingException e) {
                throw new IdentityRecoveryException("Error occurred while encoding the provided callback URL.", e);
            }
        }

        return callbackRegex == null || callbackURL.matches(callbackRegex);
    }

    /**
     * This method is to validate the callback URL in the request with the configured one.
     *
     * @param callbackURL     CallbackURL Passed in the request.
     * @param tenantDomain    TenantDomain of the user.
     * @param isUserPortalURL Whether this is the user portal url.
     * @return The status of the validation.
     * @throws IdentityRecoveryException IdentityRecoveryException.
     * @deprecated As of release 5.17.61, replaced by {@link #isValidCallbackURL(String, String)}
     */
    @Deprecated
    public boolean isValidCallbackURL(String callbackURL, String tenantDomain, boolean isUserPortalURL)
            throws IdentityRecoveryException {

        if (isUserPortalURL) {
            if (log.isDebugEnabled()) {
                log.debug("Callback URL is equal to the user portal URL: " + callbackURL);
            }
            return true;
        }
        return isValidCallbackURL(callbackURL, tenantDomain);
    }
}
