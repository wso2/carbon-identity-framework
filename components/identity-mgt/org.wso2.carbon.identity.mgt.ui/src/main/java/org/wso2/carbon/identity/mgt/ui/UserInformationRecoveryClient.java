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
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;

public class UserInformationRecoveryClient {

    protected static Log log = LogFactory.getLog(UserInformationRecoveryClient.class);
    protected UserInformationRecoveryServiceStub stub;

    public UserInformationRecoveryClient(String url, ConfigurationContext configContext) throws AxisFault {
        try {
            stub = new UserInformationRecoveryServiceStub(configContext, url + "UserInformationRecoveryService");
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public UserInformationRecoveryClient(String cookie, String url, ConfigurationContext configContext) throws AxisFault {
        try {
            stub = new UserInformationRecoveryServiceStub(configContext, url + "UserInformationRecoveryService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public CaptchaInfoBean generateRandomCaptcha() throws AxisFault {

        try {
            return stub.getCaptcha();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public VerificationBean verifyUser(String username, CaptchaInfoBean captcha) throws AxisFault {

        try {
            return stub.verifyUser(username, captcha);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public ChallengeQuestionDTO[] getChallengeQuestions() throws AxisFault {

        try {
            return stub.getAllChallengeQuestions();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return new ChallengeQuestionDTO[0];
    }
}
