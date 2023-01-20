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
    if (jQuery(obj).prop('id') == 'openIdDefault') {
        jQuery('#saml2SSODefault').prop( "checked", false );
        jQuery('#oidcDefault').prop( "checked", false );
        jQuery('#passiveSTSDefault').prop( "checked", false );
        jQuery('#fbAuthDefault').prop( "checked", false );
        if (jQuery('#saml2SSOEnabled').prop('checked')) {
            jQuery('#saml2SSODefault').prop( "disabled", false );
        }
        if (jQuery('#oidcEnabled').prop('checked')) {
            jQuery('#oidcDefault').prop( "disabled", false );
        }
        if (jQuery('#passiveSTSEnabled').prop('checked')) {
            jQuery('#passiveSTSDefault').prop( "disabled", false );
        }
        if (jQuery('#fbAuthEnabled').prop('checked')) {
            jQuery('#fbAuthDefault').prop( "disabled", false );
        }

        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).prop( "checked", false );

            if (jQuery('#' + id).prop('checked')) {
                jQuery(defId).prop( "disabled", false );
            }
        }
        jQuery('#openIdDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'saml2SSODefault') {
        jQuery('#openIdDefault').prop( "checked", false );
        jQuery('#oidcDefault').prop( "checked", false );
        jQuery('#passiveSTSDefault').prop( "checked", false );
        jQuery('#fbAuthDefault').prop( "checked", false );
        if (jQuery('#openIdEnabled').prop('checked')) {
            jQuery('#openIdDefault').prop( "disabled", false );
        }
        if (jQuery('#oidcEnabled').prop('checked')) {
            jQuery('#oidcDefault').prop( "disabled", false );
        }
        if (jQuery('#passiveSTSEnabled').prop('checked')) {
            jQuery('#passiveSTSDefault').prop( "disabled", false );
        }
        if (jQuery('#fbAuthEnabled').prop('checked')) {
            jQuery('#fbAuthDefault').prop( "disabled", false );
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).prop( "checked", false );

            if (jQuery('#' + id).prop('checked')) {
                jQuery(defId).prop( "disabled", false );
            }
        }
        jQuery('#saml2SSODefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'oidcDefault') {
        jQuery('#openIdDefault').prop( "checked", false );
        jQuery('#saml2SSODefault').prop( "checked", false );
        jQuery('#passiveSTSDefault').prop( "checked", false );
        jQuery('#fbAuthDefault').prop( "checked", false );
        if (jQuery('#openIdEnabled').prop('checked')) {
            jQuery('#openIdDefault').prop( "disabled", false );
        }
        if (jQuery('#saml2SSOEnabled').prop('checked')) {
            jQuery('#saml2SSODefault').prop( "disabled", false );
        }
        if (jQuery('#passiveSTSEnabled').prop('checked')) {
            jQuery('#passiveSTSDefault').prop( "disabled", false );
        }
        if (jQuery('#fbAuthEnabled').prop('checked')) {
            jQuery('#fbAuthDefault').prop( "disabled", false );
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).prop( "checked", false );

            if (jQuery('#' + id).prop('checked')) {
                jQuery(defId).prop( "disabled", false );
            }
        }
        jQuery('#oidcDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'passiveSTSDefault') {
        jQuery('#openIdDefault').prop( "checked", false );
        jQuery('#saml2SSODefault').prop( "checked", false );
        jQuery('#oidcDefault').prop( "checked", false );
        jQuery('#fbAuthDefault').prop( "checked", false );
        if (jQuery('#openIdEnabled').prop('checked')) {
            jQuery('#openIdDefault').prop( "disabled", false );
        }
        if (jQuery('#saml2SSOEnabled').prop('checked')) {
            jQuery('#saml2SSODefault').prop( "disabled", false );
        }
        if (jQuery('#oidcEnabled').prop('checked')) {
            jQuery('#oidcDefault').prop( "disabled", false );
        }
        if (jQuery('#fbAuthEnabled').prop('checked')) {
            jQuery('#fbAuthDefault').prop( "disabled", false );
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).prop( "checked", false );

            if (jQuery('#' + id).prop('checked')) {
                jQuery(defId).prop( "disabled", false );
            }
        }
        jQuery('#passiveSTSDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'fbAuthDefault') {
        jQuery('#openIdDefault').prop( "checked", false );
        jQuery('#saml2SSODefault').prop( "checked", false );
        jQuery('#oidcDefault').prop( "checked", false );
        jQuery('#passiveSTSDefault').prop( "checked", false );
        if (jQuery('#openIdEnabled').prop('checked')) {
            jQuery('#openIdDefault').prop( "disabled", false );
        }
        if (jQuery('#saml2SSOEnabled').prop('checked')) {
            jQuery('#saml2SSODefault').prop( "disabled", false );
        }
        if (jQuery('#oidcEnabled').prop('checked')) {
            jQuery('#oidcDefault').prop( "disabled", false );
        }
        if (jQuery('#passiveSTSEnabled').prop('checked')) {
            jQuery('#passiveSTSDefault').prop( "disabled", false );
        }
        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).prop( "checked", false );

            if (jQuery('#' + id).prop('checked')) {
                jQuery(defId).prop( "disabled", false );
            }
        }
        jQuery('#fbAuthDefault').prop( "disabled", true );
    } else {
        for (id in getEnabledCustomAuth()) {
            var defId = id.replace("_Enabled", "_Default");
            if (jQuery(obj).prop('id') == defId) {
                jQuery('#openIdDefault').prop( "checked", false );
                jQuery('#saml2SSODefault').prop( "checked", false );
                jQuery('#oidcDefault').prop( "checked", false );
                jQuery('#passiveSTSDefault').prop( "checked", false );
                jQuery('#fbAuthDefault').prop( "checked", false );

                if (jQuery('#openIdEnabled').prop('checked')) {
                    jQuery('#openIdDefault').prop( "disabled", false );
                }
                if (jQuery('#saml2SSOEnabled').prop('checked')) {
                    jQuery('#saml2SSODefault').prop( "disabled", false );
                }
                if (jQuery('#oidcEnabled').prop('checked')) {
                    jQuery('#oidcDefault').prop( "disabled", false );
                }
                if (jQuery('#passiveSTSEnabled').prop('checked')) {
                    jQuery('#passiveSTSDefault').prop( "disabled", false );
                }
                if (jQuery('#fbAuthEnabled').prop('checked')) {
                    jQuery('#fbAuthDefault').prop( "disabled", false );
                }

                for (idE in getEnabledCustomAuth()) {
                    var defIdE = idE.replace("_Enabled", "_Default");

                    if (jQuery(obj).prop('id') == defIdE) {
                        //Nothing do
                    }
                    else {
                        jQuery('#' + defIdE).prop( "checked", false );
                        if (jQuery('#' + idE).prop('checked')) {
                            jQuery('#' + defIdE).prop( "disabled", false );
                        }
                    }
                }

                jQuery('#' + defId).prop( "disabled", true );
            }
        }
    }
}


