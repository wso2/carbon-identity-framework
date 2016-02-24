/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.processors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.RandomPassword;
import org.wso2.carbon.identity.application.common.model.RandomPasswordContainer;
import org.wso2.carbon.identity.application.common.cache.RandomPasswordContainerCache;
import org.wso2.carbon.identity.application.common.cache.RandomPasswordContainerCacheEntry;
import org.wso2.carbon.identity.application.common.cache.RandomPasswordContainerCacheKey;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Use this class to secure sensitive properties display on html front-end
 */
public class RandomPasswordProcessor {

    private static final Log log = LogFactory.getLog(RandomPasswordProcessor.class);

    private static volatile RandomPasswordProcessor randomPasswordProcessor = null;

    private RandomPasswordProcessor (){

    }

    public static RandomPasswordProcessor getInstance(){

        if(randomPasswordProcessor == null){
            synchronized (RandomPassword.class){
                if(randomPasswordProcessor == null){
                    randomPasswordProcessor = new RandomPasswordProcessor();
                }
            }
        }

        return randomPasswordProcessor;
    }

    /**
     * Remove original passwords with random passwords when sending password properties to UI front-end
     * @param properties
     */
    public Property[] removeOriginalPasswords(Property[] properties){

        if (ArrayUtils.isEmpty(properties)){
            return new Property[0];
        }

        properties = addUniqueIdProperty(properties);
        String uuid = IdentityApplicationManagementUtil
                .getPropertyValue(properties, IdentityApplicationConstants.UNIQUE_ID_CONSTANT);
        String randomPhrase = IdentityApplicationConstants.RANDOM_PHRASE_PREFIX + uuid;
        RandomPassword[] randomPasswords = replaceOriginalPasswordsWithRandomPasswords(
                randomPhrase, properties);
        if (!ArrayUtils.isEmpty(randomPasswords)) {
            addPasswordContainerToCache(randomPasswords, uuid);
        }

        return properties;
    }

    /**
     * Remove random passwords with original passwords when sending password properties to Service Back-end
     * @param properties
     */
    public Property[] removeRandomPasswords(Property[] properties, boolean withCacheClear) {

        if (ArrayUtils.isEmpty(properties)) {
            return new Property[0];
        }

        String uuid = IdentityApplicationManagementUtil.getPropertyValue(properties,
                                                                         IdentityApplicationConstants.UNIQUE_ID_CONSTANT);
        if (StringUtils.isBlank(uuid)) {
            if (log.isDebugEnabled()) {
                log.debug("Cache Key not found for Random Password Container");
            }
        } else {
            properties = removeUniqueIdProperty(properties);
            RandomPassword[] randomPasswords = getRandomPasswordContainerFromCache(uuid, withCacheClear);
            if (!ArrayUtils.isEmpty(randomPasswords)) {
                replaceRandomPasswordsWithOriginalPasswords(properties,
                                                            randomPasswords);
            }
        }
        return properties;
    }

    private void addPasswordContainerToCache(RandomPassword[] randomPasswords, String uuid) {

        if(randomPasswords == null){
            if (log.isDebugEnabled()) {
                log.debug("Random passwords not available for Password Container");
            }
        }
        RandomPasswordContainer randomPasswordContainer = new RandomPasswordContainer();
        randomPasswordContainer.setRandomPasswords(randomPasswords);
        randomPasswordContainer.setUniqueID(uuid);

        RandomPasswordContainerCacheKey cacheKey = new RandomPasswordContainerCacheKey(uuid);

        RandomPasswordContainerCacheEntry cacheEntry = new RandomPasswordContainerCacheEntry(randomPasswordContainer);

        if (log.isDebugEnabled()){
            log.debug("Adding Random passwords to Cache with Key: " + uuid);
        }

        RandomPasswordContainerCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    private RandomPassword[] getRandomPasswordContainerFromCache(String uuid, boolean withCacheClear) {

        RandomPasswordContainerCacheKey cacheKey = new RandomPasswordContainerCacheKey(uuid);
        RandomPasswordContainerCache cache = RandomPasswordContainerCache.getInstance();
        RandomPasswordContainerCacheEntry cacheEntry = cache.getValueFromCache(cacheKey);
        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for cache key :" + uuid);
            }
            return new RandomPassword[0];
        }

