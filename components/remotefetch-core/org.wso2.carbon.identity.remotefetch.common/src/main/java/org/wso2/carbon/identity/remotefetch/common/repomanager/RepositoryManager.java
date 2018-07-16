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

package org.wso2.carbon.identity.remotefetch.common.repomanager;

import org.wso2.carbon.identity.remotefetch.common.ConfigFileContent;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Interface to define a repository manager
 * that communicates with a remote repository and handles it locally
 */
public interface RepositoryManager {

    /**
     * Change for updates on the remote repository
     * and fetches to local
     *
     * @throws Exception
     */
    void fetchRepository() throws RemoteFetchCoreException;

    /**
     * Returns an InputStream for the specified path
     * from local repository
     *
     * @param location
     * @return
     * @throws RemoteFetchCoreException
     */
    ConfigFileContent getFile(File location) throws RemoteFetchCoreException;

    /**
     * Returns the last modified date of the local file
     *
     * @param location
     * @return
     * @throws RemoteFetchCoreException
     */
    Date getLastModified(File location) throws RemoteFetchCoreException;

    /**
     * Gets an unique identifier for file state
     *
     * @param location
     * @return
     * @throws RemoteFetchCoreException
     */
    String getRevisionHash(File location) throws RemoteFetchCoreException;

    /**
     * list files from local repository for given path
     *
     * @param location
     * @return
     * @throws RemoteFetchCoreException
     */
    List<File> listFiles(File location) throws RemoteFetchCoreException;
}
