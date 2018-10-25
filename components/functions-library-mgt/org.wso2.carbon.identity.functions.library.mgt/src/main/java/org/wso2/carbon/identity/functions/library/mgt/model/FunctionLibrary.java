package org.wso2.carbon.identity.functions.library.mgt.model;

/**
 * This is the function library entity object class.
 */
public class FunctionLibrary {

    private String functionLibraryName;
    private String description;
    private String content;


    /**
     * Get function library name.
     *
     * @return function library name
     */
    public String getFunctionLibraryName() {
        return functionLibraryName;
    }

    /**
     * Set function library name.
     *
     * @param functionLibraryName function library name
     */
    public void setFunctionLibraryName(String functionLibraryName) {
        this.functionLibraryName = functionLibraryName;
    }

    /**
     * Get function library description.
     *
     * @return function library description
     */
    public String getDescription() {
        return description; }

    /**
     * Set function library description.
     *
     * @param description function library description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get function library script.
     *
     * @return content
     */
    public String getFunctionLibraryScript() {
        return content;
    }

    /**
     * Set function library script.
     *
     * @param content content
     */
    public void setFunctionLibraryScript(String content) {
        this.content = content;
    }

}


