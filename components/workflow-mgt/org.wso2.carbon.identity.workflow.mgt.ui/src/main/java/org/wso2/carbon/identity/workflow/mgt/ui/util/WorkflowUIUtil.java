package org.wso2.carbon.identity.workflow.mgt.ui.util;

import org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParametersMetaData;
import org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorkflowUIUtil {

    /**
     * Load parameters of a workflow template
     *
     * @param requestParameterMap parameter map
     * @param workflowWizard      Workflow object
     */
    public static  void loadTemplateParameters(Map<String, String[]> requestParameterMap, WorkflowWizard workflowWizard){
        Set<String> keys = requestParameterMap.keySet();

        Map<String,Parameter> templateParameterMap = new HashMap<>();
        Template template = workflowWizard.getTemplate();
        if(template!=null) {
            ParametersMetaData parametersMetaData = template.getParametersMetaData();
            if(parametersMetaData !=null && parametersMetaData.getParameterMetaData()!=null) {
                ParameterMetaData[] parameterMetaData = parametersMetaData.getParameterMetaData();
                for (ParameterMetaData metaData : parameterMetaData) {

                    if (requestParameterMap.get(metaData.getName()) != null) {

                        String value = requestParameterMap.get(metaData.getName())[0];

                        Parameter parameter = new Parameter();
                        parameter.setParamName(metaData.getName());
                        parameter.setHolder(WorkflowUIConstants.ParameterHolder.TEMPLATE);
                        templateParameterMap.put(parameter.getParamName(), parameter);
                        parameter.setParamValue(value);
                        parameter.setQName(metaData.getName());


                    }else{
                        for (String key : keys) {
                            if (key.startsWith(metaData.getName())) {
                                Parameter parameter = new Parameter();
                                parameter.setParamName(metaData.getName());
                                parameter.setHolder(WorkflowUIConstants.ParameterHolder.TEMPLATE);
                                parameter.setQName(key);
                                templateParameterMap.put(key, parameter);
                                String[] values = requestParameterMap.get(key);
                                if (values != null && values.length > 0) {
                                    String aValue = values[0];
                                    parameter.setParamValue(aValue);
                                }
                            }

                        }

                    }
                }
            }
        }

        Collection<Parameter> values = templateParameterMap.values();
        Parameter[] parameters = values.toArray(new Parameter[values.size()]);
        workflowWizard.setTemplateParameters(parameters);
    }


    /**
     * Load implementaions related parameters of a workflow
     *
     * @param requestParameterMap parameter map
     * @param workflowWizard      Workflow object
     */
    public static  void loadWorkflowImplParameters(Map<String, String[]> requestParameterMap, WorkflowWizard workflowWizard){
        Set<String> keys = requestParameterMap.keySet();
        Parameter[] workflowImplParameters = workflowWizard.getWorkflowImplParameters();

        Map<String,Parameter> workflowImplParameterMap = new HashMap<>();
        if(workflowImplParameters!=null) {
            for (Parameter param : workflowImplParameters) {
                workflowImplParameterMap.put(param.getQName(), param);
            }
        }
        WorkflowImpl workflowImpl = workflowWizard.getWorkflowImpl();
        if(workflowImpl!=null) {
            ParametersMetaData parametersMetaData = workflowImpl.getParametersMetaData();
            if(parametersMetaData !=null && parametersMetaData.getParameterMetaData()!=null) {
                ParameterMetaData[] parameterMetaData = parametersMetaData.getParameterMetaData();
                for (ParameterMetaData metaData : parameterMetaData) {

                    if (requestParameterMap.get(metaData.getName()) != null) {

                        Parameter parameter = workflowImplParameterMap.get(metaData.getName());
                        String value = requestParameterMap.get(metaData.getName())[0];
                        if (parameter == null) {
                            parameter = new Parameter();
                            parameter.setParamName(metaData.getName());
                            parameter.setHolder(WorkflowUIConstants.ParameterHolder.WORKFLOW_IMPL);
                            workflowImplParameterMap.put(parameter.getParamName(), parameter);
                        }
                        parameter.setParamValue(value);
                        parameter.setQName(metaData.getName());


                    }else{
                        for (String key : keys) {
                            if (key.startsWith(metaData.getName())) {
                                Parameter parameter = workflowImplParameterMap.get(key);
                                if (parameter == null) {
                                    parameter = new Parameter();
                                    parameter.setParamName(metaData.getName());
                                    parameter.setHolder(WorkflowUIConstants.ParameterHolder.WORKFLOW_IMPL);
                                    parameter.setQName(key);
                                    workflowImplParameterMap.put(key, parameter);
                                }
                                String[] values = requestParameterMap.get(key);
                                if (values != null && values.length > 0) {
                                    String aValue = values[0];
                                    parameter.setParamValue(aValue);
                                }
                            }

                        }

                    }
                }
            }
        }

        Collection<Parameter> values = workflowImplParameterMap.values();
        Parameter[] parameters = values.toArray(new Parameter[values.size()]);
        workflowWizard.setWorkflowImplParameters(parameters);
    }
}
