package com.dmc.installAPK;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * Created by lenovo on 2017/4/27.
 * Edit by Galois Zhou on 06/01/2018 14:05.
 */


public class DownInstall extends CordovaPlugin {

  public String[] permissionArray = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
  private CallbackContext myCallbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    myCallbackContext = callbackContext;
    /**
     * appVersion
     */
    if (action.equals("autoDInstallAPK")) {

      for (int i = 0; i < permissionArray.length - 1; i++) {
        if (!cordova.hasPermission(permissionArray[i])) {
          cordova.requestPermission(this, i, permissionArray[i]);
        }
      }

      String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
      String fileName = args.getString(0);
      destination += fileName;
      final Uri uri = Uri.parse("file://" + destination);

//      Uri uri = FileProvider.getUriForFile(cordova.getActivity(), "com.android.providers.downloads.documents", new File(destination));

      //Delete update file if exists
      final File file = new File(destination);

      if (file.exists()) {
        try {
          file.delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      //get url of app on server
      String url = args.getString(1);
      //set downloadmanager
      DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
      request.setDescription(args.getString(2));
      request.setTitle(args.getString(3));
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
      );
      //set destination
      request.setDestinationUri(uri);

      // get download service and enqueue file
      final DownloadManager manager = (DownloadManager) cordova.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
      final long downloadId = manager.enqueue(request);

      //set BroadcastReceiver to install app when .apk is downloaded
      BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
          Intent install = new Intent(Intent.ACTION_VIEW);

          install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

//          兼容 android 7 以上的版本
          if (android.os.Build.VERSION.SDK_INT >= 24) {
            Uri path = FileProvider.getUriForFile(
              cordova.getActivity(),
              cordova.getActivity().getApplicationContext().getPackageName() + ".provider",
              file);

            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(path, manager.getMimeTypeForDownloadedFile(downloadId));

            List<ResolveInfo> resInfoList = cordova.getActivity().getPackageManager().queryIntentActivities(
              intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
              String packageName = resolveInfo.activityInfo.packageName;
              cordova.getActivity().grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
          } else {
            install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
          }


          cordova.getActivity().startActivity(install);
          cordova.getActivity().unregisterReceiver(this);
          cordova.getActivity().finish();
        }
      };
      //register receiver for when .apk download is compete
      cordova.getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
      return true;
    }

    // Default response to say the action hasn't been handled
    return false;
  }


  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        myCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "permission refused"));
        return;
      }
    }
    myCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "PERMS_GRANTED"));
  }

  /**
   * 看系统下载器是否能用
   *
   * @return
   */
  private boolean canDownloadState() {
    try {
      int state = cordova.getActivity().getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");

      if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
        return false;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