function deleteRow(obj) {
    jQuery(obj).parent().parent().remove();

}

function disableDefaultPwd(chkbx) {
    if (jQuery(chkbx).prop('id') == 'scimPwdProvEnabled') {
        document.getElementById("scim-default-pwd").value = "";
        var disabled = chkbx.checked;
        document.getElementById("scim-default-pwd").disabled = disabled;
    } else if (jQuery(chkbx).prop('id') == 'scim2PwdProvEnabled') {
        document.getElementById("scim2-default-pwd").value = "";
        var disabled = chkbx.checked;
        document.getElementById("scim2-default-pwd").disabled = disabled;
    }
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
    if (jQuery(obj).prop('checked')) {
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
        if (jQuery('#' + id).prop('checked')) {
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
            if (jQuery('#' + id).prop('checked')) {
                enable = true;
            }
        }
    }
    return enable;
}


function checkEnabled(obj) {

    if (jQuery(obj).prop('checked')) {
        if (jQuery(obj).prop('id') == 'openIdEnabled') {
            if (!jQuery('#saml2SSOEnabled').prop('checked') && !jQuery('#oidcEnabled').prop('checked') && !jQuery('#passiveSTSEnabled').prop('checked') && !jQuery('#fbAuthEnabled').prop('checked') && !isCustomAuthEnabled()) {
                jQuery('#openIdDefault').prop( "checked", true );
                jQuery('#openIdDefault').prop( "disabled", true );


            } else {
                jQuery('#openIdDefault').prop( "disabled", false );
            }

            jQuery('#openid_enable_logo').show();
        } else if (jQuery(obj).prop('id') == 'saml2SSOEnabled') {
            if (!jQuery('#openIdEnabled').prop('checked') && !jQuery('#oidcEnabled').prop('checked') && !jQuery('#passiveSTSEnabled').prop('checked') && !jQuery('#fbAuthEnabled').prop('checked') && !isCustomAuthEnabled()) {
                jQuery('#saml2SSODefault').prop( "checked", true );
                jQuery('#saml2SSODefault').prop( "disabled", true );
            } else {
                jQuery('#saml2SSODefault').prop( "disabled", false );
            }
            jQuery('#sampl2sso_enable_logo').show();
        } else if (jQuery(obj).prop('id') == 'oidcEnabled') {
            if (!jQuery('#openIdEnabled').prop('checked') && !jQuery('#saml2SSOEnabled').prop('checked') && !jQuery('#passiveSTSEnabled').prop('checked') && !jQuery('#fbAuthEnabled').prop('checked') && !isCustomAuthEnabled()) {
                jQuery('#oidcDefault').prop( "checked", true );
                jQuery('#oidcDefault').prop( "disabled", true );
            } else {
                jQuery('#oidcDefault').prop( "disabled", false );
            }
            jQuery('#oAuth2_enable_logo').show();
        } else if (jQuery(obj).prop('id') == 'passiveSTSEnabled') {
            if (!jQuery('#saml2SSOEnabled').prop('checked') && !jQuery('#oidcEnabled').prop('checked') && !jQuery('#openIdEnabled').prop('checked') && !jQuery('#fbAuthEnabled').prop('checked') && !isCustomAuthEnabled()) {
                jQuery('#passiveSTSDefault').prop( "checked", true );
                jQuery('#passiveSTSDefault').prop( "disabled", true );
            } else {
                jQuery('#passiveSTSDefault').prop( "disabled", false );
            }
            jQuery('#wsfederation_enable_logo').show();
        } else if (jQuery(obj).prop('id') == 'fbAuthEnabled') {
            if (!jQuery('#saml2SSOEnabled').prop('checked') && !jQuery('#oidcEnabled').prop('checked') && !jQuery('#passiveSTSEnabled').prop('checked') && !jQuery('#openIdEnabled').prop('checked') && !isCustomAuthEnabled()) {
                jQuery('#fbAuthDefault').prop( "checked", true );
                jQuery('#fbAuthDefault').prop( "disabled", true );
            } else {
                jQuery('#fbAuthDefault').prop( "disabled", false );
            }
            jQuery('#fecebook_enable_logo').show();
        } else {
            for (id in getEnabledCustomAuth()) {
                if (jQuery(obj).prop('id') == id) {
                    var defId = '#' + id.replace("_Enabled", "_Default");
                    if (!jQuery('#saml2SSOEnabled').prop('checked') && !jQuery('#oidcEnabled').prop('checked') && !jQuery('#passiveSTSEnabled').prop('checked') && !jQuery('#openIdEnabled').prop('checked') && !jQuery('#fbAuthEnabled').prop('checked') && !isOtherCustomAuthEnabled(id)) {
                        jQuery(defId).prop( "checked", true );
                        jQuery(defId).prop( "disabled", true );
                    } else {
                        jQuery(defId).prop( "disabled", false );
                    }
                }
            }
        }
    } else {
        if (jQuery(obj).prop('id') == 'openIdEnabled') {
            if (jQuery('#saml2SSOEnabled').prop('checked') ||
                jQuery('#passiveSTSEnabled').prop('checked') ||
                jQuery('#oidcEnabled').prop('checked') ||
                jQuery('#fbAuthEnabled').prop('checked') || isCustomAuthEnabled()) {

                if (jQuery('#openIdDefault').prop('checked')) {
                    jQuery('#openIdEnabled').prop( "checked", true );
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#openIdDefault').prop( "disabled", true );
                    jQuery('#openIdDefault').prop( "checked", false );
                    jQuery('#openid_enable_logo').hide();
                }
            } else {
                jQuery('#openIdDefault').prop( "disabled", true );
                jQuery('#openIdDefault').prop( "checked", false );
                jQuery('#openid_enable_logo').hide();
            }


        } else if (jQuery(obj).prop('id') == 'saml2SSOEnabled') {

            if (jQuery('#openIdEnabled').prop('checked') ||
                jQuery('#passiveSTSEnabled').prop('checked') ||
                jQuery('#oidcEnabled').prop('checked') ||
                jQuery('#fbAuthEnabled').prop('checked') || isCustomAuthEnabled()) {

                if (jQuery('#saml2SSODefault').prop('checked')) {
                    jQuery('#saml2SSOEnabled').prop( "checked", true );
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#saml2SSODefault').prop( "disabled", true );
                    jQuery('#saml2SSODefault').prop( "checked", false );
                    jQuery('#sampl2sso_enable_logo').hide();
                }
            } else {
                jQuery('#saml2SSODefault').prop( "disabled", true );
                jQuery('#saml2SSODefault').prop( "checked", false );
                jQuery('#sampl2sso_enable_logo').hide();
            }

        } else if (jQuery(obj).prop('id') == 'oidcEnabled') {

            if (jQuery('#saml2SSOEnabled').prop('checked') ||
                jQuery('#passiveSTSEnabled').prop('checked') ||
                jQuery('#openIdEnabled').prop('checked') ||
                jQuery('#fbAuthEnabled').prop('checked') || isCustomAuthEnabled()) {

                if (jQuery('#oidcDefault').prop('checked')) {
                    jQuery('#oidcEnabled').prop( "checked", true );
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#oidcDefault').prop( "disabled", true );
                    jQuery('#oidcDefault').prop( "checked", false );
                    jQuery('#oAuth2_enable_logo').hide();
                }
            } else {
                jQuery('#oidcDefault').prop( "disabled", true );
                jQuery('#oidcDefault').prop( "checked", false );
                jQuery('#oAuth2_enable_logo').hide();
            }
        } else if (jQuery(obj).prop('id') == 'passiveSTSEnabled') {

            if (jQuery('#saml2SSOEnabled').prop('checked') ||
                jQuery('#oidcEnabled').prop('checked') ||
                jQuery('#openIdEnabled').prop('checked') ||
                jQuery('#fbAuthEnabled').prop('checked') || isCustomAuthEnabled()) {

                if (jQuery('#passiveSTSDefault').prop('checked')) {
                    jQuery('#passiveSTSEnabled').prop( "checked", true );
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#passiveSTSDefault').prop( "disabled", true );
                    jQuery('#passiveSTSDefault').prop( "checked", false );
                    jQuery('#wsfederation_enable_logo').hide();
                }
            } else {
                jQuery('#passiveSTSDefault').prop( "disabled", true );
                jQuery('#passiveSTSDefault').prop( "checked", false );
                jQuery('#wsfederation_enable_logo').hide();
            }

        } else if (jQuery(obj).prop('id') == 'fbAuthEnabled') {

            if (jQuery('#saml2SSOEnabled').prop('checked') ||
                jQuery('#oidcEnabled').prop('checked') ||
                jQuery('#openIdEnabled').prop('checked') ||
                jQuery('#passiveSTSEnabled').prop('checked') || isCustomAuthEnabled()) {

                if (jQuery('#fbAuthDefault').prop('checked')) {
                    jQuery('#fbAuthEnabled').prop( "checked", true );
                    CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#fbAuthDefault').prop( "disabled", true );
                    jQuery('#fbAuthDefault').prop( "checked", false );
                    jQuery('#fecebook_enable_logo').hide();
                }
            } else {
                jQuery('#fbAuthDefault').prop( "disabled", true );
                jQuery('#fbAuthDefault').prop( "checked", false );
                jQuery('#fecebook_enable_logo').hide();
            }
        } else {
            for (id in getEnabledCustomAuth()) {
                if (jQuery(obj).prop('id') == id) {
                    var defId = '#' + id.replace("_Enabled", "_Default");
                    if (jQuery('#saml2SSOEnabled').prop('checked') ||
                        jQuery('#oidcEnabled').prop('checked') ||
                        jQuery('#passiveSTSEnabled').prop('checked') ||
                        jQuery('#openIdEnabled').prop('checked') ||
                        jQuery('#fbAuthEnabled').prop('checked') || isOtherCustomAuthEnabled(id)) {

                        if (jQuery(defId).prop('checked')) {
                            jQuery('#' + id).prop( "checked", true );
                            CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                        } else {
                            jQuery(defId).prop( "disabled", true );
                            jQuery(defId).prop( "checked", false );
                        }
                    } else {
                        jQuery(defId).prop( "disabled", true );
                        jQuery(defId).prop( "checked", false );
                    }
                }
            }
        }
    }
}



