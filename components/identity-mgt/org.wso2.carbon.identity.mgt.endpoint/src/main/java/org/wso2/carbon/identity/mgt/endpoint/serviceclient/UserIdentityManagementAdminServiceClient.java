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
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;

import java.rmi.RemoteException;

/**
 * This class invokes the client operations of UserIdentityManagementService.
 */
public class UserIdentityManagementAdminServiceClient {

    private UserIdentityManagementAdminServiceStub stub;

    /**
     * Instantiate UserIdentityManagementAdminServiceClient instance.
     *
     * @throws AxisFault
     */
    public UserIdentityManagementAdminServiceClient() throws AxisFault {
        StringBuilder builder = new StringBuilder();
        String serviceURL = null;

        serviceURL = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL())
                            .append(IdentityManagementEndpointConstants.ServiceEndpoints.USER_IDENTITY_MANAGEMENT_SERVICE)
                            .toString().replaceAll("(?<!(http:|https:))//", "/");

        stub = new UserIdentityManagementAdminServiceStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        IdentityManagementServiceUtil.getInstance().authenticate(client);
    }

    /**
     * Returns all challenge questions configured in the IdP.
     *
     * @return an array of ChallengeQuestionDTO instances
     * @throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException
     * @throws RemoteException
     */
    public ChallengeQuestionDTO[] getAllChallengeQuestions()
            throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException, RemoteException {
        return stub.getAllChallengeQuestions();
    }

    /**
     * Sets challenge questions in the IdP.
     *
     * @param challengeQuestionDTOs an array of ChallengeQuestionDTO instances
     * @throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException
     * @throws RemoteException
     */
    public void setChallengeQuestions(ChallengeQuestionDTO[] challengeQuestionDTOs)
            throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException, RemoteException {
        stub.setChallengeQuestions(challengeQuestionDTOs);
    }

    /**
     * Sets a chosen set of challenge questions and their answers for the user.
     *
     * @param userName username of the user
     * @param userChallengesDTOs an array of UserChallengesDTO instances
     * @throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException
     * @throws RemoteException
     */
    public void setChallengeQuestionsOfUser(String userName, UserChallengesDTO[] userChallengesDTOs)
            throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException, RemoteException {
        stub.setChallengeQuestionsOfUser(userName, userChallengesDTOs);
    }

}
