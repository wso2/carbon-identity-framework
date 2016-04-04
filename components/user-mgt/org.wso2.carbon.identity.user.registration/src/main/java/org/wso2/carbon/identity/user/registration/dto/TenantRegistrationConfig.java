/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.identity.user.registration.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Read the sign-up configuration in gov: registry at /repository/identity/sign-up-config
 * and populate this object.
 */

public class TenantRegistrationConfig {
    private String signUpDomain;
    private Map roles = new HashMap<>(); // role name - external (true/false) mapping

    public String getSignUpDomain() {
        return signUpDomain;
    }

    public void setSignUpDomain(String signUpDomain) {
        this.signUpDomain = signUpDomain;
    }

    public Map<String, Boolean> getRoles() {
        return roles;
    }

    public void setRoles(Map roles) {
        this.roles = roles;
    }

}