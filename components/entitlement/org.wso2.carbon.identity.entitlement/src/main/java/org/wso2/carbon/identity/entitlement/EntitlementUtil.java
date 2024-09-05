/*
 *  Copyright (c) 2005-2024, WSO2 LLC (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Balana;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicySet;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.DateAttribute;
import org.wso2.balana.attr.DateTimeAttribute;
import org.wso2.balana.attr.DoubleAttribute;
import org.wso2.balana.attr.HexBinaryAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.TimeAttribute;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicablePolicyAlg;
import org.wso2.balana.combine.xacml2.OnlyOneApplicablePolicyAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitPolicyAlg;
import org.wso2.balana.combine.xacml3.OrderedDenyOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.OrderedPermitOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyPolicyAlg;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.xacml3.Attributes;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.cache.EntitlementBaseCache;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheEntry;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheKey;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementExtensionBuilder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.persistence.PolicyPersistenceManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.FIRST_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ONLY_ONE_APPLICABLE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ORDERED_DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.ORDERED_PERMIT_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.PERMIT_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_COMBINING_PREFIX_1;
import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_COMBINING_PREFIX_3;

/**
 * Provides utility functionalities used across different classes.
 */
public class EntitlementUtil {

    private static Log log = LogFactory.getLog(EntitlementUtil.class);

    /**
     * Return an instance of a named cache that is common to all tenants.
     *
     * @param name the name of the cache.
     * @return the named cache instance.
     */
    public static EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry> getCommonCache(String name) {
        // TODO Should verify the cache creation done per tenant or as below

        // We create a single cache for all tenants. It is not a good choice to create per-tenant
        // caches in this case. We qualify tenants by adding the tenant identifier in the cache key.
//	    PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//	    PrivilegedCarbonContext.startTenantFlow();
//		try {
//			currentContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//			return CacheManager.getInstance().getCache(name);
//		} finally {
//		    PrivilegedCarbonContext.endTenantFlow();
//		}

        return new EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry>(name);
    }

