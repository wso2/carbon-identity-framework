/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.util;

import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.Credentials;

import java.util.Arrays;

/**
 * Utility class for serializing exposed user credentials into a typed JSON wire format.
 */
public final class CredentialWireFormatUtil {

    private static final char[] TYPED_CREDENTIAL_PREFIX = ("{\""
            + Credentials.TYPE_KEY + "\":\""
            + Credentials.TYPE_PLAIN_TEXT + "\",\""
            + Credentials.VALUE_KEY + "\":\"").toCharArray();
    private static final char[] TYPED_CREDENTIAL_SUFFIX = "\"}".toCharArray();
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final int MAX_ESCAPE_EXPANSION = 6;

    private CredentialWireFormatUtil() {

    }

    /**
     * Serializes the secret into a JSON character array formatted as a typed plaintext credential.
     *
     * @param secret The raw credential secret.
     * @return The typed credential JSON payload.
     */
    public static char[] toPlainTextCredentialJson(char[] secret) {

        char[] buffer = new char[TYPED_CREDENTIAL_PREFIX.length
                + secret.length * MAX_ESCAPE_EXPANSION
                + TYPED_CREDENTIAL_SUFFIX.length];
        int pos = 0;

        pos = append(buffer, pos, TYPED_CREDENTIAL_PREFIX);
        for (char c : secret) {
            pos = appendJsonEscaped(buffer, pos, c);
        }
        pos = append(buffer, pos, TYPED_CREDENTIAL_SUFFIX);

        char[] result = Arrays.copyOf(buffer, pos);
        // Wipe the oversized working buffer; it holds a copy of the (escaped) secret.
        Arrays.fill(buffer, '\0');
        return result;
    }

    /**
     * Copies the source character array into the destination buffer.
     *
     * @param buffer The destination buffer.
     * @param pos    The starting index position.
     * @param src    The source array.
     * @return The next available index position.
     */
    private static int append(char[] buffer, int pos, char[] src) {

        System.arraycopy(src, 0, buffer, pos, src.length);
        return pos + src.length;
    }

    /**
     * Appends the JSON-escaped representation of a single character into the target buffer.
     *
     * @param buffer The destination buffer.
     * @param pos    The current index position.
     * @param c      The character to evaluate and escape.
     * @return The updated index position.
     */
    private static int appendJsonEscaped(char[] buffer, int pos, char c) {

        switch (c) {
            case '"':
                return appendEscape(buffer, pos, '"');
            case '\\':
                return appendEscape(buffer, pos, '\\');
            case '\b':
                return appendEscape(buffer, pos, 'b');
            case '\f':
                return appendEscape(buffer, pos, 'f');
            case '\n':
                return appendEscape(buffer, pos, 'n');
            case '\r':
                return appendEscape(buffer, pos, 'r');
            case '\t':
                return appendEscape(buffer, pos, 't');
            default:
                if (c < 0x20) {
                    return appendUnicodeEscape(buffer, pos, c);
                }
                buffer[pos] = c;
                return pos + 1;
        }
    }

    /**
     * Appends a two-character short escape sequence to the buffer.
     *
     * @param buffer     The destination buffer.
     * @param pos        The current index position.
     * @param escapeChar The character following the backslash.
     * @return The updated index position.
     */
    private static int appendEscape(char[] buffer, int pos, char escapeChar) {

        buffer[pos] = '\\';
        buffer[pos + 1] = escapeChar;
        return pos + 2;
    }

    /**
     * Appends a six-character Unicode escape sequence for control characters.
     *
     * @param buffer The destination buffer.
     * @param pos    The current index position.
     * @param c      The control character to convert.
     * @return The updated index position.
     */
    private static int appendUnicodeEscape(char[] buffer, int pos, char c) {

        buffer[pos] = '\\';
        buffer[pos + 1] = 'u';
        buffer[pos + 2] = '0';
        buffer[pos + 3] = '0';
        buffer[pos + 4] = HEX_DIGITS[(c >> 4) & 0xF];
        buffer[pos + 5] = HEX_DIGITS[c & 0xF];
        return pos + 6;
    }
}
