package org.wso2.carbon.identity.application.mgt.validator;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.core.IdentityRegistryResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validator class to be used to validate the SAML inbound properties, before it is persisted.
 */
public class SAMLValidator implements ApplicationValidator {

    private static final String SAMLSSO = "samlsso";

    private static final String ISSUER = "issuer";
    private static final String ISSUER_QUALIFIER = "issuerQualifier";
    private static final String SIGNING_ALGORITHM_URI = "signingAlgorithmURI";
    private static final String DIGEST_ALGORITHM_URI = "digestAlgorithmURI";
    private static final String ASSERTION_ENCRYPTION_ALGORITHM_URI = "assertionEncryptionAlgorithmURI";
    private static final String KEY_ENCRYPTION_ALGORITHM_URI = "keyEncryptionAlgorithmURI";
    private static final String ASSERTION_CONSUMER_URLS = "assertionConsumerUrls";
    private static final String DEFAULT_ASSERTION_CONSUMER_URL = "defaultAssertionConsumerUrl";
    private static final String CERT_ALIAS = "certAlias";
    private static final String DO_SIGN_RESPONSE = "doSignResponse";
    private static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "attrConsumServiceIndex";
    private static final String DO_SINGLE_LOGOUT = "doSingleLogout";
    private static final String DO_FRONT_CHANNEL_LOGOUT = "doFrontChannelLogout";
    private static final String FRONT_CHANNEL_LOGOUT_BINDING = "frontChannelLogoutBinding";
    private static final String IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED = "isAssertionQueryRequestProfileEnabled";
    private static final String SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES = "supportedAssertionQueryRequestTypes";
    private static final String ENABLE_SAML2_ARTIFACT_BINDING = "enableSAML2ArtifactBinding";
    private static final String DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = "doValidateSignatureInArtifactResolve";
    private static final String LOGIN_PAGE_URL = "loginPageURL";
    private static final String SLO_RESPONSE_URL = "sloResponseURL";
    private static final String SLO_REQUEST_URL = "sloRequestURL";
    private static final String REQUESTED_CLAIMS = "requestedClaims";
    private static final String REQUESTED_AUDIENCES = "requestedAudiences";
    private static final String REQUESTED_RECIPIENTS = "requestedRecipients";
    private static final String ENABLE_ATTRIBUTES_BY_DEFAULT = "enableAttributesByDefault";
    private static final String NAME_ID_CLAIM_URI = "nameIdClaimUri";
    private static final String NAME_ID_FORMAT = "nameIDFormat";
    private static final String IDP_INIT_SSO_ENABLED = "idPInitSSOEnabled";
    private static final String IDP_INIT_SLO_ENABLED = "idPInitSLOEnabled";
    private static final String IDP_INIT_SLO_RETURN_TO_URLS = "idpInitSLOReturnToURLs";
    private static final String DO_ENABLE_ENCRYPTED_ASSERTION = "doEnableEncryptedAssertion";
    private static final String DO_VALIDATE_SIGNATURE_IN_REQUESTS = "doValidateSignatureInRequests";
    private static final String IDP_ENTITY_ID_ALIAS = "idpEntityIDAlias";
    private static final String IS_UPDATE = "isUpdate";

    private static final String INVALID_SIGNING_ALGORITHM_URI = "Invalid Response Signing Algorithm: %s";
    private static final String INVALID_DIGEST_ALGORITHM_URI = "Invalid Response Digest Algorithm: %s";
    private static final String INVALID_ASSERTION_ENCRYPTION_ALGORITHM_URI = "Invalid Assertion Encryption Algorithm:" +
            " %s";
    private static final String INVALID_KEY_ENCRYPTION_ALGORITHM_URI = "Invalid Key Encryption Algorithm: %s";
    private static final String ISSUER_ALREADY_EXISTS = "An application with the SAML issuer: %s already exists in " +
            "tenantDomain: %s";
    private static final String ISSUER_WITH_ISSUER_QUALIFIER_ALREADY_EXISTS = "SAML2 Service Provider already exists " +
            "with the same issuer name: %s and qualifier name: %s , in tenantDomain: %s";

    @Override
    public int getOrderId() {
        return 1;
    }

