/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth2.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.util.concurrent.BlockingDeque;

/**
 *
 */
public class AuthPersistenceTask implements Runnable {

    private static Log log = LogFactory.getLog(TokenPersistenceTask.class);
    private BlockingDeque<AuthContextTokenDO> authContextTokenQueue;

    public AuthPersistenceTask(BlockingDeque<AuthContextTokenDO> authContextTokenQueue) {
        this.authContextTokenQueue = authContextTokenQueue;
    }

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("Auth Token context persist consumer is started");
        }

        while (true) {
            try {
                AuthContextTokenDO authContextTokenDO = authContextTokenQueue.take();
                if (authContextTokenDO != null) {
                    if (authContextTokenDO.getAuthzCodeDO() == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Auth Token Data removing Task is started to run");
                        }
                        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
                        tokenMgtDAO.doExpireAuthzCode(authContextTokenDO.getAuthzCode());
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Auth Token Data persisting Task is started to run");
                        }
                        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
                        tokenMgtDAO.persistAuthorizationCode(authContextTokenDO.getAuthzCode(),
                                authContextTokenDO.getConsumerKey(), authContextTokenDO.getCallbackUrl(),
                                authContextTokenDO.getAuthzCodeDO());
                    }
                }
            } catch (InterruptedException | IdentityOAuth2Exception e) {
                log.error(e);
            }

        }
    }

}
