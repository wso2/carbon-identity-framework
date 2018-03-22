/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt.common;

import java.security.SecureRandom;
import java.util.Random;

/**
 * This class is used to generate a random password of 8 characters length
 */
public class DefaultPasswordGenerator implements RandomPasswordGenerator {

    private static final int PASSWORD_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();

    @Override
    public char[] generatePassword() {

        // Pick from some letters that won't be easily mistaken for each other.
        // So, for example, omit o O and 0, 1 l and L.
        // This will generate a random password which satisfy the following regex.
        // ^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$}
        String characters = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ!@#$%&*";
        String digits = "23456789";
        String lowercaseLetters = "abcdefghjkmnpqrstuvwxyz";
        String uppercaseLetters = "ABCDEFGHJKMNPQRSTUVWXYZ";
        String specialCharacters = "!@#$%&*";
        int mandatoryCharactersCount = 4;

        StringBuilder pw = new StringBuilder();
        int index;
        for (int i = 0; i < PASSWORD_LENGTH - mandatoryCharactersCount; i++) {
            index = RANDOM.nextInt(characters.length());
            pw.append(characters.charAt(index));
        }

        index = RANDOM.nextInt(digits.length());
        pw.append(digits.charAt(index));

        index = RANDOM.nextInt(lowercaseLetters.length());
        pw.append(lowercaseLetters.charAt(index));

        index = RANDOM.nextInt(uppercaseLetters.length());
        pw.append(uppercaseLetters.charAt(index));

        index = RANDOM.nextInt(specialCharacters.length());
        pw.append(specialCharacters.charAt(index));

        char[] password = new char[pw.length()];
        pw.getChars(0, pw.length(), password, 0);
        return password;
    }
}
