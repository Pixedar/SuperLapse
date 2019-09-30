package pixedar.com.superlapse.Camera2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.util.CircularArray;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import pixedar.com.superlapse.Camera2.LightMeter.DslrSettings;
import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.Dslr.PictureView;
import pixedar.com.superlapse.Dslr.ptp.Camera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpUsbConnection;
import pixedar.com.superlapse.Dslr.ptp.PtpUsbService;
import pixedar.com.superlapse.Dslr.ptp.WorkerNotifier;
import pixedar.com.superlapse.Dslr.ptp.model.LiveViewData;
import pixedar.com.superlapse.DslrTimelapseSettingsController;
import pixedar.com.superlapse.TimelapseSettingsController;
import pixedar.com.superlapse.util.log;

public class DslrController implements CameraController {
    private PtpCamera camera;
    private PictureView view;
    private Map<String, CameraParameters.Param[]> camParams;
    private int height;
    private int width;
    private Context context;
    private final int minDelay = 1000;
    private boolean timelapseStarted = false;
    private boolean focusStackingEnabled = false;
    private boolean showExponsureOnPhoto = true;
    private OnCameraInitListener onCameraInitListener;
    private boolean retrievePicture = true;
    private int direction;
    private boolean changeFocus = false;
    private CircularArray<Bitmap> bitmapCircularArray = new CircularArray<>();
    private final int bitmapArraySize = 60;
    private Handler handler = new Handler();
    private boolean recordBitmaps = false;
    private boolean recordIsPlaying = false;
    DslrController(PictureView view, Map<String, CameraParameters.Param[]> camParams, DisplayMetrics displaymetrics, Context context) {
        this.view = view;
        this.camParams = camParams;
        setViewListeners(view);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;
        this.context = context;

    }
    private void saveBimaps(Bitmap source){
        if(bitmapCircularArray.size() <bitmapArraySize){
            bitmapCircularArray.addLast(getResizedBitmap(source,450));
        }else {
            bitmapCircularArray.popFirst();
            bitmapCircularArray.addLast(getResizedBitmap(source,450));
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setViewListeners(final PictureView view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && camera.isLiveViewOpen() && camera.isLiveViewAfAreaSupported()) {
                    float x = ((event.getX() - v.getLeft()) / width);
                    float y = ((event.getY() - v.getTop()) / height);
                    camera.setLiveViewAfArea(y, x);
                    camera.focus();
                }
                return false;
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int[] index = {0};
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recordBitmaps = true;
                        recordIsPlaying = true;
                       Log.d("ONLONGCLICK",String.valueOf(bitmapCircularArray.size()));
                        log.log(bitmapCircularArray.size());
                        if(bitmapCircularArray.size() ==0){
                            Toast.makeText(context,"started saving photos",Toast.LENGTH_SHORT).show();
                        }
                        if(index[0] < bitmapCircularArray.size()-1) {
                            view.setPicture(bitmapCircularArray.get(index[0]));
                            index[0]++;
                            log.log( index[0]);
                            handler.postDelayed(this,40);
                        }else {
                            recordIsPlaying = true;
                        }
                    }
                });
                return false;
            }
        });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
    public void OnCustomEvent(DslrTimelapseSettingsController.CustomEvent event) {
        camera = event.eventItem;
        onCameraInitListener.initialised();
        setCameraListener(this.camera);
        camera.setProperty(Camera.Property.AfModeSelect, 0x01); // single point  focus //todo dodadc do cameraParameters i menu w ktorym sie wybiera
        this.camera.setLiveView(true);
    }

    private void setCameraListener(final PtpCamera camera) {
        camera.setListener(new Camera.CameraListener() {
            @Override
            public void onCameraReconnected(PtpUsbConnection connection) {
                camera.setConnection(connection);
            }

            @Override
            public void onCameraStarted(Camera camera) {

            }

            @Override
            public void onCameraStopped(Camera camera) {
                log.log("camera stopped");
            }

            @Override
            public void onNoCameraFound() {

            }

            @Override
            public void onError(String message) {
                log.log("on Error");
                log.log(message);
            }

            @Override
            public void onPropertyChanged(int property, int value) {
                log.log(property);
                log.log(value);
                //     str+="case " + String.valueOf(index)+": return 0x"+String.format("%02X ",value)+";\n";
                //     index++;
            }

            @Override
            public void onPropertyStateChanged(int property, boolean enabled) {

            }

            @Override
            public void onPropertyDescChanged(int property, int[] values) {

            }

            @Override
            public void onLiveViewStarted() {
                if (!focusQueue.isEmpty()) {
                    Focus focus = focusQueue.poll();
                    driveLens(focus.getValue(), focus.getParams());
                }
                camera.getLiveViewPicture(null);
            }

            @Override
            public void onLiveViewData(LiveViewData data) {
                view.setLiveViewData(data);
                LiveViewData liveViewData = data;
                camera.getLiveViewPicture(liveViewData);
            }

            @Override
            public void onLiveViewStopped() {
                log.log("liveViewStoped");
            }

            @Override
            public void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
                log.log("onCapturedPictureReceived " + filename + " " + String.valueOf(thumbnail.getWidth()) + " " + String.valueOf(bitmap.getWidth()));
                if(!recordIsPlaying) {
                    if (showExponsureOnPhoto) {
                        view.setPicture(drawExponsureOnBitmap(bitmap));
                    } else {
                        view.setPicture(bitmap);
                    }
                }
                if(recordBitmaps){
                    saveBimaps(bitmap);
                }
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
                if (changeFocus && focusSteps != null) {
                    setFocusSeqence(value, focusSteps, direction);
                    changeFocus = false;
                }
            }

            @Override
            public void onFocusPointsChanged() {

            }

            @Override
            public void onObjectAdded(int handle, int format) {
                if (TimelapseSettingsController.settings.speed >= minDelay && !focusStackingEnabled) {
                    //  if (format != PtpConstants.ObjectFormat.EXIF_JPEG) { //todo
                    if (retrievePicture) {
                        camera.retrievePicture(handle);
                    }
                    //    }
                } else {
                    camera.setLiveView(true);
                }
            }
        });
    }

    @Override
    public void onViewCreated() {
        retrievePicture = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onResume() {
        log.log("onResume");
        retrievePicture = true;
    }

    @Override
    public void onPause() {
        log.log("onPause");
        retrievePicture = false;
    }


    @Override
    public void changeAspectRatio(boolean fillScreen) {

    }

    private Bitmap drawExponsureOnBitmap(Bitmap bitmap) {
        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int[] pixels = new int[myBitmap.getHeight() * myBitmap.getWidth()];
        myBitmap.getPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        for (int i = 0; i < myBitmap.getWidth() * myBitmap.getHeight(); i++) {
            int r = Color.red(pixels[i]);
            int g = Color.green(pixels[i]);
            int b = Color.blue(pixels[i]);
            if (r + g + b == 255 * 3) {
                pixels[i] = Color.RED;
            } else if (r + g + b < 2) {
                pixels[i] = Color.BLUE;
            }
        }
        myBitmap.setPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        return myBitmap;
    }

    @Override
    public void onExponsureTimeChange(int progress) {
        if (camera != null) {
            TimelapseSettingsController.settings.exponsureTime = CameraParameters.getExponusreTimeinMillis(camera.getProductId(), progress);
            if (CameraParameters.isBulbSupported(camera.getProductId()) && progress == 0) {
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(context)
                        .setView(input)
                        .setMessage("Set bulb time")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TimelapseSettingsController.settings.bulbTime = Integer.valueOf(input.getText().toString());
                                TimelapseSettingsController.settings.exponsureTime = TimelapseSettingsController.settings.bulbTime;
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
            camera.setProperty(Camera.Property.ShutterSpeed, (Integer) camParams.get(CameraParameters.SHUTTER)[progress].getValue());
            try {
                DslrSettings.SHUTTER = Integer.valueOf(camParams.get(CameraParameters.SHUTTER)[progress].getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onIsoChange(int progress) {
        if (camera != null) {
            camera.setProperty(Camera.Property.IsoSpeed, (Integer) camParams.get(CameraParameters.ISO)[progress].getValue());
            DslrSettings.ISO = Integer.valueOf(camParams.get(CameraParameters.ISO)[progress].getName());
        }
    }

    @Override
    public void onFChange(int progress) {
        if (camera != null) {
            camera.setProperty(Camera.Property.ApertureValue, (Integer) camParams.get(CameraParameters.APERTURE)[progress].getValue());
            DslrSettings.F_STOP = Integer.valueOf(camParams.get(CameraParameters.APERTURE)[progress].getName());
        }
    }

    @Override
    public WorkerNotifier getNotifier() {
        return PtpUsbService.getWorkerNotifier();
    }

    int value;
    CameraParameters.FocusSteps focusSteps;

    public CameraParameters.FocusSteps getFocusSteps() {
        return (CameraParameters.FocusSteps) CameraParameters.getFocus(camera.getProductId());
    }

    @Override
    public void setExponsureOnPhoto(boolean value) {
        showExponsureOnPhoto = value;
    }

    @Override
    public void setOnCamraInitListener(OnCameraInitListener onCamraInitListener) {
        this.onCameraInitListener = onCamraInitListener;
    }

    private class Focus {
        public int getValue() {
            return value;
        }

        public CameraParameters.Param[] getParams() {
            return params;
        }

        int value;
        CameraParameters.Param[] params;

        public Focus(int value, CameraParameters.Param[] params) {
            this.params = params;
            this.value = value;
        }
    }

    Queue<Focus> focusQueue = new LinkedList<>();

    @Override
    public void onFocusChange(int value, CameraParameters.Param[] params) {
        if (params != null) {
            if (timelapseStarted && focusStackingEnabled) {
                focusQueue.add(new Focus(value, params));
            } else {
                driveLens(value, params);
            }
        } else {
            log.log("paramsIsNull");
            //todo
        }
    }

    private void driveLens(int value, CameraParameters.Param[] params) {
        focusSteps = (CameraParameters.FocusSteps) params[0];
        if (value < 0) {
            direction = Camera.DriveLens.Far;
            this.value = Math.abs(value);
        } else {
            direction = Camera.DriveLens.Near;
            this.value = value;
        }
        changeFocus = true;
        camera.focus();
    }

    private void setFocusSeqence(int value, CameraParameters.FocusSteps focusSteps, int direction) {
        int a = focusSteps.getSoftSteps() / focusSteps.getHardSteps();
        int b = focusSteps.getSoftSteps() / focusSteps.getMediumSteps();
        int hardSteps = (value / a);
        int mediumSteps = (value - hardSteps * a) / b;
        int softSteps = value - hardSteps * a - mediumSteps * b;
        for (int k = 0; k < hardSteps; k++) {
            camera.driveLens(direction, Camera.DriveLens.Hard);
        }
        for (int k = 0; k < mediumSteps; k++) {
            camera.driveLens(direction, Camera.DriveLens.Medium);
        }
        for (int k = 0; k < softSteps; k++) {
            camera.driveLens(direction, Camera.DriveLens.Soft);
        }
    }

    @Override
    public void stopPreview() {
        view.setVisibility(View.INVISIBLE);
        retrievePicture = false;
    }

    @Override
    public void resumePreview() {
        view.setVisibility(View.VISIBLE);
        retrievePicture = true;
    }

    @Override
    public void onSceneModeChanged(int index) {

    }

    @Override
    public void finishAtivity() {
        if (camera.isLiveViewOpen()) {
            camera.setLiveView(false);
        }
        if( astroModeEnabled ){
            camera.setProperty(Camera.Property.noiseReduction,1);
        }
        camera.shutdown();
    }

    public int getCameraBatteryLevel() {
        return camera.getProperty(Camera.Property.BatteryLevel);
    }

    @Override
    public void takePhoto() {
       /* if (focusStackingEnabled) { //todo
            camera.captureNoAf();
        } else {
            camera.capture();
        }*/
        camera.capture(); // zawsze jest captureNoAf bo wczesniej wylaczyłem autofokus
    }

    @Override
    public void endPreview() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public int getIso() {
        return camera.getProperty(Camera.Property.IsoSpeed);
    }

    @Override
    public int getAperture() {
        return camera.getProperty(Camera.Property.ApertureValue);
    }

    @Override
    public int getShutter() {
        return camera.getProperty(Camera.Property.ShutterSpeed);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void startTimelapse() {
        camera.setProperty(Camera.Property.AfModeSelect, 0x004); // turn on manual focus to lock focus // todo
        if(TimelapseController.IS_BT_CONNECTED){
            camera.setProperty(Camera.Property.stillCaptureMode,0x8017);
/*            res.AddValues("Single shot (single-frame shooting)", 0x0001);
            res.AddValues("Continuous high-speed shooting (CH)", 0x0002);
            res.AddValues("Continuous low-speed shooting (CL)", 0x8010);
            res.AddValues("Self-timer", 0x8011);
            res.AddValues("Mirror-up", 0x8012);
            res.AddValues("Quiet shooting", 0x8016);
            res.AddValues("Remote control", 0x8017);*/
        }
        if (TimelapseSettingsController.settings.speed >= minDelay && !focusStackingEnabled) {
            camera.setLiveView(false);
        }
        view.setOnTouchListener(null);
        timelapseStarted = true;

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void startBulb(int time) {

    }


    @Override
    public ValueAnimationDialog onValueAnimationClicked(CameraParameters.Param[] currentParams, int progress, FragmentManager fragmentManager, final String type) {
        ValueAnimationDialog newFragment = new ValueAnimationDialog();
        newFragment.setParams(currentParams, progress, type);
        newFragment.show(fragmentManager, "abc");
        newFragment.setOnPositiveButtonClickedListener(new ValueAnimationDialog.OnPositiveButtonClicked() {
            @Override
            public void onClicked() {
                if (type.equals(CameraParameters.FOCUS)) {
                    focusStackingEnabled = true;
                    if (!camera.isLiveViewOpen()) {
                        camera.setLiveView(true);
                    }
                }
            }
        });
        return newFragment;
    }
    boolean astroModeEnabled = false;

    @Override
    public void onExtraModesClicked(int index) {
        switch (index) {
            case 0:
                TimelapseSettingsController.settings.extraMode.index = index;
                TimelapseSettingsController.settings.extraMode.modelId = camera.getProductId();
                break;
            case 1:
                if (camera.getProductId() == PtpConstants.Product.NikonD3400) {
                    camera.setProperty(Camera.Property.ShutterSpeed, (Integer) camParams.get(CameraParameters.SHUTTER)[1].getValue());
                    camera.setProperty(Camera.Property.ShutterSpeed, (Integer) camParams.get(CameraParameters.ISO)[6].getValue());
                    camera.setProperty(Camera.Property.ShutterSpeed, (Integer) camParams.get(CameraParameters.APERTURE)[0].getValue());
                    camera.setProperty(Camera.Property.noiseReduction,0);
                    for (int k = 0; k < 3; k++) {
                        camera.driveLens(Camera.DriveLens.Far, Camera.DriveLens.Hard);
                    }
                    astroModeEnabled = true;
                }
                break;

        }
    }

    @Override
    public String getExtraModeName(int index) {
        return null;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
