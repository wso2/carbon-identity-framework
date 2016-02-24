/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.entitlement.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

public class SchemaBuilder implements Runnable {

    private static Log log = LogFactory.getLog(SchemaBuilder.class);

    private EntitlementConfigHolder configHolder;

    public SchemaBuilder(EntitlementConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    @Override
    public void run() {
        try {
            buildPolicySchema();
            log.info("XACML policy schema loaded successfully.");
        } catch (Exception e) {
            configHolder.getEngineProperties().setProperty(EntitlementExtensionBuilder.PDP_SCHEMA_VALIDATION, "false");
            log.warn("Error while loading policy schema. Schema validation will be disabled.");
        }
    }

    /**
     * Builds the policy schema map. There are three schemas.
     *
     * @param configHolder holder EntitlementConfigHolder
     * @throws SAXException if fails
     */
    public void buildPolicySchema() throws SAXException {

        if (!"true".equalsIgnoreCase((String) configHolder.getEngineProperties().get(
                EntitlementExtensionBuilder.PDP_SCHEMA_VALIDATION))) {
            log.warn("PDP schema validation disabled.");
            return;
        }

        String[] schemaNSs = new String[]{PDPConstants.XACML_1_POLICY_XMLNS,
                PDPConstants.XACML_2_POLICY_XMLNS,
                PDPConstants.XACML_3_POLICY_XMLNS};

        for (String schemaNS : schemaNSs) {

            String schemaFile;

            if (PDPConstants.XACML_1_POLICY_XMLNS.equals(schemaNS)) {
                schemaFile = PDPConstants.XACML_1_POLICY_SCHEMA_FILE;
            } else if (PDPConstants.XACML_2_POLICY_XMLNS.equals(schemaNS)) {
                schemaFile = PDPConstants.XACML_2_POLICY_SCHEMA_FILE;
            } else {
                schemaFile = PDPConstants.XACML_3_POLICY_SCHEMA_FILE;
            }

            InputStream schemaFileStream = EntitlementExtensionBuilder.class.getResourceAsStream("/" + schemaFile);
            try{
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaFileStream));
                configHolder.getPolicySchemaMap().put(schemaNS, schema);
            } finally {
                IdentityIOStreamUtils.closeInputStream(schemaFileStream);
            }
        }
    }

}