        RandomPasswordContainer randomPasswordContainer = cacheEntry.getRandomPasswordContainer();

        if (randomPasswordContainer == null || randomPasswordContainer.getRandomPasswords() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find Random Passwords from Random Password Container");
            }
            return new RandomPassword[0];
        }

        if (withCacheClear){
            if (log.isDebugEnabled()){
                log.debug("Removing Cache Entry with Key: " + uuid);
            }
            cache.clearCacheEntry(cacheKey);
        }
        return randomPasswordContainer.getRandomPasswords();
    }

    /**
     * Replace original passwords with provided random phrase
     * @param randomPhrase
     * @param properties
     * @return
     */
    private RandomPassword[] replaceOriginalPasswordsWithRandomPasswords(String randomPhrase, Property[] properties) {

        ArrayList<RandomPassword> randomPasswordArrayList = new ArrayList<RandomPassword>();
        if (properties != null) {
            for (Property property : properties) {

                if (property == null || property.getName() == null || property.getValue() == null) {
                    continue;
                }
                if (property.getName().contains(IdentityApplicationConstants.PASSWORD)) {

                    if (log.isDebugEnabled()){
                        log.debug("Found Password Property :" + property.getValue());
                    }
                    RandomPassword randomPassword = new RandomPassword();
                    randomPassword.setPropertyName(property.getName());
                    randomPassword.setPassword(property.getValue());
                    randomPassword.setRandomPhrase(randomPhrase);
                    randomPasswordArrayList.add(randomPassword);

                    property.setValue(randomPhrase);
                }
            }
        }
        return randomPasswordArrayList.toArray(new RandomPassword[randomPasswordArrayList.size()]);
    }

    /**
     * Remove random password list with corresponding old original password if value has not been changed
     * @param properties
     * @param randomPasswords
     */
    private void replaceRandomPasswordsWithOriginalPasswords(Property[] properties, RandomPassword randomPasswords[]) {

        if (ArrayUtils.isEmpty(properties) || ArrayUtils.isEmpty(randomPasswords)) {
            return;
        }

        for (Property property : properties) {
            if (property == null || StringUtils.isBlank(property.getName()) ||
                StringUtils.isBlank(property.getValue())) {
                continue;
            }
            if (property.getName().contains(IdentityApplicationConstants.PASSWORD)) {
                for (RandomPassword randomPassword : randomPasswords) {
                    if (property.getName().equals(randomPassword.getPropertyName())) {
                        //if password property value equal to random phrase, user didn't change the password hence set
                        // old password
                        if (property.getValue().equals(randomPassword.getRandomPhrase())) {
                            if (log.isDebugEnabled()){
                                log.debug("User didn't changed password property: " + property.getName() +
                                          " hence replace Random Password with Original Password");
                            }
                            property.setValue(randomPassword.getPassword());
                        }
                    }
                }
            }
        }
    }

    private Property[] addUniqueIdProperty(Property [] properties) {

        if (ArrayUtils.isEmpty(properties)){
            return new Property[0];
        }

        String uuid = UUID.randomUUID().toString();
        Property uniqueIdProperty = new Property();
        uniqueIdProperty.setName(IdentityApplicationConstants.UNIQUE_ID_CONSTANT);
        uniqueIdProperty.setValue(uuid);
        if (log.isDebugEnabled()){
            log.debug("Adding uniqueId property: " + uuid);
        }
        properties = (Property[]) ArrayUtils.add(properties, uniqueIdProperty);

        return properties;
    }

    private Property[] removeUniqueIdProperty(Property [] properties) {

        if (ArrayUtils.isEmpty(properties)){
            return new Property[0];
        }

        for (int i=0 ; i < properties.length; i++) {
            if (properties[i] == null){
                continue;
            }
            if (IdentityApplicationConstants.UNIQUE_ID_CONSTANT.equals(properties[i].getName())) {
                Property[] propertiesTemp = properties;

                if (log.isDebugEnabled()){
                    log.debug("Removing uniqueId property: " + properties[i].getName());
                }
                properties = (Property[])ArrayUtils.removeElement(properties, properties[i]);
                //Removing uniqueId property from existing properties too
                propertiesTemp[i] = null;
            }
        }
        return properties;
    }
}
