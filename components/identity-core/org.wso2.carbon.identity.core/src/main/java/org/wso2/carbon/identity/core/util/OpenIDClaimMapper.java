/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.identity.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class manages mapping of OpenID Simple Registration[SReg] to two schemas
 * used in OpenID Attribute Exchanged [AxSchema & OpenID schema]
 */
public class OpenIDClaimMapper {

    public final static String LN_CLAIM_MAPPER = "ClaimMapper";
    public final static String LN_CLAIM = "Claim";
    public final static String LN_SREG = "SReg";
    public final static String LN_AXSCHEMA = "AxSchema";
    public final static String LN_OPENID_SCHEMA = "OpenIDSchema";

    // If you want to add a new mapping - need to edit this file
    private final static String OPENID_CLAIM_MAPPER_FILE_PATH = "/openid-claim-mapper.xml";

    private static HashMap<String, String> axMapping = new HashMap<String, String>();
    private static HashMap<String, String> sregMapping = new HashMap<String, String>();

    private static OpenIDClaimMapper instance;

    /**
     * Private constructor, makes the class singleton
     *
     * @throws Exception TODO: Define a custom exception
     */
    private OpenIDClaimMapper() throws Exception {
        process(CarbonUtils.getCarbonConfigDirPath() + OPENID_CLAIM_MAPPER_FILE_PATH);
    }

    /**
     * Creates and maintains a singleton instance of OpenIDClaimMapper
     *
     * @return An instance of OpenIDClaimMapper
     * @throws Exception
     */
    public static OpenIDClaimMapper getInstance() throws Exception {
        if (instance == null) {
            instance = new OpenIDClaimMapper();
        }
        return instance;
    }

    public HashMap<String, String> getAxMapping() {
        return axMapping;
    }

    public HashMap<String, String> getSregMapping() {
        return sregMapping;
    }

    /**
     * Process the OpenID mapping file
     *
     * @param filePath Path to the OpenID attribute mapping file
     * @throws Exception
     */
    private void process(String filePath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(filePath);
        OMElement elem = builder.getDocumentElement();
        process(elem);
    }

    /**
     * Populate attribute mapping HashMaps
     *
     * @param initialClaims Root element
     * @throws Exception
     */
    private void process(OMElement initialClaims) throws Exception {

        Iterator claims = initialClaims.getChildrenWithName(new QName(LN_CLAIM));

        OMElement claimElement = null;
        OMElement sreg = null;
        OMElement axSchema = null;
        OMElement openidSchema = null;

        while (claims.hasNext()) {
            claimElement = (OMElement) claims.next();
            sreg = claimElement.getFirstChildWithName(new QName(LN_SREG));
            axSchema = claimElement.getFirstChildWithName(new QName(LN_AXSCHEMA));
            openidSchema = claimElement.getFirstChildWithName(new QName(LN_OPENID_SCHEMA));

            if (openidSchema != null && axSchema != null) {
                if (axMapping.containsKey(openidSchema.getText())) {
                    throw new Exception("Found duplicate key entries in openID claim mapper");
                }
                axMapping.put(openidSchema.getText(), axSchema.getText());
            }

            if (sreg != null && openidSchema != null) {
                if (sregMapping.containsKey(openidSchema.getText())) {
                    throw new Exception("Found duplicate key entries in openID claim mapper");
                }
                sregMapping.put(openidSchema.getText(), sreg.getText());
            }

            if (sreg != null && axSchema != null) {
                if (sregMapping.containsKey(axSchema.getText())) {
                    throw new Exception("Found duplicate key entries in openID claim mapper");
                }
                sregMapping.put(axSchema.getText(), sreg.getText());
            }
        }
    }
}