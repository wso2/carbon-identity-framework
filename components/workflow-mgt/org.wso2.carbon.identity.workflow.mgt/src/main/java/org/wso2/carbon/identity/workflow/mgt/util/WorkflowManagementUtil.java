/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Workflow management util class.
 */
public class WorkflowManagementUtil {

    private static final Log log = LogFactory.getLog(WorkflowManagementUtil.class);

    /**
     * Un-marshall given string to given class type.
     *
     * @param xmlString XML String that is validated against its XSD.
     * @param classType Root Class Name to convert XML String to Object.
     * @param <T>       Root Class that should return.
     * @return Instance of T.
     * @throws JAXBException
     */
    public static <T> T unmarshalXML(String xmlString, Class<T> classType) throws JAXBException {

        T t = null;
        if (xmlString != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlString.getBytes(
                    StandardCharsets.UTF_8));
            JAXBContext jaxbContext = JAXBContext.newInstance(classType);

            try {
                DocumentBuilderFactory factory = IdentityUtil.getSecuredDocumentBuilderFactory();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(byteArrayInputStream);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                t = (T) jaxbUnmarshaller.unmarshal(document);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                log.error("Error while unmarshalling the XML.", e);
            }
        }
        return t;
    }

    /**
     * Reading File Content from the resource path.
     *
     * @param resourceAsStream
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String readFileFromResource(InputStream resourceAsStream) throws URISyntaxException, IOException {

        String content = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream);
            int c = -1;
            StringBuilder resourceFile = new StringBuilder();
            while ((c = bufferedInputStream.read()) != -1) {
                char val = (char) c;
                resourceFile.append(val);
            }
            content = resourceFile.toString();

        } catch (IOException e) {
            String errorMsg = "Error occurred while reading file from class path, " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
        return content;
    }

    /**
     * Utility method to read Parameter from the list.
     *
     * @param parameterList
     * @param paramName
     * @param holder
     * @return
     */
    public static Parameter getParameter(List<Parameter> parameterList, String paramName, String holder) {

        for (Parameter parameter : parameterList) {
            if (parameter.getParamName().equals(paramName) && parameter.getqName().equals(paramName) &&
                    parameter.getHolder().equals(holder)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Return no of results per page.
     *
     * @return
     */
    public static int getItemsPerPage() {

        String itemsPerPagePropertyValue =
                ServerConfiguration.getInstance().getFirstProperty(WFConstant.ParameterName.ITEMS_PER_PAGE_PROPERTY);

        try {
            if (StringUtils.isNotBlank(itemsPerPagePropertyValue)) {
                int itemsPerPage = Math.abs(Integer.parseInt(itemsPerPagePropertyValue));
                if (log.isDebugEnabled()) {
                    log.debug("Items per page for pagination is set to : " + itemsPerPage);
                }
                return itemsPerPage;
            }
        } catch (NumberFormatException e) {
            // No need to handle exception since the default value is already set.
            log.warn("Error occurred while parsing the 'ItemsPerPage' property value in carbon.xml. Defaulting to: "
                    + WFConstant.DEFAULT_RESULTS_PER_PAGE);
        }

        return WFConstant.DEFAULT_RESULTS_PER_PAGE;
    }

    /**
     * Generate owner role name for workflow.
     *
     * @param workflowName Workflow name
     * @return
     */
    public static String createWorkflowRoleName(String workflowName) {
        return UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + workflowName;
    }
}