    @Override
    public List<String> validateApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {
        List<String> validationErrors = new ArrayList<>();
        if (serviceProvider.getInboundAuthenticationConfig() == null
                || serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() == null) {
            return validationErrors;
        }
        for (InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig:
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()) {
            if (inboundAuthenticationRequestConfig.getInboundAuthType().equals(SAMLSSO)) {
                validateSAMLProperties(validationErrors, inboundAuthenticationRequestConfig, tenantDomain);
                break;
            }
        }
        return validationErrors;
    }

    private void validateSAMLProperties(List<String> validationErrors,
                                        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig,
                                        String tenantDomain) throws IdentityApplicationManagementException {
        Property[] properties = inboundAuthenticationRequestConfig.getProperties();
        HashMap<String, List<String>> map = new HashMap<>(Arrays.stream(properties).collect(Collectors.groupingBy(
                Property::getName, Collectors.mapping(Property::getValue, Collectors.toList()))));

        validateIssuer(map, validationErrors,  inboundAuthenticationRequestConfig.getInboundAuthKey(), tenantDomain);
        validateIssuerQualifier(map, validationErrors);

        if (map.containsKey(SIGNING_ALGORITHM_URI) && !StringUtils.isBlank(map.get(SIGNING_ALGORITHM_URI).get(0))
                && !Arrays.asList(getSigningAlgorithmUris()).contains(map.get(SIGNING_ALGORITHM_URI).get(0))) {
            validationErrors.add(String.format(INVALID_SIGNING_ALGORITHM_URI, map.get(SIGNING_ALGORITHM_URI).get(0)));
        }

        if (map.containsKey(DIGEST_ALGORITHM_URI) && !StringUtils.isBlank(map.get(DIGEST_ALGORITHM_URI).get(0))
                && !Arrays.asList(getDigestAlgorithmURIs()).contains(map.get(DIGEST_ALGORITHM_URI).get(0))) {
            validationErrors.add(String.format(INVALID_DIGEST_ALGORITHM_URI , map.get(DIGEST_ALGORITHM_URI).get(0)));
        }

        if (map.containsKey(ASSERTION_ENCRYPTION_ALGORITHM_URI)
                && !StringUtils.isBlank(map.get(ASSERTION_ENCRYPTION_ALGORITHM_URI).get(0))
                && !Arrays.asList(getAssertionEncryptionAlgorithmURIs()).contains(
                        map.get(ASSERTION_ENCRYPTION_ALGORITHM_URI).get(0))) {
            validationErrors.add(String.format(INVALID_ASSERTION_ENCRYPTION_ALGORITHM_URI,
                    map.get(ASSERTION_ENCRYPTION_ALGORITHM_URI).get(0)));
        }

        if (map.containsKey(KEY_ENCRYPTION_ALGORITHM_URI)
                && !StringUtils.isBlank(map.get(KEY_ENCRYPTION_ALGORITHM_URI).get(0))
                && !Arrays.asList(getKeyEncryptionAlgorithmURIs()).contains(
                        map.get(KEY_ENCRYPTION_ALGORITHM_URI).get(0))) {
            validationErrors.add(String.format(INVALID_KEY_ENCRYPTION_ALGORITHM_URI,
                    map.get(KEY_ENCRYPTION_ALGORITHM_URI).get(0)));
        }

        inboundAuthenticationRequestConfig.setProperties(Arrays.stream(properties).filter(property ->
                (!property.getName().equals(IS_UPDATE))).toArray(Property[]::new));
    }

    private boolean isIssuerExists(String issuer, String tenantDomain) throws IdentityApplicationManagementException {
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        try {
            if (applicationDAO.getServiceProviderNameByClientId(issuer, SAMLSSO, tenantDomain) != null) {
                return true;
            }
            return false;
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityApplicationManagementException("Error while checking SAML issuer exists", e);
        }
    }

    private void validateIssuerQualifier(HashMap<String, List<String>> map, List<String> validationErrors) {
        if (map.containsKey(ISSUER_QUALIFIER) && (map.get(ISSUER_QUALIFIER) != null)
                && StringUtils.isNotBlank(map.get(ISSUER_QUALIFIER).get(0))
                && map.get(ISSUER_QUALIFIER).get(0).contains("@")) {
            String errorMessage = "\'@\' is a reserved character. Cannot be used for Service Provider Qualifier Value.";
            validationErrors.add(errorMessage);
        }
    }

