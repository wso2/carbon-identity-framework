
/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.multiplecredentials;

import org.wso2.carbon.user.core.multiplecredentials.Credential;
import org.wso2.carbon.user.mgt.common.ClaimValue;
import org.wso2.carbon.user.mgt.common.MultipleCredentialsUserAdminException;

public class MultipleCredentialsUserAdmin {


    public void addUserWithUserId(String userId, Credential credential, String[] roleList, ClaimValue[] claims,
                                  String profileName) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.addUser(userId, credential, roleList, claims, profileName);
    }


    public String getUserId(Credential credential) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.getUserId(credential);
    }

    /**
     * @param credential
     * @param roleList
     * @param claims
     * @param profileName
     * @throws MultipleCredentialsUserAdminException
     */
    public void addUser(Credential credential, String[] roleList, ClaimValue[] claims,
                        String profileName) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.addUser(credential, roleList, claims, profileName);
    }

    /**
     * @param credential
     * @param roleList
     * @param claims
     * @param profileName
     * @throws MultipleCredentialsUserAdminException
     */
    public void addUsers(Credential[] credential, String[] roleList, ClaimValue[] claims,
                         String profileName) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.addUsers(credential, roleList, claims, profileName);
    }

    /**
     * @param identifier
     * @param credentialType
     * @throws MultipleCredentialsUserAdminException
     */
    public void deleteUser(String identifier, String credentialType)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.deleteUser(identifier, credentialType);
    }

    /**
     * @param anIdentifier
     * @param credentialType
     * @param credential
     * @throws MultipleCredentialsUserAdminException
     */
    public void addCredential(String anIdentifier, String credentialType, Credential credential)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.addCredential(anIdentifier, credentialType, credential);
    }

    /**
     * @param identifier
     * @param credentialType
     * @param credential
     * @throws MultipleCredentialsUserAdminException
     */
    public void updateCredential(String identifier, String credentialType, Credential credential)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.updateCredential(identifier, credentialType, credential);
    }


    /**
     * @param identifier
     * @param credentialType
     * @throws MultipleCredentialsUserAdminException
     */
    public void deleteCredential(String identifier, String credentialType)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.deleteCredential(identifier, credentialType);
    }

    /**
     * @param anIdentifier
     * @param credentialType
     * @return
     * @throws MultipleCredentialsUserAdminException
     */
    public Credential[] getCredentials(String anIdentifier, String credentialType)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.getCredentials(anIdentifier, credentialType);
    }

    /**
     * @param credential
     * @return
     * @throws MultipleCredentialsUserAdminException
     */
    public boolean authenticate(Credential credential) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.authenticate(credential);
    }


    public void setUserClaimValue(String identifer, String credentialType, String claimURI, String claimValue,
                                  String profileName) throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.setUserClaimValue(identifer, credentialType, claimURI, claimValue, profileName);
    }


    public void setUserClaimValues(String identifer, String credentialType, ClaimValue[] claims, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.setUserClaimValues(identifer, credentialType, claims, profileName);
    }


    public void deleteUserClaimValue(String identifer, String credentialType, String claimURI, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.deleteUserClaimValue(identifer, credentialType, claimURI, profileName);
    }


    public void deleteUserClaimValues(String identifer, String credentialType, String[] claims, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        multipleCredentialsUserProxy.deleteUserClaimValues(identifer, credentialType, claims, profileName);
    }


    public String getUserClaimValue(String identifer, String credentialType, String claimUri, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.getUserClaimValue(identifer, credentialType,
                claimUri, profileName);

    }


    public ClaimValue[] getUserClaimValues(String identifer, String credentialType, String[] claims, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.getUserClaimValues(identifer, credentialType,
                claims, profileName);

    }


    public ClaimValue[] getAllUserClaimValues(String identifer, String credentialType, String profileName)
            throws MultipleCredentialsUserAdminException {
        MultipleCredentialsUserProxy multipleCredentialsUserProxy =
                new MultipleCredentialsUserProxy();
        return multipleCredentialsUserProxy.getAllUserClaimValues(identifer, credentialType,
                profileName);

    }

}
