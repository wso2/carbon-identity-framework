/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.search.PolicySearch;
import org.wso2.carbon.identity.entitlement.wsxacml.XACMLHandler;

/**
 * Entitlement Service class which exposes the PDP
 */
public class EntitlementService implements XACMLHandler {

    private static Log log = LogFactory.getLog(EntitlementService.class);

    /**
     * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
     * hand back to the PEP. PEP needs construct the XACML request before sending it to the
     * EntitlementEngine
     *
     * @param request XACML request as a String Object
     * @return XACML response as a String Object
     * @throws EntitlementException throws
     */
    public String getDecision(String request) throws EntitlementException {

        String response;
        try {
            EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
            response = entitlementEngine.evaluate(request);
            return response;
        } catch (Exception e) {
            log.error("Error occurred while evaluating XACML request", e);
            throw new EntitlementException("Error occurred while evaluating XACML request");
        }
    }

    /**
     * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
     * hand back to the PEP. Here PEP does not need construct the XACML request before sending it to the
     * EntitlementEngine. Just can send the single attribute value. But here default attribute ids and data types
     * are used
     *
     * @param subject     subject
     * @param resource    resource
     * @param action      action
     * @param environment environment
     * @return XACML response as a String Object
     * @throws EntitlementException throws
     */
    public String getDecisionByAttributes(String subject, String resource, String action,
                                          String[] environment) throws EntitlementException {
        try {
            EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
            return entitlementEngine.evaluate(subject, resource, action, environment);
        } catch (Exception e) {
            log.error("Error occurred while evaluating XACML request", e);
            throw new EntitlementException("Error occurred while evaluating XACML request");
        }
    }


    /**
     * Evaluates the given XACML request and returns the Response as boolean value.
     * Here PEP does not need construct the XACML request before sending it to the
     * EntitlementEngine. Just can send the single attribute value. But here default
     * attribute ids and data types are used.
     * if result permit, return true else false  such as Deny based PEP
     *
     * @param subject  subject
     * @param resource resource
     * @param action   action
     * @return XACML response as boolean true or false
     * @throws Exception throws
     */
    public boolean getBooleanDecision(String subject, String resource, String action) throws Exception {
        try {
            EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
            String response = entitlementEngine.evaluate(subject, resource, action, null);
            if (response.contains("Permit")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error occurred while evaluating XACML request", e);
            throw new Exception("Error occurred while evaluating XACML request");
        }
    }

    /**
     * Gets entitled resources for given user or role
     * This method can be only used, if all policies in PDP are defined with default categories i.e
     * subject, resource and action and default attribute Ids and #string data type.
     *
     * @param subjectName       subject Name, User or Role name
     * @param subjectId         attribute id of the subject, user or role
     * @param resourceName      resource Name
     * @param action            action name
     * @param enableChildSearch whether search is done for the child resources under the given  resource name
     * @return entitled resources as String array
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws if invalid data is provided
     */
    public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                                      String subjectId, String action, boolean enableChildSearch)
            throws EntitlementException {

        if (subjectName == null) {
            throw new EntitlementException(
                    "Invalid input data - either the user name or role name should be non-null");
        }

        PolicySearch policySearch = EntitlementEngine.getInstance().getPolicySearch();
        return policySearch.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                enableChildSearch);
    }

    /**
     * Gets all entitled attributes for given set of attributes
     * this an universal method to do policy search and find entitlement attributes
     *
     * @param identifier      identifier to separate out the attributes that is used for search
     *                        this is not required and can be null
     * @param givenAttributes user provided attributes
     * @return all the attributes that is entitled
     * @throws EntitlementException if fails
     */
    public EntitledResultSetDTO getAllEntitlements(String identifier, AttributeDTO[] givenAttributes)
            throws EntitlementException {
        PolicySearch policySearch = EntitlementEngine.getInstance().getPolicySearch();
        return policySearch.getEntitledAttributes(identifier, givenAttributes);
    }

    /**
     * Evaluates the given XACML request for given SAML based authorization query
     *
     * @param request XACML request as a String Object
     * @return XACML response as a String Object
     * @throws Exception throws if fails
     */
    public String XACMLAuthzDecisionQuery(String request) throws Exception {
        return getDecision(request);
    }

}
