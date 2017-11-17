# cordova-plugin-downinstall
Download automatic installation APK 

How do I use?
-------------------

use npm:

```npm
  cordova plugin add cordova-plugin-downinstall
```
remove npm:

```npm
  cordova plugin remove cordova-plugin-downinstall
```

## Example

index.js code:

    window.DownInstall.autoDInstallAPK(["newapp",    //your app fileName
                        "http://www.***.com/app/newapp.apk",    //your apk download url
                        "app download",   //your apk download description
                        "new app downloading"],   //your apk download title
                        function(ok) {
                        },
                        function(error) {
                           if("permission refused"==error)}{
                              alert("permission refused");
                            }
                        });
