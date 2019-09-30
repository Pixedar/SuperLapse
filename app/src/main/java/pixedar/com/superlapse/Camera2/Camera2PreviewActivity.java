package pixedar.com.superlapse.Camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.camera2.CaptureRequest;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import pixedar.com.superlapse.Camera2.LightMeter.CameraLightMeter;
import pixedar.com.superlapse.Camera2.Utility.ErrorDialog;
import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.Dslr.PictureView;
import pixedar.com.superlapse.DslrTimelapseSettingsController;
import pixedar.com.superlapse.R;
import pixedar.com.superlapse.TimelapseSettingsController;
import pixedar.com.superlapse.util.log;

public class Camera2PreviewActivity extends Activity implements View.OnClickListener {

    private FloatingActionButton startButton;
    private FloatingActionButton hdrButton;
    private FloatingActionButton shutterSpeedButton;
    private FloatingActionButton isoButton;
    private FloatingActionButton ascpectRatioButton;
    private FloatingActionButton autoButton;
    private FloatingActionButton apertureButton;
    private FloatingActionButton focusButton;
    private FloatingActionButton extraModesButton;
    private FloatingActionButton showExposureOnPhotoButton;
    private ArrayList<Button> extrraModesButtons = new ArrayList<>();
    private SeekBar camSettingsSeekBar;
    private TextView camSettingsValue;
    private TextView progressValue;
    private PictureView dslrView;
    private ImageView valueAnimationButton;
    private ImageView focusLockButton;
    private AutoFitTextureView textureView;
    private LinearLayout extraModesLayout;
    private final int buttonDisabledColor = R.color.colorAccent;
    private final int buttonEnabledColor = R.color.black;
    private boolean isHdrEnabled = false;
    private boolean isShutterSpeedEnabled = false;
    private boolean isIsoEnabled = false;
    private boolean isPreviewVisible = false;
    private boolean isFocusEnabled = false;
    private boolean isExtraModesVisible = false;
    private boolean isIsoAnimated = false;
    private boolean isShutterAnimated = false;
    private boolean isApertureAnimated = false;
    private boolean isFocusAnimated = false;
    private boolean timelapseStarted = false;
    private boolean isExponsureOnPhoto = true;
    public static int isoValue = 0;
    public static int shutterSpeedValue = 0;
    public static int aperturedValue = 0;
    private static int focusValue = 0;
    private String type = CameraParameters.ISO;
    private boolean started = false;
    private boolean fillScreen = false;
    private boolean autoModeStatus = false;
    private boolean isApertureEnabled = false;
    private CameraController cameraController;
    private TimelapseController timelapseController;
    private CameraLightMeter cameraLightMeter;
    private Map<String, CameraParameters.Param[]> params;
    private CameraParameters.Param[] currentParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        handleSettingsSlider();
        cameraLightMeter = new CameraLightMeter();
        setParams();
        setController();
        cameraController.setOnCamraInitListener(new CameraController.OnCameraInitListener() {
            @Override
            public void initialised() {
                setCurrentCameraSettings();
            }
        });