function checkProvEnabled(obj) {

    if (jQuery(obj).prop('checked')) {
        if (jQuery(obj).prop('id') == 'googleProvEnabled') {

            if (!jQuery('#sfProvEnabled').prop('checked') && !jQuery('#scimProvEnabled').prop('checked') && !jQuery('#scim2ProvEnabled').prop('checked')) {

                jQuery('#googleProvDefault').prop( "checked", true );
                jQuery('#googleProvDefault').prop( "disabled", true );
            } else {
                jQuery('#googleProvDefault').prop( "disabled", false );
            }

            jQuery('#google_enable_logo').show();

        } else if (jQuery(obj).prop('id') == 'sfProvEnabled') {

            if (!jQuery('#googleProvEnabled').prop('checked') && !jQuery('#scimProvEnabled').prop('checked') && !jQuery('#scim2ProvEnabled').prop('checked')) {

                jQuery('#sfProvDefault').prop( "checked", true );
                jQuery('#sfProvDefault').prop( "disabled", true );
            } else {
                jQuery('#sfProvDefault').prop( "disabled", false );
            }

            jQuery('#sf_enable_logo').show();

        } else if (jQuery(obj).prop('id') == 'scimProvEnabled') {

            if (!jQuery('#googleProvEnabled').prop('checked') && !jQuery('#sfProvEnabled').prop('checked') && !jQuery('#scim2ProvEnabled').prop('checked')) {

                jQuery('#scimProvDefault').prop( "checked", true );
                jQuery('#scimProvDefault').prop( "disabled", true );
            } else {
                jQuery('#scimProvDefault').prop( "disabled", false );
            }

            jQuery('#scim_enable_logo').show();

        } else if (jQuery(obj).prop('id') == 'scim2ProvEnabled') {

            if (!jQuery('#googleProvEnabled').prop('checked') && !jQuery('#sfProvEnabled').prop('checked') && !jQuery('#scimProvEnabled').prop('checked')) {

                jQuery('#scim2ProvDefault').prop( "checked", true );
                jQuery('#scim2ProvDefault').prop( "disabled", true );
            } else {
                jQuery('#scim2ProvDefault').prop( "disabled", false );
            }

            jQuery('#scim2_enable_logo').show();

        }
    } else {
        if (jQuery(obj).prop('id') == 'googleProvEnabled') {

            if (jQuery('#sfProvEnabled').prop('checked') ||
                jQuery('#scimProvEnabled').prop('checked') ||
                jQuery('#scim2ProvEnabled').prop('checked')) {

                if (jQuery('#googleProvDefault').prop('checked')) {
                    //jQuery('#googleProvEnabled').prop('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#googleProvDefault').prop( "disabled", true );
                    jQuery('#googleProvDefault').prop( "checked", false );
                    jQuery('#google_enable_logo').hide();
                }
            } else {
                jQuery('#googleProvDefault').prop( "disabled", true );
                jQuery('#googleProvDefault').prop( "checked", false );
                jQuery('#google_enable_logo').hide();
            }

        } else if (jQuery(obj).prop('id') == 'sfProvEnabled') {

            if (jQuery('#googleProvEnabled').prop('checked') ||
                jQuery('#scimProvEnabled').prop('checked') ||
                jQuery('#scim2ProvEnabled').prop('checked')) {

                if (jQuery('#sfProvDefault').prop('checked')) {
                    // jQuery('#sfProvEnabled').prop('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#sfProvDefault').prop( "disabled", true );
                    jQuery('#sfProvDefault').prop( "checked", false );
                    jQuery('#sf_enable_logo').hide();
                }
            } else {
                jQuery('#sfProvDefault').prop( "disabled", true );
                jQuery('#sfProvDefault').prop( "checked", false );
                jQuery('#sf_enable_logo').hide();
            }

        } else if (jQuery(obj).prop('id') == 'scimProvEnabled') {

            if (jQuery('#sfProvEnabled').prop('checked') ||
                jQuery('#googleProvEnabled').prop('checked') ||
                jQuery('#scim2ProvEnabled').prop('checked')) {

                if (jQuery('#scimProvDefault').prop('checked')) {
                    // jQuery('#scimProvEnabled').prop('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#scimProvDefault').prop( "disabled", true );
                    jQuery('#scimProvDefault').prop( "checked", false );
                    jQuery('#scim_enable_logo').hide();
                }
            } else {
                jQuery('#scimProvDefault').prop( "disabled", true );
                jQuery('#scimProvDefault').prop( "checked", false );
                jQuery('#scim_enable_logo').hide();
            }

        } else if (jQuery(obj).prop('id') == 'scim2ProvEnabled') {

            if (jQuery('#sfProvEnabled').prop('checked') ||
                jQuery('#googleProvEnabled').prop('checked') ||
                jQuery('#scimProvEnabled').prop('checked')) {

                if (jQuery('#scim2ProvDefault').prop('checked')) {
                    // jQuery('#scim2ProvEnabled').prop('checked','checked');
                    // CARBON.showWarningDialog("Make other enabled authenticator to default before disabling default authenticator");
                } else {
                    jQuery('#scim2ProvDefault').prop( "disabled", true );
                    jQuery('#scim2ProvDefault').prop( "checked", false );
                    jQuery('#scim2_enable_logo').hide();
                }
            } else {
                jQuery('#scim2ProvDefault').prop( "disabled", true );
                jQuery('#scim2ProvDefault').prop( "checked", false );
                jQuery('#scim2_enable_logo').hide();
            }
        }
    }
}

