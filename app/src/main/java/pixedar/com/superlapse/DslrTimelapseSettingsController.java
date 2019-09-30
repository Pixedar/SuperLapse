package pixedar.com.superlapse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Size;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import pixedar.com.superlapse.Camera2.Camera2PreviewActivity;
import pixedar.com.superlapse.Dslr.ptp.Camera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpUsbConnection;
import pixedar.com.superlapse.Dslr.ptp.model.LiveViewData;
import pixedar.com.superlapse.Dslr.ptp.model.StorageInfo;
import pixedar.com.superlapse.util.log;

public class DslrTimelapseSettingsController extends TimelapseSettingsFragment {
    public static Settings settings;
    private static final String DESCRIBABLE_KEY = "describable_key";
    private static final Size[] NIKON_D4000_SUPPORTED_JPEG_SIZES = new Size[]{new Size(6000,4000),new Size(4496,3000),new Size(2992,2000)};
    private static final int NIKON_D3400_COMPRESION_RANGE = 3;
    private PtpCamera camera;
    private int freeSpaceInImages =1;
    private long freeSpaceInMegaBytes  = 1;
    private String saveOnSDText;
/*
    @SuppressLint("ValidFragment")
    public DslrTimelapseSettingsController(PagerAdapter.Model model) {
        DslrTimelapseSettingsController fragment = new DslrTimelapseSettingsController();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, model);
        fragment.setArguments(bundle);

        return fragment;
    }
*/

    public void setCamera(PtpCamera camera){
        this.camera = camera;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    //    PagerAdapter.Model model = (PagerAdapter.Model) getArguments().getSerializable(PagerAdapter.DESCRIBABLE_KEY);
     //   camera = model.getCamera();
    }
    private String getFreeSpace(StorageInfo storageInfo){
    long bytesAvailable = storageInfo.freeSpaceInBytes;
    long megaBytesAvailable =  (bytesAvailable / (1024 * 1024));

    if (megaBytesAvailable > 1024) {
        return " (" + String.format("%.1f", megaBytesAvailable / 1024.0f) + " GB)";
    } else {
        return " (" + String.valueOf(megaBytesAvailable) + " mb)";
    }
    }

    Camera.StorageInfoListener storageInfoListener = new Camera.StorageInfoListener() {
        @Override
        public void onStorageFound(int handle, String label, final StorageInfo storageInfo) {
            if (getActivity() != null) {
            disableSaveOnInternal();
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveOnSD.setText(saveOnSDText + getFreeSpace(storageInfo));
                    }
                });
                freeSpaceInImages = storageInfo.freeSpaceInImages;
                freeSpaceInMegaBytes = storageInfo.freeSpaceInBytes / (1024 * 1024);
                updateTimeBar();
            }
        }

        @Override
        public void onAllStoragesFound() {

        }

        @Override
        public void onImageHandlesRetrieved(int[] handles) {

        }
    };
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setResMenu(NIKON_D4000_SUPPORTED_JPEG_SIZES); //todo
        //  compressionSeekBar[0].setMax(NIKON_D3400_COMPRESION_RANGE-1)
        //  camera.getProperty(Camera.Property.ImgeSize);
        compressionSeekBar[0].setEnabled(false);
        setResMenu();
        updateResolutionMenu(camera.getProperty(Camera.Property.Compression));
        updateTimeBar();

        camera.retrieveStorages(storageInfoListener);
        saveOnSDText = (String) saveOnSD.getText();

        camera.setListener(new Camera.CameraListener() {
            @Override
            public void onCameraReconnected(PtpUsbConnection connection) {

            }

            @Override
            public void onCameraStarted(Camera camera) {

            }

            @Override
            public void onCameraStopped(Camera camera) {

            }

            @Override
            public void onNoCameraFound() {

            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onPropertyChanged(int property, int value) {
                log.log("1");
              //  log.log(property);
                switch(property){
                    case Camera.Property.Compression:
                        camera.retrieveStorages(storageInfoListener);
                        break;
                }

            }

            @Override
            public void onPropertyStateChanged(int property, boolean enabled) {
                log.log("2");
            }

            @Override
            public void onPropertyDescChanged(int property, int[] values) {
                log.log("3");
            }

            @Override
            public void onLiveViewStarted() {

            }

            @Override
            public void onLiveViewData(LiveViewData data) {

            }

            @Override
            public void onLiveViewStopped() {

            }

            @Override
            public void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {

            }

            @Override
            public void onBulbStarted() {

            }

            @Override
            public void onBulbExposureTime(int seconds) {

            }

            @Override
            public void onBulbStopped() {

            }

            @Override
            public void onFocusStarted() {

            }

            @Override
            public void onFocusEnded(boolean hasFocused) {

            }

            @Override
            public void onFocusPointsChanged() {

            }

            @Override
            public void onObjectAdded(int handle, int format) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    void updateTimeBar() {
        settings = settingsT;
        timeSeekBar.setMax(calcMaxTime());
        sizeTextView.setText(getSizeString());
    }
    private int calcMaxTime(){
       return  Math.round(freeSpaceInImages*settings.speed/ (60000.0f));
    }
    private String getSizeString(){
        float ma = (freeSpaceInMegaBytes/freeSpaceInImages)*((settings.time*60000.0f)/settings.speed)/ (1024.0f);
        return String.format("%.2f", ma) + " GB (" + String.valueOf((int) ((ma * 1024 / freeSpaceInMegaBytes ) * 100)) + "%)";
/*
        float ma;
        if ( settings.time == 0) {
            ma = (((max * 60.0f * 1000.0f /  settings.speed) * max) / (1024.0f * 1024.0f));
        } else {
            ma = ((( settings.time * 60.0f * 1000.0f /  settings.speed) * max) / (1024.0f * 1024.0f)); //w gb
        }
            return String.format("%.2f", ma) + " GB (" + String.valueOf((int) ((ma * 1024 / freeSpaceInMegaBytes ) * 100)) + "%)";*/
    }


    @Override
    void onResolutionMenuChanged(int index) {
    //    camera.setProperty(Camera.Property.ImgeSize,4);
        if(index == 3){
            camera.setProperty(Camera.Property.Compression,4);
        }else if(index == 4){
            camera.setProperty(Camera.Property.Compression,7);
        }else{
            camera.setProperty(Camera.Property.Compression,index);
        }


     //   index2++;
    }

    @Override
    void onCompressionChanged(int progress) {

    }
    public static class CustomEvent {

        public PtpCamera eventItem;
        public CustomEvent(PtpCamera eventItem) {
            this.eventItem = eventItem;
        }
    }
    @Override
    void onStartPressed() {
        Intent intent = new Intent(getActivity(), Camera2PreviewActivity.class);
        intent.putExtra("dslr",true);
        intent.putExtra("productId",camera.getProductId());
        startActivity(intent);
        EventBus.getDefault().postSticky(new CustomEvent(camera));
    }

    private void updateCompressionValue(int progress){
        compressionValue[0].setText(getTypeString(progress));
    }


    public void setResMenu(){
        fullHd.setText(getTypeString(0));
        twoK.setText(getTypeString(1));
        fourKC.setText(getTypeString(2));
        fourK.setText(getTypeString(3));
        rawRadioButton.setText(getTypeString(4));

    }
    String getTypeString(int index){
        String extra ="";
        switch (index){
            case 0: extra = "JPEG basic"; break;
            case 1: extra = "JPEG normal"; break;
            case 2: extra = "JPEG fine"; break;
            case 3: extra = "RAW"; break;
            case 4: extra = "RAW+JPEG fine";break;
        }
        return extra;
    }
}
