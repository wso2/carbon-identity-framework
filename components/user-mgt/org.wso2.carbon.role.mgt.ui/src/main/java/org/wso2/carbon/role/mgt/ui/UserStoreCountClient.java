/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.role.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.count.stub.UserStoreCountServiceStub;
import org.wso2.carbon.identity.user.store.count.stub.UserStoreCountServiceUserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.stub.dto.PairDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UserStoreCountClient {

    protected UserStoreCountServiceStub stub = null;

    protected static final Log log = LogFactory.getLog(UserStoreCountClient.class);

    public UserStoreCountClient(String cookie, String url, String serviceName,
                                ConfigurationContext configContext) throws Exception {
        try {
            stub = new UserStoreCountServiceStub(configContext, url + serviceName);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);


            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public UserStoreCountClient(String cookie, String url, ConfigurationContext configContext)
            throws Exception {
        try {

            stub = new UserStoreCountServiceStub(configContext, url + "UserStoreCountService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Map<String, String> countUsers(String filter) throws AxisFault {
        Map<String, String> counts = new HashMap<>();
        try {
            counts = convertArrayToMap(stub.countUsers(filter));
        } catch (Exception e) {
            handleException(e);
        }
        return counts;
    }

    public Map<String, String> countRoles(String filter) throws AxisFault {
        Map<String, String> counts = new HashMap<>();
        try {
            counts = convertArrayToMap(stub.countRoles(filter));
        } catch (Exception e) {
            handleException(e);
        }
        return counts;
    }

    public Map<String, String> countByClaim(String claimURI, String value) throws AxisFault {
        Map<String, String> counts = new HashMap<>();
        try {
            counts = convertArrayToMap(stub.countClaim(claimURI, value));
        } catch (Exception e) {
            handleException(e);
        }
        return counts;
    }

    public Map<String, String> countByClaims(Map<String, String> claims) throws AxisFault {
        Map<String, String> counts = new HashMap<>();
        try {
            counts = convertArrayToMap(stub.countClaims(convertMapToArray(claims)));
        } catch (Exception e) {
            handleException(e);
        }
        return counts;
    }

    public long countUsersInDomain(String filter, String domain) throws AxisFault {
        try {
            return stub.countUsersInDomain(filter, domain);
        } catch (Exception e) {
            handleException(e);
        }

        return -1;
    }

    public long countRolesInDomain(String filter, String domain) throws AxisFault {
        try {
            return stub.countRolesInDomain(filter, domain);
        } catch (Exception e) {
            handleException(e);
        }
        return -1;
    }

    public long countByClaimInDomain(String claimURI, String filter, String domain) throws AxisFault {
        try {
            return stub.countByClaimInDomain(claimURI, filter, domain);
        } catch (Exception e) {
            handleException(e);
        }
        return -1;
    }

    public long countByClaimsInDomain(PairDTO[] pairDTOs, String domain) throws AxisFault {
        try {
            return stub.countByClaimsInDomain(pairDTOs, domain);
        } catch (Exception e) {
            handleException(e);
        }
        return -1;
    }

    public Set<String> getCountableUserStores() throws AxisFault{
        try {
            if(stub.getCountEnabledUserStores() != null) {
                return new HashSet<String>(Arrays.asList(stub.getCountEnabledUserStores()));
            }
        } catch (Exception e) {
            handleException(e);
        }
        return new HashSet<>();
    }


    protected String[] handleException(Exception e) throws AxisFault {

        String errorMessage = "Unknown";

        if (e instanceof UserStoreCountServiceUserStoreCounterException) {
            UserStoreCountServiceUserStoreCounterException countException = (UserStoreCountServiceUserStoreCounterException) e;
            if (countException.getFaultMessage().getUserStoreCounterException() != null) {
                errorMessage = countException.getFaultMessage().getUserStoreCounterException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }

        log.error(errorMessage, e);
        throw new AxisFault(errorMessage, e);

    }

    /**
     * Converts a given array of PairDTOs to a Map
     *
     * @param pairDTOs
     * @return
     */
    private Map<String, String> convertArrayToMap(PairDTO[] pairDTOs) {
        Map<String, String> map = new HashMap<>();
        if (pairDTOs != null) {
            for (PairDTO pairDTO : pairDTOs) {
                map.put(pairDTO.getKey(), pairDTO.getValue());
            }
        }
        return map;
    }

    /**
     * Converts a given Map to an array of PairDTOs
     *
     * @param claims
     * @return
     */
    private PairDTO[] convertMapToArray(Map<String, String> claims) {
        PairDTO[] pairs = new PairDTO[claims.size()];
        Iterator iterator = claims.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            PairDTO pair = new PairDTO();
            pair.setKey((String) entry.getKey());
            pair.setValue((String) entry.getValue());
            pairs[i] = pair;
            i++;
        }

        return pairs;
    }

}

