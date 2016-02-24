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

package org.wso2.carbon.identity.entitlement.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder;

/**
 *
 */
public class EntitlementAdminServiceClient {

    private static final Log log = LogFactory.getLog(EntitlementAdminServiceClient.class);
    private EntitlementAdminServiceStub stub;

    /**
     * Instantiates EntitlementServiceClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where EntitlementPolicyAdminService is
     *                         running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public EntitlementAdminServiceClient(String cookie, String backendServerURL,
                                         ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "EntitlementAdminService";
        stub = new EntitlementAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Clears the decision cache maintained by the PDP.
     *
     * @throws AxisFault
     */
    public void clearDecisionCache() throws AxisFault {

        try {
            stub.clearDecisionCache();
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
    }

    /**
     * Clears the attribute cache maintained by the PDP.
     *
     * @throws AxisFault
     */
    public void clearAttributeCache() throws AxisFault {

        try {
            stub.clearAllAttributeCaches();
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
    }


    /**
     * Evaluate XACML request with PDP
     *
     * @param request XACML request as String
     * @return XACML response as String
     * @throws AxisFault if fails
     */
    public String getDecision(String request) throws AxisFault {
        try {
            return stub.doTestRequest(request);
        } catch (Exception e) {
            handleException("Error occurred while test policy evaluation", e);
        }
        return null;
    }

    /**
     * Evaluate XACML request with PDP
     *
     * @param policies
     * @param request  XACML request as String
     * @return XACML response as String
     * @throws AxisFault if fails
     */
    public String getDecision(String request, String[] policies) throws AxisFault {
        try {
            return stub.doTestRequestForGivenPolicies(request, policies);
        } catch (Exception e) {
            handleException("Error occurred while test policy evaluation", e);
        }
        return null;
    }

    public PDPDataHolder getPDPData() throws AxisFault {

        try {
            return stub.getPDPData();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }


    public PolicyFinderDataHolder getPolicyFinderData(String finderName) throws AxisFault {

        try {
            return stub.getPolicyFinderData(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public PIPFinderDataHolder getPIPAttributeFinderData(String finderName) throws AxisFault {

        try {
            return stub.getPIPAttributeFinderData(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public PIPFinderDataHolder getPIPResourceFinderData(String finderName) throws AxisFault {

        try {
            return stub.getPIPResourceFinderData(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public void refreshAttributeFinder(String finderName) throws AxisFault {

        try {
            stub.refreshAttributeFinder(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void refreshResourceFinder(String finderName) throws AxisFault {

        try {
            stub.refreshResourceFinder(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void refreshPolicyFinder(String finderName) throws AxisFault {

        try {
            stub.refreshPolicyFinders(finderName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * Get  globally defined policy combining algorithm
     *
     * @return policy combining algorithm as a String
     * @throws AxisFault
     */
    public String getGlobalPolicyAlgorithm() throws AxisFault {
        try {
            return stub.getGlobalPolicyAlgorithm();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Set policy combining algorithm globally
     *
     * @param policyAlgorithm policy combining algorithm as a String
     * @throws AxisFault
     */
    public void setGlobalPolicyAlgorithm(String policyAlgorithm) throws AxisFault {
        try {
            stub.setGlobalPolicyAlgorithm(policyAlgorithm);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param msg Error message
     * @param e   Exception
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
