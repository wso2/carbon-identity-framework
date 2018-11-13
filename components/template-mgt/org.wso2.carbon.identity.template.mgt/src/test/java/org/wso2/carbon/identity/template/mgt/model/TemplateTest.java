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

public class TemplateTest {

    private static final String templateName = "sampleTemplate";
    private static final String description = "sample Description";
    private static final String script = "sample Script";
    private static Integer templateId = 1;
    private static Integer tenantId = -1234;
    Template template = new Template(templateId, tenantId, templateName, description, script);
    Template template1 = new Template(templateName, description, script);

    @Test
    public void testGetTemplateId() {

        Assert.assertEquals(template.getTemplateId(), templateId);
    }

    @Test
    public void testSetTemplateId() {

        Integer templateId = 101;
        template1.setTemplateId(templateId);
        Assert.assertEquals(template1.getTemplateId(), templateId);
    }

    @Test
    public void testGetTenantId() {

        Assert.assertEquals(template.getTenantId(), tenantId);
    }

    @Test
    public void testSetTenantId() {

        Integer tenantId = 101;
        template1.setTenantId(tenantId);
        Assert.assertEquals(template1.getTenantId(), tenantId);
    }

    @Test
    public void testGetTemplateName() {

        Assert.assertEquals(template.getTemplateName(), templateName);
    }

    @Test
    public void testSetTemplateName() {

        String templateName = "test Name";
        template.setTemplateName(templateName);
        Assert.assertEquals(template.getTemplateName(), templateName);
    }

    @Test
    public void testGetDescription() {

        Assert.assertEquals(template.getDescription(), description);
    }

    @Test
    public void testSetDescription() {

        String description = "test description";
        template.setDescription(description);
        Assert.assertEquals(template.getDescription(), description);
    }

    @Test
    public void testGetTemplateScript() {

        Assert.assertEquals(template.getTemplateScript(), script);
    }

    @Test
    public void testSetTemplateScript() {

        String script = "test script";
        template.setTemplateScript(script);
        Assert.assertEquals(template.getTemplateScript(), script);
    }
}