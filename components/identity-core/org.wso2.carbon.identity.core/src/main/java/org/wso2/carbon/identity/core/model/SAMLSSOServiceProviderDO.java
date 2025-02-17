/*
 * Copyright 2005-2025 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.model;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.MultiValuedPropertyKey;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.BACKCHANNEL_LOGOUT_BINDING;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SAMLSSOServiceProviderDO implements Serializable {

    private static final long serialVersionUID = 7998724745099007704L;

    String tenantDomain;
    private String issuer;
    private String issuerQualifier;
    private String assertionConsumerUrl;
    private String[] assertionConsumerUrls;
    private List<String> assertionConsumerUrlList;
    private String defaultAssertionConsumerUrl;
    private String certAlias;
    private String sloResponseURL;
    private String sloRequestURL;
    private boolean doSingleLogout;
    private String loginPageURL;
    private boolean doSignResponse;
    private boolean doSignAssertions;
    private String attributeConsumingServiceIndex;
    private String[] requestedClaims;
    private List<String> requestedClaimsList;
    private String[] requestedAudiences;
    private List<String> requestedAudiencesList;
    private String[] requestedRecipients;
    private List<String> requestedRecipientsList;
    private boolean enableAttributesByDefault;
    private String attributeNameFormat;
    private String nameIdClaimUri;
    private String nameIDFormat;
    private boolean isIdPInitSSOEnabled;
    private boolean idPInitSLOEnabled;
    private String[] idpInitSLOReturnToURLs;
    private List<String> idpInitSLOReturnToURLList;
    private boolean doEnableEncryptedAssertion;
    private boolean doValidateSignatureInRequests;
    private boolean doValidateSignatureInArtifactResolve;
    private String signingAlgorithmUri;
    private String digestAlgorithmUri;
    private String assertionEncryptionAlgorithmUri;
    private String keyEncryptionAlgorithmUri;
    private String signingCertificate;
    private String encryptionCertificate;
    private X509Certificate x509Certificate;
    private boolean isAssertionQueryRequestProfileEnabled;
    private String supportedAssertionQueryRequestTypes;
    private boolean enableSAML2ArtifactBinding;
    private boolean samlECP;
    private String idpEntityIDAlias;
    private boolean doFrontChannelLogout;
    private String frontChannelLogoutBinding;

    public SAMLSSOServiceProviderDO(SAMLSSOServiceProviderDO samlSSOServiceProviderDO) {

        this.tenantDomain = samlSSOServiceProviderDO.tenantDomain;
        this.issuer = samlSSOServiceProviderDO.issuer;
        this.issuerQualifier = samlSSOServiceProviderDO.issuerQualifier;
        this.assertionConsumerUrl = samlSSOServiceProviderDO.assertionConsumerUrl;
        this.assertionConsumerUrls = samlSSOServiceProviderDO.assertionConsumerUrls != null ?
                samlSSOServiceProviderDO.assertionConsumerUrls.clone() : null;
        this.assertionConsumerUrlList = samlSSOServiceProviderDO.assertionConsumerUrlList != null ?
                new ArrayList<>(samlSSOServiceProviderDO.assertionConsumerUrlList) : null;
        this.defaultAssertionConsumerUrl = samlSSOServiceProviderDO.defaultAssertionConsumerUrl;
        this.certAlias = samlSSOServiceProviderDO.certAlias;
        this.sloResponseURL = samlSSOServiceProviderDO.sloResponseURL;
        this.sloRequestURL = samlSSOServiceProviderDO.sloRequestURL;
        this.doSingleLogout = samlSSOServiceProviderDO.doSingleLogout;
        this.loginPageURL = samlSSOServiceProviderDO.loginPageURL;
        this.doSignResponse = samlSSOServiceProviderDO.doSignResponse;
        this.doSignAssertions = samlSSOServiceProviderDO.doSignAssertions;
        this.attributeConsumingServiceIndex = samlSSOServiceProviderDO.attributeConsumingServiceIndex;
        this.requestedClaims =
                samlSSOServiceProviderDO.requestedClaims != null ? samlSSOServiceProviderDO.requestedClaims.clone() :
                        null;
        this.requestedClaimsList = samlSSOServiceProviderDO.requestedClaimsList != null ?
                new ArrayList<>(samlSSOServiceProviderDO.requestedClaimsList) : null;
        this.requestedAudiences = samlSSOServiceProviderDO.requestedAudiences != null ?
                samlSSOServiceProviderDO.requestedAudiences.clone() : null;
        this.requestedAudiencesList = samlSSOServiceProviderDO.requestedAudiencesList != null ?
                new ArrayList<>(samlSSOServiceProviderDO.requestedAudiencesList) : null;
        this.requestedRecipients = samlSSOServiceProviderDO.requestedRecipients != null ?
                samlSSOServiceProviderDO.requestedRecipients.clone() : null;
        this.requestedRecipientsList = samlSSOServiceProviderDO.requestedRecipientsList != null ?
                new ArrayList<>(samlSSOServiceProviderDO.requestedRecipientsList) : null;
        this.enableAttributesByDefault = samlSSOServiceProviderDO.enableAttributesByDefault;
        this.nameIdClaimUri = samlSSOServiceProviderDO.nameIdClaimUri;
        this.nameIDFormat = samlSSOServiceProviderDO.nameIDFormat;
        this.isIdPInitSSOEnabled = samlSSOServiceProviderDO.isIdPInitSSOEnabled;
        this.idPInitSLOEnabled = samlSSOServiceProviderDO.idPInitSLOEnabled;
        this.idpInitSLOReturnToURLs = samlSSOServiceProviderDO.idpInitSLOReturnToURLs != null ?
                samlSSOServiceProviderDO.idpInitSLOReturnToURLs.clone() : null;
        this.idpInitSLOReturnToURLList = samlSSOServiceProviderDO.idpInitSLOReturnToURLList != null ?
                new ArrayList<>(samlSSOServiceProviderDO.idpInitSLOReturnToURLList) : null;
        this.doEnableEncryptedAssertion = samlSSOServiceProviderDO.doEnableEncryptedAssertion;
        this.doValidateSignatureInRequests = samlSSOServiceProviderDO.doValidateSignatureInRequests;
        this.doValidateSignatureInArtifactResolve = samlSSOServiceProviderDO.doValidateSignatureInArtifactResolve;
        this.signingAlgorithmUri = samlSSOServiceProviderDO.signingAlgorithmUri;
        this.digestAlgorithmUri = samlSSOServiceProviderDO.digestAlgorithmUri;
        this.assertionEncryptionAlgorithmUri = samlSSOServiceProviderDO.assertionEncryptionAlgorithmUri;
        this.keyEncryptionAlgorithmUri = samlSSOServiceProviderDO.keyEncryptionAlgorithmUri;
        this.signingCertificate = samlSSOServiceProviderDO.signingCertificate;
        this.encryptionCertificate = samlSSOServiceProviderDO.encryptionCertificate;
        this.x509Certificate = samlSSOServiceProviderDO.x509Certificate;
        this.isAssertionQueryRequestProfileEnabled = samlSSOServiceProviderDO.isAssertionQueryRequestProfileEnabled;
        this.supportedAssertionQueryRequestTypes = samlSSOServiceProviderDO.supportedAssertionQueryRequestTypes;
        this.enableSAML2ArtifactBinding = samlSSOServiceProviderDO.enableSAML2ArtifactBinding;
        this.samlECP = samlSSOServiceProviderDO.samlECP;
        this.idpEntityIDAlias = samlSSOServiceProviderDO.idpEntityIDAlias;
        this.doFrontChannelLogout = samlSSOServiceProviderDO.doFrontChannelLogout;
        this.frontChannelLogoutBinding = samlSSOServiceProviderDO.frontChannelLogoutBinding;
        this.attributeNameFormat = samlSSOServiceProviderDO.getAttributeNameFormat();
    }

    public void setDoValidateSignatureInArtifactResolve(boolean doValidateSignatureInArtifactResolve) {

        this.doValidateSignatureInArtifactResolve = doValidateSignatureInArtifactResolve;
    }

    public boolean isDoValidateSignatureInArtifactResolve() {

        return doValidateSignatureInArtifactResolve;
    }

    public void setEnableSAML2ArtifactBinding(boolean enableSAML2ArtifactBinding) {
        this.enableSAML2ArtifactBinding = enableSAML2ArtifactBinding;
    }

    public boolean isEnableSAML2ArtifactBinding() {
        return enableSAML2ArtifactBinding;
    }


    public SAMLSSOServiceProviderDO() {
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_SIGNING_ALGORITHM))) {
            signingAlgorithmUri = IdentityUtil.getProperty(IdentityConstants.ServerConfig
                    .SSO_DEFAULT_SIGNING_ALGORITHM).trim();
        } else {
            signingAlgorithmUri = IdentityCoreConstants.XML_SIGNATURE_ALGORITHM_RSA_SHA256_URI;
        }
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_DIGEST_ALGORITHM))) {
            digestAlgorithmUri = IdentityUtil.getProperty(IdentityConstants.ServerConfig
                    .SSO_DEFAULT_DIGEST_ALGORITHM).trim();
        } else {
            digestAlgorithmUri = IdentityCoreConstants.XML_DIGEST_ALGORITHM_SHA256;
        }
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_ASSERTION_ENCRYPTION_ALGORITHM))) {
            assertionEncryptionAlgorithmUri = IdentityUtil.getProperty(IdentityConstants.ServerConfig
                    .SSO_DEFAULT_ASSERTION_ENCRYPTION_ALGORITHM).trim();
        } else {
            assertionEncryptionAlgorithmUri = IdentityCoreConstants.XML_ASSERTION_ENCRYPTION_ALGORITHM_AES256;
        }
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_KEY_ENCRYPTION_ALGORITHM))) {
            keyEncryptionAlgorithmUri = IdentityUtil.getProperty(IdentityConstants.ServerConfig
                    .SSO_DEFAULT_KEY_ENCRYPTION_ALGORITHM).trim();
        } else {
            keyEncryptionAlgorithmUri = IdentityCoreConstants.XML_KEY_ENCRYPTION_ALGORITHM_RSAOAEP;
        }
    }

    public String getSigningCertificate() {
        return signingCertificate;
    }

    public void setSigningCertificate(String signingCertificate) {
        this.signingCertificate = signingCertificate;
    }

    public String getNameIDFormat() {
        return nameIDFormat;
    }

    public void setNameIDFormat(String nameIDFormat) {
        this.nameIDFormat = nameIDFormat;
    }

    public String getEncryptionCertificatee() {
        return encryptionCertificate;
    }

    public void setEncryptionCertificate(String encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
    }

    public String getNameIdClaimUri() {
        return nameIdClaimUri;
    }

    public void setNameIdClaimUri(String nameIdClaimUri) {
        this.nameIdClaimUri = nameIdClaimUri;
    }

    public boolean isEnableAttributesByDefault() {
        return enableAttributesByDefault;
    }

    public void setEnableAttributesByDefault(boolean enableAttributesByDefault) {
        this.enableAttributesByDefault = enableAttributesByDefault;
    }

    public String getAttributeNameFormat() {

        return attributeNameFormat;
    }

    public void setAttributeNameFormat(String attributeNameFormat) {

        this.attributeNameFormat = attributeNameFormat;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        if (issuer != null) {
            this.issuer = issuer.replaceAll("[\n\r]", "").trim();
        }
    }

    public boolean isDoFrontChannelLogout() {
        return doFrontChannelLogout;
    }

    public void setDoFrontChannelLogout(boolean doFrontChannelLogout) {
        this.doFrontChannelLogout = doFrontChannelLogout;
    }

    public String getFrontChannelLogoutBinding() {
        return frontChannelLogoutBinding;
    }

    public void setFrontChannelLogoutBinding(String frontChannelLogoutBinding) {
        this.frontChannelLogoutBinding = frontChannelLogoutBinding;
    }

    /**
     * Get qualifier of the issuer.
     *
     * @return Issuer Qualifier
     */
    public String getIssuerQualifier() {

        return issuerQualifier;
    }

    /**
     * Set issuer qualifier.
     *
     * @param issuerQualifier
     */
    public void setIssuerQualifier(String issuerQualifier) {

        if (StringUtils.isNotBlank(issuerQualifier)) {
            this.issuerQualifier = issuerQualifier;
        }
    }

    public String getAssertionConsumerUrl() {
        return assertionConsumerUrl;
    }

    public void setAssertionConsumerUrl(String assertionConsumerUrl) {
        if (assertionConsumerUrl != null) {
            this.assertionConsumerUrl = assertionConsumerUrl.replaceAll("[\n\r]", "").trim();
        }
    }

    public boolean isAssertionQueryRequestProfileEnabled() {
        return isAssertionQueryRequestProfileEnabled;
    }

    public void setAssertionQueryRequestProfileEnabled(boolean isAssertionQueryRequestProfileEnabled) {
        this.isAssertionQueryRequestProfileEnabled = isAssertionQueryRequestProfileEnabled;
    }

    public String getSupportedAssertionQueryRequestTypes() {
        return supportedAssertionQueryRequestTypes;
    }

    public void setSupportedAssertionQueryRequestTypes(String supportedAssertionQueryRequestTypes) {
        this.supportedAssertionQueryRequestTypes = supportedAssertionQueryRequestTypes;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getSloResponseURL() {
        return sloResponseURL;
    }

    public void setSloResponseURL(String sloResponseURL) {
        if (sloResponseURL != null) {
            this.sloResponseURL = sloResponseURL.replaceAll("[\n\r]", "").trim();
        }
    }

    public boolean isDoSingleLogout() {
        return doSingleLogout;
    }

    public void setDoSingleLogout(boolean doSingleLogout) {
        this.doSingleLogout = doSingleLogout;
    }

    public String getLoginPageURL() {
        return loginPageURL;
    }

    public void setLoginPageURL(String loginPageURL) {
        if (StringUtils.isNotBlank(loginPageURL)) {
            this.loginPageURL = loginPageURL.replaceAll("[\n\r]", "").trim();
        } else {
            this.loginPageURL = null;
        }
    }

    public boolean isDoSignAssertions() {
        return doSignAssertions;
    }

    public void setDoSignAssertions(boolean doSignAssertions) {
        this.doSignAssertions = doSignAssertions;
    }

    public String getAttributeConsumingServiceIndex() {
        return attributeConsumingServiceIndex;
    }

    public void setAttributeConsumingServiceIndex(String attributeConsumingServiceIndex) {
        this.attributeConsumingServiceIndex = attributeConsumingServiceIndex;
    }

    public String getSigningAlgorithmUri() {
        return signingAlgorithmUri;
    }

    public void setSigningAlgorithmUri(String signingAlgorithmUri) {
        if (StringUtils.isNotEmpty(signingAlgorithmUri)) {
            this.signingAlgorithmUri = signingAlgorithmUri;
        }
    }

    public String getDigestAlgorithmUri() {
        return digestAlgorithmUri;
    }

    public void setDigestAlgorithmUri(String digestAlgorithmUri) {
        if (StringUtils.isNotEmpty(digestAlgorithmUri)) {
            this.digestAlgorithmUri = digestAlgorithmUri;
        }
    }

    public String getAssertionEncryptionAlgorithmUri() {
        return assertionEncryptionAlgorithmUri;
    }

    public void setAssertionEncryptionAlgorithmUri(String assertionEncryptionAlgorithmUri) {
        if (StringUtils.isNotBlank(assertionEncryptionAlgorithmUri)) {
            this.assertionEncryptionAlgorithmUri = assertionEncryptionAlgorithmUri;
        }
    }

    public String getKeyEncryptionAlgorithmUri() {
        return keyEncryptionAlgorithmUri;
    }

    public void setKeyEncryptionAlgorithmUri(String keyEncryptionAlgorithmUri) {
        if (StringUtils.isNotBlank(keyEncryptionAlgorithmUri)) {
            this.keyEncryptionAlgorithmUri = keyEncryptionAlgorithmUri;
        }
    }

    /**
     * @return the requestedClaims
     */
    public String[] getRequestedClaims() {
        if (requestedClaims != null) {
            return requestedClaims.clone();
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    /**
     * @param requestedClaims the requestedClaims to set
     */
    public void setRequestedClaims(List<String> requestedClaims) {
        if (requestedClaims != null) {
            this.requestedClaimsList = requestedClaims;
            this.requestedClaims = requestedClaims.toArray(new String[requestedClaims.size()]);
        }
    }

    /**
     * @param requestedClaims the requestedClaims to set
     */
    public void setRequestedClaims(String[] requestedClaims) {
        if (requestedClaims != null) {
            this.requestedClaims = requestedClaims.clone();
            this.requestedClaimsList = Arrays.asList(requestedClaims);
        }
    }

    /**
     * @return the requestedClaims
     */
    public List<String> getRequestedClaimsList() {
        if (requestedClaimsList != null) {
            return requestedClaimsList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @return the requestedAudiences
     */
    public String[] getRequestedAudiences() {
        if (requestedAudiences != null) {
            return requestedAudiences.clone();
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    /**
     * @param requestedAudiences the requestedAudiences to set
     */
    public void setRequestedAudiences(List<String> requestedAudiences) {
        if (requestedAudiences != null) {
            this.requestedAudiencesList = requestedAudiences;
            this.requestedAudiences = requestedAudiences.toArray(new String[requestedAudiencesList.size()]);
        }
    }

    /**
     * @param requestedAudiences the requestedAudiences to set
     */
    public void setRequestedAudiences(String[] requestedAudiences) {
        if (requestedAudiences != null) {
            this.requestedAudiences = requestedAudiences.clone();
            this.requestedAudiencesList = Arrays.asList(requestedAudiences);
        }
    }

    /**
     * @return the requestedAudiences
     */
    public List<String> getRequestedAudiencesList() {
        if (requestedAudiencesList != null) {
            return requestedAudiencesList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @return the requestedRecipients
     */
    public String[] getRequestedRecipients() {
        if (requestedRecipients != null) {
            return requestedRecipients.clone();
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    /**
     * @param requestedRecipientsList the requestedRecipients to set
     */
    public void setRequestedRecipients(List<String> requestedRecipientsList) {
        this.requestedRecipientsList = requestedRecipientsList;
        if (requestedRecipientsList != null) {
            this.requestedRecipients = requestedRecipientsList.toArray(new String[requestedRecipientsList.size()]);
        } else {
            this.requestedRecipients = null;
        }
    }

    /**
     * @param requestedRecipients the requestedRecipients to set
     */
    public void setRequestedRecipients(String[] requestedRecipients) {
        if (requestedRecipients != null) {
            this.requestedRecipients = requestedRecipients.clone();
            this.requestedRecipientsList = Arrays.asList(requestedRecipients);
        } else {
            this.requestedRecipients = null;
            this.requestedRecipientsList = null;
        }
    }

    /**
     * @return the requestedRecipients
     */
    public List<String> getRequestedRecipientsList() {
        if (requestedRecipientsList != null) {
            return requestedRecipientsList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @return the doSignResponse
     */
    public boolean isDoSignResponse() {
        return doSignResponse;
    }

    /**
     * @param doSignResponse the doSignResponse to set
     */
    public void setDoSignResponse(boolean doSignResponse) {
        this.doSignResponse = doSignResponse;
    }

    public boolean isIdPInitSSOEnabled() {
        return isIdPInitSSOEnabled;
    }

    public void setIdPInitSSOEnabled(boolean idPInitSSOEnabled) {
        isIdPInitSSOEnabled = idPInitSSOEnabled;
    }

    public boolean isDoEnableEncryptedAssertion() {
        return doEnableEncryptedAssertion;
    }

    public void setDoEnableEncryptedAssertion(boolean doEnableEncryptedAssertion) {
        this.doEnableEncryptedAssertion = doEnableEncryptedAssertion;
    }

    public boolean isDoValidateSignatureInRequests() {
        return doValidateSignatureInRequests;
    }

    public void setDoValidateSignatureInRequests(boolean doValidateSignatureInRequests) {
        this.doValidateSignatureInRequests = doValidateSignatureInRequests;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String[] getAssertionConsumerUrls() {
        if (assertionConsumerUrls != null) {
            return assertionConsumerUrls.clone();
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    public List<String> getAssertionConsumerUrlList() {
        if (assertionConsumerUrlList != null) {
            return assertionConsumerUrlList;
        } else {
            return Collections.emptyList();
        }
    }

    public void setAssertionConsumerUrls(String[] assertionConsumerUrls) {
        if (assertionConsumerUrls != null) {
            this.assertionConsumerUrls = assertionConsumerUrls.clone();
            this.assertionConsumerUrlList = Arrays.asList(assertionConsumerUrls);
        } else {
            this.assertionConsumerUrls = null;
            this.assertionConsumerUrlList = null;
        }
    }

    public void setAssertionConsumerUrls(List<String> assertionConsumerUrlList) {
        this.assertionConsumerUrlList = assertionConsumerUrlList;
        if (assertionConsumerUrlList != null) {
            this.assertionConsumerUrls = assertionConsumerUrlList.toArray(new String[assertionConsumerUrlList.size()]);
        } else {
            this.assertionConsumerUrls = null;
        }
    }

    public String getDefaultAssertionConsumerUrl() {
        return defaultAssertionConsumerUrl;
    }

    public void setDefaultAssertionConsumerUrl(String defaultAssertionConsumerUrl) {
        if (StringUtils.isNotBlank(defaultAssertionConsumerUrl)) {
            this.defaultAssertionConsumerUrl = defaultAssertionConsumerUrl.replaceAll("[\n\r]", "").trim();
        } else {
            this.defaultAssertionConsumerUrl = null;
        }
    }

    public String getSloRequestURL() {
        return sloRequestURL;
    }

    public void setSloRequestURL(String sloRequestURL) {
        if (StringUtils.isNotBlank(sloRequestURL)) {
            this.sloRequestURL = sloRequestURL.replaceAll("[\n\r]", "").trim();
        } else {
            this.sloRequestURL = null;
        }
    }

    public boolean isIdPInitSLOEnabled() {
        return idPInitSLOEnabled;
    }

    public void setIdPInitSLOEnabled(boolean idPInitSLOEnabled) {
        this.idPInitSLOEnabled = idPInitSLOEnabled;
    }

    public String[] getIdpInitSLOReturnToURLs() {
        if (idpInitSLOReturnToURLs != null) {
            return idpInitSLOReturnToURLs.clone();
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    public void setIdpInitSLOReturnToURLs(String[] idpInitSLOReturnToURLs) {
        if (idpInitSLOReturnToURLs != null) {
            this.idpInitSLOReturnToURLs = idpInitSLOReturnToURLs.clone();
            this.idpInitSLOReturnToURLList = Arrays.asList(idpInitSLOReturnToURLs);
        } else {
            this.idpInitSLOReturnToURLs = null;
            this.idpInitSLOReturnToURLList = null;
        }
    }

    public List<String> getIdpInitSLOReturnToURLList() {
        if (idpInitSLOReturnToURLList != null) {
            return idpInitSLOReturnToURLList;
        } else {
            return Collections.emptyList();
        }
    }

    public void setIdpInitSLOReturnToURLs(List<String> idpInitSLOReturnToURLList) {
        this.idpInitSLOReturnToURLList = idpInitSLOReturnToURLList;
        if (idpInitSLOReturnToURLList != null) {
            this.idpInitSLOReturnToURLs = idpInitSLOReturnToURLList.toArray(
                    new String[idpInitSLOReturnToURLList.size()]);
        } else {
            this.idpInitSLOReturnToURLs = null;
        }
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public void setSamlECP(boolean samlECP) {
        this.samlECP = samlECP;
    }

    public boolean isSamlECP() {
        return samlECP;
    }

    /**
     * Get IdP Entity ID alias.
     *
     * @return IdP Entity ID Alias
     */
    public String getIdpEntityIDAlias() {

        return idpEntityIDAlias;
    }

    /**
     * Set IdP Entity ID alias.
     *
     * @param idpEntityIDAlias
     */
    public void setIdpEntityIDAlias(String idpEntityIDAlias) {

        this.idpEntityIDAlias = idpEntityIDAlias;
    }

    public String getSingleLogoutMethod() {

        if (doFrontChannelLogout) {
            return frontChannelLogoutBinding;
        } else {
            return BACKCHANNEL_LOGOUT_BINDING;
        }
    }

    /**
     * Get configs of the SAML SSO IdP.
     *
     * @return List of ServiceProviderProperty.
     */
    public List<ServiceProviderProperty> getMultiValuedProperties() {

        List<ServiceProviderProperty> multiValuedProperties = new ArrayList<>();

        // Multi-valued attributes.
        getAssertionConsumerUrlList().forEach(assertionConUrl ->
                putIfNotNull(multiValuedProperties, MultiValuedPropertyKey.ASSERTION_CONSUMER_URLS.toString(),
                        assertionConUrl));
        getRequestedRecipientsList().forEach(requestedRecipient ->
                putIfNotNull(multiValuedProperties, MultiValuedPropertyKey.RECIPIENTS.toString(),
                        requestedRecipient));
        getRequestedAudiencesList().forEach(requestedAudience ->
                putIfNotNull(multiValuedProperties, MultiValuedPropertyKey.AUDIENCES.toString(),
                        requestedAudience));
        getIdpInitSLOReturnToURLList().forEach(idpInitSLOReturnToURL ->
                putIfNotNull(multiValuedProperties, MultiValuedPropertyKey.SLO_RETURN_TO_URLS.toString(),
                        idpInitSLOReturnToURL));

        return multiValuedProperties;
    }

    /**
     * Add a list of multivalued properties.
     *
     * @param multiValuedProperties List of ServiceProviderProperty.
     */
    public void addMultiValuedProperties(List<ServiceProviderProperty> multiValuedProperties) {

        if (multiValuedProperties == null) {
            return;
        }

        multiValuedProperties.forEach(this::addMultiValuedProperty);
    }

    /**
     * Add a multivalued property.
     *
     * @param multiValuedProperty ServiceProviderProperty.
     */
    private void addMultiValuedProperty(ServiceProviderProperty multiValuedProperty) {

        if (multiValuedProperty == null) {
            return;
        }
        String key = multiValuedProperty.getKey();
        String value = multiValuedProperty.getValue();

        if (MultiValuedPropertyKey.ASSERTION_CONSUMER_URLS.toString().equals(key)) {
            List<String> attributeList = getAssertionConsumerUrlList();
            if (attributeList.isEmpty()) {
                attributeList = new ArrayList<>();
            }
            attributeList.add(value);
            setAssertionConsumerUrls(attributeList);
        } else if (MultiValuedPropertyKey.RECIPIENTS.toString().equals(key)) {
            List<String> attributeList = getRequestedRecipientsList();
            if (attributeList.isEmpty()) {
                attributeList = new ArrayList<>();
            }
            attributeList.add(value);
            setRequestedRecipients(attributeList);
        } else if (MultiValuedPropertyKey.AUDIENCES.toString().equals(key)) {
            List<String> attributeList = getRequestedAudiencesList();
            if (attributeList.isEmpty()) {
                attributeList = new ArrayList<>();
            }
            attributeList.add(value);
            setRequestedAudiences(attributeList);
        } else if (MultiValuedPropertyKey.SLO_RETURN_TO_URLS.toString().equals(key)) {
            List<String> attributeList = getIdpInitSLOReturnToURLList();
            if (attributeList.isEmpty()) {
                attributeList = new ArrayList<>();
            }
            attributeList.add(value);
            setIdpInitSLOReturnToURLs(attributeList);
        }
    }

    /**
     * Put a key value pair to a list if the value is not null.
     *
     * @param list  List of ServiceProviderProperty.
     * @param key   Key.
     * @param value Value.
     */
    private void putIfNotNull(List<ServiceProviderProperty> list, String key, String value) {

        if (StringUtils.isNotBlank(value)) {
            list.add(new ServiceProviderProperty(key, value));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SAMLSSOServiceProviderDO that = (SAMLSSOServiceProviderDO) o;
        return doSingleLogout == that.doSingleLogout &&
                doSignResponse == that.doSignResponse &&
                doSignAssertions == that.doSignAssertions &&
                enableAttributesByDefault == that.enableAttributesByDefault &&
                isIdPInitSSOEnabled == that.isIdPInitSSOEnabled &&
                idPInitSLOEnabled == that.idPInitSLOEnabled &&
                doEnableEncryptedAssertion == that.doEnableEncryptedAssertion &&
                doValidateSignatureInRequests == that.doValidateSignatureInRequests &&
                doValidateSignatureInArtifactResolve == that.doValidateSignatureInArtifactResolve &&
                enableSAML2ArtifactBinding == that.enableSAML2ArtifactBinding &&
                samlECP == that.samlECP &&
                doFrontChannelLogout == that.doFrontChannelLogout &&
                Objects.equals(tenantDomain, that.tenantDomain) &&
                Objects.equals(issuer, that.issuer) &&
                Objects.equals(issuerQualifier, that.issuerQualifier) &&
                Objects.equals(assertionConsumerUrl, that.assertionConsumerUrl) &&
                Arrays.equals(assertionConsumerUrls, that.assertionConsumerUrls) &&
                Objects.equals(defaultAssertionConsumerUrl, that.defaultAssertionConsumerUrl) &&
                Objects.equals(certAlias, that.certAlias) &&
                Objects.equals(sloResponseURL, that.sloResponseURL) &&
                Objects.equals(sloRequestURL, that.sloRequestURL) &&
                Objects.equals(loginPageURL, that.loginPageURL) &&
                Objects.equals(attributeConsumingServiceIndex, that.attributeConsumingServiceIndex) &&
                Arrays.equals(requestedClaims, that.requestedClaims) &&
                Arrays.equals(requestedAudiences, that.requestedAudiences) &&
                Arrays.equals(requestedRecipients, that.requestedRecipients) &&
                Objects.equals(nameIdClaimUri, that.nameIdClaimUri) &&
                Objects.equals(nameIDFormat, that.nameIDFormat) &&
                Arrays.equals(idpInitSLOReturnToURLs, that.idpInitSLOReturnToURLs) &&
                Objects.equals(signingAlgorithmUri, that.signingAlgorithmUri) &&
                Objects.equals(digestAlgorithmUri, that.digestAlgorithmUri) &&
                Objects.equals(assertionEncryptionAlgorithmUri, that.assertionEncryptionAlgorithmUri) &&
                Objects.equals(keyEncryptionAlgorithmUri, that.keyEncryptionAlgorithmUri) &&
                Objects.equals(signingCertificate, that.signingCertificate) &&
                Objects.equals(encryptionCertificate, that.encryptionCertificate) &&
                Objects.equals(idpEntityIDAlias, that.idpEntityIDAlias) &&
                Objects.equals(frontChannelLogoutBinding, that.frontChannelLogoutBinding);
    }

    @Override
    public int hashCode() {

        int h = Objects.hash(tenantDomain, issuer, issuerQualifier, assertionConsumerUrl, defaultAssertionConsumerUrl,
                certAlias, sloResponseURL, sloRequestURL, doSingleLogout, loginPageURL, doSignResponse,
                doSignAssertions, attributeConsumingServiceIndex, enableAttributesByDefault, nameIdClaimUri,
                nameIDFormat, isIdPInitSSOEnabled, idPInitSLOEnabled, doEnableEncryptedAssertion,
                doValidateSignatureInRequests, doValidateSignatureInArtifactResolve, signingAlgorithmUri,
                digestAlgorithmUri, assertionEncryptionAlgorithmUri, keyEncryptionAlgorithmUri, signingCertificate,
                encryptionCertificate, isAssertionQueryRequestProfileEnabled, supportedAssertionQueryRequestTypes,
                enableSAML2ArtifactBinding, samlECP, idpEntityIDAlias, doFrontChannelLogout, frontChannelLogoutBinding);
        h = 31 * h + Arrays.hashCode(assertionConsumerUrls);
        h = 31 * h + Arrays.hashCode(requestedClaims);
        h = 31 * h + Arrays.hashCode(requestedAudiences);
        h = 31 * h + Arrays.hashCode(requestedRecipients);
        h = 31 * h + Arrays.hashCode(idpInitSLOReturnToURLs);
        return h;
    }
}