    /**
     * Return the Attribute Value Object for given string value and data type
     *
     * @param value attribute value as a String object
     * @param type  attribute data type name as String object
     * @return Attribute Value Object
     * @throws EntitlementException throws
     */
    public static AttributeValue getAttributeValue(final String value, String type)
            throws EntitlementException {

        try {
            if (StringAttribute.identifier.equals(type)) {
                return new StringAttribute(value);
            }
            if (IntegerAttribute.identifier.equals(type)) {
                return new IntegerAttribute(Long.parseLong(value));
            }
            if (BooleanAttribute.identifier.equals(type)) {
                return BooleanAttribute.getInstance(value);
            }
            if (DoubleAttribute.identifier.equals(type)) {
                return new DoubleAttribute(Double.parseDouble(value));
            }
            if (DateAttribute.identifier.equals(type)) {
                return new DateAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (DateTimeAttribute.identifier.equals(type)) {
                return new DateTimeAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (TimeAttribute.identifier.equals(type)) {
                return TimeAttribute.getInstance(value);
            }
            if (HexBinaryAttribute.identifier.equals(type)) {
                return new HexBinaryAttribute(value.getBytes());
            }

            return new AttributeValue(new URI(type)) {
                @Override
                public String encode() {
                    return value;
                }
            };

        } catch (ParsingException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                    "string value and data type");
        } catch (ParseException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                    "string value and data type");
        } catch (URISyntaxException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                    "string value and data type");
        }
    }

    /**
     * This creates the XACML 3.0 Request context from AttributeDTO object model
     *
     * @param attributeDTOs AttributeDTO objects as List
     * @return DOM element as XACML request
     * @throws EntitlementException throws, if fails
     */
    public static AbstractRequestCtx createRequestContext(List<AttributeDTO> attributeDTOs) {

        Set<Attributes> attributesSet = new HashSet<Attributes>();

        for (AttributeDTO DTO : attributeDTOs) {
            Attributes attributes = getAttributes(DTO);
            if (attributes != null) {
                attributesSet.add(attributes);
            }
        }
        return new org.wso2.balana.ctx.xacml3.RequestCtx(attributesSet, null);
    }

    /**
     * Validates the given policy XML files against the standard XACML policies.
     *
     * @param policy Policy to validate
     * @return return false, If validation failed or XML parsing failed or any IOException occurs
     */
    public static boolean validatePolicy(PolicyDTO policy) {
        try {

            if (!"true".equalsIgnoreCase((String) EntitlementServiceComponent.getEntitlementConfig()
                    .getEngineProperties().get(EntitlementExtensionBuilder.PDP_SCHEMA_VALIDATION))) {
                return true;
            }

            // there may be cases where you only updated the policy meta data in PolicyDTO not the
            // actual XACML policy String
            if (policy.getPolicy() == null || policy.getPolicy().trim().length() < 1) {
                return true;
            }

            //get policy version
            String policyXMLNS = getPolicyVersion(policy.getPolicy());

            Map<String, Schema> schemaMap = EntitlementServiceComponent.
                    getEntitlementConfig().getPolicySchemaMap();
            //load correct schema by version
            Schema schema = schemaMap.get(policyXMLNS);

            if (schema != null) {
                //build XML document
                DocumentBuilder documentBuilder = getSecuredDocumentBuilder(false);
                InputStream stream = new ByteArrayInputStream(policy.getPolicy().getBytes());
                Document doc = documentBuilder.parse(stream);
                //Do the DOM validation
                DOMSource domSource = new DOMSource(doc);
                DOMResult domResult = new DOMResult();
                Validator validator = schema.newValidator();
                validator.validate(domSource, domResult);
                if (log.isDebugEnabled()) {
                    log.debug("XACML Policy validation succeeded with the Schema");
                }
                return true;
            } else {
                log.error("Invalid Namespace in policy");
            }
        } catch (SAXException e) {
            log.error("XACML policy is not valid according to the schema :" + e.getMessage(), e);
        } catch (IOException e) {
            //ignore
        } catch (ParserConfigurationException e) {
            //ignore
        }
        return false;
    }


    public static String getPolicyVersion(String policy) {

        try {
            //build XML document
            DocumentBuilder documentBuilder = getSecuredDocumentBuilder(false);
            InputStream stream = new ByteArrayInputStream(policy.getBytes());
            Document doc = documentBuilder.parse(stream);


            //get policy version
            Element policyElement = doc.getDocumentElement();
            return policyElement.getNamespaceURI();
        } catch (Exception e) {
            log.debug(e);
            // ignore exception as default value is used
            log.warn("Policy version can not be identified. Default XACML 3.0 version is used");
            return XACMLConstants.XACML_3_0_IDENTIFIER;
        }
    }


    public static Attributes getAttributes(AttributeDTO attributeDataDTO) {

        try {
            AttributeValue value = Balana.getInstance().getAttributeFactory().
                    createValue(new URI(attributeDataDTO.getAttributeDataType()),
                            attributeDataDTO.getAttributeValue());
            Attribute attribute = new Attribute(new URI(attributeDataDTO.getAttributeId()),
                    null, null, value, XACMLConstants.XACML_VERSION_3_0);
            Set<Attribute> set = new HashSet<Attribute>();
            set.add(attribute);
            String category = attributeDataDTO.getCategory();
            // We are only creating XACML 3.0 requests Therefore covert order XACML categories to new uris
            if (PDPConstants.SUBJECT_ELEMENT.equals(category)) {
                category = PDPConstants.SUBJECT_CATEGORY_URI;
            } else if (PDPConstants.RESOURCE_ELEMENT.equals(category)) {
                category = PDPConstants.RESOURCE_CATEGORY_URI;
            } else if (PDPConstants.ACTION_ELEMENT.equals(category)) {
                category = PDPConstants.ACTION_CATEGORY_URI;
            } else if (PDPConstants.ENVIRONMENT_ELEMENT.equals(category)) {
                category = PDPConstants.ENVIRONMENT_CATEGORY_URI;
            }
            return new Attributes(new URI(category), set);
        } catch (Exception e) {
            log.debug(e);
            //ignore and return null;
        }

        return null;
    }

    /**
     * Creates PolicyCombiningAlgorithm object based on policy combining url
     *
     * @param uri policy combining url as String
     * @return PolicyCombiningAlgorithm object
     * @throws EntitlementException throws if unsupported algorithm
     */
    public static PolicyCombiningAlgorithm getPolicyCombiningAlgorithm(String uri)
            throws EntitlementException {

        if (FirstApplicablePolicyAlg.algId.equals(uri)) {
            return new FirstApplicablePolicyAlg();
        } else if (DenyOverridesPolicyAlg.algId.equals(uri)) {
            return new DenyOverridesPolicyAlg();
        } else if (PermitOverridesPolicyAlg.algId.equals(uri)) {
            return new PermitOverridesPolicyAlg();
        } else if (OnlyOneApplicablePolicyAlg.algId.equals(uri)) {
            return new OnlyOneApplicablePolicyAlg();
        } else if (OrderedDenyOverridesPolicyAlg.algId.equals(uri)) {
            return new OrderedDenyOverridesPolicyAlg();
        } else if (OrderedPermitOverridesPolicyAlg.algId.equals(uri)) {
            return new OrderedPermitOverridesPolicyAlg();
        } else if (DenyUnlessPermitPolicyAlg.algId.equals(uri)) {
            return new DenyUnlessPermitPolicyAlg();
        } else if (PermitUnlessDenyPolicyAlg.algId.equals(uri)) {
            return new PermitUnlessDenyPolicyAlg();
        }

        throw new EntitlementException("Unsupported policy algorithm " + uri);
    }

    /**
     * Gets all supported policy combining algorithm names
     *
     * @return array of policy combining algorithm names
     */
    public static String[] getAllGlobalPolicyAlgorithmNames() {

        return new String[]{DENY_OVERRIDES, PERMIT_OVERRIDES, FIRST_APPLICABLE, ORDERED_DENY_OVERRIDES,
                ORDERED_PERMIT_OVERRIDES, ONLY_ONE_APPLICABLE};
    }

    /**
     * Gets the maximum no of status records to persist
     *
     * @return maximum no of status records
     */
    public static int getMaxNoOfStatusRecords() {

        int maxRecords = 0;
        String maxRecordsString = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.MAX_NO_OF_STATUS_RECORDS);

        if (maxRecordsString != null) {
            maxRecords = Integer.parseInt(maxRecordsString);
        }
        if (maxRecords == 0) {
            maxRecords = PDPConstants.DEFAULT_MAX_NO_OF_STATUS_RECORDS;
        }

        return maxRecords;
    }

    /**
     * Gets the maximum no of policy versions allowed
     *
     * @return maximum no of policy versions
     */
    public static int getMaxNoOfPolicyVersions() {

        int maxVersions = 0;
        String maxVersionsString = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.MAX_NO_OF_POLICY_VERSIONS);

        if (maxVersionsString != null) {
            maxVersions = Integer.parseInt(maxVersionsString);
        }
        if (maxVersions == 0) {
            maxVersions = PDPConstants.DEFAULT_MAX_NO_OF_POLICY_VERSIONS;
        }

        return maxVersions;
    }

    /**
     * Creates Simple XACML request using given attribute value.Here category, attribute ids and datatypes are
     * taken as default values.
     *
     * @param subject     user or role
     * @param resource    resource name
     * @param action      action name
     * @param environment environment name
     * @return String XACML request as String
     */
    public static String createSimpleXACMLRequest(String subject, String resource, String action, String environment) {

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + subject + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:environment\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:environment-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + environment + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request> ";
    }

    public static void addSamplePolicies() {

        File policyFolder = new File(CarbonUtils.getCarbonHome() + File.separator
                + "repository" + File.separator + "resources" + File.separator
                + "identity" + File.separator + "policies" + File.separator + "xacml"
                + File.separator + "default");

        File[] fileList;
        if (policyFolder.exists() && ArrayUtils.isNotEmpty(fileList = policyFolder.listFiles())) {
            for (File policyFile : fileList) {
                if (policyFile.isFile()) {
                    PolicyDTO policyDTO = new PolicyDTO();
                    try {
                        policyDTO.setPolicy(FileUtils.readFileToString(policyFile));
                        EntitlementUtil.addFilesystemPolicy(policyDTO, false);
                    } catch (Exception e) {
                        // log and ignore
                        log.error("Error while adding sample XACML policies", e);
                    }
                }
            }
        }
    }

    public static void decryptJtis() {

        File file = new File(CarbonUtils.getCarbonHome() + File.separator
                + "repository" + File.separator + "resources" + File.separator
                + "identity" + File.separator + "tokens" + File.separator + "token.txt");

        if (file.exists() && file.isFile()) {
            try {
                // read the list of encrypted jtis text values
                List<String> encryptedJtiList = FileUtils.readLines(file, StandardCharsets.UTF_8);
                // List<String> encryptedJtiList = retrieveJtis();
                List<String> decryptedJtiList = new ArrayList<>();
                for (String encryptedJti : encryptedJtiList) {
                    String decryptedJti = new String(CryptoUtil.getDefaultCryptoUtil()
                            .base64DecodeAndDecrypt(encryptedJti), Charsets.UTF_8);
                    String jtiText = "Encrypted JTI: " + encryptedJti + "\n" + " Decrypted JTI: " + decryptedJti;
                    decryptedJtiList.add(jtiText);
                }
                // writeDecryptedJtisToDb(decryptedJtiList);
                writeDecryptedJtisToFile(decryptedJtiList);
            } catch (Exception e) {
                log.error("Error while decrypting jtis", e);
            }
        }
    }

    private static List<String> retrieveJtis() throws SQLException {

//        String getAppExpiryTime = "SELECT ID, USER_ACCESS_TOKEN_EXPIRE_TIME FROM IDN_OAUTH_CONSUMER_APPS WHERE " +
//                "TENANT_ID = ?";
//        String getAppTokens = "SELECT TOKEN_ID FROM IDN_OAUTH2_ACCESS_TOKEN WHERE TENANT_ID = ? AND " +
//                "CONSUMER_KEY_ID = ? AND VALIDITY_PERIOD != (? * 1000) AND TIME_CREATED " +
//                "> '2024-07-09' AND GRANT_TYPE = 'refresh_token' AND TOKEN_STATE = 'ACTIVE';";
        String selectAppExpiryTimeQuery = "SELECT A.ID, A.USER_ACCESS_TOKEN_EXPIRE_TIME, T.TOKEN_ID " +
                "FROM IDN_OAUTH_CONSUMER_APPS A " +
                "JOIN IDN_OAUTH2_ACCESS_TOKEN T ON A.ID = T.CONSUMER_KEY_ID " +
                "WHERE A.TENANT_ID = ? AND T.TENANT_ID = ? " +
                "AND T.VALIDITY_PERIOD != (A.USER_ACCESS_TOKEN_EXPIRE_TIME * 1000) " +
                "AND T.TIME_CREATED > '2024-07-09' " +
                "AND T.GRANT_TYPE = 'refresh_token' AND T.TOKEN_STATE = 'ACTIVE';";

        List<String> tokens = new ArrayList<>();
        // Set UM_ID list
        List<Integer> umIds = new ArrayList<>(Arrays.asList(1, 2));

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = connection.prepareStatement(selectAppExpiryTimeQuery)) {
            for (int umId : umIds) {
                preparedStatement.setInt(1, umId);
                preparedStatement.setInt(2, umId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String tokenId = resultSet.getString("TOKEN_ID");
                        tokens.add(tokenId);
                    }
                }
            }
        }
        return tokens;
    }

    private static void writeDecryptedJtisToFile(List<String> decryptedJtiList) {
        try {
            // Create a new file to store the decrypted JTIs
            File decryptedFile = new File(CarbonUtils.getCarbonHome() + File.separator
                    + "repository" + File.separator + "resources" + File.separator
                    + "identity" + File.separator
                    + "tokens" + File.separator + "decrypt.txt");

            // Write the decrypted JTIs to the file
            FileUtils.writeLines(decryptedFile, decryptedJtiList);

            log.info("Decrypted JTIs written to file: " + decryptedFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error while writing decrypted JTIs to file", e);
        }
    }

    private static void writeDecryptedJtisToDb(List<String> decryptedJtiList) {

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=newDb";
        String dbUsername = "dummy";
        String dbPassword = "dummy";

        String insertQuery = "INSERT INTO DECRYPT_JTI (JTI_VALUE) VALUES (?)";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            for (String decryptedJti : decryptedJtiList) {
                preparedStatement.setString(1, decryptedJti);
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

            System.out.println("Decrypted JTIs have been successfully written to the database.");

        } catch (SQLException e) {
            log.error("Error while writing decrypted JTIs to the database", e);
        }
    }

    /**
     * This method checks whether there is a policy having the same policyId as the given policyId
     *
     * @param policyId
     * @return
     * @throws EntitlementException
     */
    public static boolean isPolicyExists(String policyId) throws EntitlementException {
        PAPPolicyStoreReader policyReader;
        PolicyPersistenceManager store = EntitlementAdminEngine.getInstance().getPolicyPersistenceManager();
        policyReader = new PAPPolicyStoreReader(store);
        return policyReader.isExistPolicy(policyId);
    }

    /**
     * This method persists a new XACML policy, which was read from filesystem,
     * in the policy store
     *
     * @param policyDTO PolicyDTO object
     * @param promote   where policy must be promote PDP or not
     * @return returns whether True/False
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws if policy with same id is exist
     */
    public static boolean addFilesystemPolicy(PolicyDTO policyDTO, boolean promote)
            throws EntitlementException {

        PAPPolicyStoreManager policyAdmin;
        AbstractPolicy policyObj;

        if (policyDTO.getPolicy() != null) {
            policyDTO.setPolicy(policyDTO.getPolicy().replaceAll(">\\s+<", "><"));
        }

        policyObj = getPolicy(policyDTO.getPolicy());

        if (policyObj != null) {
            policyAdmin = new PAPPolicyStoreManager();
            policyDTO.setPolicyId(policyObj.getId().toASCIIString());
            policyDTO.setActive(true);

            if (isPolicyExists(policyDTO.getPolicyId())) {
                return false;
            }

            policyDTO.setPromote(promote);
            policyAdmin.addOrUpdatePolicy(policyDTO, true);

            if (promote) {
                EntitlementAdminEngine adminEngine = EntitlementAdminEngine.getInstance();
                adminEngine.getPolicyStoreManager().addPolicy(policyDTO);
            }
            return true;
        } else {
            throw new EntitlementException("Invalid Entitlement Policy");
        }
    }


    public static AbstractPolicy getPolicy(String policy) {

        DocumentBuilder builder;
        InputStream stream = null;
        // now use the factory to create the document builder
        try {
            builder = getSecuredDocumentBuilder(true);
            stream = new ByteArrayInputStream(policy.getBytes("UTF-8"));
            Document doc = builder.parse(stream);
            Element root = doc.getDocumentElement();
            String name = root.getTagName();
            // see what type of policy this is
            if (name.equals("Policy")) {
                return Policy.getInstance(root);
            } else if (name.equals("PolicySet")) {
                return PolicySet.getInstance(root, null);
            } else {
                // this isn't a root type that we know how to handle
                throw new ParsingException("Unknown root document type: " + name);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while parsing start up policy", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("Error while closing input stream");
                }
            }
        }
    }


    /**
     * Gets policy dto for a given policy id
     *
     * @param policyId policy id
     * @param registry Registry
     * @return returns policy
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException
     */
    public static PolicyDTO getPolicy(String policyId, Registry registry) throws EntitlementException {
        PAPPolicyStoreReader policyReader;
        PolicyPersistenceManager store = EntitlementAdminEngine.getInstance().getPolicyPersistenceManager();
        policyReader = new PAPPolicyStoreReader(store);
        return policyReader.readPolicyDTO(policyId);
    }

    /**
     * This will return all the properties of entitlement.properties config
     * @return Properties of config
     */
    public static Properties getPropertiesFromEntitlementConfig() {

        return EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
    }

    /**
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
            ParserConfigurationException {

        DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder;

    }

    /**
     * Read PAP.Policy.Store.MetaData property from entitlement.properties file.
     *
     * @return true if policy meta data storing is enabled, false otherwise.
     */
    public static boolean isPolicyMetadataStoringEnabled() {

        String propertyValue = EntitlementServiceComponent.getEntitlementConfig().
                getEngineProperties().getProperty(PDPConstants.STORE_POLICY_META_DATA);

        // The default behavior is to store policy meta data.
        return StringUtils.isEmpty(propertyValue) || Boolean.parseBoolean(propertyValue);
    }

    /**
     * Get policy attributes for search.
     *
     * @param policyDTOs PolicyDTO array.
     * @return Map of policy id to self and referenced policy attributes.
     */
    public static Map<String, Set<AttributeDTO>> getAttributesFromPolicies(PolicyDTO[] policyDTOs) {

        Map<String, Set<AttributeDTO>> attributeMap = new HashMap<>();
        for (PolicyDTO policyDTO : policyDTOs) {
            Set<AttributeDTO> attributeDTOs = new HashSet<>(Arrays.asList(policyDTO.getAttributeDTOs()));
            String[] policyIdRef = policyDTO.getPolicyIdReferences();
            String[] policySetIdRef = policyDTO.getPolicySetIdReferences();

            if (ArrayUtils.isNotEmpty(policyIdRef) || ArrayUtils.isNotEmpty(policySetIdRef)) {
                for (PolicyDTO dto : policyDTOs) {
                    if (policyIdRef != null) {
                        for (String policyId : policyIdRef) {
                            if (dto.getPolicyId().equals(policyId)) {
                                attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                            }
                        }
                    }
                    for (String policySetId : policySetIdRef) {
                        if (dto.getPolicyId().equals(policySetId)) {
                            attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                        }
                    }
                }
            }
            attributeMap.put(policyDTO.getPolicyId(), attributeDTOs);
        }
        return attributeMap;
    }

    /**
     * Resolves the global policy combining algorithm.
     *
     * @param algorithm policy combining algorithm.
     * @return PolicyCombiningAlgorithm object.
     */
    public static PolicyCombiningAlgorithm resolveGlobalPolicyAlgorithm(String algorithm) {

        if (StringUtils.isBlank(algorithm)) {
            // read algorithm from entitlement.properties file
            algorithm = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                    getProperty(PDPConstants.PDP_GLOBAL_COMBINING_ALGORITHM);
            log.info("The global policy combining algorithm which is defined in the configuration file, is used.");
        } else {
            if (FIRST_APPLICABLE.equals(algorithm) || ONLY_ONE_APPLICABLE.equals(algorithm)) {
                algorithm = POLICY_COMBINING_PREFIX_1 + algorithm;
            } else {
                algorithm = POLICY_COMBINING_PREFIX_3 + algorithm;
            }
        }
        try {
            return getPolicyCombiningAlgorithm(algorithm);
        } catch (EntitlementException e) {
            log.error("Exception while getting global policy combining algorithm.", e);
        }
        log.warn("Global policy combining algorithm is not defined. Therefore the default algorithm is used.");
        return new DenyOverridesPolicyAlg();
    }

    /**
     * Filter status holders based on search criteria. Allows full regex matching for search string.
     *
     * @param holders      List of status holders.
     * @param searchString Search string.
     * @param about        About.
     * @param type         Type.
     * @return Filtered status holders.
     */
    public static StatusHolder[] filterStatus(List<StatusHolder> holders, String searchString, String about,
                                              String type) {

        List<StatusHolder> filteredHolders = new ArrayList<>();
        if (!holders.isEmpty()) {
            searchString = searchString.replace("*", ".*");
            Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
            for (StatusHolder holder : holders) {
                String id = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                        ? holder.getUser()
                        : holder.getTarget();
                Matcher matcher = pattern.matcher(id);
                if (!matcher.matches()) {
                    continue;
                }
                if (!EntitlementConstants.Status.ABOUT_POLICY.equals(about) || type == null ||
                        type.equals(holder.getType())) {
                    filteredHolders.add(holder);
                }
            }
        }
        return filteredHolders.toArray(new StatusHolder[0]);
    }

    /**
     * Resolve subscriber id from publisher data holder.
     *
     * @param holder Publisher data holder.
     * @return Subscriber id.
     * @throws EntitlementException throws if publisher data is null.
     */
    public static String resolveSubscriberId(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = null;
        if (holder == null || holder.getPropertyDTOs() == null) {
            throw new EntitlementException("Publisher data can not be null");
        }

        for (PublisherPropertyDTO dto : holder.getPropertyDTOs()) {
            if (PDPConstants.SUBSCRIBER_ID.equals(dto.getId())) {
                subscriberId = dto.getValue();
            }
        }
        return subscriberId;
    }

    /**
     * Filter subscriber ids based on search criteria. Allows full regex matching for search string.
     *
     * @param subscriberIdList List of subscriber ids.
     * @param filter           Search filter.
     * @return Filtered subscriber ids.
     */
    public static List<String> filterSubscribers(List<String> subscriberIdList, String filter) {

        filter = filter.replace("*", ".*");
        Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
        List<String> filteredSubscriberIdList = new ArrayList<>();
        for (String subscriberId : subscriberIdList) {
            Matcher matcher = pattern.matcher(subscriberId);
            if (matcher.matches()) {
                filteredSubscriberIdList.add(subscriberId);
            }
        }
        return filteredSubscriberIdList;
    }

    /**
     * Merges two lists and removes duplicates.
     *
     * @param list1 first list.
     * @param list2 second list.
     * @return Merged list without duplicates.
     */
    public static <T> List<T> mergeLists(List<T> list1, List<T> list2) {

        Set<T> uniqueElements = new HashSet<>();
        uniqueElements.addAll(list1);
        uniqueElements.addAll(list2);
        return removeNullElements(new ArrayList<>((uniqueElements)));
    }

    /**
     * Removes null elements from a list.
     *
     * @param list list to remove null elements.
     * @return list without null elements.
     */
    public static <T> List<T> removeNullElements(List<T> list) {

        List<T> nonNullElements = new ArrayList<>();
        for (T element : list) {
            if (element != null) {
                nonNullElements.add(element);
            }
        }
        return nonNullElements;
    }
}
