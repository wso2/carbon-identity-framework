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
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesCollectionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;

import java.rmi.RemoteException;

/**
 * This class invokes the client operations of UserInformationRecoveryService.
 */
public class UserInformationRecoveryClient {

    private UserInformationRecoveryServiceStub stub;

    /**
     * Initiates UserInformationRecoveryClient instance
     *
     * @throws AxisFault
     */
    public UserInformationRecoveryClient() throws AxisFault {
        StringBuilder builder = new StringBuilder();
        String serviceURL = null;

        serviceURL = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL()).append
                (IdentityManagementEndpointConstants.ServiceEndpoints.USER_INFORMATION_RECOVERY_SERVICE).toString()
                            .replaceAll("(?<!(http:|https:))//", "/");

        stub = new UserInformationRecoveryServiceStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        IdentityManagementServiceUtil.getInstance().authenticate(client);
    }

    /**
     * Generates a captcha.
     *
     * @return an instance of CaptchaInfoBean which includes captcha information
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public CaptchaInfoBean generateCaptcha()
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getCaptcha();
    }

    /**
     * Verifies the captcha answer.
     *
     * @param username username of the user
     * @param captcha  an instance of CaptchaInfoBean
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean verifyUser(String username, CaptchaInfoBean captcha)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyUser(username, captcha);
    }

    /**
     * Sends the password recovery notification.
     *
     * @param username         username of the user
     * @param key              confirmation code
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean sendRecoveryNotification(String username, String key)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.sendRecoveryNotification(username, key,
                                             IdentityManagementEndpointConstants.PasswordRecoveryOptions.EMAIL);
    }

    /**
     * Verifies the password recovery notification confirmation.
     *
     * @param username username of the user
     * @param code     confirmation code
     * @param captcha an instance of CaptchaInfoBean
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean verifyConfirmationCode(String username, String code,
                                                   CaptchaInfoBean captcha)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyConfirmationCode(username, code, captcha);
    }

    /**
     * Resets the password.
     *
     * @param username username of the user
     * @param confirmationCode confirmation code
     * @param newPassword new password
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean resetPassword(String username, String confirmationCode,
                                          String newPassword)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.updatePassword(username, confirmationCode, newPassword);
    }

    /**
     * Returns the question ids of the challenge questions configured by the user.
     *
     * @param username username of the user
     * @param confirmationCode confirmation code
     * @return an instance of ChallengeQuestionIdsDTO which holds the status and question ids
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public ChallengeQuestionIdsDTO getUserChallengeQuestionIds(String username,
                                                               String confirmationCode)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getUserChallengeQuestionIds(username, confirmationCode);
    }

    /**
     * Returns the question corresponded with the provided question id.
     *
     * @param username username of the user
     * @param code confirmation code
     * @param id question id
     * @return and instance of UserChallengesDTO which holds the question
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public UserChallengesDTO getChallengeQuestion(String username, String code, String id)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getUserChallengeQuestion(username, code, id);
    }

    /**
     * Returns the challenge questions configured for the user.
     *
     * @param username username of the user
     * @param confirmationCode confirmation code
     * @return an instance of UserChallengesCollectionDTO which holds user challenge questions
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public UserChallengesCollectionDTO getChallengeQuestions(String username, String confirmationCode)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getUserChallengeQuestions(username, confirmationCode);
    }

    /**
     * Verifies the provided answer for the respective question.
     *
     * @param username username of the user
     * @param code confirmation code
     * @param id question id
     * @param answer user answer
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean verifyUserChallengeAnswer(String username, String code, String id, String answer)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyUserChallengeAnswer(username, code, id, answer);
    }

    /**
     * Verifies user answers for the user challenge question set.
     *
     * @param username username of the user
     * @param confirmationCode confirmation code
     * @param userChallengesDTOs an array of UserChallengesDTO instances which holds the respective question and answer
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean verifyUserChallengeAnswers(String username, String confirmationCode,
                                                      UserChallengesDTO[] userChallengesDTOs)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyUserChallengeAnswers(username, confirmationCode, userChallengesDTOs);
    }

    /**
     * Returns the claims supported.
     *
     * @param dialect claim dialect
     * @return an array of UserIdentityClaimDTO instances
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityExceptionException
     */
    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect)
            throws RemoteException, UserInformationRecoveryServiceIdentityExceptionException {
        return stub.getUserIdentitySupportedClaims(dialect);
    }

    /**
     * Verifies the captcha answer and recovers the username via the provided claims
     *
     * @param claims claims of the user
     * @param captcha an instance of CaptchaInfoBean
     * @param tenantDomain tenant domain
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
                                          String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {
        return stub.verifyAccount(claims, captcha, tenantDomain);
    }

    /**
     * Registers the user.
     *
     * @param userName username
     * @param password password
     * @param claims claims of the user
     * @param profileName profile name
     * @param tenantDomain tenant domain
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean registerUser(String userName, String password,
                                         UserIdentityClaimDTO[] claims, String profileName,
                                         String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {
        return stub.registerUser(userName, password, claims, profileName, tenantDomain);
    }

    /**
     * Confirms self registration notification.
     *
     * @param userName username of the user
     * @param code confirmation code
     * @param captcha an instance of CaptchaInfoBean
     * @param tenantDomain tenant domain
     * @return an instance of VerificationBean which denotes the status
     * @throws RemoteException
     * @throws UserInformationRecoveryServiceIdentityMgtServiceExceptionException
     */
    public VerificationBean confirmUserSelfRegistration
            (String userName, String code, CaptchaInfoBean captcha, String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
    }
}
