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

package org.wso2.carbon.identity.mgt.endpoint;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;

import java.rmi.RemoteException;

public class UserInformationRecoveryClient {

    private UserInformationRecoveryServiceStub stub;

    public UserInformationRecoveryClient() throws AxisFault {
        StringBuilder builder = new StringBuilder();
        String serviceURL = null;

        String serviceUrl = IdentityUtil.getServicePath();
        IdentityUtil.getServerURL(serviceUrl, true, true);

        serviceURL = builder.append(IdentityUtil.getServerURL(serviceUrl, true, true)).append
                (Constants.USER_INFORMATION_RECOVERY_SERVICE).toString().replaceAll("(?<!(http:|https:))//", "/");
        stub = new UserInformationRecoveryServiceStub(serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);

    }

    public CaptchaInfoBean generateCaptcha()
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getCaptcha();
    }

    public VerificationBean VerifyUser(String username, CaptchaInfoBean captcha)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyUser(username, captcha);
    }

    public VerificationBean sendRecoveryNotification(String username, String key, String notificationType)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.sendRecoveryNotification(username, key, "EMAIL");
    }

    public VerificationBean verifyConfirmationCode(String username, String code,
                                                   CaptchaInfoBean captcha)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyConfirmationCode(username, code, captcha);
    }

    public VerificationBean resetPassword(String username, String confirmationCode,
                                          String newPassword)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.updatePassword(username, confirmationCode, newPassword);
    }

    public ChallengeQuestionIdsDTO getChallengeQuestionIds(String username,
                                                           String confirmationCode)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getUserChallengeQuestionIds(username, confirmationCode);
    }

    public UserChallengesDTO getChallengeQuestion(String username, String code, String id)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.getUserChallengeQuestion(username, code, id);
    }

    public VerificationBean checkAnswer(String username, String code, String id, String answer)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.verifyUserChallengeAnswer(username, code, id, answer);
    }

    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect)
            throws RemoteException, UserInformationRecoveryServiceIdentityExceptionException {
        return stub.getUserIdentitySupportedClaims(dialect);
    }

    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
                                          String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {
        return stub.verifyAccount(claims, captcha, tenantDomain);
    }

    public VerificationBean registerUser(String userName, String password,
                                         UserIdentityClaimDTO[] claims, String profileName,
                                         String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {
        return stub.registerUser(userName, password, claims, profileName, tenantDomain);
    }

    public VerificationBean confirmUserSelfRegistration
            (String userName, String code, CaptchaInfoBean captcha, String tenantDomain)
            throws RemoteException, UserInformationRecoveryServiceIdentityMgtServiceExceptionException {

        return stub.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
    }
}
