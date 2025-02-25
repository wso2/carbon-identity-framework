/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for managing SAML SSO service providers in the Registry.
 */
public class RegistrySAMLSSOServiceProviderDAOImpl implements SAMLSSOServiceProviderDAO {

    public RegistrySAMLSSOServiceProviderDAOImpl() {

    }

    SAMLSSOServiceProviderDO buildSAMLSSOServiceProviderDAO (Resource resource) {
        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        serviceProviderDO.setAssertionConsumerUrls(resource.getPropertyValues(
                IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resource.getProperty(
                IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL));
        serviceProviderDO.setCertAlias(resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS));

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM))) {
            serviceProviderDO.setSigningAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_SIGNING_ALGORITHM));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED) !=
                null) {
            serviceProviderDO.setAssertionQueryRequestProfileEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED).trim()));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES) !=
                null) {
            serviceProviderDO.setSupportedAssertionQueryRequestTypes(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES).trim());
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_SAML2_ARTIFACT_BINDING) !=
                null) {
            serviceProviderDO.setEnableSAML2ArtifactBinding(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_SAML2_ARTIFACT_BINDING).trim()));
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM))) {
            serviceProviderDO.setDigestAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_DIGEST_ALGORITHM));
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM))) {
            serviceProviderDO.setAssertionEncryptionAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM));
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM))) {
            serviceProviderDO.setKeyEncryptionAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT) != null) {
            serviceProviderDO.setDoSingleLogout(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT).trim()));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT) != null) {
            serviceProviderDO.setNameIDFormat(resource.
                    getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI) != null) {
            if (Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI).trim())) {
                serviceProviderDO.setNameIdClaimUri(resource.
                        getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_CLAIMURI));
            }
        }

        serviceProviderDO.setLoginPageURL(resource.
                getProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL));

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE) != null) {
            serviceProviderDO.setDoSignResponse(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE).trim()));
        }

        if (serviceProviderDO.isDoSingleLogout()) {
            serviceProviderDO.setSloResponseURL(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SLO_RESPONSE_URL));
            serviceProviderDO.setSloRequestURL(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SLO_REQUEST_URL));
            // Check front channel logout enable.
            if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_FRONT_CHANNEL_LOGOUT) != null) {
                serviceProviderDO.setDoFrontChannelLogout(Boolean.valueOf(resource.getProperty(
                        IdentityRegistryResources.PROP_SAML_SSO_DO_FRONT_CHANNEL_LOGOUT).trim()));
                if (serviceProviderDO.isDoFrontChannelLogout()) {
                    if (resource.getProperty(IdentityRegistryResources.
                            PROP_SAML_SSO_FRONT_CHANNEL_LOGOUT_BINDING) != null) {
                        serviceProviderDO.setFrontChannelLogoutBinding(resource.getProperty(
                                IdentityRegistryResources.PROP_SAML_SSO_FRONT_CHANNEL_LOGOUT_BINDING));
                    } else {
                        // Default is redirect-binding.
                        serviceProviderDO.setFrontChannelLogoutBinding(IdentityRegistryResources
                                .DEFAULT_FRONT_CHANNEL_LOGOUT_BINDING);
                    }

                }
            }
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS) != null) {
            serviceProviderDO.setDoSignAssertions(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS).trim()));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_ENABLE_ECP) != null) {
            serviceProviderDO.setSamlECP(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_ENABLE_ECP).trim()));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX) != null) {
            serviceProviderDO
                    .setAttributeConsumingServiceIndex(resource
                            .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX));
        } else {
            // Specific DB's (like oracle) returns empty strings as null.
            serviceProviderDO.setAttributeConsumingServiceIndex("");
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS) != null) {
            serviceProviderDO.setRequestedClaims(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES) != null) {
            serviceProviderDO.setRequestedAudiences(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS) != null) {
            serviceProviderDO.setRequestedRecipients(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT) != null) {
            String enableAttrByDefault = resource
                    .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT);
            serviceProviderDO.setEnableAttributesByDefault(Boolean.valueOf(enableAttrByDefault));
        }
        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIBUTE_NAME_FORMAT) != null) {
            serviceProviderDO.setAttributeNameFormat(
                    resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIBUTE_NAME_FORMAT));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED) != null) {
            serviceProviderDO.setIdPInitSSOEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED).trim()));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED) != null) {
            serviceProviderDO.setIdPInitSLOEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED).trim()));
            if (serviceProviderDO.isIdPInitSLOEnabled() && resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS) != null) {
                serviceProviderDO.setIdpInitSLOReturnToURLs(resource.getPropertyValues(
                        IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS));
            }
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION) != null) {
            serviceProviderDO.setDoEnableEncryptedAssertion(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION).trim()));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS) != null) {
            serviceProviderDO.setDoValidateSignatureInRequests(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS).trim()));
        }
        if (resource.getProperty(
                IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE) != null) {
            serviceProviderDO.setDoValidateSignatureInArtifactResolve(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE).trim()));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_QUALIFIER) != null) {
            serviceProviderDO.setIssuerQualifier(resource
                    .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_QUALIFIER));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_ENTITY_ID_ALIAS) != null) {
            serviceProviderDO.setIdpEntityIDAlias(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_IDP_ENTITY_ID_ALIAS));
        }
        return serviceProviderDO;
    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId) throws IdentityException {

        Registry registry = getRegistry(tenantId);
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(serviceProviderDO.getIssuer());

        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            Resource resource = createResource(serviceProviderDO, registry);
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }
            registry.put(path, resource);
            return true;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            throw new IdentityException("Error while adding SAML Service Provider.", e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred, registry);
        }
    }

    private Resource createResource(SAMLSSOServiceProviderDO serviceProviderDO, Registry registry)
            throws RegistryException {

        Resource resource = registry.newResource();
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER,
                serviceProviderDO.getIssuer());
        resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS,
                serviceProviderDO.getAssertionConsumerUrlList());
        resource.addProperty(IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL,
                serviceProviderDO.getDefaultAssertionConsumerUrl());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS,
                serviceProviderDO.getCertAlias());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL,
                serviceProviderDO.getLoginPageURL());
        resource.addProperty(
                IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT,
                serviceProviderDO.getNameIDFormat());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM, serviceProviderDO
                .getSigningAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM, serviceProviderDO
                .getDigestAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM, serviceProviderDO
                .getAssertionEncryptionAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM, serviceProviderDO
                .getKeyEncryptionAlgorithmUri());
        if (serviceProviderDO.getNameIdClaimUri() != null
                && serviceProviderDO.getNameIdClaimUri().trim().length() > 0) {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI,
                    "true");
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_NAMEID_CLAIMURI,
                    serviceProviderDO.getNameIdClaimUri());
        } else {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI,
                    "false");
        }

        String doSingleLogout = String.valueOf(serviceProviderDO.isDoSingleLogout());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT, doSingleLogout);
        if (serviceProviderDO.isDoSingleLogout()) {
            if (StringUtils.isNotBlank(serviceProviderDO.getSloResponseURL())) {
                resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_RESPONSE_URL,
                        serviceProviderDO.getSloResponseURL());
            }
            if (StringUtils.isNotBlank(serviceProviderDO.getSloRequestURL())) {
                resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_REQUEST_URL,
                        serviceProviderDO.getSloRequestURL());
            }
            // Create doFrontChannelLogout property in the registry.
            String doFrontChannelLogout = String.valueOf(serviceProviderDO.isDoFrontChannelLogout());
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_FRONT_CHANNEL_LOGOUT, doFrontChannelLogout);
            if (serviceProviderDO.isDoFrontChannelLogout()) {
                // Create frontChannelLogoutMethod property in the registry.
                resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_FRONT_CHANNEL_LOGOUT_BINDING,
                        serviceProviderDO.getFrontChannelLogoutBinding());
            }
        }

        String doSignResponse = String.valueOf(serviceProviderDO.isDoSignResponse());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE,
                doSignResponse);
        String isAssertionQueryRequestProfileEnabled = String.valueOf(serviceProviderDO
                .isAssertionQueryRequestProfileEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED,
                isAssertionQueryRequestProfileEnabled);
        String supportedAssertionQueryRequestTypes = serviceProviderDO.getSupportedAssertionQueryRequestTypes();
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES,
                supportedAssertionQueryRequestTypes);
        String isEnableSAML2ArtifactBinding = String.valueOf(serviceProviderDO
                .isEnableSAML2ArtifactBinding());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_SAML2_ARTIFACT_BINDING,
                isEnableSAML2ArtifactBinding);
        String doSignAssertions = String.valueOf(serviceProviderDO.isDoSignAssertions());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS,
                doSignAssertions);
        String isSamlECP = String.valueOf(serviceProviderDO.isSamlECP());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_ENABLE_ECP,
                isSamlECP);
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedClaimsList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS,
                    serviceProviderDO.getRequestedClaimsList());
        }
        if (serviceProviderDO.getAttributeConsumingServiceIndex() != null) {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX,
                    serviceProviderDO.getAttributeConsumingServiceIndex());
        }
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedAudiencesList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES,
                    serviceProviderDO.getRequestedAudiencesList());
        }
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedRecipientsList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS,
                    serviceProviderDO.getRequestedRecipientsList());
        }

        String enableAttributesByDefault = String.valueOf(serviceProviderDO.isEnableAttributesByDefault());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT,
                enableAttributesByDefault);
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIBUTE_NAME_FORMAT,
                serviceProviderDO.getAttributeNameFormat());
        String idPInitSSOEnabled = String.valueOf(serviceProviderDO.isIdPInitSSOEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED,
                idPInitSSOEnabled);
        String idPInitSLOEnabled = String.valueOf(serviceProviderDO.isIdPInitSLOEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED, idPInitSLOEnabled);
        if (serviceProviderDO.isIdPInitSLOEnabled() && serviceProviderDO.getIdpInitSLOReturnToURLList().size() > 0) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS,
                    serviceProviderDO.getIdpInitSLOReturnToURLList());
        }
        String enableEncryptedAssertion = String.valueOf(serviceProviderDO.isDoEnableEncryptedAssertion());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION,
                enableEncryptedAssertion);

        String validateSignatureInRequests = String.valueOf(serviceProviderDO.isDoValidateSignatureInRequests());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS,
                validateSignatureInRequests);

        String validateSignatureInArtifactResolve =
                String.valueOf(serviceProviderDO.isDoValidateSignatureInArtifactResolve());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE,
                validateSignatureInArtifactResolve);
        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_QUALIFIER, serviceProviderDO
                    .getIssuerQualifier());
        }
        if (StringUtils.isNotBlank(serviceProviderDO.getIdpEntityIDAlias())) {
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_ENTITY_ID_ALIAS, serviceProviderDO
                    .getIdpEntityIDAlias());
        }
        return resource;
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer, int tenantId)
            throws IdentityException {

        Registry registry = getRegistry(tenantId);
        String currentPath = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(currentIssuer);
        String newPath = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(serviceProviderDO.getIssuer());

        boolean isIssuerUpdated = !StringUtils.equals(currentPath, newPath);
        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            Resource resource = createResource(serviceProviderDO, registry);
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }
            // Delete the current resource if the issuer value is updated.
            if (isIssuerUpdated) {
                registry.delete(currentPath);
            }
            // Update the resource.
            // If the issuer is updated, new resource will be created.
            // If the issuer is not updated, existing resource will be updated.
            registry.put(newPath, resource);
            return true;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            throw new IdentityException("Error while updating SAML Service Provider.", e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred, registry);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId) throws IdentityException {

        Registry registry = getRegistry(tenantId);
        List<SAMLSSOServiceProviderDO> serviceProvidersList = new ArrayList<>();
        try {
            if (registry.resourceExists(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)) {
                Resource samlSSOServiceProvidersResource = registry.get(IdentityRegistryResources
                        .SAML_SSO_SERVICE_PROVIDERS);
                if (samlSSOServiceProvidersResource instanceof Collection) {
                    Collection samlSSOServiceProvidersCollection = (Collection) samlSSOServiceProvidersResource;
                    String[] resources = samlSSOServiceProvidersCollection.getChildren();
                    for (String resource : resources) {
                        getChildResources(resource, serviceProvidersList, registry);
                    }
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error reading Service Providers from Registry", e);
        }
        return serviceProvidersList.toArray(new SAMLSSOServiceProviderDO[serviceProvidersList.size()]);
    }

    @Override
    public boolean removeServiceProvider(String issuer, int tenantId) throws IdentityException {

        Registry registry = getRegistry(tenantId);

        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            // Since we are getting a global registry object, better to check whether this is a task inside already
            // started transaction.
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }
            registry.delete(path);
            return true;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            throw new IdentityException("Error while removing SAML Service Provider.", e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred, registry);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId) throws IdentityException {

        if (!isServiceProviderExists(issuer, tenantId)) {
            return null;
        }
        Registry registry = getRegistry(tenantId);
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        SAMLSSOServiceProviderDO serviceProviderDO = null;
        try {
            serviceProviderDO = buildSAMLSSOServiceProviderDAO(registry.get(path));
        } catch (RegistryException e) {
            throw IdentityException.error(
                    "Error occurred while checking if resource path \'" + path + "\' exists in " + "registry", e);
        }
        return serviceProviderDO;
    }

    @Override
    public boolean isServiceProviderExists(String issuer, int tenantId) throws IdentityException {

        Registry registry = getRegistry(tenantId);
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            throw IdentityException.error("Error occurred while checking if resource path \'" + path + "\' exists in " +
                    "registry");
        }
    }

    private String encodePath(String path) {
        String encodedStr = new String(Base64.encodeBase64(path.getBytes()));
        return encodedStr.replace("=", "");
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        if (addServiceProvider(serviceProviderDO, tenantId)) {
            return serviceProviderDO;
        }
        return null;
    }

    /**
     * Commit or rollback the registry operation depends on the error condition.
     * @param isErrorOccurred Identifier for error transactions.
     * @throws IdentityException Error while committing or running rollback on the transaction.
     */
    private void commitOrRollbackTransaction(boolean isErrorOccurred, Registry registry) throws IdentityException {

        try {
            // Rollback the transaction if there is an error, Otherwise try to commit.
            if (isErrorOccurred) {
                registry.rollbackTransaction();
            } else {
                registry.commitTransaction();
            }
        } catch (RegistryException ex) {
            throw new IdentityException("Error occurred while trying to commit or rollback the registry operation.", ex);
        }
    }

    /**
     * This helps to find resources in a recursive manner.
     *
     * @param parentResource      parent resource Name.
     * @param serviceProviderList child resource list.
     * @throws RegistryException
     */
    private void getChildResources(String parentResource, List<SAMLSSOServiceProviderDO> serviceProviderList,
                                   Registry registry) throws RegistryException {

        if (registry.resourceExists(parentResource)) {
            Resource resource = registry.get(parentResource);
            if (resource instanceof Collection) {
                Collection collection = (Collection) resource;
                String[] resources = collection.getChildren();
                for (String res : resources) {
                    getChildResources(res, serviceProviderList, registry);
                }
            } else {
                serviceProviderList.add(buildSAMLSSOServiceProviderDAO(resource));
            }
        }
    }

    private Registry getRegistry(int tenantId) throws IdentityException {

        try {
            Registry registry = IdentityTenantUtil.getRegistryService().getConfigSystemRegistry(tenantId);
            return registry;
        } catch (RegistryException e) {
            throw new IdentityException("Error while retrieving registry", e);
        }
    }
}
