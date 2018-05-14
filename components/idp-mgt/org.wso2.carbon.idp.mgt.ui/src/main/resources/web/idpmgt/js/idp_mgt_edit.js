/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
function checkDefault(obj) {
    if (jQuery(obj).attr('id') == 'openIdDefault') {
        jQuery('#saml2SSODefault').removeAttr('checked');
        jQuery('#oidcDefault').removeAttr('checked');
        jQuery('#passiveSTSDefault').removeAttr('checked');
        jQuery('#fbAuthDefault').removeAttr('checked');
        if (jQuery('#saml2SSOEnabled').attr('checked')) {
            jQuery('#saml2SSODefault').removeAttr('disabled');
        }
        if (jQuery('#oidcEnabled').attr('checked')) {
            jQuery('#oidcDefault').removeAttr('disabled');
        }
        if (jQuery('#passiveSTSEnabled').attr('checked')) {
            jQuery('#passiveSTSDefault').removeAttr('disabled');
        }
        if (jQuery('#fbAuthEnabled').attr('checked')) {
            jQuery('#fbAuthDefault').removeAttr('disabled');
        }

        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('checked');

            if (jQuery('#' + id).attr('checked')) {
                jQuery(defId).removeAttr('disabled');
            }
        }
        jQuery('#openIdDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'saml2SSODefault') {
        jQuery('#openIdDefault').removeAttr('checked');
        jQuery('#oidcDefault').removeAttr('checked');
        jQuery('#passiveSTSDefault').removeAttr('checked');
        jQuery('#fbAuthDefault').removeAttr('checked');
        if (jQuery('#openIdEnabled').attr('checked')) {
            jQuery('#openIdDefault').removeAttr('disabled');
        }
        if (jQuery('#oidcEnabled').attr('checked')) {
            jQuery('#oidcDefault').removeAttr('disabled');
        }
        if (jQuery('#passiveSTSEnabled').attr('checked')) {
            jQuery('#passiveSTSDefault').removeAttr('disabled');
        }
        if (jQuery('#fbAuthEnabled').attr('checked')) {
            jQuery('#fbAuthDefault').removeAttr('disabled');
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('checked');

            if (jQuery('#' + id).attr('checked')) {
                jQuery(defId).removeAttr('disabled');
            }
        }
        jQuery('#saml2SSODefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'oidcDefault') {
        jQuery('#openIdDefault').removeAttr('checked');
        jQuery('#saml2SSODefault').removeAttr('checked');
        jQuery('#passiveSTSDefault').removeAttr('checked');
        jQuery('#fbAuthDefault').removeAttr('checked');
        if (jQuery('#openIdEnabled').attr('checked')) {
            jQuery('#openIdDefault').removeAttr('disabled');
        }
        if (jQuery('#saml2SSOEnabled').attr('checked')) {
            jQuery('#saml2SSODefault').removeAttr('disabled');
        }
        if (jQuery('#passiveSTSEnabled').attr('checked')) {
            jQuery('#passiveSTSDefault').removeAttr('disabled');
        }
        if (jQuery('#fbAuthEnabled').attr('checked')) {
            jQuery('#fbAuthDefault').removeAttr('disabled');
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('checked');

            if (jQuery('#' + id).attr('checked')) {
                jQuery(defId).removeAttr('disabled');
            }
        }
        jQuery('#oidcDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'passiveSTSDefault') {
        jQuery('#openIdDefault').removeAttr('checked');
        jQuery('#saml2SSODefault').removeAttr('checked');
        jQuery('#oidcDefault').removeAttr('checked');
        jQuery('#fbAuthDefault').removeAttr('checked');
        if (jQuery('#openIdEnabled').attr('checked')) {
            jQuery('#openIdDefault').removeAttr('disabled');
        }
        if (jQuery('#saml2SSOEnabled').attr('checked')) {
            jQuery('#saml2SSODefault').removeAttr('disabled');
        }
        if (jQuery('#oidcEnabled').attr('checked')) {
            jQuery('#oidcDefault').removeAttr('disabled');
        }
        if (jQuery('#fbAuthEnabled').attr('checked')) {
            jQuery('#fbAuthDefault').removeAttr('disabled');
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('checked');

            if (jQuery('#' + id).attr('checked')) {
                jQuery(defId).removeAttr('disabled');
            }
        }
        jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'fbAuthDefault') {
        jQuery('#openIdDefault').removeAttr('checked');
        jQuery('#saml2SSODefault').removeAttr('checked');
        jQuery('#oidcDefault').removeAttr('checked');
        jQuery('#passiveSTSDefault').removeAttr('checked');
        if (jQuery('#openIdEnabled').attr('checked')) {
            jQuery('#openIdDefault').removeAttr('disabled');
        }
        if (jQuery('#saml2SSOEnabled').attr('checked')) {
            jQuery('#saml2SSODefault').removeAttr('disabled');
        }
        if (jQuery('#oidcEnabled').attr('checked')) {
            jQuery('#oidcDefault').removeAttr('disabled');
        }
        if (jQuery('#passiveSTSEnabled').attr('checked')) {
            jQuery('#passiveSTSDefault').removeAttr('disabled');
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('checked');

            if (jQuery('#' + id).attr('checked')) {
                jQuery(defId).removeAttr('disabled');
            }
        }
        jQuery('#fbAuthDefault').attr('disabled', 'disabled');
    } else {
        for (id in getEnabledCustomAuth()) {
            var defId = id.replace("_Enabled", "_Default");
            if (jQuery(obj).attr('id') == defId) {
                jQuery('#openIdDefault').removeAttr('checked');
                jQuery('#saml2SSODefault').removeAttr('checked');
                jQuery('#oidcDefault').removeAttr('checked');
                jQuery('#passiveSTSDefault').removeAttr('checked');
                jQuery('#fbAuthDefault').removeAttr('checked');

                if (jQuery('#openIdEnabled').attr('checked')) {
                    jQuery('#openIdDefault').removeAttr('disabled');
                }
                if (jQuery('#saml2SSOEnabled').attr('checked')) {
                    jQuery('#saml2SSODefault').removeAttr('disabled');
                }
                if (jQuery('#oidcEnabled').attr('checked')) {
                    jQuery('#oidcDefault').removeAttr('disabled');
                }
                if (jQuery('#passiveSTSEnabled').attr('checked')) {
                    jQuery('#passiveSTSDefault').removeAttr('disabled');
                }
                if (jQuery('#fbAuthEnabled').attr('checked')) {
                    jQuery('#fbAuthDefault').removeAttr('disabled');
                }

                for (idE in getEnabledCustomAuth()) {
                    var defIdE = idE.replace("_Enabled", "_Default");

                    if (jQuery(obj).attr('id') == defIdE) {
                        //Nothing do
                    }
                    else {
                        jQuery('#' + defIdE).removeAttr('checked');
                        if (jQuery('#' + idE).attr('checked')) {
                            jQuery('#' + defIdE).removeAttr('disabled');
                        }
                    }
                }

                jQuery('#' + defId).attr('disabled', 'disabled');
            }
        }
    }
}


function deleteRow(obj) {
    jQuery(obj).parent().parent().remove();

}


function disableDefaultPwd(chkbx) {
    document.getElementById("scim-default-pwd").value = "";
    var disabled = chkbx.checked;
    document.getElementById("scim-default-pwd").disabled = disabled;
}
var deletedRoleRows = [];
function deleteRoleRow(obj) {
    if (jQuery(obj).parent().prev().children()[0].value != '') {
        deletedRoleRows.push(jQuery(obj).parent().prev().children()[0].value);
    }
    jQuery(obj).parent().parent().remove();

}


function deleteRows() {
    $.each($('.claimrow'), function () {
        $(this).parent().parent().remove();
    });
}

function checkEnabledLogo(obj, name) {
    if (jQuery(obj).attr('checked')) {
        jQuery('#custom_auth_head_enable_logo_' + name).show();
    } else {
        jQuery('#custom_auth_head_enable_logo_' + name).hide();
    }

}

function getEnabledCustomAuth() {
    var textMap = {};

    jQuery("input[name$='_Enabled']").each(function () {
        textMap[this.id] = $(this).text();
    });

    return textMap;
}

function isCustomAuthEnabled() {
    var enable = false;
    for (id in getEnabledCustomAuth()) {
        if (jQuery('#' + id).attr('checked')) {
            enable = true;
        }
    }
    return enable;
}


function isOtherCustomAuthEnabled(selectedId) {
    var enable = false;
    for (id in getEnabledCustomAuth()) {
        if (id == selectedId) {
            //other than selected
        } else {
            if (jQuery('#' + id).attr('checked')) {
                enable = true;
            }
        }
    }
    return enable;
}


function checkEnabled(obj) {

    if (jQuery(obj).attr('checked')) {
        if (jQuery(obj).attr('id') == 'openIdEnabled') {
            if (!jQuery('#saml2SSOEnabled').attr('checked') && !jQuery('#oidcEnabled').attr('checked') && !jQuery('#passiveSTSEnabled').attr('checked') && !jQuery('#fbAuthEnabled').attr('checked') && !isCustomAuthEnabled()) {
                jQuery('#openIdDefault').attr('checked', 'checked');
                jQuery('#openIdDefault').attr('disabled', 'disabled');


            } else {
                jQuery('#openIdDefault').removeAttr('disabled');
            }

            jQuery('#openid_enable_logo').show();
        } else if (jQuery(obj).attr('id') == 'saml2SSOEnabled') {
            if (!jQuery('#openIdEnabled').attr('checked') && !jQuery('#oidcEnabled').attr('checked') && !jQuery('#passiveSTSEnabled').attr('checked') && !jQuery('#fbAuthEnabled').attr('checked') && !isCustomAuthEnabled()) {
                jQuery('#saml2SSODefault').attr('checked', 'checked');
                jQuery('#saml2SSODefault').attr('disabled', 'disabled');
            } else {
                jQuery('#saml2SSODefault').removeAttr('disabled');
            }
            jQuery('#sampl2sso_enable_logo').show();
        } else if (jQuery(obj).attr('id') == 'oidcEnabled') {
            if (!jQuery('#openIdEnabled').attr('checked') && !jQuery('#saml2SSOEnabled').attr('checked') && !jQuery('#passiveSTSEnabled').attr('checked') && !jQuery('#fbAuthEnabled').attr('checked') && !isCustomAuthEnabled()) {
                jQuery('#oidcDefault').attr('checked', 'checked');
                jQuery('#oidcDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#oidcDefault').removeAttr('disabled');
            }
            jQuery('#oAuth2_enable_logo').show();
        } else if (jQuery(obj).attr('id') == 'passiveSTSEnabled') {
            if (!jQuery('#saml2SSOEnabled').attr('checked') && !jQuery('#oidcEnabled').attr('checked') && !jQuery('#openIdEnabled').attr('checked') && !jQuery('#fbAuthEnabled').attr('checked') && !isCustomAuthEnabled()) {
                jQuery('#passiveSTSDefault').attr('checked', 'checked');
                jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#passiveSTSDefault').removeAttr('disabled');
            }
            jQuery('#wsfederation_enable_logo').show();
        } else if (jQuery(obj).attr('id') == 'fbAuthEnabled') {
            if (!jQuery('#saml2SSOEnabled').attr('checked') && !jQuery('#oidcEnabled').attr('checked') && !jQuery('#passiveSTSEnabled').attr('checked') && !jQuery('#openIdEnabled').attr('checked') && !isCustomAuthEnabled()) {
                jQuery('#fbAuthDefault').attr('checked', 'checked');
                jQuery('#fbAuthDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#fbAuthDefault').removeAttr('disabled');
            }
            jQuery('#fecebook_enable_logo').show();
        } else {
            for (id in getEnabledCustomAuth()) {
                if (jQuery(obj).attr('id') == id) {
                    var defId = '#' + id.replace("_Enabled", "_Default");
                    if (!jQuery('#saml2SSOEnabled').attr('checked') && !jQuery('#oidcEnabled').attr('checked') && !jQuery('#passiveSTSEnabled').attr('checked') && !jQuery('#openIdEnabled').attr('checked') && !jQuery('#fbAuthEnabled').attr('checked') && !isOtherCustomAuthEnabled(id)) {
                        jQuery(defId).attr('checked', 'checked');
                        jQuery(defId).attr('disabled', 'disabled');
                    } else {
                        jQuery(defId).removeAttr('disabled');
                    }
                }
            }
        }
    } else {
        if (jQuery(obj).attr('id') == 'openIdEnabled') {
            if (jQuery('#saml2SSOEnabled').attr('checked') ||
                jQuery('#passiveSTSEnabled').attr('checked') ||
                jQuery('#oidcEnabled').attr('checked') ||
                jQuery('#fbAuthEnabled').attr('checked') || isCustomAuthEnabled()) {

                if (jQuery('#openIdDefault').attr('checked')) {
                    jQuery('#openIdEnabled').attr('checked', 'checked');
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#openIdDefault').attr('disabled', 'disabled');
                    jQuery('#openIdDefault').removeAttr('checked');
                    jQuery('#openid_enable_logo').hide();
                }
            } else {
                jQuery('#openIdDefault').attr('disabled', 'disabled');
                jQuery('#openIdDefault').removeAttr('checked');
                jQuery('#openid_enable_logo').hide();
            }


        } else if (jQuery(obj).attr('id') == 'saml2SSOEnabled') {

            if (jQuery('#openIdEnabled').attr('checked') ||
                jQuery('#passiveSTSEnabled').attr('checked') ||
                jQuery('#oidcEnabled').attr('checked') ||
                jQuery('#fbAuthEnabled').attr('checked') || isCustomAuthEnabled()) {

                if (jQuery('#saml2SSODefault').attr('checked')) {
                    jQuery('#saml2SSOEnabled').attr('checked', 'checked');
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#saml2SSODefault').attr('disabled', 'disabled');
                    jQuery('#saml2SSODefault').removeAttr('checked');
                    jQuery('#sampl2sso_enable_logo').hide();
                }
            } else {
                jQuery('#saml2SSODefault').attr('disabled', 'disabled');
                jQuery('#saml2SSODefault').removeAttr('checked');
                jQuery('#sampl2sso_enable_logo').hide();
            }

        } else if (jQuery(obj).attr('id') == 'oidcEnabled') {

            if (jQuery('#saml2SSOEnabled').attr('checked') ||
                jQuery('#passiveSTSEnabled').attr('checked') ||
                jQuery('#openIdEnabled').attr('checked') ||
                jQuery('#fbAuthEnabled').attr('checked') || isCustomAuthEnabled()) {

                if (jQuery('#oidcDefault').attr('checked')) {
                    jQuery('#oidcEnabled').attr('checked', 'checked');
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#oidcDefault').attr('disabled', 'disabled');
                    jQuery('#oidcDefault').removeAttr('checked');
                    jQuery('#oAuth2_enable_logo').hide();
                }
            } else {
                jQuery('#oidcDefault').attr('disabled', 'disabled');
                jQuery('#oidcDefault').removeAttr('checked');
                jQuery('#oAuth2_enable_logo').hide();
            }
        } else if (jQuery(obj).attr('id') == 'passiveSTSEnabled') {

            if (jQuery('#saml2SSOEnabled').attr('checked') ||
                jQuery('#oidcEnabled').attr('checked') ||
                jQuery('#openIdEnabled').attr('checked') ||
                jQuery('#fbAuthEnabled').attr('checked') || isCustomAuthEnabled()) {

                if (jQuery('#passiveSTSDefault').attr('checked')) {
                    jQuery('#passiveSTSEnabled').attr('checked', 'checked');
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
                    jQuery('#passiveSTSDefault').removeAttr('checked');
                    jQuery('#wsfederation_enable_logo').hide();
                }
            } else {
                jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
                jQuery('#passiveSTSDefault').removeAttr('checked');
                jQuery('#wsfederation_enable_logo').hide();
            }

        } else if (jQuery(obj).attr('id') == 'fbAuthEnabled') {

            if (jQuery('#saml2SSOEnabled').attr('checked') ||
                jQuery('#oidcEnabled').attr('checked') ||
                jQuery('#openIdEnabled').attr('checked') ||
                jQuery('#passiveSTSEnabled').attr('checked') || isCustomAuthEnabled()) {

                if (jQuery('#fbAuthDefault').attr('checked')) {
                    jQuery('#fbAuthEnabled').attr('checked', 'checked');
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#fbAuthDefault').attr('disabled', 'disabled');
                    jQuery('#fbAuthDefault').removeAttr('checked');
                    jQuery('#fecebook_enable_logo').hide();
                }
            } else {
                jQuery('#fbAuthDefault').attr('disabled', 'disabled');
                jQuery('#fbAuthDefault').removeAttr('checked');
                jQuery('#fecebook_enable_logo').hide();
            }
        } else {
            for (id in getEnabledCustomAuth()) {
                if (jQuery(obj).attr('id') == id) {
                    var defId = '#' + id.replace("_Enabled", "_Default");
                    if (jQuery('#saml2SSOEnabled').attr('checked') ||
                        jQuery('#oidcEnabled').attr('checked') ||
                        jQuery('#passiveSTSEnabled').attr('checked') ||
                        jQuery('#openIdEnabled').attr('checked') ||
                        jQuery('#fbAuthEnabled').attr('checked') || isOtherCustomAuthEnabled(id)) {

                        if (jQuery(defId).attr('checked')) {
                            jQuery('#' + id).attr('checked', 'checked');
                            CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                        } else {
                            jQuery(defId).attr('disabled', 'disabled');
                            jQuery(defId).removeAttr('checked');
                        }
                    } else {
                        jQuery(defId).attr('disabled', 'disabled');
                        jQuery(defId).removeAttr('checked');
                    }
                }
            }
        }
    }
}



function checkProvEnabled(obj) {

    if (jQuery(obj).attr('checked')) {
        if (jQuery(obj).attr('id') == 'googleProvEnabled') {

            if (!jQuery('#sfProvEnabled').attr('checked') && !jQuery('#scimProvEnabled').attr('checked') && !jQuery('#spmlProvEnabled').attr('checked')) {

                jQuery('#googleProvDefault').attr('checked', 'checked');
                jQuery('#googleProvDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#googleProvDefault').removeAttr('disabled');
            }

            jQuery('#google_enable_logo').show();

        } else if (jQuery(obj).attr('id') == 'sfProvEnabled') {

            if (!jQuery('#googleProvEnabled').attr('checked') && !jQuery('#scimProvEnabled').attr('checked') && !jQuery('#spmlProvEnabled').attr('checked')) {

                jQuery('#sfProvDefault').attr('checked', 'checked');
                jQuery('#sfProvDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#sfProvDefault').removeAttr('disabled');
            }

            jQuery('#sf_enable_logo').show();

        } else if (jQuery(obj).attr('id') == 'scimProvEnabled') {

            if (!jQuery('#googleProvEnabled').attr('checked') && !jQuery('#sfProvEnabled').attr('checked') && !jQuery('#spmlProvEnabled').attr('checked')) {

                jQuery('#scimProvDefault').attr('checked', 'checked');
                jQuery('#scimProvDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#scimProvDefault').removeAttr('disabled');
            }

            jQuery('#scim_enable_logo').show();

        } else if (jQuery(obj).attr('id') == 'spmlProvEnabled') {

            if (!jQuery('#googleProvEnabled').attr('checked') && !jQuery('#sfProvEnabled').attr('checked') && !jQuery('#scimProvEnabled').attr('checked')) {

                jQuery('#spmlProvDefault').attr('checked', 'checked');
                jQuery('#spmlProvDefault').attr('disabled', 'disabled');
            } else {
                jQuery('#spmlProvDefault').removeAttr('disabled');
            }

            jQuery('#spml_enable_logo').show();
        }
    } else {
        if (jQuery(obj).attr('id') == 'googleProvEnabled') {

            if (jQuery('#sfProvEnabled').attr('checked') ||
                jQuery('#spmlProvEnabled').attr('checked') ||
                jQuery('#scimProvEnabled').attr('checked')) {

                if (jQuery('#googleProvDefault').attr('checked')) {
                    //jQuery('#googleProvEnabled').attr('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#googleProvDefault').attr('disabled', 'disabled');
                    jQuery('#googleProvDefault').removeAttr('checked');
                    jQuery('#google_enable_logo').hide();
                }
            } else {
                jQuery('#googleProvDefault').attr('disabled', 'disabled');
                jQuery('#googleProvDefault').removeAttr('checked');
                jQuery('#google_enable_logo').hide();
            }

        } else if (jQuery(obj).attr('id') == 'sfProvEnabled') {

            if (jQuery('#googleProvEnabled').attr('checked') ||
                jQuery('#spmlProvEnabled').attr('checked') ||
                jQuery('#scimProvEnabled').attr('checked')) {

                if (jQuery('#sfProvDefault').attr('checked')) {
                    // jQuery('#sfProvEnabled').attr('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#sfProvDefault').attr('disabled', 'disabled');
                    jQuery('#sfProvDefault').removeAttr('checked');
                    jQuery('#sf_enable_logo').hide();
                }
            } else {
                jQuery('#sfProvDefault').attr('disabled', 'disabled');
                jQuery('#sfProvDefault').removeAttr('checked');
                jQuery('#sf_enable_logo').hide();
            }

        } else if (jQuery(obj).attr('id') == 'scimProvEnabled') {

            if (jQuery('#sfProvEnabled').attr('checked') ||
                jQuery('#spmlProvEnabled').attr('checked') ||
                jQuery('#googleProvEnabled').attr('checked')) {

                if (jQuery('#scimProvDefault').attr('checked')) {
                    // jQuery('#scimProvEnabled').attr('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#scimProvDefault').attr('disabled', 'disabled');
                    jQuery('#scimProvDefault').removeAttr('checked');
                    jQuery('#scim_enable_logo').hide();
                }
            } else {
                jQuery('#scimProvDefault').attr('disabled', 'disabled');
                jQuery('#scimProvDefault').removeAttr('checked');
                jQuery('#scim_enable_logo').hide();
            }

        } else if (jQuery(obj).attr('id') == 'spmlProvEnabled') {

            if (jQuery('#sfProvEnabled').attr('checked') ||
                jQuery('#scimProvEnabled').attr('checked') ||
                jQuery('#googleProvEnabled').attr('checked')) {

                if (jQuery('#spmlProvDefault').attr('checked')) {
                    // jQuery('#spmlProvEnabled').attr('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#spmlProvDefault').attr('disabled', 'disabled');
                    jQuery('#spmlProvDefault').removeAttr('checked');
                    jQuery('#spml_enable_logo').hide();
                }
            } else {
                jQuery('#spmlProvDefault').attr('disabled', 'disabled');
                jQuery('#spmlProvDefault').removeAttr('checked');
                jQuery('#spml_enable_logo').hide();
            }
        }
    }
}

function checkProvDefault(obj) {
    if (jQuery(obj).attr('id') == 'googleProvDefault') {
        jQuery('#sfProvDefault').removeAttr('checked');
        jQuery('#scimProvDefault').removeAttr('checked');
        jQuery('#spmlProvDefault').removeAttr('checked');
        if (jQuery('#sfProvEnabled').attr('checked')) {
            jQuery('#sfProvDefault').removeAttr('disabled');
        }
        if (jQuery('#scimProvEnabled').attr('checked')) {
            jQuery('#scimProvDefault').removeAttr('disabled');
        }
        if (jQuery('#spmlProvEnabled').attr('checked')) {
            jQuery('#spmlProvDefault').removeAttr('disabled');
        }
        jQuery('#googleProvDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'sfProvDefault') {
        jQuery('#googleProvDefault').removeAttr('checked');
        jQuery('#scimProvDefault').removeAttr('checked');
        jQuery('#spmlProvDefault').removeAttr('checked');
        if (jQuery('#googleProvEnabled').attr('checked')) {
            jQuery('#googleProvDefault').removeAttr('disabled');
        }
        if (jQuery('#scimProvEnabled').attr('checked')) {
            jQuery('#scimProvDefault').removeAttr('disabled');
        }
        if (jQuery('#spmlProvEnabled').attr('checked')) {
            jQuery('#spmlProvDefault').removeAttr('disabled');
        }
        jQuery('#sfProvDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'scimProvDefault') {
        jQuery('#googleProvDefault').removeAttr('checked');
        jQuery('#sfProvDefault').removeAttr('checked');
        jQuery('#spmlProvDefault').removeAttr('checked');
        if (jQuery('#googleProvEnabled').attr('checked')) {
            jQuery('#googleProvDefault').removeAttr('disabled');
        }
        if (jQuery('#spmlProvEnabled').attr('checked')) {
            jQuery('#spmlProvDefault').removeAttr('disabled');
        }
        if (jQuery('#sfProvEnabled').attr('checked')) {
            jQuery('#sfProvDefault').removeAttr('disabled');
        }
        jQuery('#scimProvDefault').attr('disabled', 'disabled');
    } else if (jQuery(obj).attr('id') == 'spmlProvDefault') {
        jQuery('#googleProvDefault').removeAttr('checked');
        jQuery('#sfProvDefault').removeAttr('checked');
        jQuery('#scimProvDefault').removeAttr('checked');
        if (jQuery('#openIdEnabled').attr('checked')) {
            jQuery('#googleProvDefault').removeAttr('disabled');
        }
        if (jQuery('#googleProvEnabled').attr('checked')) {
            jQuery('#sfProvDefault').removeAttr('disabled');
        }
        if (jQuery('#scimProvEnabled').attr('checked')) {
            jQuery('#scimProvDefault').removeAttr('disabled');
        }
        jQuery('#spmlProvDefault').attr('disabled', 'disabled');
    }
}

function idpMgtCancel() {
    location.href = "idp-mgt-list.jsp"
}

function showHidePassword(element, inputId) {
    if ($(element).text() == 'Show') {
        document.getElementById(inputId).type = 'text';
        $(element).text('Hide');
    } else {
        document.getElementById(inputId).type = 'password';
        $(element).text('Show');
    }
}

function emailValidator(name) {
    var errorMsg = "";
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    if (name == "") {
        errorMsg = "null";
    } else if (!name.match(new RegExp(emailPattern))) {
        errorMsg = "notValied";
    }
    return errorMsg;
}

function doValidation() {
    var reason = "";
    reason = validateEmpty("idPName");
    if (reason != "") {
        CARBON.showWarningDialog("Name of IdP cannot be empty");
        return false;
    }

    if (jQuery('#openIdEnabled').attr('checked')) {

        if ($('#openIdUrl').val() == "") {
            CARBON.showWarningDialog('OpenID Server URL cannot be empty');
            return false;
        }
    }

    if (jQuery('#saml2SSOEnabled').attr('checked')) {

        if ($('#meta_data_saml').val() == "") {

            if ($('#idPEntityId').val() == "") {
                CARBON.showWarningDialog('Identity Provider Entity Id cannot be empty');
                return false;
            }
            if ($('#ssoUrl').val() == "") {
                CARBON.showWarningDialog('SSO URL cannot be empty');
                return false;
            }
        }

        if ($('#spEntityId').val() == "") {
            CARBON.showWarningDialog('Service Provider Entity Id cannot be empty');
            return false;
        }


    }

    if ($('#meta_data_saml').val() != ""  && !jQuery('#saml2SSOEnabled').attr('checked') ) {
        if ($('#spEntityId').val() == "") {
            CARBON.showWarningDialog('Service Provider Entity Id cannot be empty');
            return false;
        }

    }

    if (jQuery('#oidcEnabled').attr('checked')) {

        if ($('#authzUrl').val() == "") {
            CARBON.showWarningDialog('OAuth2/OpenId  Authorization Endpoint URL cannot be empty');
            return false;
        }

        if ($('#tokenUrl').val() == "") {
            CARBON.showWarningDialog('OAuth2/OpenId Token Endpoint URL cannot be empty');
            return false;
        }

        if ($('#clientId').val() == "") {
            CARBON.showWarningDialog('OAuth2/OpenId Client Id cannot be empty');
            return false;
        }

        if ($('#clientSecret').val() == "") {
            CARBON.showWarningDialog('OAuth2/OpenId Client Secret cannot be empty');
            return false;
        }

    }

    if (jQuery('#passiveSTSEnabled').attr('checked')) {

        if ($('#passiveSTSRealm').val() == "") {
            CARBON.showWarningDialog('Passive STS Realm cannot be empty');
            return false;
        }

        if ($('#passiveSTSUrl').val() == "") {
            CARBON.showWarningDialog('Passive STS URL cannot be empty');
            return false;
        }
    }

    if (jQuery('#fbAuthEnabled').attr('checked')) {

        if ($('#fbClientId').val() == "") {
            CARBON.showWarningDialog('Facebook Client Id cannot be empty');
            return false;
        }

        if ($('#fbClientSecret').val() == "") {
            CARBON.showWarningDialog('Facebook Client Secret cannot be empty');
            return false;
        }
    }


    if (jQuery('#googleProvEnabled').attr('checked')) {

        if ($('#google_prov_domain_name').val() == "") {
            CARBON.showWarningDialog('Google Domain cannot be empty');
            return false;
        }


        var errorMsg = emailValidator($('#google_prov_service_acc_email').val());
        if (errorMsg == "null") {
            CARBON.showWarningDialog('Google connector Service Account Email cannot be empty');
            return false;
        } else if (errorMsg == "notValied") {
            CARBON.showWarningDialog('Google connector Service Account Email is not valid');
            return false;
        }

        var errorMsgAdmin = emailValidator($('#google_prov_admin_email').val());
        if (errorMsgAdmin == "null") {
            CARBON.showWarningDialog('Google connector Administrator\'s Email cannot be empty');
            return false;
        } else if (errorMsgAdmin == "notValied") {
            CARBON.showWarningDialog('Google connector Administrator\'s Email is not valid');
            return false;
        }


        if ($('#google_prov_application_name').val() == "") {
            CARBON.showWarningDialog('Google connector Application Name cannot be empty');
            return false;
        }

        if ($('#google_prov_email_claim_dropdown').val() == "") {
            CARBON.showWarningDialog('Google connector Primary Email claim URI should be selected ');
            return false;
        }

        if ($('#google_prov_givenname_claim_dropdown').val() == "") {
            CARBON.showWarningDialog('Google connector Given Name claim URI should be selected ');
            return false;
        }

        if ($('#google_prov_familyname_claim_dropdown').val() == "") {
            CARBON.showWarningDialog('Google connector Family Name claim URI should be selected ');
            return false;
        }

    }

    if (jQuery('#sfProvEnabled').attr('checked')) {

        if ($('#sf-api-version').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration API version cannot be empty');
            return false;
        }

        if ($('#sf-domain-name').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Domain Name cannot be empty');
            return false;
        }

        if ($('#sf-clientid').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Client Id cannot be empty');
            return false;
        }

        if ($('#sf-client-secret').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Client Secret cannot be empty');
            return false;
        }

        if ($('#sf-username').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Username cannot be empty');
            return false;
        }

        if ($('#sf-password').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Password cannot be empty');
            return false;
        }

        if ($('#sf-token-endpoint').val() == "") {
            CARBON.showWarningDialog('Salesforce Provisioning Configuration Oauth2 Token Endpoint cannot be empty');
            return false;
        }
    }


    if (jQuery('#scimProvEnabled').attr('checked')) {

        if ($('#scim-username').val() == "") {
            CARBON.showWarningDialog('Scim Configuration username cannot be empty');
            return false;
        }

        if ($('#scim-password').val() == "") {
            CARBON.showWarningDialog('Scim Configuration password cannot be empty');
            return false;
        }

        if ($('#scim-user-ep').val() == "") {
            CARBON.showWarningDialog('Scim Configuration User endpoint cannot be empty');
            return false;
        }
    }

    if (jQuery('#spmlProvEnabled').attr('checked')) {

        if ($('#spml-ep').val() == "") {
            CARBON.showWarningDialog('SPML Endpoint cannot be empty');
            return false;
        }

        if ($('#spml-oc').val() == "") {
            CARBON.showWarningDialog('SPML Object class cannot be empty');
            return false;
        }

    }

    for (var i = 0; i <= claimRowId; i++) {
        if (document.getElementsByName('claimrowname_' + i)[0] != null) {
            reason = validateEmpty('claimrowname_' + i);
            if (reason != "") {
                CARBON.showWarningDialog("Claim URI strings cannot be of zero length");
                return false;
            }
        }
    }

    for (var i = 0; i <= roleRowId; i++) {
        if (document.getElementsByName('rolerowname_' + i)[0] != null) {
            reason = validateEmpty('rolerowname_' + i);
            if (reason != "") {
                CARBON.showWarningDialog("Role name strings cannot be of zero length");
                return false;
            }
        }
    }

    return true;
}

var deleteClaimRows = [];
function deleteClaimRow(obj) {
    if (jQuery(obj).parent().prev().children()[0].value != '') {
        deleteClaimRows.push(jQuery(obj).parent().prev().children()[0].value);
    }
    jQuery(obj).parent().parent().remove();
    if ($(jQuery('#claimAddTable tr')).length == 1) {
        $(jQuery('#claimAddTable')).toggle();
    }
    claimURIDropdownPopulator();
}

function htmlEncode(value) {
    // Create a in-memory div, set it's inner text(which jQuery automatically encodes)
    // then grab the encoded contents back out.  The div never exists on the page.
    var output = $('<div/>').text(value).html();
    output = output.replace(/"/g,"&quot;");
    output = output.replace(/'/g,'&#39;');

    return output;
}

jQuery('#includeAuthnCtxReq').click(function () {
    jQuery('#authentication_context_class_dropdown').attr('disabled', true);
    jQuery('#auth_context_comparison_level_dropdown').attr('disabled', true);
});


jQuery('#logoutRequestSigned').click(function () {
    if (jQuery(this).is(":checked") || jQuery("#authnRequestSigned").is(":checked")) {
        jQuery('#signature_algorithem_dropdown').removeAttr('disabled');
        jQuery('#digest_algorithem_dropdown').removeAttr('disabled');
    } else {
        jQuery('#signature_algorithem_dropdown').attr('disabled', true);
        jQuery('#digest_algorithem_dropdown').attr('disabled', true);
    }
});

jQuery('#includeAuthnCtxNo').click(function () {
    jQuery('#authentication_context_class_dropdown').attr('disabled', true);
    jQuery('#auth_context_comparison_level_dropdown').attr('disabled', true);
});

jQuery('#includeAuthnCtxYes').click(function () {
    jQuery('#authentication_context_class_dropdown').removeAttr('disabled');
    jQuery('#auth_context_comparison_level_dropdown').removeAttr('disabled');
});

jQuery('#authnRequestSigned').click(function () {
    if (jQuery(this).is(":checked") || jQuery("#logoutRequestSigned").is(":checked")) {
        jQuery('#signature_algorithem_dropdown').removeAttr('disabled');
        jQuery('#digest_algorithem_dropdown').removeAttr('disabled');
    } else {
        jQuery('#signature_algorithem_dropdown').attr('disabled', true);
        jQuery('#digest_algorithem_dropdown').attr('disabled', true);
    }
});

jQuery('#claimAddTable .claimrow').blur(function () {
    claimURIDropdownPopulator();
});
jQuery('#claimMappingDeleteLink').click(function () {
    $(jQuery('#claimMappingDiv')).toggle();
    var input = document.createElement('input');
    input.type = "hidden";
    input.name = "deleteClaimMappings";
    input.id = "deleteClaimMappings";
    input.value = "true";
    document.forms['idp-mgt-edit-form'].appendChild(input);
});

jQuery('#roleMappingDeleteLink').click(function () {
    $(jQuery('#roleMappingDiv')).toggle();
    var input = document.createElement('input');
    input.type = "hidden";
    input.name = "deleteRoleMappings";
    input.id = "deleteRoleMappings";
    input.value = "true";
    document.forms['idp-mgt-edit-form'].appendChild(input);
});
jQuery('#provision_disabled').click(function () {
    jQuery('#provision_static_dropdown').attr('disabled', 'disabled');
    $('input[name=choose_jit_type_group]').attr('disabled', 'disabled');
});
jQuery('#provision_static').click(function () {
    jQuery('#provision_static_dropdown').removeAttr('disabled');
    $('input[name=choose_jit_type_group]').removeAttr('disabled');
});

jQuery('#password_provisioning').click(function () {
    jQuery('#modify_username').removeAttr('disabled');
});


jQuery('#choose_dialet_type1').click(function () {
    $(".customClaim").hide();
    $(".role_claim").hide();
    deleteRows();
    claimURIDropdownPopulator();
    $("#advancedClaimMappingAddTable tbody > tr").remove();
    $('#advancedClaimMappingAddTable').hide();

});

jQuery('#choose_dialet_type2').click(function () {
    $(".customClaim").show();
    $(".role_claim").show();
    $("#advancedClaimMappingAddTable tbody > tr").remove();
    $('#advancedClaimMappingAddTable').hide();
    claimURIDropdownPopulator();
});


jQuery('#advancedClaimMappingAddLink').click(function () {
    var selectedIDPClaimName = $('select[name=idpClaimsList2]').val();
    if (selectedIDPClaimName == "" || selectedIDPClaimName == null) {
        CARBON.showWarningDialog('Add valid attribute');
        return false;
    }
    advancedClaimMappinRowID++;
    $("#advanced_claim_id_count").val(advancedClaimMappinRowID + 1);
    jQuery('#advancedClaimMappingAddTable').append(jQuery('<tr>' +
        '<td><input type="text" style="width: 99%;" value="' + selectedIDPClaimName + '" id="advancnedIdpClaim_' + advancedClaimMappinRowID + '" name="advancnedIdpClaim_' + advancedClaimMappinRowID + '" readonly="readonly" /></td>' +
        '<td><input type="text" style="width: 99%;" id="advancedDefault_' + advancedClaimMappinRowID + '" name="advancedDefault_' + advancedClaimMappinRowID + '"/></td> ' +
        '<td><a onclick="deleteRow(this);return false;" href="#" class="icon-link" style="background-image: url(../images/delete.gif)"> Delete</a></td>' +

        '</tr>'));

    $(jQuery('#advancedClaimMappingAddTable')).show();

});


jQuery('#roleAddLink').click(function () {
    roleRowId++;
    $("#rolemappingrow_id_count").val(roleRowId + 1);
    jQuery('#roleAddTable').append(jQuery('<tr><td><input type="text" id="rolerowname_' + roleRowId + '" name="rolerowname_' + roleRowId + '"/></td>' +
        '<td><input type="text" id="localrowname_' + roleRowId + '" name="localrowname_' + roleRowId + '"/></td>' +
        '<td><a onclick="deleteRoleRow(this)" class="icon-link" ' +
        'style="background-image: url(../images/delete.gif)">' +
        'Delete' +
        '</a></td></tr>'));
    if ($(jQuery('#roleAddTable tr')).length == 2) {
        $(jQuery('#roleAddTable')).toggle();
    }
});
