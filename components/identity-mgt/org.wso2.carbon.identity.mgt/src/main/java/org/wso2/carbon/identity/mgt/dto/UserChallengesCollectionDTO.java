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

package org.wso2.carbon.identity.mgt.dto;

/**
 * This class is used to to communicate challenge questions configured by the user to the client.
 */
public class UserChallengesCollectionDTO {

    private String key;

    private UserChallengesDTO[] userChallengesDTOs = new UserChallengesDTO[0];

    private String error;

    /**
     * Returns the confirmation code.
     *
     * @return confirmation code
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the confirmation code.
     *
     * @param key confirmation code
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the challenge questions configured for the user.
     *
     * @return an array of UserChallengesDTO instances which holds challenge questions configured for user
     */
    public UserChallengesDTO[] getUserChallengesDTOs() {
        return userChallengesDTOs;
    }

    /**
     * Sets challenge question configured for user.
     *
     * @param userChallengesDTOs an array of UserChallengesDTO instances which holds challenge questions configured for user
     */
    public void setUserChallengesDTOs(UserChallengesDTO[] userChallengesDTOs) {
        this.userChallengesDTOs = userChallengesDTOs;
    }

    /**
     * Returns the server error occurred.
     *
     * @return error
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the server error occurred.
     *
     * @param error server error
     */
    public void setError(String error) {
        this.error = error;
    }
}
