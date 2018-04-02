function onInitialRequest(context) {

    executeStep({
        id: '1',
        on: {
            success: function (context) {


                // DUMMY TEST
                var user = context.subject;
                var username = user.username;
                var isUserLocked = isAccountLocked(username);
                if (isUserLocked) {
                    Log.info("---------------------- ACCOUNT LOCKED FOR USER: " + user.username + ' ---------------------------');
                    sendError({});
                }
            },
            fail : function (context) {

                var accountLocked = function (username) {
                    var x = querySiddhiRuntime("LockAccountOnFailureApp", "from AccountLockedTable on user == '" + username + "'");
                    return x !== null && x.length > 0;
                };

                // DUMMY TEST
                var appName = 'LockAccountOnFailureApp';
                var streamName = 'login_failure_stream';

                var user = context.lastAttemptedSubject;
                var username = context.lastAttemptedSubject.username;
                var sp = context.serviceProviderName;

                var payload = {'user' : username, 'service_provider' : sp };
                publishEvent(appName, streamName, payload);


                // Let's check whether user was locked
                var isUserLocked = isAccountLocked(username);
                if (isUserLocked) {
                    Log.info("---------------------- ACCOUNT LOCKED FOR USER: " + user.username + ' ---------------------------');
                }
            }
        }
    });
}

function isAccountLocked(username) {
    var x = querySiddhiRuntime("LockAccountOnFailureApp", "from AccountLockedTable on user == '" + username + "'");
    return x !== null && x.length > 0;
}
