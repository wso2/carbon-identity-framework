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

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.wso2.carbon.identity.mgt.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserPassword;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAllAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.PasswordRecoverySecurityQuestion;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST client for password reset with security question
 */
public class PasswordRecoverySecurityQuestionClient {

    StringBuilder builder = new StringBuilder();
    String url = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
            .replace(IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL_DOMAIN,
                    IdentityManagementEndpointConstants.UserInfoRecovery.REST_API_URL_DOMAIN);

    public Response initiateUserChallengeQuestion(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestion(user);
        return response;
    }

    public Response verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest, Map<String, String> headers) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = create(url,
                PasswordRecoverySecurityQuestion.class,
                IdentityManagementServiceUtil.getInstance().getJSONProvider(), null, headers);
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswer(verifyAnswerRequest);
        return response;
    }

    public Response updatePassword(UserPassword userPassword) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.updatePassword(userPassword);
        return response;
    }

    public Response initiateUserChallengeQuestionAtOnce(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestionAtOnce(user);
        return response;
    }

    public Response verifyUserChallengeAnswerAtOnce(VerifyAllAnswerRequest verifyAllAnswerRequest) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswerAtOnce(verifyAllAnswerRequest);
        return response;
    }

    public static <T> T create(String baseAddress, Class<T> cls, List<?> providers, String configLocation, Map<String, String> headers) {
        JAXRSClientFactoryBean bean = getBean(baseAddress, cls, configLocation, headers);
        bean.setProviders(providers);
        return bean.create(cls, new Object[0]);
    }

    private static JAXRSClientFactoryBean getBean(String baseAddress, Class<?> cls, String configLocation, Map<String, String> headers) {
        JAXRSClientFactoryBean bean = getBean(baseAddress, configLocation, headers);
        bean.setServiceClass(cls);
        return bean;
    }

    static JAXRSClientFactoryBean getBean(String baseAddress, String configLocation, Map<String, String> headers) {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        if (configLocation != null) {
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus(configLocation);
            bean.setBus(bus);
        }
        bean.setAddress(baseAddress);
        if (headers != null && !headers.isEmpty()) {
            bean.setHeaders(headers);
        }
        return bean;
    }

}
