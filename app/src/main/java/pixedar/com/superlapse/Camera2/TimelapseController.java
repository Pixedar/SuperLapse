package pixedar.com.superlapse.Camera2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import pixedar.com.superlapse.Camera2.LightMeter.SensorLightMeter;
import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.DslrTimelapseSettingsController;
import pixedar.com.superlapse.Settings;
import pixedar.com.superlapse.StorageHelper;
import pixedar.com.superlapse.TimelapseSettingsController;
import pixedar.com.superlapse.util.log;

public class TimelapseController implements Runnable {
    private int time = 0;
    private boolean isSlider;

    private CameraController cameraController;
    private Handler handler;

    public int getCounter() {
        return counter;
    }

    private int counter = 0;
    private int initialFreeSpace = 0;
    private Context context;
    private Intent batteryStatus;
    private Bluetooth bluetooth;
    private Settings settings;
    private boolean isDslr;
    public static boolean IS_BT_CONNECTED = false;
    public static final int SAFE_INTERVAL = 100;
    SensorLightMeter sensorLightMeter;
    TextView progressText;
    TimelapseController(boolean isSlider,boolean isDslr, CameraController cameraController, Handler handler, Context context,TextView progressText){
        IntentFilter intentFilter= new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, intentFilter);

        this.isSlider = isSlider;
        this.cameraController = cameraController;
        this.handler = handler;
        this.context = context;
        initBt();
        initialFreeSpace = getFreeSpace();
        this.isDslr = isDslr;
        this.progressText = progressText;
        this.progressText.setVisibility(View.VISIBLE);
        context.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(!isDslr){
            settings = TimelapseSettingsController.settings;
        }else {
            settings = DslrTimelapseSettingsController.settings;
        }

