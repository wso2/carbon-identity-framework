/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication fail Node.
 * Contains parameters required to process authentication failure.
 */
public class FailNode extends AbstractAuthGraphNode implements AuthGraphNode {

    private static final long serialVersionUID = 9217119784147648132L;
    private boolean showErrorPage;
    private String errorPageUri;
    private Map<String, String> failureData;

    public String getErrorPageUri() {
        return errorPageUri;
    }

    public void setErrorPageUri(String errorPageUri) {
        this.errorPageUri = errorPageUri;
    }

    boolean isShowErrorPage() {
        return showErrorPage;
    }

    public void setShowErrorPage(boolean showErrorPage) {
        this.showErrorPage = showErrorPage;
    }

    @Override
    public String getName() {
        //TODO: Implement this
        return null;
    }

    public Map<String, String> getFailureData() {

        if (failureData == null) {
            failureData = new HashMap<>();
        }
        return failureData;
    }
}
