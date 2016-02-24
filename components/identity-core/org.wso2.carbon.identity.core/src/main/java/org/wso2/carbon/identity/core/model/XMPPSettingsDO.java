/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.identity.core.model;

public class XMPPSettingsDO {

    private String xmppServer;
    private String xmppUserName;
    private String userCode;
    private boolean isXmppEnabled;
    private boolean isPINEnabled;

    public boolean isPINEnabled() {
        return isPINEnabled;
    }

    public void setPINEnabled(boolean PINEnabled) {
        isPINEnabled = PINEnabled;
    }

    public boolean isXmppEnabled() {
        return isXmppEnabled;
    }

    public void setXmppEnabled(boolean xmppEnabled) {
        isXmppEnabled = xmppEnabled;
    }


    /**
     * @return XMPP server name
     */
    public String getXmppServer() {
        return xmppServer;
    }

    /**
     * @param xmppServer
     */
    public void setXmppServer(String xmppServer) {
        this.xmppServer = xmppServer;
    }

    /**
     * @return XMPP username
     */
    public String getXmppUserName() {
        return xmppUserName;
    }

    /**
     * @param xmppUserName
     */
    public void setXmppUserName(String xmppUserName) {
        this.xmppUserName = xmppUserName;
    }

    /**
     * @return usercode
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * @param userCode
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
