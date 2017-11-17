var exec = require('cordova/exec');

exports.autoDInstallAPK = function(arg0, success, error) {
    exec(success, error, "DownInstall", "autoDInstallAPK", arg0);
};