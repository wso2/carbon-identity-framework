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

package org.wso2.carbon.idp.mgt.util;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;

import java.util.HashMap;
import java.util.Map;

public class ActionMgtTestUtil {
    
    public static final String ASSOCIATED_ACTION_ID = "Dummy_Action_ID";

    public static Action createAction(EndpointConfig endpointConfig) {

        Action.ActionResponseBuilder actionResponseBuilder = new Action.ActionResponseBuilder();
        actionResponseBuilder.id(ASSOCIATED_ACTION_ID);
        actionResponseBuilder.name("SampleAssociatedAction");
        actionResponseBuilder.type(Action.ActionTypes.AUTHENTICATION);
        actionResponseBuilder.description("SampleDescription");
        actionResponseBuilder.status(Action.Status.ACTIVE);
        actionResponseBuilder.endpoint(endpointConfig);
        return actionResponseBuilder.build();
    }

    public static EndpointConfig createEndpointConfig(String uri, String username, String password) {

        EndpointConfig.EndpointConfigBuilder endpointConfigBuilder = new EndpointConfig.EndpointConfigBuilder();
        endpointConfigBuilder.uri(uri);
        endpointConfigBuilder.authentication(
                new Authentication.BasicAuthBuilder(username, password).build());
        return endpointConfigBuilder.build();
    }

    public static IdentityProvider createIdPWithUserDefinedFederatedAuthenticatorConfig(String idpName,
                                                                                  EndpointConfig endpointConfig) {

        // Initialize Test Identity Provider 4 with custom user defined federated authenticator.
        IdentityProvider newUserDefinedIdp = new IdentityProvider();
        newUserDefinedIdp.setIdentityProviderName(idpName);

        UserDefinedFederatedAuthenticatorConfig userDefinedFederatedAuthenticatorConfig = new
                UserDefinedFederatedAuthenticatorConfig();
        userDefinedFederatedAuthenticatorConfig.setDisplayName("DisplayName1");
        userDefinedFederatedAuthenticatorConfig.setName("custom-FedAuthenticator");
        userDefinedFederatedAuthenticatorConfig.setEnabled(true);
        userDefinedFederatedAuthenticatorConfig.setEndpointConfig(
                buildUserDefinedAuthenticatorEndpointConfig(endpointConfig));
        Property property = new Property();
        property.setName("actionId");
        property.setValue(ASSOCIATED_ACTION_ID);
        property.setConfidential(false);
        userDefinedFederatedAuthenticatorConfig.setProperties(new Property[]{property});
        newUserDefinedIdp.setFederatedAuthenticatorConfigs(
                new FederatedAuthenticatorConfig[]{userDefinedFederatedAuthenticatorConfig});
        newUserDefinedIdp.setDefaultAuthenticatorConfig(userDefinedFederatedAuthenticatorConfig);
        return newUserDefinedIdp;
    }

    public static UserDefinedAuthenticatorEndpointConfig buildUserDefinedAuthenticatorEndpointConfig(
            EndpointConfig endpointConfig) {

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri(endpointConfig.getUri());
        endpointConfigBuilder.authenticationType(endpointConfig.getAuthentication().getType().getName());
        Map<String, String> propMap = new HashMap<>();
        endpointConfig.getAuthentication().getProperties()
                .forEach(prop -> propMap.put(prop.getName(), prop.getValue()));
        endpointConfigBuilder.authenticationProperties(propMap);
        return endpointConfigBuilder.build();
    }
}
