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

import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnector;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnectorBuilder;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnectorBuilderException;

import java.util.Map;

public class GitRepositoryConnectorBuilder extends RepositoryConnectorBuilder {

    Map<String,String> repoAttributes;

    @Override
    public RepositoryConnector build() throws RepositoryConnectorBuilderException {
        repoAttributes = this.fetchConfig.getRepositoryConnectorAttributes();
        String branch, uri;

        if(repoAttributes.containsKey("uri")){
            uri = repoAttributes.get("uri");
        }else{
            throw new RepositoryConnectorBuilderException("No URI specified in RemoteFetchConfiguration Repository");
        }

        if(repoAttributes.containsKey("branch")){
            branch = repoAttributes.get("branch");
        }else{
            throw new RepositoryConnectorBuilderException("No branch specified in RemoteFetchConfiguration Repository");
        }


        GitRepositoryConnector gitRepoConnector =
                new GitRepositoryConnector("repo-" + this.fetchConfig.getRemoteFetchConfigurationId() ,
                        uri, branch);
        return gitRepoConnector;
    }
}
