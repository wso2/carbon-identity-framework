/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.mail;

/**
 * AdminManagement Configurations, those to be included in the admin management email to be sent.
 */
public class EmailConfig {

    public final static String DEFAULT_VALUE_SUBJECT = "Configure Admin Account";
    public final static String DEFAULT_VALUE_MESSAGE = "Please point your browser to : ";


    private String configType;
    private String subject = DEFAULT_VALUE_SUBJECT;
    private String emailBody = DEFAULT_VALUE_MESSAGE;
    private String emailFooter;
    private String redirectPath;
    private String targetEpr;

    public String getTargetEpr() {
        return targetEpr;
    }

    public void setTargetEpr(String targetEpr) {
        this.targetEpr = targetEpr;
    }


    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailMessage) {
        this.emailBody = emailMessage;
    }

    public String getEmailFooter() {
        return emailFooter;
    }

    public void setEmailFooter(String emailFooter) {
        this.emailFooter = emailFooter;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }
}
