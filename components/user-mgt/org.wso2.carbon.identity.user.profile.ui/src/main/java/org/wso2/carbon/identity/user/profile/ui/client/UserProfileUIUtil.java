/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.user.profile.ui.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.Map;

/**
 * Util class responsible for performing user profile UI related utilities.
 */
public class UserProfileUIUtil {

    private static final Log log = LogFactory.getLog(UserProfileUIUtil.class);

    private static final String ENCRYPT_USERNAME_IN_URL = "encryptUsernameInUrl";

    private static final String bypassRoleName = "Internal/system";
    /**
     * Encrypt and Base64 encode the username with Carbon server's public key, if usernameEncryptionInUrl property is
     * set to true in user-mgt.xml, else return the username without encrypting.
     *
     * @param username Username to encrypt
     * @return Encrypted and base64Encoded username if usernameEncryptionInUrl property is set to true user-mgt.xml,
     * else return the username without encrypting
     * @throws UserProfileUIException
     */
    public static String getEncryptedAndBase64encodedUsername(String username) throws UserProfileUIException {
        String encryptedAndBase64EncodedUsername = null;
        try {
            if (StringUtils.isNotBlank(username)) {
                boolean isUsernameEncryptionEnabled = isUsernameEncryptionEnabled();
                if (isUsernameEncryptionEnabled) {
                    encryptedAndBase64EncodedUsername = CryptoUtil.getDefaultCryptoUtil().
                            encryptAndBase64Encode(username.getBytes());
                } else {
                    return username;
                }
            }
        } catch (CryptoException e) {
            String message = String.format("Error while trying to encrypt the username : '%s' ", username);
            log.error(message, e);
            throw new UserProfileUIException(message, e);
        } catch (CarbonException | UserStoreException e) {
            String message = "Error while trying to get UserRealm";
            log.error(message, e);
            throw new UserProfileUIException(message, e);
        }

        return encryptedAndBase64EncodedUsername;
    }

    /**
     * Decrypt the encrypted username using Carbon server's private key.
     *
     * @param encryptedAndBase64EncodedUsername Encrypted username which is encrypted by Carbon server's private key
     * @return Decrypted username if usernameEncryptionInUrl property is set to true user-mgt.xml, else return the
     * provided input parameter as it is.
     * @throws UserProfileUIException
     */
    public static String getDecryptedUsername(String encryptedAndBase64EncodedUsername) throws UserProfileUIException {
        try {
            boolean isUsernameEncryptionEnabled = isUsernameEncryptionEnabled();
            if (isUsernameEncryptionEnabled) {
                return new String(CryptoUtil.getDefaultCryptoUtil().
                        base64DecodeAndDecrypt(encryptedAndBase64EncodedUsername));
            } else {
                return encryptedAndBase64EncodedUsername;
            }
        } catch (CryptoException e) {
            String message = String.format("Error while trying to decrypt the username : '%s' ",
                    encryptedAndBase64EncodedUsername);
            log.error(message, e);
            throw new UserProfileUIException(message, e);
        } catch (CarbonException | UserStoreException e) {
            String message = "Error while trying to get UserRealm";
            log.error(message, e);
            throw new UserProfileUIException(message, e);
        }
    }

    private static boolean isUsernameEncryptionEnabled() throws CarbonException, UserStoreException {
        return Boolean.parseBoolean(AdminServicesUtil.getUserRealm().getRealmConfiguration()
                .getRealmProperties().get(ENCRYPT_USERNAME_IN_URL));
    }
    /**
     * Check whether the account is attached with account lock bypassable role.
     *
     * @param userName user name whos roles needs to be listed
     * @return true or false based on user roles
     * @throws CarbonException, UserStoreException
     */
    public static boolean isAccountLockable(String userName)
            throws CarbonException, UserStoreException {
        boolean isLockable = true;
        String[] roleList = AdminServicesUtil.getUserRealm().getUserStoreManager().getRoleListOfUser(userName);
        if (roleList != null) {
            for (String roleName : roleList) {
                if (roleName.equals(bypassRoleName)) {
                    isLockable = false;
                    break;
                }
            }

        }
        return isLockable;
    }
}
