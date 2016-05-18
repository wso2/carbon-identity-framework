/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.endpoint.serviceclient;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

/**
 * This class invokes the client operations of UserRegistrationAdminClient.
 */
public class UserRegistrationAdminServiceClient {

    private UserRegistrationAdminServiceStub stub;
    private Properties prop;

    public UserRegistrationAdminServiceClient() throws AxisFault {

        StringBuilder builder = new StringBuilder();
        String serviceURL = null;

        serviceURL = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL())
                            .append(IdentityManagementEndpointConstants.ServiceEndpoints.USER_REGISTRATION_SERVICE)
                            .toString().replaceAll("(?<!(http:|https:))//", "/");

        stub = new UserRegistrationAdminServiceStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
    }

    /**
     * Add new user.
     *
     * @param username   Username of the user.
     * @param password   Password of the user.
     * @param userFields User fields to be updated.
     * @throws java.rmi.RemoteException
     * @throws UserRegistrationAdminServiceException
     */
    public void addUser(String username, char[] password, List<UserFieldDTO> userFields) throws RemoteException,
                                                                                                UserRegistrationAdminServiceException {

        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(username);
        userDTO.setPassword(new String(password));

        userDTO.setUserFields(userFields.toArray(new UserFieldDTO[userFields.size()]));
        stub.addUser(userDTO);
    }

    /**
     * Get the user fields for given dialect.
     *
     * @param dialect Dialect to use.
     * @return User fields.
     * @throws UserRegistrationAdminServiceIdentityException
     * @throws java.rmi.RemoteException
     */
    public UserFieldDTO[] readUserFieldsForUserRegistration(String dialect)
            throws UserRegistrationAdminServiceIdentityException, RemoteException {

        return stub.readUserFieldsForUserRegistration(dialect);
    }

    /**
     * Check whether the user exists.
     *
     * @param username Username of the user.
     * @return True if user exists.
     * @throws java.rmi.RemoteException
     * @throws UserRegistrationAdminServiceException
     */
    public boolean isUserExist(String username) throws RemoteException,
                                                       UserRegistrationAdminServiceUserRegistrationException {

        return stub.isUserExist(username);
    }

}
