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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttribute;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ATTRIBUTE_ID;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.CLAIM_CONFIG;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.CLAIM_URI;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.DIALECT;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.DIALECTS;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.DISPLAY_NAME;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.LOCAL_DIALECT_URL;

/**
 * Loader class to load default userstore attribute mappings.
 */
public class DefaultUserStoreAttributeConfigLoader {

    /**
     * Load default userstore attribute mappings from claim-config.xml file.
     *
     * @return Map of default userstore attribute mappings.
     * @throws IdentityUserStoreServerException If an error occurred while loading default userstore attribute mappings.
     */
    public Map<String, UserStoreAttribute> loadDefaultUserStoreAttributeMappings()
            throws IdentityUserStoreServerException {

        File attributeMappingXml = new File(CarbonUtils.getCarbonConfigDirPath(), CLAIM_CONFIG);
        if (!attributeMappingXml.exists()) {
            throw new IdentityUserStoreServerException(String.format("Claim-config.xml file is not available at %s/%s.",
                    CarbonUtils.getCarbonConfigDirPath(), CLAIM_CONFIG));
        }
        try (InputStream inStream = new FileInputStream(attributeMappingXml)) {
            return buildDefaultAttributeMappings(inStream);
        } catch (IOException | XMLStreamException | OMException e) {
            throw new IdentityUserStoreServerException("Error occurred while reading claim-config.xml.", e);
        }
    }

    private Map<String, UserStoreAttribute> buildDefaultAttributeMappings(InputStream inStream)
            throws XMLStreamException, OMException, IdentityUserStoreServerException {

        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        Iterator iterator = builder.getDocumentElement().getFirstChildWithName(new QName(DIALECTS))
                .getChildrenWithLocalName(DIALECT);
        if (iterator == null) {
            throw new IdentityUserStoreServerException("Claim-config.xml file does not have dialects.");
        }
        OMElement localClaimElement = getLocalClaimElement(iterator);
        if (localClaimElement == null) {
            throw new IdentityUserStoreServerException("Local claims cannot be found in claim-config.xml file.");
        }
        Iterator claimIterator = localClaimElement.getChildElements();
        return getDefaultAttributeMappings(claimIterator);
    }

    private OMElement getLocalClaimElement(Iterator iterator) {

        while (iterator.hasNext()) {
            OMElement localClaimElement = (OMElement) iterator.next();
            Iterator attributeIterator = localClaimElement.getAllAttributes();
            if (attributeIterator != null) {
                String attributeValue = ((OMAttribute) attributeIterator.next()).getAttributeValue();
                if (StringUtils.equalsIgnoreCase(LOCAL_DIALECT_URL, attributeValue)) {
                    return localClaimElement;
                }
            }
        }
        return null;
    }

    private Map<String, UserStoreAttribute> getDefaultAttributeMappings(Iterator claimIterator) {

        Map<String, UserStoreAttribute> defaultAttributeMappings = new HashMap<>();
        while (claimIterator.hasNext()) {
            OMElement claimElement = (OMElement) claimIterator.next();
            Iterator attributeIterator = claimElement.getChildElements();
            UserStoreAttribute userStoreAttribute = new UserStoreAttribute();
            while (attributeIterator.hasNext()) {
                OMElement attributes = (OMElement) attributeIterator.next();
                String attributeQName = attributes.getQName().getLocalPart();
                if (StringUtils.equalsIgnoreCase(DISPLAY_NAME, attributeQName)) {
                    userStoreAttribute.setDisplayName(attributes.getText());
                } else if (StringUtils.equalsIgnoreCase(ATTRIBUTE_ID, attributeQName)) {
                    userStoreAttribute.setMappedAttribute(attributes.getText());
                } else if (StringUtils.equalsIgnoreCase(CLAIM_URI, attributeQName)) {
                    userStoreAttribute.setClaimUri(attributes.getText());
                    userStoreAttribute.setClaimId(Base64.getUrlEncoder().withoutPadding().
                            encodeToString(attributes.getText().getBytes(StandardCharsets.UTF_8)));
                }
            }
            defaultAttributeMappings.put(userStoreAttribute.getClaimId(), userStoreAttribute);
        }
        return defaultAttributeMappings;
    }
}
