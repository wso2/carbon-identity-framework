/*
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

package org.wso2.carbon.identity.mgt.password;

import java.security.SecureRandom;
import java.util.Random;

/**
 * default password generator with 8 letter password
 * @deprecated use {@link org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator} instead.
 */
@Deprecated
public class DefaultPasswordGenerator implements RandomPasswordGenerator {

    //TODO : read the lenth from the user-mgt.xml
    private static final int PASSWORD_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();


    @Override
    public char[] generatePassword() {
        // Pick from some letters that won't be easily mistaken for each
        // other. So, for example, omit o O and 0, 1 l and L.
        String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";

        StringBuilder pw = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = (int) (RANDOM.nextDouble() * letters.length());
            pw.append(letters.substring(index, index + 1));
        }
        char[] password = new char[pw.length()];
        pw.getChars(0, pw.length(), password, 0);
        return password;
    }
}
