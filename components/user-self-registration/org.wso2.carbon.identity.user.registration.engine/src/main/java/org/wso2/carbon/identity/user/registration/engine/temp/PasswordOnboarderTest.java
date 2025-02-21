/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.temp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.registration.engine.executor.action.AttributeCollection;
import org.wso2.carbon.identity.user.registration.engine.executor.action.Authentication;
import org.wso2.carbon.identity.user.registration.engine.executor.action.CredentialEnrollment;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.util.Constants;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_CRED_REQUIRED;

public class PasswordOnboarderTest implements Authentication, AttributeCollection, CredentialEnrollment {

    private static final String USERNAME = "http://wso2.org/claims/username";

    public String getName() {

        return Constants.PWD_EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse authenticate(RegistrationContext context) {

        return null;
    }

    @Override
    public ExecutorResponse collect(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
//        if (STATUS_ATTR_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(USERNAME)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                Map<String, Object> userClaims = new HashMap<>();
                userClaims.put(USERNAME, userInputs.get(USERNAME));
                response.setUpdatedUserClaims(userClaims);
                return response;
            } else {
//        }
//        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getUsernameData());
            return response;
        }
//        response.setResult("ERROR");
//        return response;
    }

    @Override
    public ExecutorResponse enrollCredential(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
//        if (STATUS_CRED_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(PASSWORD)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                Map<String, String> credentials = new HashMap<>();
                credentials.put(PASSWORD, userInputs.get(PASSWORD));
                response.setUserCredentials(credentials);
                return response;
            } else {
//        }
//        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_CRED_REQUIRED);
            response.setRequiredData(getPasswordData());
            return response;
        }
//        response.setResult("ERROR");
//        return response;
    }

    @Override
    public List<String> getInitiationData() {

        List<String> response = new ArrayList<>();
        response.add(USERNAME);
        response.add(PASSWORD);
        return response;
    }

    private List<String> getUsernameData() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<String> inputMetaData = new ArrayList<>();
        inputMetaData.add(USERNAME);
        return inputMetaData;
    }

    private List<String> getPasswordData() {

        List<String> inputMetaData = new ArrayList<>();
        inputMetaData.add(PASSWORD);
        return inputMetaData;
    }
}