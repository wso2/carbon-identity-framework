/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.internal.IdentityCommonDataHolder;
import org.wso2.carbon.identity.common.internal.config.ConfigParser;
import org.wso2.carbon.identity.common.internal.cache.CacheConfig;
import org.wso2.carbon.identity.common.internal.cache.CacheConfigKey;
import org.wso2.carbon.identity.common.internal.cookie.CookieConfig;
import org.wso2.carbon.identity.common.internal.cookie.CookieConfigKey;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfig;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfigKey;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityUtils {

    private static Logger logger = LoggerFactory.getLogger(IdentityUtils.class);

    private static volatile IdentityUtils instance = null;

    public static final ThreadLocal<Map<String, Object>> threadLocals = new ThreadLocal<Map<String, Object>> () {

        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap();
        }
    };

    private IdentityUtils() {
        ConfigParser.getInstance();
    }

    public static IdentityUtils getInstance() {
        if(instance == null) {
            synchronized (IdentityUtils.class) {
                if(instance == null) {
                    instance = new IdentityUtils();
                }
            }
        }
        return instance;
    }

    public Map<HandlerConfigKey, HandlerConfig> getHandlerConfig() {
        return IdentityCommonDataHolder.getInstance().getHandlerConfig();
    }

    public Map<CacheConfigKey, CacheConfig> getCacheConfig() {
        return IdentityCommonDataHolder.getInstance().getCacheConfig();
    }

    public Map<CookieConfigKey, CookieConfig> getCookieConfig() {
        return IdentityCommonDataHolder.getInstance().getCookieConfig();
    }

    public static String generateHmacSHA1(String secretKey, String baseString) throws SignatureException {
//        try {
//            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), Constants.HMAC_SHA1);
//            Mac mac = Mac.getInstance(Constants.HMAC_SHA1);
//            mac.init(key);
//            byte[] rawHmac = mac.doFinal(baseString.getBytes());
//            return Base64.encode(rawHmac);
//        } catch (Exception e) {
//            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
//        }
        return null;
    }

    /**
     * Generates a secure random hexadecimal string using SHA1 PRNG and digest
     *
     * @return Random hexadecimal encoded String
     */
    public static String generateUUID() throws Exception {

        String uuid = null;
//        try {
//            // SHA1 Pseudo Random Number Generator
//            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
//
//            // random number
//            String randomNum = Integer.toString(prng.nextInt());
//            MessageDigest sha = MessageDigest.getInstance("SHA-256");
//            byte[] digest = sha.digest(randomNum.getBytes());
//
//            // Hexadecimal encoding
//            return new String(Hex.encodeHex(digest));
//
//        } catch (NoSuchAlgorithmException e) {
//            throw new Exception("Failed to generate UUID ", e);
//        }
        return uuid;
    }

    /**
     * Generates a random number using two UUIDs and HMAC-SHA1
     *
     */
    public static String generateRandomNumber() throws IdentityRuntimeException {
        try {
            String secretKey = UUID.randomUUID().toString();
            String baseString = UUID.randomUUID().toString();

            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            String random = null;
//            random = new String(Base64.getEncoder().encode(rawHmac));
            return random;
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            throw IdentityRuntimeException.error("Error occurred while generating random number.", e);
        }
    }

    public static int generateRandomInteger() throws IdentityRuntimeException {

        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            int number = prng.nextInt();
            while (number < 0) {
                number = prng.nextInt();
            }
            return number;
        } catch (NoSuchAlgorithmException e) {
            throw IdentityRuntimeException.error("Error occurred while generating random integer.", e);
        }

    }

    public static String getIdentityConfigDirPath() {
        return null;
    }

    public static String getServicePath() {
        // may not be needed in C5 because each service can be hosted on a unique service context
        return null;
    }



    /**
     * Get the server synchronization tolerance value in seconds
     *
     * @return clock skew in seconds
     */
    public static int getClockSkewInSeconds() {

        return 0;
    }



    /**
     * Validates an URI.
     *
     * @param uriString URI String
     * @return <code>true</code> if valid URI, <code>false</code> otherwise
     */
    public static boolean validateURI(String uriString) {

        if (uriString != null) {
            try {
                URL url = new URL(uriString);
            } catch (MalformedURLException e) {
                logger.debug(e.getMessage(), e);
                return false;
            }
        } else {
            String errorMsg = "Invalid URL: \'NULL\'";
            logger.debug(errorMsg);
            return false;
        }
        return true;
    }

    /**
     * @param array
     * @return
     */
    public static boolean exclusiveOR(boolean[] array) {
        boolean foundTrue = false;
        for (boolean temp : array) {
            if (temp) {
                if (foundTrue) {
                    return false;
                } else {
                    foundTrue = true;
                }
            }
        }
        return foundTrue;
    }

    public static String calculateHmacSha1(String key, String value) throws SignatureException {
        String result = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());
//            result = Base64.getEncoder().encode(rawHmac);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to create the HMAC Signature", e);
            }
            throw new SignatureException("Failed to calculate HMAC : " + e.getMessage());
        }
        return result;
    }

    // May not be needed going forward
    public static int getTenantId(String tenantDomain) throws IdentityRuntimeException {
        return 0;
    }

    // May not be needed going forward
    public static String getTenantDomain(int tenantId) throws IdentityRuntimeException {
        return null;
    }

    // User store case sensitivity check method must come from RealmService

    // Session cleanup period, session cleanup timeout, operation cleanup period and operation cleanup timeout must
    // be handled by authentication component

    // extracting/appending userstore domain from/to a username/groupname, reading primary userstore domain, etc. must
    // come from RealmService
}