        if(TimelapseSettingsController.settings.valueAnimationParams.lightMeter.enabled){
            sensorLightMeter = new SensorLightMeter(settings);
        }
       // Log.d("DEBUG",String.valueOf(settings.time));
    }
    private void initBt(){
        bluetooth = new Bluetooth();
        bluetooth.setConnectionLitener(new Bluetooth.ConnectionListener() {
            @Override
            public void connected() {
               IS_BT_CONNECTED = true;
            }

            @Override
            public void connectionFailed() {

            }
        });
    }
    private int getBatteryStatus(){
        return  batteryLevel;
    }
    public void startTimelapse(){
        if(IS_BT_CONNECTED){
            if(bluetooth.send(getSettingsString())) {
                TimelapseController.this.run();
            }
        }else {
            this.run();
        }
    }
    private String getSettingsString(){
        return String.valueOf(settings.speed)+";"+"0";
    }
    private String getProgressString(){
        log.log("time");
        log.log(settings.time);
        log.log(settings.time*6000);
        return String.valueOf(counter / 30) + "s " + String.valueOf(Math.round (((time / 60000.0f) / settings.time) * 100)) + "% " + getTimeStringFormMillis(time) +" battery: "+calcSmarphoneBattery() +" "+calcDslrBattery();
    }
    private int lastSmarphoneBatteryLevel= -1;
    private long lastSmartphoneBatteryCheckTime = 0;
    private String lastSmarphoneDslrBatteryLife  ="";
    private String calcSmarphoneBattery(){
        int level = getBatteryStatus();
        String result = String.valueOf(level)+"% ";
        if(lastSmarphoneBatteryLevel!= level){
            if(lastSmartphoneBatteryCheckTime != 0){
                long timeBetween = System.currentTimeMillis() - lastDslrBatteryCheckTime;
                lastSmarphoneDslrBatteryLife=getEstimatedBattteryLifeString(timeBetween,level);
            }
            lastSmartphoneBatteryCheckTime =  System.currentTimeMillis();
            lastSmarphoneBatteryLevel= level;
        }
        return  result+lastSmarphoneDslrBatteryLife;
    }
    private int lastDslrBatteryLevel= -1;
    private long lastDslrBatteryCheckTime = 0;
    private String lastCalcDslrBatteryLife  ="";
    private String calcDslrBattery(){
        String result ="";
        if(isDslr){
            result = "dslrBattery: ";
            int level = ((DslrController)cameraController).getCameraBatteryLevel();
            result+=String.valueOf(level)+"% ";
            if(lastDslrBatteryLevel!= level){
                if(lastDslrBatteryCheckTime != 0){
                    long timeBetween = System.currentTimeMillis() - lastDslrBatteryCheckTime;
                    lastCalcDslrBatteryLife = getEstimatedBattteryLifeString(timeBetween,level);
                }
                lastDslrBatteryCheckTime =  System.currentTimeMillis();
                lastDslrBatteryLevel= level;
            }
        }
        return result + lastCalcDslrBatteryLife;
    }
    private String getEstimatedBattteryLifeString(long timeBetween, int level){
        long t =(timeBetween*level);
        return getTimeStringFormMillis(t);
    }
    private String getTimeStringFormMillis(long t){
        t = t/1000;
        if(t < 60) {
            return String.valueOf(t) +" s";
        }else if(t > 60&&t < 3600){
            return String.valueOf(t/60) +" min " + String.valueOf((t - (int)(t/60)*60)) +" s";
        }else if(t > 3600){
            return String.valueOf(t/3600) +" h "+ String.valueOf((t - (int)(t/3600)*3600)/60) +" min";
        }
        return "";
    }
    int batteryLevel =0;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        }
    };


    private String getBatteryLevelString(int value){
        return String.valueOf(value*10)+"%";
    }
        @Override
        public void run() {
/*        if(TimelapseSettingsController.settings.valueAnimationParams.lightMeter.enabled){
            cameraController.startBulb((int) sensorLightMeter.getResult());
        }*/

        if(settings.extraMode.index !=-1){
            if(settings.extraMode.index==0&&settings.extraMode.modelId== PtpConstants.Product.NikonD3400) {
                cameraController.onExponsureTimeChange(Camera2PreviewActivity.shutterSpeedValue - Settings.DsleHdrMode.RANGE);
                cameraController.takePhoto();
                cameraController.onExponsureTimeChange(Camera2PreviewActivity.shutterSpeedValue);
                cameraController.takePhoto();
                cameraController.onExponsureTimeChange(Camera2PreviewActivity.shutterSpeedValue + Settings.DsleHdrMode.RANGE);
            }
        }else if(!IS_BT_CONNECTED){
            cameraController.takePhoto();
        }

            if(DslrTimelapseSettingsController.settings.valueAnimationParams.keyframes.size() > 0){ //todo na razie diala tylkod la dslr bo nie napsialem dla telefonu
            log.log("controller size >0");
            for(Settings.ValueAnimationParams.Keyframes k:DslrTimelapseSettingsController.settings.valueAnimationParams.keyframes) {
                log.log("controller size Q");
                switch (k.type) {
                    case CameraParameters.ISO:
                        log.log("controller size ISO");
                        log.log(k.keyframes[counter]);
                        cameraController.onIsoChange(k.keyframes[counter]);
                        break;
                    case CameraParameters.SHUTTER:
                        cameraController.onExponsureTimeChange(k.keyframes[counter]);
                        break;
                    case CameraParameters.APERTURE:
                        cameraController.onFChange(k.keyframes[counter]);
                        break;
                    case CameraParameters.FOCUS:
                        log.log("focusMode");
                        cameraController.onFocusChange(k.keyframes[counter], new CameraParameters.Param[]{cameraController.getFocusSteps()});
                }
            }
            }
            time += settings.speed ;
            if (time < settings.time * 60000 && getBatteryStatus() > settings.batteryLevel) {
                if (isSlider) {
                    try {
                        bluetooth.send();
                    } catch (IOException e) {
                        e.printStackTrace();
                        msg("bt send error");
                    }
                    if (counter < settings.images) { //todo nie bedzie dzialac bo images nie sa nigdzie ustawiaone
                        start();
                    } else {
                        stop();
                    }
                } else {
                    start();
                    progressText.setText(getProgressString());
                    cameraController.getNotifier().onTimelapseStatusChanged(getProgressString());
                }

            } else {
                stop();
            }

        }
    private void msg(String s) {
        Toast.makeText(context.getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    public void start() {
        if(settings.speed < settings.exponsureTime){
            handler.postDelayed(this, settings.exponsureTime +SAFE_INTERVAL);
        }else if(settings.speed < SAFE_INTERVAL){
            handler.postDelayed(this, SAFE_INTERVAL);
        }else {
            handler.postDelayed(this,settings.speed);
        }
        counter++;

    }

    public void stop() {
        cameraController.getNotifier().onTimelapseEnded();
        if(!isDslr){
            autoSizeDetector();
            ImageSaver.counter = 0;
        }
        cameraController.endPreview();
        cameraController.finishAtivity();
        cameraController.getNotifier().onTimelapseEnded();
        handler.removeCallbacks(this);
        try {
            bluetooth.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            msg("cannot disconnet bt");
        }
    }


    private void autoSizeDetector(){
    //    if (counter > 100 && Integer.valueOf(camera.getParameters().get(CameraParameters.getModeString(31))) < 2000000) { ???????????
        if (counter > 100){
            int averangeSize = (initialFreeSpace - getFreeSpace()) / (counter + 1);  // +1 !!!!!
            int maxSize;
            if (TimelapseSettingsController.settings.compression!= 100) {
                maxSize = (int) (averangeSize / (0.953 - 0.2016 * Math.log(100 - TimelapseSettingsController.settings.compression)));
            } else {
                maxSize = (int) (averangeSize / (0.953 - 0.2016 * Math.log(100 - 99.21)));
            }

            SharedPreferences settings = context.getSharedPreferences("SuperLapsePref", 0);
            SharedPreferences.Editor editor = settings.edit();


            editor.putInt("numberOfRecords", TimelapseSettingsController.settings.numberOfRecords + 1);

            for (int k = 0; k < TimelapseSettingsController.settings.resoluionMenu.length; k++) {
                if (TimelapseSettingsController.settings.resoluionMenu[k]) {
                    TimelapseSettingsController.settings.maxPictureSize[k] = maxSize;
                    //  editor.putInt(String.valueOf(k), maxSize);
                    editor.putLong(String.valueOf(k), TimelapseSettingsController.settings.maxPictureSize[k] + maxSize);
                    // break;
                } else {
                //    editor.putLong(String.valueOf(k), 2 * TimelapseSettingsController.settings.maxPictureSize[k]);
                    editor.putLong(String.valueOf(k), TimelapseSettingsController.settings.maxPictureSize[k] +(TimelapseSettingsController.settings.maxPictureSize[k]/ TimelapseSettingsController.settings.numberOfRecords));
                }
            }
            editor.apply();
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

    private int getFreeSpace() {
        StatFs stat;
        if (TimelapseSettingsController.settings.saveOnSD) {
            stat = new StatFs(getExternalSDPath());
        } else {
            stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        }

        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        return (int) (bytesAvailable / (1024));
    }

}
