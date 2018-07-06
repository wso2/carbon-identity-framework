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

package org.wso2.carbon.identity.remotefetch.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.dao.RemoteFetchConfigurationDAO;
import org.wso2.carbon.identity.remotefetch.core.dao.impl.RemoteFetchConfigurationDAOImpl;

import java.util.HashMap;

public class RemoteFetchCore implements Runnable{

    private Log log = LogFactory.getLog(RemoteFetchCore.class);

    @Override
    public void run() {
        RemoteFetchConfiguration rfc = new RemoteFetchConfiguration();

        HashMap<String,String> example = new HashMap<>();
        example.put("foo","bar");

        rfc.setRepositoryConnectorType("git");
        rfc.setActionListenerType("polling");
        rfc.setConfgiurationDeployerType("SP");
        rfc.setRepositoryConnectorAttributes(example);
        rfc.setActionListenerAttributes(example);
        rfc.setTenantId(0);

        RemoteFetchConfigurationDAO rfd = new RemoteFetchConfigurationDAOImpl();
        try {
//            int id = rfd.createRemoteFetchConfiguration(rfc);
            rfd.getAllRemoteFetchConfigurations().forEach((RemoteFetchConfiguration config) ->{
                System.out.println(config.toString());
            });
//            RemoteFetchConfiguration rfc = rfd.getRemoteFetchConfiguration(2);
//            rfc.setActionListenerType("LKTEST");
//            rfd.updateRemoteFetchConfiguration(rfc,0);
//            System.out.println(rfd.getRemoteFetchConfiguration(2));
//            rfd.deleteRemoteFetchConfiguration(id);
        } catch (RemoteFetchCoreException e) {
            e.printStackTrace();
        }
    }
}
