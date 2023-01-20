package org.wso2.carbon.identity.workflow.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class WorkflowManagementUtil {
    private static final Log log = LogFactory.getLog(WorkflowManagementUtil.class);

    /**
     * Create a internal role in workflow domain with same name as workflow.
     *
     * @param workflowName Workflow name
     * @throws WorkflowException
     */
    public static void createAppRole(String workflowName) throws WorkflowException {
        String roleName = createWorkflowRoleName(workflowName);
        String qualifiedUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String[] user = {qualifiedUsername};

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating workflow role : " + roleName + " and assign the user : "
                        + Arrays.toString(user) + " to that role");
            }
            CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .addRole(roleName, user, null);
        } catch (UserStoreException e) {
            throw new WorkflowException("Error while creating role", e);
        }

    }

    /**
     * Delete role created for workflow.
     *
     * @param workflowName Workflow  name
     * @throws WorkflowException
     */
    public static void deleteWorkflowRole(String workflowName) throws WorkflowException {
        String roleName = createWorkflowRoleName(workflowName);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting workflow role : " + roleName);
            }
            CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .deleteRole(roleName);
        } catch (UserStoreException e) {
            throw new WorkflowException("Error while creating workflow", e);
        }
    }

    /**
     * Update name of workflow role
     *
     * @param oldWorkflowName Previous role name
     * @param newWorkflowName New role name
     * @throws WorkflowException
     */
    public static void updateWorkflowRoleName(String oldWorkflowName, String newWorkflowName) throws
            WorkflowException {
        String oldRoleName = createWorkflowRoleName(oldWorkflowName);
        String newRoleName = createWorkflowRoleName(newWorkflowName);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating workflow role : " + oldRoleName);
            }
            CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .updateRoleName(oldRoleName, newRoleName);
        } catch (UserStoreException e) {
            throw new WorkflowException("Error while updating workflow role name.", e);
        }

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


    /**
     * Un-marshall given string to given class type
     *
     * @param xmlString XML String that is validated against its XSD
     * @param classType Root Class Name to convert XML String to Object
     * @param <T>       Root Class that should return
     * @return Instance of T
     * @throws JAXBException
     */
    public static <T> T unmarshalXML(String xmlString, Class<T> classType) throws JAXBException {
        T t = null;
        if (xmlString != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlString.getBytes());
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
     * Reading File Content from the resource path
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
     * Utility method to read Parameter from the list
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
     * Return no of results per page
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


}
