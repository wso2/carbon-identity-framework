package org.wso2.carbon.identity.template.mgt.model;

import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

public class TemplateXDSWrapper implements XDSWrapper {

    private Template template;
    private String templateName;
    private String templateId;
    private String timestamp;

    public TemplateXDSWrapper(TemplateXDSWrapperBuilder builder) {

        this.template = builder.template;
        this.templateName = builder.templateName;
        this.templateId = builder.templateId;
        this.timestamp = builder.timestamp;
    }

    public Template getTemplate() {

        return template;
    }

    public String getTemplateName() {

        return templateName;
    }

    public String getTemplateId() {

        return templateId;
    }

    public static final class TemplateXDSWrapperBuilder {

        private Template template;
        private String templateName;
        private String templateId;
        private String timestamp;

        public TemplateXDSWrapperBuilder() {
        }

        public TemplateXDSWrapperBuilder setTemplate(Template template) {

            this.template = template;
            return this;
        }

        public TemplateXDSWrapperBuilder setTemplateName(String templateName) {

            this.templateName = templateName;
            return this;
        }

        public TemplateXDSWrapperBuilder setTemplateId(String templateId) {

            this.templateId = templateId;
            return this;
        }

        public TemplateXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new TemplateXDSWrapper(this);
        }

    }
}
