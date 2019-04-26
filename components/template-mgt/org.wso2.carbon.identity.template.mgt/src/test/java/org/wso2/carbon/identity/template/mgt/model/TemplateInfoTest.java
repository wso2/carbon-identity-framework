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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TemplateInfoTest {

    private static final String templateName = "sampleTemplate";
    private static final String description = "sample Description";
    private static Integer templateId = 1;
    private static Integer tenantId = -1234;
    TemplateInfo testTemplateInfo1 = new TemplateInfo(templateId, tenantId, templateName);
    TemplateInfo testTemplateInfo2 = new TemplateInfo(templateName, description);

    @Test
    public void testGetTemplateId() {

        Assert.assertEquals(testTemplateInfo1.getTemplateId(), templateId);
    }

    @Test
    public void testGetTenantId() {

        Assert.assertEquals(testTemplateInfo1.getTenantId(), tenantId);
    }

    @Test
    public void testGetTemplateName() {

        Assert.assertEquals(testTemplateInfo1.getTemplateName(), templateName);
    }

    @Test
    public void testGetDescription() {

        Assert.assertEquals(testTemplateInfo2.getDescription(), description);
    }
}