    private void validateIssuer(HashMap<String, List<String>> map, List<String> validationErrors, String inboundAuthKey,
                                String tenantDomain) throws IdentityApplicationManagementException {

        if (!map.containsKey(ISSUER) || (map.get(ISSUER) == null) || StringUtils.isBlank(map.get(ISSUER).get(0))) {
            validationErrors.add("A value for the Issuer is mandatory.");
            return;
        }

        String issuerWithQualifier = inboundAuthKey;
        String issuerWithoutQualifier = map.get(ISSUER).get(0);
        if (map.containsKey(ISSUER_QUALIFIER) && (map.get(ISSUER_QUALIFIER) != null)
                && StringUtils.isNotBlank(map.get(ISSUER_QUALIFIER).get(0))) {
            issuerWithQualifier = getIssuerWithQualifier(inboundAuthKey, map.get(ISSUER_QUALIFIER).get(0));
            issuerWithoutQualifier = getIssuerWithoutQualifier(map.get(ISSUER).get(0));
        }

        if (!issuerWithoutQualifier.equals(inboundAuthKey)) {
            validationErrors.add(String.format("The Inbound Auth Key of the  application name %s " +
                    "is not match with SAML issuer %s.", inboundAuthKey, map.get(ISSUER).get(0)));
        }

        if (map.get(ISSUER).get(0).contains("@")) {
            String errorMessage = "\'@\' is a reserved character. Cannot be used for Service Provider Entity ID.";
            validationErrors.add(errorMessage);
        }

        //Have to check whether issuer exists in create or import (POST) operation.
        if (map.containsKey(IS_UPDATE) && (map.get(IS_UPDATE) != null) && map.get(IS_UPDATE).get(0).equals("false")
                && isIssuerExists(issuerWithQualifier, tenantDomain)) {
            if (map.containsKey(ISSUER_QUALIFIER) && (map.get(ISSUER_QUALIFIER) != null)
                    && StringUtils.isNotBlank(map.get(ISSUER_QUALIFIER).get(0))) {
                validationErrors.add(String.format(ISSUER_WITH_ISSUER_QUALIFIER_ALREADY_EXISTS, inboundAuthKey,
                        map.get(ISSUER_QUALIFIER).get(0), tenantDomain));
            } else {
                validationErrors.add(String.format(ISSUER_ALREADY_EXISTS, map.get(ISSUER).get(0), tenantDomain));
            }
        }
    }

    public String[] getSigningAlgorithmUris() {

        Collection<String> uris = IdentityApplicationManagementUtil.getXMLSignatureAlgorithms().values();
        return uris.toArray(new String[uris.size()]);
    }

    public String[] getDigestAlgorithmURIs() {

        Collection<String> digestAlgoUris = IdentityApplicationManagementUtil.getXMLDigestAlgorithms().values();
        return digestAlgoUris.toArray(new String[digestAlgoUris.size()]);
    }

    public String[] getAssertionEncryptionAlgorithmURIs() {

        Collection<String> assertionEncryptionAlgoUris =
                IdentityApplicationManagementUtil.getXMLAssertionEncryptionAlgorithms().values();
        return assertionEncryptionAlgoUris.toArray(new String[assertionEncryptionAlgoUris.size()]);
    }

    public String[] getKeyEncryptionAlgorithmURIs() {

        Collection<String> keyEncryptionAlgoUris =
                IdentityApplicationManagementUtil.getXMLKeyEncryptionAlgorithms().values();
        return keyEncryptionAlgoUris.toArray(new String[keyEncryptionAlgoUris.size()]);
    }

    /**
     * Get the issuer value to be added to registry by appending the qualifier.
     *
     * @param issuer value given as 'issuer' when configuring SAML SP.
     * @return issuer value with qualifier appended.
     */
    public static String getIssuerWithQualifier(String issuer, String qualifier) {

        if (StringUtils.isNotBlank(qualifier)) {
            return issuer + IdentityRegistryResources.QUALIFIER_ID + qualifier;
        } else {
            return issuer;
        }
    }
    /**
     * Get the issuer value by removing the qualifier.
     *
     * @param issuerWithQualifier issuer value saved in the registry.
     * @return issuer value given as 'issuer' when configuring SAML SP.
     */
    public static String getIssuerWithoutQualifier(String issuerWithQualifier) {

        return StringUtils.substringBeforeLast(issuerWithQualifier, IdentityRegistryResources.QUALIFIER_ID);
    }

}
