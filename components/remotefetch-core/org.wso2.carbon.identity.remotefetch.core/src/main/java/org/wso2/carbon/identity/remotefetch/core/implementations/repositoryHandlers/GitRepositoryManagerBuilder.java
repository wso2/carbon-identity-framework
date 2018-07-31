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

package org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers;

import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManager;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerBuilder;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerBuilderException;

import java.util.Map;

public class GitRepositoryManagerBuilder extends RepositoryManagerBuilder {

    Map<String, String> repoAttributes;

    @Override
    public RepositoryManager build() throws RepositoryManagerBuilderException {

        repoAttributes = this.fetchConfig.getRepositoryManagerAttributes();

        String branch;
        String uri;

        if (repoAttributes.containsKey("uri")) {
            uri = repoAttributes.get("uri");
        } else {
            throw new RepositoryManagerBuilderException("No URI specified in RemoteFetchConfiguration Repository");
        }

        if (repoAttributes.containsKey("branch")) {
            branch = repoAttributes.get("branch");
        } else {
            throw new RepositoryManagerBuilderException("No branch specified in RemoteFetchConfiguration Repository");
        }

        return new GitRepositoryManager("repo-" + this.fetchConfig.getRemoteFetchConfigurationId(), uri, branch);
    }
}
