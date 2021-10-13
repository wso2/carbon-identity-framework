/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.idp.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.secretprocessor.PlainTextPersistenceProcessor;
import org.wso2.carbon.idp.mgt.secretprocessor.SecretPersistenceProcessor;

/**
 * This class loads configurations related to identity provider secret processing from
 * repository/conf/identity/identity.xml.
 */
public class IdPSecretConfiguration {

    private static final Log log = LogFactory.getLog(IdPSecretConfiguration.class);

    private static IdPSecretConfiguration instance;

    private String IdPSecretPersistenceProcessorClassName =
            "org.wso2.carbon.idp.mgt.secretprocessor.PlainTextPersistenceProcessor";

    private SecretPersistenceProcessor persistenceProcessor = null;

    private IdPSecretConfiguration() {

        readIdPSecretPersistenceProcessorConfig();
    }

    public static IdPSecretConfiguration getInstance() {

        if (instance == null) {
            synchronized (IdPSecretConfiguration.class) {
                if (instance == null) {
                    instance = new IdPSecretConfiguration();
                }
            }
        }
        return instance;
    }

    private void readIdPSecretPersistenceProcessorConfig() {

        String identityProviderSecretPersistenceProcessor = IdentityUtil.getProperty(
                "IdentityProviderSecretPersistenceProcessor");

        if (StringUtils.isNotBlank(identityProviderSecretPersistenceProcessor)) {
            IdPSecretPersistenceProcessorClassName = identityProviderSecretPersistenceProcessor.trim();
        }

        if (log.isDebugEnabled()) {
            log.debug("Identity provider secret persistence processor was set to : " +
                    IdPSecretPersistenceProcessorClassName);
        }
    }

    /**
     * Get the relevant SecretPersistenceProcessor implementation.
     *
     * @return SecretPersistenceProcessor implementation.
     */
    public SecretPersistenceProcessor getPersistenceProcessor() {

        if (persistenceProcessor == null) {
            synchronized (this) {
                if (persistenceProcessor == null) {
                    try {
                        Class clazz =
                                this.getClass().getClassLoader()
                                        .loadClass(IdPSecretPersistenceProcessorClassName);
                        persistenceProcessor = (SecretPersistenceProcessor) clazz.newInstance();

                        if (log.isDebugEnabled()) {
                            log.debug("An instance of " + IdPSecretPersistenceProcessorClassName +
                                    " is created for IdPSecretConfiguration.");
                        }
                    } catch (Exception e) {
                        String errorMsg = "Error when instantiating the SecretPersistenceProcessor : " +
                                IdPSecretPersistenceProcessorClassName +
                                ". Defaulting to PlainTextPersistenceProcessor.";
                        log.error(errorMsg, e);
                        persistenceProcessor = new PlainTextPersistenceProcessor();
                    }
                }
            }
        }
        return persistenceProcessor;
    }

}
