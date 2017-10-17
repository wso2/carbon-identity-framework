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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class UserProfileUIUtil {

    private static final Log log = LogFactory.getLog(UserProfileUIUtil.class);

    /**
     * Encrypt and Base64 encode the username with Carbon server's private key.
     * @param username Username to encrypt
     * @return Encrypted and Base64Encoded username
     * @throws UserProfileUIException
     */
    public static String getEncryptedAndBase64encodedUsername(String username) throws UserProfileUIException{
        String encryptedAndBase64EncodedUsername = null;
        try {
            if (username != null) {
                encryptedAndBase64EncodedUsername = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(username.getBytes());
            }
        } catch (CryptoException e) {
            log.error(String.format("Error while trying to encrypt the username : '%s' ", username), e);
            throw new UserProfileUIException(e);
        }

        return encryptedAndBase64EncodedUsername;
    }

    /**
     * Decrypt the encrypted username using Carbon server's public key.
     * @param encryptedAndBase64EncodedUsername Encrypted and Base64Encoded username which is encrypted by Carbon
     *                                          server's private key
     * @return Decrypted username
     * @throws UserProfileUIException
     */
    public static String getDecryptedUsername(String encryptedAndBase64EncodedUsername) throws UserProfileUIException {
        try {
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(encryptedAndBase64EncodedUsername));
        } catch (CryptoException e) {
            log.error(String.format("Error while trying to decrypt the username : '%s' ", encryptedAndBase64EncodedUsername), e);
            throw new UserProfileUIException(e);
        }
    }
}
