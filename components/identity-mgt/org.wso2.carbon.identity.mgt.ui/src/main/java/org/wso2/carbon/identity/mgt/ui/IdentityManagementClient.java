/*
 *
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;

/**
 *
 */
public class IdentityManagementClient {

    public static final String USER_CHALLENGE_QUESTION = "user.challenge.question";
    protected static Log log = LogFactory.getLog(IdentityManagementClient.class);
    protected UserIdentityManagementServiceStub stub = null;

    public IdentityManagementClient(String url, ConfigurationContext configContext)
            throws java.lang.Exception {
        try {
            stub = new UserIdentityManagementServiceStub(configContext, url + "UserIdentityManagementService");
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public IdentityManagementClient(String cookie, String url, ConfigurationContext configContext)
            throws java.lang.Exception {
        try {
            stub = new UserIdentityManagementServiceStub(configContext, url + "UserIdentityManagementService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public boolean processPasswordRecoveryLink(String userId, String userKey)
            throws AxisFault {
        try {
            return stub.processPasswordRecovery(userId, userKey, "EMAIL");
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userId, String userKey)
            throws AxisFault {
        try {
            return stub.getChallengeQuestionsForUser(userId, userKey);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return new UserChallengesDTO[0];
    }

    public VerificationBean verifyChallengeQuestion(String userId, String userKey, String question,
                                                    String answer) throws AxisFault {
        try {
            UserChallengesDTO dto = new UserChallengesDTO();
            dto.setQuestion(question);
            dto.setAnswer(answer);
            return stub.verifyChallengeQuestion(userId, userKey, new UserChallengesDTO[]{dto});
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public VerificationBean confirmLink(String confirmationKey) throws AxisFault {
        try {
            return stub.confirmUserAccount(confirmationKey);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public boolean updateCredential(String userId, String userKey, String password,
                                    CaptchaInfoBean captchaInfoBean) throws AxisFault {
        try {
            VerificationBean bean = stub.updateCredential(userId, userKey, password, captchaInfoBean);
            return bean.getVerified();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean unlockUserAccount(String userId, String userKey) {

        return false;
    }

    // TODO
    public boolean processAccountRecovery() throws AxisFault {

        return false;
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

}
