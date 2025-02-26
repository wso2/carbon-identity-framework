/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Read and parse dialect configurations in schemas.xml file.
 */
public class DialectConfigParser {

    private static final String SCHEMA_FILE_NAME = "schemas.xml";
    private static final String SCHEMAS_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";
    private static final String DEFAULT_SCHEMA_CONFIG = "DefaultSchema";
    private static final String ADD_SCHEMA_CONFIG = "AddSchema";
    private static final String REMOVE_SCHEMA_CONFIG = "RemoveSchema";
    private static final String SCHEMAS_CONFIG = "Schemas";
    private static final String SCHEMA_CONFIG = "Schema";
    private static final String SCHEMA_ID_CONFIG = "id";
    private static final String ATTRIBUTE_CONFIG = "Attribute";

    private static final Log log = LogFactory.getLog(DialectConfigParser.class);

    private final String schemasFilePath;
    private Map<String, String> claimsMap = Collections.emptyMap();
    private Map<String, String> additionsToDefaultDialects = Collections.emptyMap();
    private Map<String, String> removalsFromDefaultDialects = Collections.emptyMap();

    private DialectConfigParser() {

        schemasFilePath = IdentityUtil.getIdentityConfigDirPath() + File.separator + SCHEMA_FILE_NAME;
        buildConfiguration();
    }

    private static final class SchemaConfigParserHolder {

        static final DialectConfigParser schemaConfigParser = new DialectConfigParser();
    }

    public static DialectConfigParser getInstance() {

        return SchemaConfigParserHolder.schemaConfigParser;
    }

    private void buildConfiguration() {

        Path schemaPath = Paths.get(schemasFilePath);
        if (Files.notExists(schemaPath)) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to find a valid configuration file in path: " + schemasFilePath);
            }
            return;
        }

        try (InputStream inputStream = Files.newInputStream(schemaPath)) {
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            OMElement rootElement = builder.getDocumentElement();
            claimsMap = buildSchemasConfiguration(rootElement, DEFAULT_SCHEMA_CONFIG);
            additionsToDefaultDialects = buildSchemasConfiguration(rootElement, ADD_SCHEMA_CONFIG);
            removalsFromDefaultDialects = buildSchemasConfiguration(rootElement, REMOVE_SCHEMA_CONFIG);

            if (additionsToDefaultDialects != null) {
                additionsToDefaultDialects.forEach((key, value) -> {
                    if (!claimsMap.containsKey(key)) {
                        claimsMap.put(key, value);
                    }
                });
            }
            if (removalsFromDefaultDialects != null) {
                removalsFromDefaultDialects.forEach((key, value) -> claimsMap.remove(key));
            }
        } catch (IOException | XMLStreamException e) {
            throw IdentityRuntimeException.error("Error occurred while reading schema configuration in path: " +
                    schemasFilePath, e);
        }
    }

    private Map<String, String> buildSchemasConfiguration(OMElement rootElement, String configName) {

        OMElement configSchema = rootElement.getFirstChildWithName(new QName(SCHEMAS_NAMESPACE, configName));
        if (configSchema == null) {
            return Collections.emptyMap();
        }
        OMElement schemas = configSchema.getFirstChildWithName(new QName(SCHEMAS_NAMESPACE, SCHEMAS_CONFIG));
        if (schemas == null || !schemas.getChildrenWithLocalName(SCHEMA_CONFIG).hasNext()) {
            return Collections.emptyMap();
        }
        Iterator schemaIterator = schemas.getChildrenWithLocalName(SCHEMA_CONFIG);
        Map<String, String> dataMap = new HashMap<>();
        while (schemaIterator.hasNext()) {
            OMElement schema = (OMElement) schemaIterator.next();
            String schemaId = schema.getAttributeValue(new QName(SCHEMA_ID_CONFIG));
            if (StringUtils.isBlank(schemaId)) {
                log.warn("Skipping schema with missing or empty '" + SCHEMA_ID_CONFIG + "' attribute in " + schemasFilePath);
                continue;
            }
            Iterator attributes = schema.getChildrenWithLocalName(ATTRIBUTE_CONFIG);
            if (!attributes.hasNext()) {
                continue;
            }

            while (attributes.hasNext()) {
                OMElement attribute = (OMElement) attributes.next();
                if (attribute != null && StringUtils.isNotBlank(attribute.getText())) {
                    dataMap.put(attribute.getText(), schemaId);
                }
            }
        }
        return dataMap;
    }

    /**
     * Return claims supported by the server.
     *
     * @return Claim Map.
     */
    public Map<String, String> getClaimsMap() {

        return claimsMap;
    }

    /**
     * Get the additions to the default schema.
     *
     * @return Additions to the default schema.
     */
    public Map<String, String> getAdditionsToDefaultDialects() {

        return additionsToDefaultDialects;
    }

    /**
     * Get the removals from the default schema.
     *
     * @return Removals from the default schema.
     */
    public Map<String, String> getRemovalsFromDefaultDialects() {

        return removalsFromDefaultDialects;
    }
}

