package org.wso2.carbon.identity.application.mgt.validator;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
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


    public static final String ISSUER = "issuer";
    public static final String ISSUER_QUALIFIER = "issuerQualifier";
    public static final String ASSERTION_CONSUMER_URLS = "assertionConsumerUrls";
    public static final String DEFAULT_ASSERTION_CONSUMER_URL = "defaultAssertionConsumerUrl";
    public static final String SIGNING_ALGORITHM_URI = "signingAlgorithmURI";
    public static final String DIGEST_ALGORITHM_URI = "digestAlgorithmURI";
    public static final String ASSERTION_ENCRYPTION_ALGORITHM_URI = "assertionEncryptionAlgorithmURI";
    public static final String KEY_ENCRYPTION_ALGORITHM_URI = "keyEncryptionAlgorithmURI";
    public static final String CERT_ALIAS = "certAlias";
    public static final String DO_SIGN_RESPONSE = "doSignResponse";
    private static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "attrConsumServiceIndex";
    public static final String DO_SINGLE_LOGOUT = "doSingleLogout";
    public static final String DO_FRONT_CHANNEL_LOGOUT = "doFrontChannelLogout";
    public static final String FRONT_CHANNEL_LOGOUT_BINDING = "frontChannelLogoutBinding";
    public static final String IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED = "isAssertionQueryRequestProfileEnabled";
    public static final String SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES = "supportedAssertionQueryRequestTypes";
    public static final String ENABLE_SAML2_ARTIFACT_BINDING = "enableSAML2ArtifactBinding";
    public static final String DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE = "doValidateSignatureInArtifactResolve";
    public static final String LOGIN_PAGE_URL = "loginPageURL";
    public static final String SLO_RESPONSE_URL = "sloResponseURL";
    public static final String SLO_REQUEST_URL = "sloRequestURL";
    public static final String REQUESTED_CLAIMS = "requestedClaims";
    public static final String REQUESTED_AUDIENCES = "requestedAudiences";
    public static final String REQUESTED_RECIPIENTS = "requestedRecipients";
    public static final String ENABLE_ATTRIBUTES_BY_DEFAULT = "enableAttributesByDefault";
    public static final String NAME_ID_CLAIM_URI = "nameIdClaimUri";
    public static final String NAME_ID_FORMAT = "nameIDFormat";
    public static final String IDP_INIT_SSO_ENABLED = "idPInitSSOEnabled";
    public static final String IDP_INIT_SLO_ENABLED = "idPInitSLOEnabled";
    public static final String IDP_INIT_SLO_RETURN_TO_URLS = "idpInitSLOReturnToURLs";
    public static final String DO_ENABLE_ENCRYPTED_ASSERTION = "doEnableEncryptedAssertion";
    public static final String DO_VALIDATE_SIGNATURE_IN_REQUESTS = "doValidateSignatureInRequests";
    public static final String IDP_ENTITY_ID_ALIAS = "idpEntityIDAlias";

    private static final String INVALID_SIGNING_ALGORITHM_URI = "Invalid Response Signing Algorithm: %s";
    private static final String INVALID_DIGEST_ALGORITHM_URI = "Invalid Response Digest Algorithm: %s";
    private static final String INVALID_ASSERTION_ENCRYPTION_ALGORITHM_URI = "Invalid Assertion Encryption Algorithm:" +
            " %s";
    private static final String INVALID_KEY_ENCRYPTION_ALGORITHM_URI = "Invalid Key Encryption Algorithm: %s";
    private static final String INVALID_ISSUER = "An application with the SAML issuer: %s already exists in " +
            "tenantDomain: %s";

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
            if (inboundAuthenticationRequestConfig.getInboundAuthType().equals("samlsso")) {
                validateSAMLProperties(validationErrors, inboundAuthenticationRequestConfig, tenantDomain);
                break;
            }
        }
        return validationErrors;
    }

    private void validateSAMLProperties(List<String> validationErrors,
                                        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig,
                                        String tenantDomain) {
        Property[] properties = inboundAuthenticationRequestConfig.getProperties();
        HashMap<String, List<String>> map = new HashMap<>(Arrays.stream(properties).collect(Collectors.groupingBy(
                Property::getName, Collectors.mapping(Property::getValue, Collectors.toList()))));

        validateIssuer(map, validationErrors,  inboundAuthenticationRequestConfig.getInboundAuthKey());
        validateIssuerQualifier(map, validationErrors);

        if (map.containsKey(ISSUER) && !StringUtils.isBlank(map.get(ISSUER).get(0)) &&
                isIssuerExists(map.get(ISSUER).get(0))) {
            validationErrors.add(String.format(INVALID_ISSUER, map.get(ISSUER).get(0), tenantDomain));
        }

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
    }

    private boolean isIssuerExists(String issuer) {
        //TODO :complete
        return false;
    }

    private void validateIssuerQualifier(HashMap<String, List<String>> map, List<String> validationErrors) {
        if (map.containsKey(ISSUER_QUALIFIER) && (map.get(ISSUER_QUALIFIER) != null)
                && StringUtils.isNotBlank(map.get(ISSUER_QUALIFIER).get(0))
                && map.get(ISSUER_QUALIFIER).get(0).contains("@")) {
            String errorMessage = "\'@\' is a reserved character. Cannot be used for Service Provider Qualifier Value.";
            validationErrors.add(errorMessage);
        }
    }

    private void validateIssuer(HashMap<String, List<String>> map, List<String> validationErrors, String issuer) {
        if (map.containsKey(ISSUER_QUALIFIER) && (map.get(ISSUER_QUALIFIER) != null)
                && StringUtils.isNotBlank(map.get(ISSUER_QUALIFIER).get(0))) {
            issuer = getIssuerWithoutQualifier(issuer);
        }

        if (!map.containsKey(ISSUER) || (map.get(ISSUER) == null) || StringUtils.isBlank(map.get(ISSUER).get(0))) {
            validationErrors.add("A value for the Issuer is mandatory.");
            return;
        }

        if (!map.get(ISSUER).get(0).equals(issuer)) {
            validationErrors.add(String.format("The Inbound Auth Key of the  application name %s " +
                    "is not match with SAML issuer %s.", issuer, map.get(ISSUER).get(0)));
        }

        if (map.get(ISSUER).get(0).contains("@")) {
            String errorMessage = "\'@\' is a reserved character. Cannot be used for Service Provider Entity ID.";
            validationErrors.add(errorMessage);
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
     * Get the issuer value by removing the qualifier.
     *
     * @param issuerWithQualifier issuer value saved in the registry.
     * @return issuer value given as 'issuer' when configuring SAML SP.
     */
    public static String getIssuerWithoutQualifier(String issuerWithQualifier) {

        return StringUtils.substringBeforeLast(issuerWithQualifier, IdentityRegistryResources.QUALIFIER_ID);
    }

}
