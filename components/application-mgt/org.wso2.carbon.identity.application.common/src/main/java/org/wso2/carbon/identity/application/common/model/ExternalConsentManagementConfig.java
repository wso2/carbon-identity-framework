package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlElement;

/**
 * Representation of an ExternalConsentManagement Config.
 */
public class ExternalConsentManagementConfig implements Serializable {

    private static final long serialVersionUID = 928301275168169633L;

    private static final String ENABLED_ELEM = "Enabled";
    private static final String URL_ELEM = "ConsentUrl";

    @XmlElement(name = ENABLED_ELEM)
    private boolean enabled;

    @XmlElement(name = URL_ELEM)
    private String consentUrl;

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getExternalConsentUrl() {

        return consentUrl;
    }

    public void setExternalConsentUrl(String consentUrl) {

        this.consentUrl = consentUrl;
    }

    /**
     * Returns a ExternalConsentManagementConfig instance populated from the given OMElement
     * The OMElement is of the form below
     * <ExternalConsentManagementConfiguration>
     * <Enabled></Enabled>
     * <ConsentUrl></ConsentUrl>
     * </ExternalConsentManagementConfiguration>
     *
     * @param externalConsentManagementConfigOM OMElement to populate externalConsentManagementConfig
     * @return populated ExternalConsentManagementConfig instance
     */
    public static ExternalConsentManagementConfig build(OMElement externalConsentManagementConfigOM) {

        ExternalConsentManagementConfig externalConsentManagementConfig = new ExternalConsentManagementConfig();

        if (externalConsentManagementConfigOM == null) {
            return externalConsentManagementConfig;
        }

        Iterator<?> iterator = externalConsentManagementConfigOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            if (ENABLED_ELEM.equals(omElement.getLocalName())) {
                externalConsentManagementConfig.setEnabled(
                                Boolean.parseBoolean(omElement.getText()));
            } else if (URL_ELEM.equals(omElement.getLocalName())) {
                externalConsentManagementConfig.setExternalConsentUrl(omElement.getText());
            }
        }

        return externalConsentManagementConfig;
    }

}