function checkProvDefault(obj) {
    if (jQuery(obj).prop('id') == 'googleProvDefault') {
        jQuery('#sfProvDefault').prop( "checked", false );
        jQuery('#scimProvDefault').prop( "checked", false );
        jQuery('#scim2ProvDefault').prop( "checked", false );
        if (jQuery('#sfProvEnabled').prop('checked')) {
            jQuery('#sfProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scimProvEnabled').prop('checked')) {
            jQuery('#scimProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scim2ProvEnabled').prop('checked')) {
            jQuery('#scim2ProvDefault').prop( "disabled", false );
        }
        jQuery('#googleProvDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'sfProvDefault') {
        jQuery('#googleProvDefault').prop( "checked", false );
        jQuery('#scimProvDefault').prop( "checked", false );
        jQuery('#scim2ProvDefault').prop( "checked", false );
        if (jQuery('#googleProvEnabled').prop('checked')) {
            jQuery('#googleProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scimProvEnabled').prop('checked')) {
            jQuery('#scimProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scim2ProvEnabled').prop('checked')) {
            jQuery('#scim2ProvDefault').prop( "disabled", false );
        }
        jQuery('#sfProvDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'scimProvDefault') {
        jQuery('#googleProvDefault').prop( "checked", false );
        jQuery('#sfProvDefault').prop( "checked", false );
        jQuery('#scim2ProvDefault').prop( "checked", false );
        if (jQuery('#googleProvEnabled').prop('checked')) {
            jQuery('#googleProvDefault').prop( "disabled", false );
        }
        if (jQuery('#sfProvEnabled').prop('checked')) {
            jQuery('#sfProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scim2ProvEnabled').prop('checked')) {
            jQuery('#scim2ProvDefault').prop( "disabled", false );
        }
        jQuery('#scimProvDefault').prop( "disabled", true );
    } else if (jQuery(obj).prop('id') == 'scim2ProvDefault') {
        jQuery('#googleProvDefault').prop( "checked", false );
        jQuery('#sfProvDefault').prop( "checked", false );
        jQuery('#scimProvDefault').prop( "checked", false );
        if (jQuery('#googleProvEnabled').prop('checked')) {
            jQuery('#googleProvDefault').prop( "disabled", false );
        }
        if (jQuery('#sfProvEnabled').prop('checked')) {
            jQuery('#sfProvDefault').prop( "disabled", false );
        }
        if (jQuery('#scimProvEnabled').prop('checked')) {
            jQuery('#scimProvDefault').prop( "disabled", false );
        }
        jQuery('#scim2ProvDefault').prop( "disabled", true );
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
    var emailPattern = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
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

    if (jQuery('#openIdEnabled').prop('checked')) {

        if ($('#openIdUrl').val() == "") {
            CARBON.showWarningDialog('OpenID Server URL cannot be empty');
            return false;
        }
    }

    if (jQuery('#saml2SSOEnabled').prop('checked')) {

        if ($('#meta_data_saml').val() == "") {

            if ($('#idPEntityId').val() == "") {
                CARBON.showWarningDialog('Identity Provider Entity ID cannot be empty');
                return false;
            }
            if ($('#ssoUrl').val() == "") {
                CARBON.showWarningDialog('SSO URL cannot be empty');
                return false;
            }
        }

        if ($('#spEntityId').val() == "") {
            CARBON.showWarningDialog('Service Provider Entity ID cannot be empty');
            return false;
        }


    }

    if ($('#meta_data_saml').val() != ""  && !jQuery('#saml2SSOEnabled').prop('checked') ) {
        if ($('#spEntityId').val() == "") {
            CARBON.showWarningDialog('Service Provider Entity ID cannot be empty');
            return false;
        }

    }

    if (jQuery('#oidcEnabled').prop('checked')) {

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

        if ($('#scopes').val() != "" && $('#oidcQueryParam').val().toLowerCase().includes('scope=')) {
            CARBON.showWarningDialog('Cannot set scopes in both Scopes and Additional Query Parameters.' +
                ' Recommend to use Scopes field.');
            return false;
        }

        if ($('#scopes').val() != "" && $('#scopes').val().indexOf('openid') == -1) {
            CARBON.showWarningDialog('Scopes must contain \'openid\'');
            return false;
        }
    }

    if (jQuery('#passiveSTSEnabled').prop('checked')) {

        if ($('#passiveSTSRealm').val() == "") {
            CARBON.showWarningDialog('Passive STS Realm cannot be empty');
            return false;
        }

        if ($('#passiveSTSUrl').val() == "") {
            CARBON.showWarningDialog('Passive STS URL cannot be empty');
            return false;
        }
    }

    if (jQuery('#fbAuthEnabled').prop('checked')) {

        if ($('#fbClientId').val() == "") {
            CARBON.showWarningDialog('Facebook Client Id cannot be empty');
            return false;
        }

        if ($('#fbClientSecret').val() == "") {
            CARBON.showWarningDialog('Facebook Client Secret cannot be empty');
            return false;
        }
    }


    if (jQuery('#googleProvEnabled').prop('checked')) {

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

    if (jQuery('#sfProvEnabled').prop('checked')) {

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


    if (jQuery('#scimProvEnabled').prop('checked')) {

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

    if (jQuery('#scim2ProvEnabled').prop('checked')) {

        if ($('#scim2-username').val() == "") {
            CARBON.showWarningDialog('Scim2 Configuration username cannot be empty');
            return false;
        }

        if ($('#scim2-password').val() == "") {
            CARBON.showWarningDialog('Scim2 Configuration password cannot be empty');
            return false;
        }

        if ($('#scim2-user-ep').val() == "") {
            CARBON.showWarningDialog('Scim2 Configuration User endpoint cannot be empty');
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
    jQuery('#authentication_context_class_dropdown').prop('disabled', true);
    jQuery('#auth_context_comparison_level_dropdown').prop('disabled', true);
    jQuery('#addAuthenticationContextClassBtn').attr('disabled', true);
});


jQuery('#logoutRequestSigned').click(function () {
    if (jQuery(this).is(":checked") || jQuery("#authnRequestSigned").is(":checked") ||
        (jQuery("#enableArtifactBinding").is(":checked") && jQuery("#artifactResolveReqSigned").is(":checked"))) {
        jQuery('#signature_algorithem_dropdown').prop( "disabled", false );
        jQuery('#digest_algorithem_dropdown').prop( "disabled", false );
    } else {
        jQuery('#signature_algorithem_dropdown').prop('disabled', true);
        jQuery('#digest_algorithem_dropdown').prop('disabled', true);
    }
});

jQuery('#includeAuthnCtxNo').click(function () {
    jQuery('#authentication_context_class_dropdown').prop('disabled', true);
    jQuery('#auth_context_comparison_level_dropdown').prop('disabled', true);
    jQuery('#addAuthenticationContextClassBtn').attr('disabled', true);
    jQuery("#authnContextClsTable > tbody").empty();
    jQuery('#authnContextCls').attr('value', "");
});

jQuery('#includeAuthnCtxYes').click(function () {
    jQuery('#authentication_context_class_dropdown').prop( "disabled", false );
    jQuery('#auth_context_comparison_level_dropdown').prop( "disabled", false );
    jQuery('#addAuthenticationContextClassBtn').removeAttr('disabled');
    jQuery("#authnContextClsTable").removeAttr('hidden');
});

jQuery('#authnRequestSigned').click(function () {
    if (jQuery(this).is(":checked") || jQuery("#logoutRequestSigned").is(":checked") ||
        (jQuery("#enableArtifactBinding").is(":checked") && jQuery("#artifactResolveReqSigned").is(":checked"))) {
        jQuery('#signature_algorithem_dropdown').prop( "disabled", false );
        jQuery('#digest_algorithem_dropdown').prop( "disabled", false );
    } else {
        jQuery('#signature_algorithem_dropdown').prop('disabled', true);
        jQuery('#digest_algorithem_dropdown').prop('disabled', true);
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
    jQuery('#provision_static_dropdown').prop( "disabled", true );
    $('input[name=choose_jit_type_group]').prop( "disabled", true );
});
jQuery('#provision_static').click(function () {
    jQuery('#provision_static_dropdown').prop( "disabled", false );
    $('input[name=choose_jit_type_group]').prop( "disabled", false );
});

jQuery('#password_provisioning').click(function () {
    jQuery('#modify_username').prop( "disabled", false );
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

    // Encoding the selectedIDPClaimName to avoid possible xss vulnerabilities.
    selectedIDPClaimName = htmlEncode(selectedIDPClaimName);
    jQuery('#advancedClaimMappingAddTable').append(jQuery('<tr>' +
        '<td><input type="text" style="width: 99%;" value="' + htmlEncode(selectedIDPClaimName) + '" id="advancnedIdpClaim_' + advancedClaimMappinRowID + '" name="advancnedIdpClaim_' + advancedClaimMappinRowID + '" readonly="readonly" /></td>' +
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

jQuery('#artifactResolveReqSigned').click(function() {
    if (jQuery(this).is(":checked") || jQuery("#authnRequestSigned").is(":checked") ||
        jQuery("#logoutRequestSigned").is(":checked")) {
        jQuery('#signature_algorithem_dropdown').prop( "disabled", false );
        jQuery('#digest_algorithem_dropdown').prop( "disabled", false );
    } else {
        jQuery('#signature_algorithem_dropdown').prop('disabled', true);
        jQuery('#digest_algorithem_dropdown').prop('disabled', true);
    }
});

jQuery('#enableArtifactBinding').click(function() {
    if ((jQuery(this).is(":checked") && jQuery("#artifactResolveReqSigned").is(":checked")) ||
        jQuery("#authnRequestSigned").is(":checked") ||
        jQuery("#logoutRequestSigned").is(":checked")) {
        jQuery('#signature_algorithem_dropdown').prop( "disabled", false );
        jQuery('#digest_algorithem_dropdown').prop( "disabled", false );
    } else if (!jQuery(this).is(":checked") && (jQuery("#authnRequestSigned").is(":checked") ||
        jQuery("#logoutRequestSigned").is(":checked"))) {
        jQuery('#signature_algorithem_dropdown').prop( "disabled", false );
        jQuery('#digest_algorithem_dropdown').prop( "disabled", false );
    } else {
        jQuery('#signature_algorithem_dropdown').prop('disabled', true);
        jQuery('#digest_algorithem_dropdown').prop('disabled', true);
    }
});
