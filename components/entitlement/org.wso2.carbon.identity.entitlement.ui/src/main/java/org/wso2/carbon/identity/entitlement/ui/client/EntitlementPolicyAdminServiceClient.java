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
package org.wso2.carbon.identity.entitlement.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder;

import java.util.List;


public class EntitlementPolicyAdminServiceClient {

    private static final Log log = LogFactory.getLog(EntitlementPolicyAdminServiceClient.class);
    private EntitlementPolicyAdminServiceStub stub;

    /**
     * Instantiates EntitlementServiceClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where EntitlementPolicyAdminService is
     *                         running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public EntitlementPolicyAdminServiceClient(String cookie, String backendServerURL,
                                               ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "EntitlementPolicyAdminService";
        stub = new EntitlementPolicyAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * @param policyTypeFilter
     * @param policySearchString
     * @param pageNumber
     * @param isPDPPolicy
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies that reside in the
     * given page.
     * @throws AxisFault
     */
    public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter, String policySearchString,
                                                int pageNumber, boolean isPDPPolicy) throws AxisFault {
        try {
            return stub.getAllPolicies(policyTypeFilter, policySearchString, pageNumber, isPDPPolicy);
        } catch (Exception e) {
            String message = "Error while loading all policies from backend service";
            handleException(e);
        }
        PaginatedPolicySetDTO paginatedPolicySetDTO = new PaginatedPolicySetDTO();
        paginatedPolicySetDTO.setPolicySet(new PolicyDTO[0]);
        return paginatedPolicySetDTO;
    }

    /**
     * Gets policy DTO for given policy id
     *
     * @param policyId    policy id
     * @param isPDPPolicy
     * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getPolicy(String policyId, boolean isPDPPolicy) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getPolicy(policyId, isPDPPolicy);
            if (dto != null && dto.getPolicy() != null) {
                dto.setPolicy(dto.getPolicy().trim().replaceAll("><", ">\n<"));
            }
        } catch (Exception e) {
            handleException(e);
        }
        return dto;
    }

    /**
     * Gets policy DTO for given policy id  with given version
     *
     * @param policyId policy id
     * @param version
     * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getPolicyByVersion(String policyId, String version) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getPolicyByVersion(policyId, version);
            if (dto != null && dto.getPolicy() != null) {
                dto.setPolicy(dto.getPolicy().trim().replaceAll("><", ">\n<"));
            }
        } catch (Exception e) {
            handleException(e);
        }
        return dto;
    }

    /**
     * Gets light weight policy DTO for given policy id
     *
     * @param policyId policy id
     * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getLightPolicy(String policyId) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getLightPolicy(policyId);
        } catch (Exception e) {
            handleException(e);
        }
        return dto;
    }

    /**
     * Rollbacks policy DTO for given policy version
     *
     * @param policyId policy id
     * @param version  policy version
     * @throws AxisFault throws
     */
    public void rollBackPolicy(String policyId, String version) throws AxisFault {

        try {
            stub.rollBackPolicy(policyId, version);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * @param policyIds
     * @throws AxisFault
     */
    public void removePolicies(String[] policyIds, boolean dePromote) throws AxisFault {
        try {
            stub.removePolicies(policyIds, dePromote);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void dePromotePolicy(String policyId) throws AxisFault {
        try {
            stub.dePromotePolicy(policyId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void enableDisablePolicy(String policyId, boolean enable) throws AxisFault {
        try {
            stub.enableDisablePolicy(policyId, enable);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void orderPolicy(String policyId, int order) throws AxisFault {
        try {
            stub.orderPolicy(policyId, order);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param policy
     * @throws AxisFault
     */
    public void updatePolicy(PolicyDTO policy) throws AxisFault {
        try {
            if (policy.getPolicy() != null && policy.getPolicy().trim().length() > 0) {
                policy.setPolicy(policy.getPolicy().trim().replaceAll(">\\s+<", "><"));
            }
            stub.updatePolicy(policy);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param policy
     * @throws AxisFault
     */
    public void addPolicy(PolicyDTO policy) throws AxisFault {
        try {
            policy.setPolicy(policy.getPolicy().trim().replaceAll(">\\s+<", "><"));
            stub.addPolicy(policy);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * adding an entitlement policy which is extracted using file upload executor
     *
     * @param content content of the policy as a <code>String</code> Object
     * @throws AxisFault, throws if fails
     */
    public void uploadPolicy(String content) throws AxisFault {

        PolicyDTO dto = new PolicyDTO();
        dto.setPolicy(content);
        dto.setPolicy(dto.getPolicy().trim().replaceAll(">\\s+<", "><"));
        try {
            stub.addPolicy(dto);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Import XACML policy from registry
     *
     * @param policyRegistryPath registry path
     * @throws AxisFault
     */
    public void importPolicyFromRegistry(String policyRegistryPath) throws AxisFault {

        try {
            stub.importPolicyFromRegistry(policyRegistryPath);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Returns the list of policy set ids available in PDP
     *
     * @return list of policy set ids
     * @throws AxisFault
     */
    public String[] getAllPolicyIds() throws AxisFault {

        try {
            return stub.getAllPolicyIds("*");
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    /**
     * @param requestContext
     * @return
     * @throws FileUploadException
     */
    private List parseRequest(ServletRequestContext requestContext) throws FileUploadException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        return upload.parseRequest(requestContext);
    }

    /**
     * Gets attribute value tree for given attribute type
     *
     * @param dataModule
     * @param category
     * @param regexp
     * @param dataLevel
     * @param limit
     * @return attribute value tree
     * @throws AxisFault throws
     */
    public EntitlementTreeNodeDTO getEntitlementData(String dataModule, String category,
                                                     String regexp, int dataLevel, int limit) throws AxisFault {
        try {
            return stub.getEntitlementData(dataModule, category, regexp, dataLevel, limit);
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    /**
     * @return
     * @throws AxisFault
     */
    public EntitlementFinderDataHolder[] getEntitlementDataModules() throws AxisFault {

        try {
            return stub.getEntitlementDataModules();
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    /**
     * Gets all subscriber ids
     *
     * @param subscriberSearchString subscriberSearchString
     * @return subscriber ids as String array
     * @throws AxisFault throws
     */
    public String[] getSubscriberIds(String subscriberSearchString) throws AxisFault {

        try {
            return stub.getSubscriberIds(subscriberSearchString);
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    /**
     * Gets subscriber data
     *
     * @param id subscriber id
     * @return subscriber data as SubscriberDTO object
     * @throws AxisFault throws
     */
    public PublisherDataHolder getSubscriber(String id) throws AxisFault {

        try {
            return stub.getSubscriber(id);
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    /**
     * Updates or creates subscriber data
     *
     * @param holder subscriber data as ModuleDataHolder object
     * @param update
     * @throws AxisFault throws
     */
    public void updateSubscriber(PublisherDataHolder holder, boolean update) throws AxisFault {

        try {
            if (update) {
                stub.updateSubscriber(holder);
            } else {
                stub.addSubscriber(holder);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Removes publisher data
     *
     * @param id subscriber id
     * @throws AxisFault throws
     */
    public void deleteSubscriber(String id) throws AxisFault {

        try {
            stub.deleteSubscriber(id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Publishes given set of policies to given set of subscribers
     *
     * @param policies     policy ids as String array, if null or empty, all policies are published
     * @param subscriberId subscriber ids as String array, if null or empty, publish to all subscribers
     * @param version
     * @param action
     * @param enabled
     * @param order
     * @throws AxisFault throws
     */
    public void publish(String[] policies, String[] subscriberId, String action, String version,
                        boolean enabled, int order) throws AxisFault {
        try {
            stub.publishPolicies(policies, subscriberId, action, version, enabled, order);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Get all publisher modules properties that is needed to configure
     *
     * @return publisher modules properties as ModuleDataHolder
     * @throws AxisFault throws
     */
    public PublisherDataHolder[] getPublisherModuleData() throws AxisFault {

        try {
            return stub.getPublisherModuleData();
        } catch (Exception e) {
            handleException(e);
        }

        return new PublisherDataHolder[0];
    }

    public String[] getPolicyVersions(String policyId) throws AxisFault {
        try {
            return stub.getPolicyVersions(policyId);
        } catch (Exception e) {
            handleException(e);
        }

        return new String[0];
    }

    public PaginatedStatusHolder getStatusData(String about, String key, String type,
                                               String searchString, int pageNumber) throws AxisFault {
        try {
            return stub.getStatusData(about, key, type, searchString, pageNumber);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param e Exception
     * @throws AxisFault
     */
    private void handleException(Exception e) throws AxisFault {

        String errorMessage = "Unknown";

        if (e instanceof EntitlementPolicyAdminServiceEntitlementException) {
            EntitlementPolicyAdminServiceEntitlementException entitlementException =
                    (EntitlementPolicyAdminServiceEntitlementException) e;
            if (entitlementException.getFaultMessage().getEntitlementException() != null) {
                errorMessage = entitlementException.getFaultMessage().getEntitlementException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }

        throw new AxisFault(errorMessage, e);
    }
}
