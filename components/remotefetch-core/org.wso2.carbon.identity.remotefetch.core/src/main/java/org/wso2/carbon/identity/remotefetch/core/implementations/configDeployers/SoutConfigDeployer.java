/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.core.implementations.configDeployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers.GitRepositoryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The code contained in this file is fore demo purposes for showing the deployment of
 * a XML configuration in the core.
 */
public class SoutConfigDeployer implements ConfigDeployer {
    private static Log log = LogFactory.getLog(GitRepositoryManager.class);

    /**
     * Deploy the configuration read from stream
     *
     * @param reader
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deploy(InputStream reader) throws RemoteFetchCoreException {
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new InputStreamReader(reader,"UTF-8"));
            log.info("Deploying to STDIO");

            String line;
            while ((line = buffer.readLine()) != null) {
                System.out.println(line);
            }
        }catch (IOException e){
            throw new RemoteFetchCoreException("Unable to read configuration file");
        }
    }

    /**
     * resolve the unique identifier for the configuration
     *
     * @param reader
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public String resolveConfigName(InputStream reader) throws RemoteFetchCoreException {

        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new InputStreamReader(reader,"UTF-8"));

            String line;
            if ((line = buffer.readLine()) != null) {
                line =  line.replace(" ","");
                line = buffer.readLine();
            }
            return line;
        }catch (IOException e){
            throw new RemoteFetchCoreException("Unable to read configuration file");
        }
    }

    /**
     * returns an identifier for the configuration type being deployed
     *
     * @return
     */
    @Override
    public String getConfigType() {

        return null;
    }
}
