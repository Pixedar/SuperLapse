package pixedar.com.superlapse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import pixedar.com.superlapse.Camera2.Camera2PreviewActivity;
import pixedar.com.superlapse.util.log;

import static android.provider.DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;

public class TimelapseSettingsController extends TimelapseSettingsFragment{
    public static Settings settings;
    public static DocumentFile pickedDir;
    private Intent batteryStatus;
    private int maxSdFreeSpace = 0;
    public static final String TAG = TimelapseSettingsController.class.getSimpleName();
    private int maxInternalFreeSpace = 0;

 /*   TimelapseSettingsController(Context context,Settings settings){
        IntentFilter intentFilter= new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, intentFilter);
        TimelapseSettingsController.settings = settings;
    }*/
 long freeSpaceInMegabytes0 =     getFreeSpaceStringInMegaBytes(0);
long freeSpaceInMegabytes1 = getFreeSpaceStringInMegaBytes(1);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        loadSettings(getContext());
        IntentFilter intentFilter= new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, intentFilter);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settings = settingsT;

        batterySeekBar.setMax(getBatteryLevel(getContext()));
        saveOnInternal.setText(saveOnInternal.getText() + getFreeSpaceString(1));
        saveOnSD.setText(saveOnSD.getText() + getFreeSpaceString(0));
        setSaveOnSDStatus(checkExternalMemory());
    }

    @Override
    public void updateTimeBar() {
        settings = settingsT;
        timeSeekBar.setMax(calcMaxTime());
        sizeTextView.setText(getSizeString(calcMaxTime()));

    }

    @Override
    void onResolutionMenuChanged(int index) {
        settings = settingsT;
    }

    @Override
    void onCompressionChanged(int progress) {
        settings = settingsT;
    }

    @SuppressLint("WrongConstant")
    @Override
    void onStartPressed() {
        if ( settingsT.saveOnSD) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.setFlags(FLAG_DIR_SUPPORTS_CREATE);
            startActivityForResult(intent, 43);
        }else {
            Intent intent = new Intent(getActivity(), Camera2PreviewActivity.class);
            startActivity(intent);
        }
    }

    private boolean externalMemoryAvailable() {
        if (!Environment.isExternalStorageRemovable()) {
            //device support sd card. We need to check sd card availability.
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED) || state.equals(
                    Environment.MEDIA_MOUNTED_READ_ONLY);
        } else {
            Log.e(TAG, "device not support sd card");
            return false;
        }
    }

    public boolean checkExternalMemory(){
        if(!externalMemoryAvailable()){
            TimelapseSettingsController.settings.saveOnSD = false;
            settings.saveOnInternal = true;
            return  false;
        }
        TimelapseSettingsController.settings.saveOnSD = true;
        settings.saveOnInternal = false;
        return  true;
    }
    public String getSizeString(int max){
        if(settings.captureRawImages){
            //  max =  settings.rawSize/ settings.rawRecords;
            max =  (int)settings.rawSize;
        }else {
            for (int k = 0; k <  settings.resoluionMenu.length; k++) {
                if ( settings.resoluionMenu[k]) {
                    //    max =  settings.maxPictureSize[k];
                    max =  (int)settings.maxPictureSize[k] /  settings.numberOfRecords;

                    break;
                }
            }
        }
        log.log(max);
        float ma;
        if ( settings.time == 0) {
            ma = getFreeSpaceStringInMegaBytes(0)/1024;
        } else {
            ma = ((( settings.time * 60.0f * 1000.0f /  settings.speed) * max) / ( 1024.0f*1024.0f)); //w gb
        }
        if (settings.saveOnSD) {
           return String.format("%.2f", ma) + " GB (" + String.valueOf((int) ((ma * 1024 / freeSpaceInMegabytes0 ) * 100)) + "%)";
        }
        return String.format("%.2f", ma) + " GB (" + String.valueOf((int) ((ma * 1024 / freeSpaceInMegabytes1) * 100)) + "%)";
    }
    public int calcMaxTime(){
        // int max = 80;
        long max = 80;
        if(settings.captureRawImages){
            //  max =  settings.rawSize/ settings.rawRecords;
            max =  settings.rawSize;
        }else {
            for (int k = 0; k <  settings.resoluionMenu.length; k++) {
                if ( settings.resoluionMenu[k]) {
                   //    max =  settings.maxPictureSize[k];
                   max =  settings.maxPictureSize[k] /  settings.numberOfRecords;

                    break;
                }
            }
        }
        if(!settings.captureRawImages) {
            if (settings.compression!= 100) {
                max = (int) (max * (0.953 - 0.2016 * Math.log(100 -  settings.compression)));
            } else {
                max = (int) (max * (0.953 - 0.2016 * Math.log(100 - 99.21)));
            }

        }
        if (settings.saveOnSD) {
            return Math.round( freeSpaceInMegabytes0 * 1024.0f * settings.speed / (60000.0f * max));
        }
        return Math.round(freeSpaceInMegabytes1 * 1024.0f *  settings.speed / (60000.0f * max));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        try {
            Uri treeUri = resultData.getData();
            SharedPreferences settings = getContext().getSharedPreferences("SuperLapsePref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("uri",treeUri.toString());
            editor.apply();

            pickedDir = DocumentFile.fromTreeUri(getContext(), treeUri).createDirectory(createDirectoryName());

            getContext().grantUriPermission(getContext().getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContext().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Intent intent = new Intent(getContext(), Camera2PreviewActivity.class);
            intent.putExtra("productId",CameraParameters.getDevideId(Build.MODEL));
            getContext().startActivity(intent);
          /*  if(TimelapseSettingsController.settings.captureRawImages){
                Intent intent = new Intent(getContext(), SamsungCameraPreview.class);
                getContext().startActivity(intent);
            }else {
                Intent intent = new Intent(getContext(), Camera2PreviewActivity.class);
                getContext().startActivity(intent);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public int getBatteryLevel(Context context){
        if (batteryStatus != null) {
            return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) - 1;
        }else{
            if (AppConfig.LOG) {
                Log.e(TAG, "batteryStatus is Null");
            }
            return  0;
        }
    }



    private String createDirectoryName() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        long dateTaken = calendar.getTimeInMillis();

        return DateFormat.format("yyyy-MM-dd 'at' HH꞉mm꞉ss", dateTaken).toString();
    }



    public void loadSettings(Context context) {

        SharedPreferences savedSettings = context.getSharedPreferences("SuperLapsePref", 0);
        int numberOfRecords = savedSettings.getInt("numberOfRecords", 0);
        log.log(numberOfRecords);
        if(numberOfRecords == 0){
            return;
        }
        settingsT.numberOfRecords = numberOfRecords;

        //    settings.rawRecords = settings.getInt("rawRecords", settings.rawRecords);
        //  settings.rawSize = settings.getLong("rawSize", settings.rawSize);
        for (int k = 0; k < 4; k++) {
            log.log(k);
            try {
                //    settings.maxPictureSize[k] = settings.getInt(String.valueOf(k),  settings.maxPictureSize[k]);
                long maxPictureSize = savedSettings.getLong(String.valueOf(k),  0);
                log.log(maxPictureSize );
                if(maxPictureSize ==0){
                    continue;
                }
                log.log(maxPictureSize);
                settingsT.maxPictureSize[k] = maxPictureSize;

            } catch (Exception ignored) {
            }
        }
    }

    public String getFreeSpaceString(int index) {
        long  megaBytesAvailable = getFreeSpaceStringInMegaBytes(index);
        if (megaBytesAvailable > 1024) {
            return " (" + String.format("%.1f", megaBytesAvailable / 1024.0f) + " GB)";
        } else {
            return " (" + String.valueOf(megaBytesAvailable) + " mb)";
        }
    }
    private long getFreeSpaceStringInMegaBytes(int index){
        //stat = new StatFs("/storage/6261-3334");
        StatFs stat;
        try {
            if (index == 0) {
                stat = new StatFs(getExternalSDPath());
            } else {
                stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        int megaBytesAvailable = (int) (bytesAvailable / (1024 * 1024));
        return  megaBytesAvailable;
    }
    
    public void startPreview(Activity activity, int flag) {
         if (settings.saveOnSD) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(flag);
        activity.startActivityForResult(intent, 43);
      }else {
         Intent intent = new Intent(activity, Camera2PreviewActivity.class);
         activity.startActivity(intent);
      }
    }


    private String getExternalSDPath() {
        StorageHelper.StorageVolume sv = new StorageHelper().getStorage(StorageHelper.StorageVolume.Type.EXTERNAL);
        if (sv != null) {
            return sv.file.getPath();
        } else {
            String sdpath = System.getenv("SECONDARY_STORAGE");
            if (sdpath == null || sdpath.isEmpty()) {
                sdpath = "/storage/extSdCard";
            }
            return sdpath;
        }
    }
}
