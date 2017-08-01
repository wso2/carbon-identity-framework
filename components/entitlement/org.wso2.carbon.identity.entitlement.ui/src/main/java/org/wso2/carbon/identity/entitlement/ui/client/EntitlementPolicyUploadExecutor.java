/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.ui.client;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for uploading entitlement policy files.
 * And this uses the <code>AbstractFileUploadExecutor</code>
 * which has written to handle the carbon specific file uploading
 */
public class EntitlementPolicyUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".xml"};

    private String errorRedirectionPage;

    @Override
    public boolean execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws CarbonException, IOException {

        String webContext = (String) httpServletRequest.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) httpServletRequest.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) httpServletRequest.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        errorRedirectionPage = getContextRoot(httpServletRequest) + "/" + webContext
                + "/entitlement/index.jsp";

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, httpServletRequest,
                    httpServletResponse, errorRedirectionPage);
            return false;
        }

        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configurationContext);
        List<FileItemData> fileItems = fileItemsMap.get("policyFromFileSystem");
        String msg;
        try {
            for (FileItemData fileItem : fileItems) {
                String filename = getFileName(fileItem.getFileItem().getName());
                checkServiceFileExtensionValidity(filename, ALLOWED_FILE_EXTENSIONS);

                if (!filename.endsWith(".xml")) {
                    throw new CarbonException("File with extension " +
                            getFileName(fileItem.getFileItem().getName()) + " is not supported!");
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            fileItem.getDataHandler().getInputStream()))) {

                        String temp;
                        String policyContent = "";
                        while ((temp = br.readLine()) != null) {
                            policyContent += temp;
                        }
                        if (StringUtils.isNotEmpty(policyContent)) {
                            client.uploadPolicy(policyContent);
                        }
                    } catch (IOException ex) {
                        throw new CarbonException("Policy file " + filename + "cannot be read");
                    }
                }
            }
            httpServletResponse.setContentType("text/html; charset=utf-8");
            msg = "Policy have been uploaded successfully.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, httpServletRequest,
                    httpServletResponse, getContextRoot(httpServletRequest)
                            + "/" + webContext + "/entitlement/index.jsp");
            return true;
        } catch (Exception e) {
            msg = "Policy uploading failed. " + e.getMessage();
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, httpServletRequest,
                    httpServletResponse, errorRedirectionPage);
        }
        return false;
    }

    @Override
    protected String getErrorRedirectionPage() {
        return errorRedirectionPage;
    }
}
