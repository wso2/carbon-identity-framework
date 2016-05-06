function updateCredentials() {
    var reason = "";
    var updateCredentialsForm = document.getElementById('updateCredentialsForm');
    var adminPassword = "";
    var adminPasswordRepeat = "";
    adminPassword = document.getElementById('admin-password');
    adminPasswordRepeat = document.getElementById('admin-password-repeat');

    if (reason == "") {
        reason += validateEmpty(adminPassword, "New Admin Password");
    }
    if (reason == "") {
        reason += validateAdminPassword(adminPassword);
    }
    if (reason == "") {
        reason += validateEmpty(adminPasswordRepeat, "New Admin Password (Repeat)");
    }
    if (reason == "") {
        if (adminPassword.value != adminPasswordRepeat.value) {
            reason += jsi18n["password.mismatched"];
        }
        if (adminPassword.value.length < 6) {
            reason += jsi18n["password.length"];
        }
    }
    if(reason != "") {
        CARBON.showWarningDialog(reason);
        return;
    }
    updateCredentialsForm.submit();
}

function validateAdminPassword(fld) {
    var error = "";
    if (fld.value == "") {
        error = org_wso2_carbon_registry_common_ui_jsi18n["no.password"] + "<br />";
    } else {
        fld.style.background = 'White';
    }
   return error;
}

function showregistrationfail(){
    var error = "";
    CARBON.showWarningDialog(error);
}

var captchaImgUrl;
function showCaptcha(captchaImgUrlArg) {
    captchaImgUrl = captchaImgUrlArg;
    var captchaImgDiv = document.getElementById("captchaImgDiv");
    captchaImgDiv.innerHTML = "<img src='../identity-mgt/images/ajax-loader.gif' alt='busy'/>";
    setTimeout("showCaptchaTimely()", 4000);
}

function showCaptchaTimely() {
    var captchaImgDiv = document.getElementById("captchaImgDiv");
    captchaImgDiv.innerHTML = "<img src='" + captchaImgUrl + "' alt='If you can not see the captcha " +
                    "image please refresh the page or click the link again.'/>";
}



