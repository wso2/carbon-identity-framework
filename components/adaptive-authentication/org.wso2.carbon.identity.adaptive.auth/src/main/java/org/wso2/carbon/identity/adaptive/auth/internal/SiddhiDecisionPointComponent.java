/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.adaptive.auth.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.adaptive.auth.deployer.SiddhiAppDeployer;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Component(
        name = "org.wso2.sample.siddhi.decision.point",
        immediate = true
)
public class SiddhiDecisionPointComponent {

    private static final Log log = LogFactory.getLog(SiddhiDecisionPointComponent.class);
    private SiddhiAppDeployer deployer = null;

    @Activate
    protected void activate(ComponentContext context) {

        log.info("-------------- SiddhiDecisionPointComponent ACTIVATION STARTED ----------------");

        try {
            Path siddhiAppRootPath = Paths.get(CarbonUtils.getCarbonRepository(), "siddhiApps");
            if (Files.notExists(siddhiAppRootPath)) {
                // TODO : ideally we should create this folder during feature installation....
                Files.createDirectory(siddhiAppRootPath);
            }

            // Copy our sample app to siddhiApps folder
            Path toWrite = Paths.get(siddhiAppRootPath.toAbsolutePath().toString(), "accountLockApp.siddhi");
            Files.write(toWrite, getSampleSiddhiApp().getBytes(StandardCharsets.UTF_8));

            deployer = new SiddhiAppDeployer(siddhiAppRootPath);
            deployer.start();
        } catch (Throwable throwable) {
            log.error(throwable);
        }

        log.info("-------------- SiddhiDecisionPointComponent ACTIVATION COMPLETED ----------------");
    }

    private String getSampleSiddhiApp() throws IOException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("accountLockOnFailureApp.siddhi");
        return readInputStream(stream);
    }

    private String readInputStream(InputStream input) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.info("SiddhiDecisionPointComponent bundle is deactivated");
        if (deployer != null) {
            deployer.stop();
        }
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService")
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }
}