        timelapseController = new TimelapseController(getSliderStatus(), getDslrStatus(), cameraController, new Handler(), getApplicationContext(), progressValue);
    }

    private void setCurrentCameraSettings() {
        isoValue = searchValue(cameraController.getIso(), params.get(CameraParameters.ISO));
        aperturedValue = searchValue(cameraController.getAperture(), params.get(CameraParameters.APERTURE));
        shutterSpeedValue = searchValue(cameraController.getShutter(), params.get(CameraParameters.SHUTTER));
    }

    private int searchValue(int val, CameraParameters.Param[] params) {
        for (int k = 0; k < params.length; k++) {
            if (params[k].getValue().intValue() == val) {
                return k;
            }
        }
        return 0;
    }

    private void setParams() {
        if (getIntent().hasExtra("productId")) {
            int productId = getIntent().getIntExtra("productId", 0);
            params = CameraParameters.getParams(productId);
        } else {
            //todo
        }
    }

    @SuppressLint("RestrictedApi")
    private void setController() {
        if (getIntent().hasExtra("dslr")) {
            textureView.setVisibility(View.GONE);
            dslrView.setVisibility(View.VISIBLE);
            apertureButton.setVisibility(View.VISIBLE);
            hdrButton.setVisibility(View.GONE);
            autoButton.setVisibility(View.GONE);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            cameraController = new DslrController(dslrView, params, displaymetrics, Camera2PreviewActivity.this);

        } else {
            cameraController = new Camera2Controller(this, textureView, cameraLightMeter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        cameraController.onViewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraController.onResume();
    }

    @Override
    public void onPause() {
        cameraController.onPause();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton: {
                handleStart();
                //  cameraController.startCapture(getSliderStatus(),autoModeStatus);
                timelapseController.startTimelapse();
                cameraController.startTimelapse();
                timelapseStarted = true;
                break;
            }
            case R.id.hdrButton: {
                handleHdrMode();
                break;
            }
            case R.id.shutterSpeedButton: {
                handleShutterSpeed();
                break;
            }
            case R.id.isoButton: {
                handleIso();
                break;
            }
            case R.id.aspectRatioButton: {
                handleAspectRatio();
                break;
            }
            case R.id.autoModeButton: {
                handleAutoButton();
                break;
            }
            case R.id.value_animation_button: {
                //   handleLightMeterButton();
                handleValueAnimationButton();
                break;
            }
            case R.id.apertureButton: {
                handleApertureButton();
                break;
            }
            case R.id.focusButton: {
                handleFocusButton();
                break;
            }
            case R.id.focus_lock_button: {
                handleRnageLock();
                break;
            }
            case R.id.extraModesButton: {
                handleExtraModes();
                break;
            }
            case R.id.showExponsureOnPhotoButton: {
                handleShowExponsureOnPhoto();
                break;
            }
            case R.id.cameraPreviewMainLayout:
            case R.id.textureView:
                handleScreenSaver();
                break;

        }
    }

    private void handleShowExponsureOnPhoto() {
        if (isExponsureOnPhoto) {
            isExponsureOnPhoto = false;
            showExposureOnPhotoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
            cameraController.setExponsureOnPhoto(isExponsureOnPhoto);
        } else {
            showExposureOnPhotoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonEnabledColor));
            isExponsureOnPhoto = true;
        }
    }

    private void handleExtraModes() {
        if (!isExtraModesVisible) {
            extraModesLayout.setVisibility(View.VISIBLE);
            extraModesButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonEnabledColor));
            for (final Button b : extrraModesButtons) {
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraController.onExtraModesClicked(extrraModesButtons.indexOf(b));
                        b.setOnClickListener(null);
                        extraModesButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
                        extraModesLayout.setVisibility(View.GONE);
                        isExtraModesVisible = false;
                    }
                });
            }
            isExtraModesVisible = true;
        } else {
            extraModesButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
            extraModesLayout.setVisibility(View.GONE);
            isExtraModesVisible = false;
        }
    }

    private void handleRnageLock() {
        new AlertDialog.Builder(Camera2PreviewActivity.this)
                .setMessage("Lock value?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (camSettingsSeekBar.getProgress() - (camSettingsSeekBar.getMax() / 2) + fousLowerBound < 0) {
                            fousLowerBound = camSettingsSeekBar.getProgress();
                        } else {
                            focusUpperBound = camSettingsSeekBar.getProgress();
                        }
                        log.log(fousLowerBound);
                        log.log(focusUpperBound);
                        log.log(((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps() * 2 - (((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps() * 2 - focusUpperBound) - fousLowerBound);
                        camSettingsSeekBar.setMax(((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps() * 2 - (((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps() * 2 - focusUpperBound) - fousLowerBound);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private ValueAnimationDialog newFragment;

    private void handleValueAnimationButton() {
        if (newFragment == null) {
            initDialog();
        } else if (!newFragment.isShowing()) {
            newFragment.addKeyFrame(camSettingsSeekBar.getProgress());
            Toast.makeText(getApplicationContext(), "new keyframe added " + camSettingsValue.getText(), Toast.LENGTH_SHORT).show();
        } else if (newFragment.isDestroyed()) {
            initDialog();
        }
    }

    private void initDialog() {
        newFragment = cameraController.onValueAnimationClicked(currentParams, camSettingsSeekBar.getProgress(), getFragmentManager(), type);
        newFragment.setOnAddButtonHoldListener(new ValueAnimationDialog.OnButtonHoldListener() {
            @Override
            public void onButtonHold(boolean isShowing) {
                if (isShowing) {
                    valueAnimationButton.setImageResource(R.drawable.ic_animate_icon);
                } else {
                    valueAnimationButton.setImageResource(R.drawable.ic_add_circle_cion);
                }
            }
        });
        newFragment.setOnPositiveButtonClickedListener(new ValueAnimationDialog.OnPositiveButtonClicked() {
            @Override
            public void onClicked() {
                if (isIsoEnabled) {
                    isoButton.setEnabled(false);
                    isoButton.setImageResource(R.drawable.ic_animate_icon);
                    isoButton.setAlpha(0.25f);
                    isIsoAnimated = true;
                    updateButton(2);
                } else if (isApertureEnabled) {
                    apertureButton.setEnabled(false);
                    apertureButton.setImageResource(R.drawable.ic_animate_icon);
                    apertureButton.setAlpha(0.25f);
                    isApertureAnimated = true;
                    updateButton(4);
                } else if (isShutterSpeedEnabled) {
                    shutterSpeedButton.setEnabled(false);
                    isShutterAnimated = true;
                    shutterSpeedButton.setImageResource(R.drawable.ic_animate_icon);
                    shutterSpeedButton.setAlpha(0.25f);
                    updateButton(3);
                } else if (isFocusEnabled) {
                    focusButton.setEnabled(false);
                    isFocusAnimated = true;
                    focusButton.setImageResource(R.drawable.ic_animate_icon);
                    focusButton.setAlpha(0.25f);
                    updateButton(5);
                }
                hideCamSettingsSlider();
            }
        });
        valueAnimationButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                valueAnimationButton.setImageResource(R.drawable.ic_animate_icon);
                newFragment.show();
                return false;
            }
        });
    }

    private boolean updateButton(int index) {
        setDisabled(index);
        switch (index) {
            case 2:
                isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(isIsoEnabled ? buttonDisabledColor : buttonEnabledColor));
                return isIsoEnabled = !isIsoEnabled;
            case 3:
                shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(isShutterSpeedEnabled ? buttonDisabledColor : buttonEnabledColor));
                return isShutterSpeedEnabled = !isShutterSpeedEnabled;
            case 4:
                apertureButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(isApertureEnabled ? buttonDisabledColor : buttonEnabledColor));
                return isApertureEnabled = !isApertureEnabled;
            case 5:
                focusButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(isApertureEnabled ? buttonDisabledColor : buttonEnabledColor));
                return isFocusEnabled = !isFocusEnabled;
            default:
                return false;
        }
    }

    private void setDisabled(int index) {
        boolean flag = false;
        switch (index) {
            case 2:
                flag = isIsoEnabled;
                break;
            case 3:
                flag = isShutterSpeedEnabled;
                break;
            case 4:
                flag = isApertureEnabled;
                break;
            case 5:
                flag = isFocusEnabled;
        }
        isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
        isIsoEnabled = false;
        shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
        isShutterSpeedEnabled = false;
        apertureButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(buttonDisabledColor));
        isApertureEnabled = false;
        switch (index) {
            case 2:
                isIsoEnabled = flag;
            case 3:
                isShutterSpeedEnabled = flag;
            case 4:
                isApertureEnabled = flag;
            case 5:
                isFocusEnabled = flag;
        }
    }


    private void showCamSettingsSlider(int max, int progress, String value) {
        camSettingsSeekBar.setMax(max);
        camSettingsSeekBar.setProgress(progress);
        camSettingsValue.setText(value);
        camSettingsSeekBar.setEnabled(true);
        camSettingsSeekBar.setVisibility(View.VISIBLE);
        camSettingsValue.setVisibility(View.VISIBLE);
        if (!timelapseStarted) {
            valueAnimationButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideCamSettingsSlider() {
        camSettingsSeekBar.setVisibility(View.INVISIBLE);
        camSettingsValue.setVisibility(View.INVISIBLE);
        camSettingsSeekBar.setVisibility(View.INVISIBLE);
        valueAnimationButton.setVisibility(View.GONE);
    }

    /*    private void handleLightMeterButton(){
            if(lightMeterStatus){
                lightMeterStatus = false;
                lightMeterButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                camSettingsSeekBar.setVisibility(View.INVISIBLE);
                camSettingsValue.setVisibility(View.INVISIBLE);
                camSettingsSeekBar.setVisibility(View.INVISIBLE);
            }else{
                lightMeterButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                camSettingsSeekBar.setMax(98);
                camSettingsSeekBar.setProgress((int) (CameraLightMeter.IIR_FILTER_COEFFICIENT*100)-1);
                camSettingsSeekBar.setVisibility(View.VISIBLE);
                camSettingsValue.setVisibility(View.VISIBLE);
                lightMeterStatus = true;
            }
            cameraController.enableLightMeter(lightMeterStatus);
        }*/
    @SuppressLint("RestrictedApi")
    private void handleAutoButton() {
        if (autoModeStatus) {
            autoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
            autoModeStatus = false;
            isoButton.setVisibility(View.GONE);
            hdrButton.setVisibility(View.GONE);
            shutterSpeedButton.setVisibility(View.GONE);
        } else {
            autoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
            autoModeStatus = true;
            isoButton.setVisibility(View.VISIBLE);
            hdrButton.setVisibility(View.VISIBLE);
            shutterSpeedButton.setVisibility(View.VISIBLE);
        }
    }

    private void handleAspectRatio() {
        cameraController.changeAspectRatio(fillScreen);
        fillScreen = !fillScreen;
    }

    public static int focusUpperBound = 0;
    public static int fousLowerBound = 0;
    private int lastProgress = 0;

    private void handleSettingsSlider() {
        camSettingsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isShutterSpeedEnabled) {
                    camSettingsValue.setText("Shutter speed " + "\n" + params.get(CameraParameters.SHUTTER)[progress].getName());
                } else if (isIsoEnabled) {
                    camSettingsValue.setText("ISO " + "\n" + params.get(CameraParameters.ISO)[progress].getName());
                } else if (isApertureEnabled) {
                    camSettingsValue.setText("F " + "\n" + params.get(CameraParameters.APERTURE)[progress].getName());
                } else if (isFocusEnabled) {
                    int focusVal = (seekBar.getProgress() + fousLowerBound) - ((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps();
                    if (focusVal > 0) {
                        camSettingsValue.setText("+" + String.valueOf(focusVal));
                    } else {
                        camSettingsValue.setText(String.valueOf(focusVal));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                lastProgress = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!timelapseStarted) {
                    setCameraSettings(seekBar.getProgress());
                } else if (lastProgress != seekBar.getProgress()) {
                    showSmoothTransitionDialog(lastProgress, seekBar.getProgress());
                }
            }
        });
    }

    private void showSmoothTransitionDialog(final int last, final int current) {
        new AlertDialog.Builder(Camera2PreviewActivity.this)
                .setMessage("Smooth transition?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DslrTimelapseSettingsController.settings.valueAnimationParams.setKeyframes(type, calcKeyFrames(last, current)); //todo
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCameraSettings(current);
                    }
                })
                .show();
    }

    private int[] calcKeyFrames(int last, int current) {
        final int numberOfImages = ((DslrTimelapseSettingsController.settings.time * 60000) / TimelapseSettingsController.settings.speed); //todo
        int[] frames = new int[numberOfImages];
        int index = last;
        for (int k = timelapseController.getCounter(); k < frames.length; k++) {
            frames[k] = index;
            if (index != current) {
                if (last > current) {
                    index--;
                } else if (last < current) {
                    index++;
                }
            }
        }
        return frames;
    }

    private void setCameraSettings(int progress) {
        if (isShutterSpeedEnabled) {
            shutterSpeedValue = progress;
            cameraController.onExponsureTimeChange(progress);
        } else if (isIsoEnabled) {
            isoValue = progress;
            cameraController.onIsoChange(progress);
        } else if (isFocusEnabled) {
            focusValue = progress;
            cameraController.onFocusChange((progress + fousLowerBound) - ((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps(), currentParams);
        }
    }

    private void handleScreenSaver() {
        if (!started && !autoModeStatus) {
            if (!isPreviewVisible) {
                cameraController.stopPreview();
                if (textureView.getVisibility() == View.GONE) {
                    dslrView.setVisibility(View.INVISIBLE);
                } else {
                    textureView.setVisibility(View.INVISIBLE);
                }
                isPreviewVisible = true;
            } else {
                if (textureView.getVisibility() == View.GONE) {
                    dslrView.setVisibility(View.VISIBLE);
                } else {
                    textureView.setVisibility(View.VISIBLE);
                }
                cameraController.resumePreview();

                isPreviewVisible = false;
            }
        } else if (autoModeStatus) {
            ErrorDialog.buildErrorDialog("Energy saving mode is unavailable with current settings").
                    show(this.getFragmentManager(), "dialog");
        }
    }

    private void handleIso() {
        if (updateButton(2)) {
            currentParams = params.get(CameraParameters.ISO);
            type = CameraParameters.ISO;
            showCamSettingsSlider(currentParams.length - 1, isoValue, "ISO " + "\n" + currentParams[isoValue].getName());
        } else {
            hideCamSettingsSlider();
        }
    }

    private void handleShutterSpeed() {
        if (updateButton(3)) {
            currentParams = params.get(CameraParameters.SHUTTER);
            type = CameraParameters.SHUTTER;
            showCamSettingsSlider(currentParams.length - 1, shutterSpeedValue, "Shutter speed " + "\n" + currentParams[shutterSpeedValue].getName());
        } else {
            hideCamSettingsSlider();
        }
    }

    private void handleApertureButton() {
        if (updateButton(4)) {
            currentParams = params.get(CameraParameters.APERTURE);
            type = CameraParameters.APERTURE;
            showCamSettingsSlider(currentParams.length - 1, aperturedValue, currentParams[aperturedValue].getName());
        } else {
            hideCamSettingsSlider();
        }
    }

    private void handleFocusButton() {
        if (updateButton(5)) {
            focusLockButton.setVisibility(View.VISIBLE);
            currentParams = params.get(CameraParameters.FOCUS);
            type = CameraParameters.FOCUS;
            if (currentParams[0].getValue().intValue() == CameraParameters.FOCUS_STEPS) {
                showCamSettingsSlider(((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps() * 2, ((CameraParameters.FocusSteps) currentParams[0]).getSoftSteps(), String.valueOf(focusValue));
            } else {
                //todo
            }

        } else {
            hideCamSettingsSlider();
            focusLockButton.setVisibility(View.GONE);
        }
        focusUpperBound = camSettingsSeekBar.getMax();
        fousLowerBound = 0;

    }

    private void handleHdrMode() {
        if (!isHdrEnabled) {
            isHdrEnabled = true;
            hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
            hdrButton.setImageResource(R.drawable.ic_hdr_on_white_48px);
            cameraController.onSceneModeChanged(CaptureRequest.CONTROL_SCENE_MODE_HDR);
        } else {
            isHdrEnabled = false;
            hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
            hdrButton.setImageResource(R.drawable.ic_hdr_off_white_48px);
            cameraController.onSceneModeChanged(-1);
        }
    }

    @SuppressLint("RestrictedApi")
    private void handleStart() {

        startButton.setVisibility(View.GONE);
        startButton.setOnClickListener(null);

        hdrButton.setVisibility(View.GONE);
        hdrButton.setOnClickListener(null);

        if (isIsoAnimated) {
            isoButton.setVisibility(View.GONE);
            isoButton.setOnClickListener(null);
        }
        if (isShutterAnimated) {
            shutterSpeedButton.setVisibility(View.GONE);
            shutterSpeedButton.setOnClickListener(null);
        }
        if (isApertureAnimated) {
            apertureButton.setVisibility(View.GONE);
            apertureButton.setOnClickListener(null);
        }
        if (isFocusAnimated) {
            focusButton.setVisibility(View.GONE);
            focusButton.setOnClickListener(null);
        }
/*
        camSettingsSeekBar.setVisibility(View.GONE);
        camSettingsSeekBar.setOnSeekBarChangeListener(null);
*/

        ascpectRatioButton.setVisibility(View.GONE);
        ascpectRatioButton.setOnClickListener(null);

        autoButton.setVisibility(View.GONE);
        autoButton.setOnClickListener(null);

        extraModesButton.setVisibility(View.GONE);
        extraModesButton.setOnClickListener(null);

        camSettingsValue.setVisibility(View.GONE);
        showExposureOnPhotoButton.setVisibility(View.VISIBLE);
        started = true;
    }

    private boolean getSliderStatus() {
        return getIntent().getBooleanExtra("slider", false);
    }

    private boolean getDslrStatus() {
        return getIntent().getBooleanExtra("dslr", false);
    }

    private int getBatteryStatus() {
        return getIntent().getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    private void init() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_preview);
        startButton = (FloatingActionButton) findViewById(R.id.startButton);
        hdrButton = (FloatingActionButton) findViewById(R.id.hdrButton);
        shutterSpeedButton = (FloatingActionButton) findViewById(R.id.shutterSpeedButton);
        ascpectRatioButton = (FloatingActionButton) findViewById(R.id.aspectRatioButton);
        focusButton = (FloatingActionButton) findViewById(R.id.focusButton);
        isoButton = (FloatingActionButton) findViewById(R.id.isoButton);
        autoButton = (FloatingActionButton) findViewById(R.id.autoModeButton);
        apertureButton = (FloatingActionButton) findViewById(R.id.apertureButton);
        extraModesButton = (FloatingActionButton) findViewById(R.id.extraModesButton);
        showExposureOnPhotoButton = (FloatingActionButton) findViewById(R.id.showExponsureOnPhotoButton);
        extraModesLayout = findViewById(R.id.extra_modes_layout);
        valueAnimationButton = findViewById(R.id.value_animation_button);
        camSettingsSeekBar = (SeekBar) findViewById(R.id.camSettingsSeekBar);
        camSettingsValue = (TextView) findViewById(R.id.camSettingsValue);
        progressValue = (TextView) findViewById(R.id.progress_text);
        textureView = (AutoFitTextureView) findViewById(R.id.textureView);
        dslrView = (PictureView) findViewById(R.id.dslrView);
        focusLockButton = findViewById(R.id.focus_lock_button);
        initExtraModesButtons();
        findViewById(R.id.cameraPreviewMainLayout).setOnClickListener(this);
        startButton.setOnClickListener(this);
        hdrButton.setOnClickListener(this);
        shutterSpeedButton.setOnClickListener(this);
        isoButton.setOnClickListener(this);
        textureView.setOnClickListener(this);
        ascpectRatioButton.setOnClickListener(this);
        autoButton.setOnClickListener(this);
        focusButton.setOnClickListener(this);
        valueAnimationButton.setOnClickListener(this);
        apertureButton.setOnClickListener(this);
        focusLockButton.setOnClickListener(this);
        extraModesButton.setOnClickListener(this);
        showExposureOnPhotoButton.setOnClickListener(this);
    }

    private void initExtraModesButtons() {
        final String prefix = "extra_modes_button";
        int index = 0;
        int resID;
        while (true) {
            resID = getResources().getIdentifier(prefix + String.valueOf(index), "id", getPackageName());
            Button btn = findViewById(resID);
            if (btn == null) {
                break;
            }
            extrraModesButtons.add(btn);
            index++;
        }
    }

    @Override
    public void onBackPressed() {
        if (timelapseController != null && started) {
            timelapseController.stop();
        }
        try {
            finish();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onStop() {
        //   timelapseController.stop(); //todo dla tlefonu powinno stopowac  wznawiac dla dslr dziala w tle
        super.onStop();
    }
}
