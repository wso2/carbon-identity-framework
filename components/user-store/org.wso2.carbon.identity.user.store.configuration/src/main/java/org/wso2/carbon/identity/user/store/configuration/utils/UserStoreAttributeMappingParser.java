/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.configuration.model.ChangedUserStoreAttributeDO;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttributeDO;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.AD_ATTRIBUTE_MAPPING_CONFIG;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ATTRIBUTES_DIR;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ATTRIBUTE_ID;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.CLAIM_URI;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.DISPLAY_NAME;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.LDAP_ATTRIBUTE_MAPPING_CONFIG;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.OPERATION;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORE_DIR;

/**
 * Parser to read and build user store mappings attribute changes.
 */
public class UserStoreAttributeMappingParser {

    private static Map<String, ChangedUserStoreAttributeDO> adUserStoreAttrChangeDOMap;
    private static Map<String, ChangedUserStoreAttributeDO> ldapUserStoreAttrChangeDOMap;
    private static final Log LOG = LogFactory.getLog(UserStoreAttributeMappingParser.class);

    private UserStoreAttributeMappingParser() {

        init();
    }

    private static final class ParserHolder {

        static final UserStoreAttributeMappingParser PARSER = new UserStoreAttributeMappingParser();
    }

    public static UserStoreAttributeMappingParser getInstance() {

        return ParserHolder.PARSER;
    }

    private static void init() {

        ldapUserStoreAttrChangeDOMap = readChangeFiles(LDAP_ATTRIBUTE_MAPPING_CONFIG);
        adUserStoreAttrChangeDOMap = readChangeFiles(AD_ATTRIBUTE_MAPPING_CONFIG);
    }

    private static Map<String, ChangedUserStoreAttributeDO> readChangeFiles(String attrMappingsFileName) {

        InputStream inStream = null;
        try {
            if (attrMappingsFileName != null) {
                File attributeMappingXml = new File(getUserStoreAttributeMappingDirPath(), attrMappingsFileName);
                if (attributeMappingXml.exists()) {
                    inStream = new FileInputStream(attributeMappingXml);
                }
            }

            if (inStream == null) {
                String message = String.format("Attribute mappings configuration file is not found at: %s/%s.",
                        getUserStoreAttributeMappingDirPath(), attrMappingsFileName);
                throw new FileNotFoundException(message);
            }

            return readConfigMappings(inStream);
        } catch (FileNotFoundException e) {
            LOG.error(String.format("Attribute mappings configuration file is not found at: %s/%s.",
                    getUserStoreAttributeMappingDirPath(), attrMappingsFileName), e);
        }
        return null;
    }

    private static Map<String, ChangedUserStoreAttributeDO> readConfigMappings(InputStream inStream) {

        try {
            StAXOMBuilder builder = new StAXOMBuilder(inStream);
            Iterator iterator = builder.getDocumentElement().getChildElements();
            Map<String, ChangedUserStoreAttributeDO> attributeChangeDOMap = new HashMap<>();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    OMElement attributeElement = (OMElement) iterator.next();
                    Iterator attributeIterator = attributeElement.getChildElements();
                    ChangedUserStoreAttributeDO changedUserStoreAttributeDO = new ChangedUserStoreAttributeDO();
                    UserStoreAttributeDO userStoreAttributeDO = new UserStoreAttributeDO();
                    while (attributeIterator.hasNext()) {
                        OMElement attributes = (OMElement) attributeIterator.next();
                        String attributeQName = attributes.getQName().getLocalPart();

                        if (StringUtils.equalsIgnoreCase(OPERATION, attributeQName)) {
                            changedUserStoreAttributeDO.setOperation(attributes.getText());
                        } else if (StringUtils.equalsIgnoreCase(ATTRIBUTE_ID, attributeQName)) {
                            userStoreAttributeDO.setMappedAttribute(attributes.getText());
                        } else if (StringUtils.equalsIgnoreCase(CLAIM_URI, attributeQName)) {
                            userStoreAttributeDO.setClaimUri(attributes.getText());
                            userStoreAttributeDO.setClaimId(Base64.getUrlEncoder().withoutPadding().
                                    encodeToString(attributes.getText().getBytes(StandardCharsets.UTF_8)));
                        } else if (StringUtils.equalsIgnoreCase(DISPLAY_NAME, attributeQName)) {
                            userStoreAttributeDO.setDisplayName(attributes.getText());
                        }
                    }
                    changedUserStoreAttributeDO.setUsAttributeDO(userStoreAttributeDO);
                    attributeChangeDOMap.put(userStoreAttributeDO.getClaimId(), changedUserStoreAttributeDO);
                }
                return attributeChangeDOMap;
            }
        } catch (XMLStreamException e) {
            LOG.error("Error occurred while reading the xml file.", e);
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                LOG.error("Error occurred while closing the input stream.", e);
            }
        }
        return null;
    }


    /**
     * Get attributes needs to be changed for AD user store types.
     *
     * @return Map adUserStoreAttrChangeDOMap.
     */
    protected Map<String, ChangedUserStoreAttributeDO> getADUserStoreAttrChangeDOMap() {

        return adUserStoreAttrChangeDOMap;
    }

    /**
     * Get attributes needs to be changed for LDAP user store types.
     *
     * @return Map ldapUserStoreAttrChangeDOMap.
     */
    protected Map<String, ChangedUserStoreAttributeDO> getLDAPUserStoreAttrChangeDOMap() {

        return ldapUserStoreAttrChangeDOMap;
    }

    private static String getUserStoreAttributeMappingDirPath() {

        return String.format("%s%s%s%s%s", CarbonUtils.getCarbonConfigDirPath(), File.separator, ATTRIBUTES_DIR,
                File.separator, USERSTORE_DIR);
    }
}
