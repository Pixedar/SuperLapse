package pixedar.com.superlapse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.camera.SCamera;
import com.samsung.android.sdk.camera.SCameraCaptureSession;
import com.samsung.android.sdk.camera.SCameraCharacteristics;
import com.samsung.android.sdk.camera.SCameraDevice;
import com.samsung.android.sdk.camera.SCameraManager;
import com.samsung.android.sdk.camera.SCaptureFailure;
import com.samsung.android.sdk.camera.SCaptureRequest;
import com.samsung.android.sdk.camera.SCaptureResult;
import com.samsung.android.sdk.camera.SDngCreator;
import com.samsung.android.sdk.camera.STotalCaptureResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import pixedar.com.superlapse.Camera2.AutoFitTextureView;
import pixedar.com.superlapse.util.FaceRectView;


public class SamsungCameraPreview extends Activity {
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "SamsungCameraPreview";


    private static final SparseIntArray DNG_ORIENTATION = new SparseIntArray();

    static {
        DNG_ORIENTATION.append(0, ExifInterface.ORIENTATION_NORMAL);
        DNG_ORIENTATION.append(90, ExifInterface.ORIENTATION_ROTATE_90);
        DNG_ORIENTATION.append(180, ExifInterface.ORIENTATION_ROTATE_180);
        DNG_ORIENTATION.append(270, ExifInterface.ORIENTATION_ROTATE_270);
    }

    private SCamera mSCamera;
    private SCameraManager mSCameraManager;
    private SCameraDevice mSCameraDevice;
    private SCameraCaptureSession mSCameraSession;
    private SCameraCharacteristics mCharacteristics;
    private SCaptureRequest.Builder mPreviewBuilder;
    private SCaptureRequest.Builder mCaptureBuilder;


    private Handler handler = new Handler();

    /**
     * Current Preview Size.
     */
    private Size mPreviewSize;
    /**
     * Current Picture Size.
     */
    private Size mPictureSize;
    /**
     * ID of the current {@link com.samsung.android.sdk.camera.SCameraDevice}.
     */
    private String mCameraId;
    /**
     * Lens facing. Camera with this facing will be opened
     */
    private int mLensFacing;
    private List<Integer> mLensFacingList;
    /**
     * Image saving format.
     */
    private int mImageFormat;
    private List<Integer> mImageFormatList;


    private AutoFitTextureView mTextureView;

    private FaceRectView mFaceRectView;
    private ImageReader mJpegReader;
    private ImageReader mRawReader;
    private ImageSaver mImageSaver = new ImageSaver();
    /**
     * A camera related listener/callback will be posted in this handler.
     */
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundHandlerThread;
    /**
     * A image saving worker Runnable will be posted to this handler.
     */
    private Handler mImageSavingHandler;
    private HandlerThread mImageSavingHandlerThread;
    private BlockingQueue<SCaptureResult> mCaptureResultQueue;
    /**
     * An orientation listener for jpeg orientation
     */
    private OrientationEventListener mOrientationListener;
    private int mLastOrientation = 0;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * True if {@link com.samsung.android.sdk.camera.SCaptureRequest#CONTROL_AF_TRIGGER} is triggered.
     */
    private boolean isAFTriggered;
    /**
     * True if {@link com.samsung.android.sdk.camera.SCaptureRequest#CONTROL_AE_PRECAPTURE_TRIGGER} is triggered.
     */
    private boolean isAETriggered;
    /**
     * Current app state.
     */
    private CAMERA_STATE mState = CAMERA_STATE.IDLE;
    /**
     * A {@link com.samsung.android.sdk.camera.SCameraCaptureSession.CaptureCallback} for {@link com.samsung.android.sdk.camera.SCameraCaptureSession#setRepeatingRequest(com.samsung.android.sdk.camera.SCaptureRequest, com.samsung.android.sdk.camera.SCameraCaptureSession.CaptureCallback, android.os.Handler)}
     */

    private boolean started = false;
    private TextView progressValue;
    private Intent batteryStatus;
    private boolean isSlider = false;
    final int[] shutterSpeedValue = {0};
    int ultraHdrCounter =0;
    long ultraHdrSpeedValue = 1000;
    int counter = 0;
    int time = 0;

    public void start() {
        started = true;
        handler.postDelayed(runnable, TimelapseSettingsController.settings.speed);
    }

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    private SCameraCaptureSession.CaptureCallback mSessionCaptureCallback = new SCameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(SCameraCaptureSession session, SCaptureRequest request, STotalCaptureResult result) {
            // Remove comment, if you want to check request/result from console log.
            // dumpCaptureResultToLog(result);
            // dumpCaptureRequestToLog(request);

            boolean sceneOverride = false;

            if (mPreviewBuilder.get(SCaptureRequest.CONTROL_MODE) == SCaptureRequest.CONTROL_MODE_USE_SCENE_MODE &&
                    mPreviewBuilder.get(SCaptureRequest.CONTROL_SCENE_MODE) != SCaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY) {
                sceneOverride = true;
            }

            // Depends on the current state and capture result, app will take next action.
            switch (getState()) {

                case IDLE:
                case TAKE_PICTURE:
                case CLOSING:
                    // do nothing
                    break;
                case PREVIEW:
                    if (result.get(SCaptureResult.STATISTICS_FACES) != null) {
                        processFace(result.get(SCaptureResult.STATISTICS_FACES),
                                result.get(SCaptureResult.SCALER_CROP_REGION));
                    }
                    break;

                // If AF is triggered and AF_STATE indicates AF process is finished, app will trigger AE pre-capture.
                case WAIT_AF: {
                    if (isAFTriggered) {
                        int afState = result.get(SCaptureResult.CONTROL_AF_STATE);
                        // Check if AF is finished.
                        if (SCaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                                SCaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState ||
                                (sceneOverride && // if scene mode is activated, 3A mode can be changed
                                        (result.get(SCaptureResult.CONTROL_AF_MODE) == null || // for the device that does not report the AF_MODE for scene mode.
                                                result.get(SCaptureResult.CONTROL_AF_MODE) == SCaptureResult.CONTROL_AF_MODE_OFF ||
                                                result.get(SCaptureResult.CONTROL_AF_MODE) == SCaptureResult.CONTROL_AF_MODE_EDOF))) {

                            // If AE mode is off or device is legacy device then skip AE pre-capture.
                            if (result.get(SCaptureResult.CONTROL_AE_MODE) != SCaptureResult.CONTROL_AE_MODE_OFF &&
                                    mCharacteristics.get(SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                                triggerAE();
                            } else {
                                takePicture();
                            }
                            isAFTriggered = false;
                        }
                    }
                    break;
                }

                // If AE is triggered and AE_STATE indicates AE pre-capture process is finished, app will take a picture.
                case WAIT_AE: {
                    if (isAETriggered) {
                        Integer aeState = result.get(SCaptureResult.CONTROL_AE_STATE);
                        if (null == aeState || // Legacy device might have null AE_STATE. However, this should not be happened as we skip triggerAE() for legacy device
                                SCaptureResult.CONTROL_AE_STATE_CONVERGED == aeState ||
                                SCaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED == aeState ||
                                SCaptureResult.CONTROL_AE_STATE_LOCKED == aeState ||
                                (sceneOverride && // if scene mode is activated, 3A mode can be changed
                                        (result.get(SCaptureResult.CONTROL_AE_MODE) == null || // for the device that does not report the AE_MODE for scene mode.
                                                result.get(SCaptureResult.CONTROL_AE_MODE) == SCaptureResult.CONTROL_AE_MODE_OFF))) {
                            takePicture();
                            isAETriggered = false;
                        }
                    }
                    break;
                }
            }
        }
    };
    /**
     * A {@link android.media.ImageReader.OnImageAvailableListener} for still capture.
     */
    private ImageReader.OnImageAvailableListener mImageCallback = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
//            if (mImageFormat == ImageFormat.JPEG) mImageSaver.save(reader.acquireNextImage(), createFileName() + ".jpg");
            mImageSaver.save(reader.acquireNextImage());
        }
    };


    @Override
    public void onPause() {
        setState(CAMERA_STATE.CLOSING);

        setOrientationListener(false);

        stopBackgroundThread();
        closeCamera();

        mSCamera = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        setState(CAMERA_STATE.IDLE);

        startBackgroundThread();

        // initialize SCamera
        mSCamera = new SCamera();
        try {
            mSCamera.initialize(this);
        } catch (SsdkUnsupportedException e) {
            showAlertDialog("Fail to initialize SCamera.", true);
            return;
        }

        mCaptureResultQueue = new LinkedBlockingQueue<>();

        setOrientationListener(true);
        createUI();
        checkRequiredFeatures();
        openCamera(mLensFacing);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samsung_camera_preview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
    }

    private void checkRequiredFeatures() {
        try {
            // Find available lens facing value for this device
            Set<Integer> lensFacings = new HashSet<Integer>();
            for (String id : mSCamera.getSCameraManager().getCameraIdList()) {
                SCameraCharacteristics cameraCharacteristics = mSCamera.getSCameraManager().getCameraCharacteristics(id);
                lensFacings.add(cameraCharacteristics.get(SCameraCharacteristics.LENS_FACING));
            }
            mLensFacingList = new ArrayList<>(lensFacings);

            mLensFacing = mLensFacingList.get(mLensFacingList.size() - 1);

            setDefaultJpegSize(mSCamera.getSCameraManager(), mLensFacing);

        } catch (CameraAccessException e) {
            showAlertDialog("Cannot access the camera.", true);
            Log.e(TAG, "Cannot access the camera.", e);
        }
    }

    /**
     * Closes a camera and release resources.
     */
    synchronized private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();

            if (mSCameraSession != null) {
                mSCameraSession.close();
                mSCameraSession = null;
            }

            if (mSCameraDevice != null) {
                mSCameraDevice.close();
                mSCameraDevice = null;
            }

            if (mJpegReader != null) {
                mJpegReader.close();
                mJpegReader = null;
            }

            if (mRawReader != null) {
                mRawReader.close();
                mRawReader = null;
            }

            mSCameraManager = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Configure required transform for {@link android.hardware.camera2.params.Face} to be displayed correctly in the screen.
     */
    private void configureFaceRectTransform() {
        int orientation = getResources().getConfiguration().orientation;
        int degrees = getWindowManager().getDefaultDisplay().getRotation() * 90;

        int result;
        if (mCharacteristics.get(SCameraCharacteristics.LENS_FACING) == SCameraCharacteristics.LENS_FACING_FRONT) {
            result = (mCharacteristics.get(SCameraCharacteristics.SENSOR_ORIENTATION) + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            result = (mCharacteristics.get(SCameraCharacteristics.SENSOR_ORIENTATION) - degrees + 360) % 360;
        }
        mFaceRectView.setTransform(mPreviewSize,
                mCharacteristics.get(SCameraCharacteristics.LENS_FACING),
                result, orientation);
    }

    /**
     * Configures requires transform {@link android.graphics.Matrix} to TextureView.
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else {
            matrix.postRotate(90 * rotation, centerX, centerY);
        }

        mTextureView.setTransform(matrix);
        mTextureView.getSurfaceTexture().setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
    }

    private boolean contains(final int[] array, final int key) {
        for (final int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates file name based on current time.
     */
    private String createFileName() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        long dateTaken = calendar.getTimeInMillis();

        return DateFormat.format("yyyyMMdd_kkmmss", dateTaken).toString();
    }

    /**
     * Create a {@link com.samsung.android.sdk.camera.SCameraCaptureSession} for preview.
     */
    synchronized private void createPreviewSession() {

        if (null == mSCamera
                || null == mSCameraDevice
                || null == mSCameraManager
                || null == mPreviewSize
                || !mTextureView.isAvailable())
            return;

        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            // Set default buffer size to camera preview size.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface surface = new Surface(texture);

            // Creates SCaptureRequest.Builder for preview with output target.
            mPreviewBuilder = mSCameraDevice.createCaptureRequest(SCameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(surface);

            // Creates SCaptureRequest.Builder for still capture with output target.
            mCaptureBuilder = mSCameraDevice.createCaptureRequest(SCameraDevice.TEMPLATE_STILL_CAPTURE);
           // setParameters(SCaptureRequest.FLASH_MODE, 0, mPreviewBuilder, mCaptureBuilder);
          //  setParameters(SCaptureRequest.CONTROL_AF_MODE, 0, mPreviewBuilder, mCaptureBuilder);
            setParameters(SCaptureRequest.TONEMAP_MODE, 2, mPreviewBuilder, mCaptureBuilder);
           // setParameters(SCaptureRequest.EDGE_MODE, 0, mPreviewBuilder, mCaptureBuilder); // 0 wył
           // setParameters(SCaptureRequest.NOISE_REDUCTION_MODE, 2, mPreviewBuilder, mCaptureBuilder);

            // Creates a SCameraCaptureSession here.
            List<Surface> outputSurface = new ArrayList<Surface>();
            outputSurface.add(surface);
            outputSurface.add(mJpegReader.getSurface());
            if (mRawReader != null) outputSurface.add(mRawReader.getSurface());

            mSCameraDevice.createCaptureSession(outputSurface, new SCameraCaptureSession.StateCallback() {
                @Override
                public void onConfigureFailed(SCameraCaptureSession sCameraCaptureSession) {
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    showAlertDialog("Fail to create camera capture session.", true);
                    setState(CAMERA_STATE.IDLE);
                }

                @Override
                public void onConfigured(SCameraCaptureSession sCameraCaptureSession) {
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    mSCameraSession = sCameraCaptureSession;
                    startPreview();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to create camera capture session.", true);
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getState() == CAMERA_STATE.PREVIEW) {

                // No AF lock is required for AF modes OFF/EDOF.
                if (mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) != SCaptureRequest.CONTROL_AF_MODE_OFF &&
                        mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) != SCaptureRequest.CONTROL_AF_MODE_EDOF) {
                    lockAF();

                    // No AE pre-capture is required for AE mode OFF or device is LEGACY.
                } else if (mPreviewBuilder.get(SCaptureRequest.CONTROL_AE_MODE) != SCaptureRequest.CONTROL_AE_MODE_OFF &&
                        mCharacteristics.get(SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    triggerAE();

                    // If AE/AF is skipped, run still capture directly.
                } else {
                    takePicture();
                }
            }

            if (started && time / 10 < TimelapseSettingsController.settings.time * 60 && batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) > TimelapseSettingsController.settings.batteryLevel) {
                if (isSlider) {
                    if (counter < TimelapseSettingsController.settings.images) {
                        start();
                    } else {
                        stop();
                        endPreview();
                    }
                } else {
                    start();
                }

            } else {
                stop();
                endPreview();
            }

        }
    };

    private void endPreview() {
        finish();
    }

    private void createUI() {

        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        mFaceRectView = (FaceRectView) findViewById(R.id.face);

        // Set SurfaceTextureListener that handle life cycle of TextureView
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // "onSurfaceTextureAvailable" is called, which means that SCameraCaptureSession is not created.
                // We need to configure transform for TextureView and crate SCameraCaptureSession.
                configureTransform(width, height);
                createPreviewSession();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // SurfaceTexture size changed, we need to configure transform for TextureView, again.
                configureTransform(width, height);
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        progressValue = (TextView) findViewById(R.id.progressValue);
        final FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.startButton);
        final FloatingActionButton hdrButton = (FloatingActionButton) findViewById(R.id.hdrButton);
        final FloatingActionButton shutterSpeedButton = (FloatingActionButton) findViewById(R.id.shutterSpeedButton);
        final FloatingActionButton isoButton = (FloatingActionButton) findViewById(R.id.isoButton);
        final FloatingActionButton focusButton = (FloatingActionButton) findViewById(R.id.focusButton);
        final SeekBar camSettingsSeekBar = (SeekBar) findViewById(R.id.camSettingsSeekBar);
        final TextView camSettingsValue = (TextView) findViewById(R.id.camSettingsValue);
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.cameraPreviewMainLayout);

        final boolean[] isShutterSpeedEnabled = {false};
        final boolean[] isIsoEnabled = {false};
        final boolean[] isPreviewVisible = {false};
        final boolean[] isHdrEnabled = {false};
        final boolean[] isfocusEnabled= {false};
        final int[] isoValue = {0};
        final int[] focusDistanceValue = {0};

        shutterSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShutterSpeedEnabled[0]) {
                    isShutterSpeedEnabled[0] = false;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsValue.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setEnabled(false);
                } else {
                    isIsoEnabled[0] = false;
                    isfocusEnabled[0] = false;
                    isShutterSpeedEnabled[0] = true;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    focusButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setMax(34);
                    camSettingsSeekBar.setProgress(shutterSpeedValue[0]);
                  //  camSettingsValue.setText("Shutter speed " + "\n" + CameraParameters.getShutter(shutterSpeedValue[0])); ///////////////////    comment due to new method in new api //todo
                    camSettingsSeekBar.setEnabled(true);
                    camSettingsSeekBar.setVisibility(View.VISIBLE);
                    camSettingsValue.setVisibility(View.VISIBLE);
                }
            }

        });

        isoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIsoEnabled[0]) {
                    isIsoEnabled[0] = false;
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsValue.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setEnabled(false);
                } else {
                    isIsoEnabled[0] = true;
                    isShutterSpeedEnabled[0] = false;
                    isfocusEnabled[0] = false;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    focusButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    camSettingsSeekBar.setMax(12);
                    camSettingsSeekBar.setProgress(isoValue[0]);
                    camSettingsValue.setText("ISO " + "\n" + CameraParameters.getIsoString(isoValue[0]));
                    camSettingsSeekBar.setEnabled(true);
                    camSettingsSeekBar.setVisibility(View.VISIBLE);
                    camSettingsValue.setVisibility(View.VISIBLE);
                }

            }
        });

        focusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isfocusEnabled[0]) {
                    isfocusEnabled[0] = false;
                    focusButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsValue.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setEnabled(false);
                } else {
                    isfocusEnabled[0] = true;
                    isShutterSpeedEnabled[0] = false;
                    isIsoEnabled[0] = false;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    focusButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    camSettingsSeekBar.setMax(101);
                    camSettingsSeekBar.setProgress(focusDistanceValue[0]);
                    camSettingsValue.setText("Disctance " + "\n" + CameraParameters.getIsoString(focusDistanceValue[0]));
                    camSettingsSeekBar.setEnabled(true);
                    camSettingsSeekBar.setVisibility(View.VISIBLE);
                    camSettingsValue.setVisibility(View.VISIBLE);
                }
            }
        });

        camSettingsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isShutterSpeedEnabled[0]) {
                    setParameters(SCaptureRequest.CONTROL_AE_MODE, 0, mPreviewBuilder, mCaptureBuilder);
                    setParameters(SCaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 0, mPreviewBuilder, mCaptureBuilder);
                    if (Objects.equals(CameraParameters.getShutterSpeedString(progress), "auto")) {
                        setParameters(SCaptureRequest.CONTROL_AE_MODE, 1, mPreviewBuilder, mCaptureBuilder);
                        setParameters(SCaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 1, mPreviewBuilder, mCaptureBuilder);
                    } else {
                        setParameters(SCaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(CameraParameters.getShutterSpeedString(progress)) * 1000, mPreviewBuilder, mCaptureBuilder);
                    }
                //    camSettingsValue.setText("Shutter speed " + "\n" + CameraParameters.getShutter(progress));///////////////////    comment due to new method in new api //todo
                } else if(isIsoEnabled[0]){
                    setParameters(SCaptureRequest.CONTROL_AE_MODE, 0, mPreviewBuilder, mCaptureBuilder);
                    setParameters(SCaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 0, mPreviewBuilder, mCaptureBuilder);
                    if (Objects.equals(CameraParameters.getIsoString(progress), "auto")) {
                        setParameters(SCaptureRequest.CONTROL_AE_MODE, 1, mPreviewBuilder, mCaptureBuilder);
                        setParameters(SCaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 1, mPreviewBuilder, mCaptureBuilder);
                    } else {
                        setParameters(SCaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(CameraParameters.getIsoString(progress)), mPreviewBuilder, mCaptureBuilder);
                    }
                    camSettingsValue.setText("ISO " + "\n" + CameraParameters.getIsoString(progress));
                }else{
                    setParameters(SCaptureRequest.CONTROL_AF_MODE,0, mPreviewBuilder, mCaptureBuilder);
                    setParameters(SCaptureRequest.PHASE_AF_MODE,0, mPreviewBuilder, mCaptureBuilder);
                    if (progress == 0) {
                        setParameters(SCaptureRequest.CONTROL_AF_MODE,4, mPreviewBuilder, mCaptureBuilder);
                       setParameters(SCaptureRequest.PHASE_AF_MODE,1, mPreviewBuilder, mCaptureBuilder);
                        camSettingsValue.setText("Distance " + "\n" + "auto");
                    } else {
                        setParameters(SCaptureRequest.LENS_FOCUS_DISTANCE, (float)((progress-1.0f)/10.0f), mPreviewBuilder, mCaptureBuilder);
                        camSettingsValue.setText("Distance " + "\n" + String.format("%.2f",(float)((progress-1.0f)/10.0f)));
                    }
                }

                startPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isShutterSpeedEnabled[0]) {
                    shutterSpeedValue[0] = seekBar.getProgress();
                } else if (isIsoEnabled[0]) {
                    isoValue[0] = seekBar.getProgress();
                } else{
                    focusDistanceValue[0] = seekBar.getProgress();
                }
            }
        });

        hdrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHdrEnabled[0]) {
                    isHdrEnabled[0] = true;
                    hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    hdrButton.setImageResource(R.drawable.ic_hdr_on_white_48px);
                    setParameters(SCaptureRequest.CONTROL_LIVE_HDR_LEVEL, 1, mPreviewBuilder, mCaptureBuilder);
                } else {
                    isHdrEnabled[0] = false;
                    setParameters(SCaptureRequest.CONTROL_LIVE_HDR_LEVEL, 0, mPreviewBuilder, mCaptureBuilder);
                    hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    hdrButton.setImageResource(R.drawable.ic_hdr_off_white_48px);
                }
                startPreview();
            }
        });

        layout.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (started) {
                    if (!isPreviewVisible[0]) {
                        mTextureView.setVisibility(View.INVISIBLE);
                        isPreviewVisible[0] = true;
                    } else {
                        mTextureView.setVisibility(View.VISIBLE);
                        isPreviewVisible[0] = false;
                    }

                }
            }
        });

        startButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shutterSpeedValue[0] != 0 || !TimelapseSettingsController.settings.ultraHdrMode) {
                    startButton.setVisibility(View.INVISIBLE);
                    startButton.setOnClickListener(null);

                    hdrButton.setVisibility(View.INVISIBLE);
                    hdrButton.setOnClickListener(null);

                    isoButton.setVisibility(View.INVISIBLE);
                    isoButton.setOnClickListener(null);

                    shutterSpeedButton.setVisibility(View.INVISIBLE);
                    shutterSpeedButton.setOnClickListener(null);

                    focusButton.setVisibility(View.INVISIBLE);
                    focusButton.setOnClickListener(null);

                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setOnSeekBarChangeListener(null);

                    camSettingsValue.setVisibility(View.INVISIBLE);
                }
                if(!TimelapseSettingsController.settings.ultraHdrMode) {
                    start();
                }else {
                    if(shutterSpeedValue[0] != 0) {
                        ultraHdrCounter = shutterSpeedValue[0] - TimelapseSettingsController.settings.ultraHdrRange;
                        if(ultraHdrCounter <=0) {
                            ultraHdrCounter = 1;
                        }
                        captureHdrImages();
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SamsungCameraPreview.this, "You must set shutter speed manually", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void setParameters(SCaptureRequest.Key key, int value, SCaptureRequest.Builder... builders) {
        if (builders != null) {
            for (SCaptureRequest.Builder builder : builders) {
                builder.set(key, value);
            }
        }
    }

    private void setParameters(SCaptureRequest.Key key, long value, SCaptureRequest.Builder... builders) {
        if (builders != null) {
            for (SCaptureRequest.Builder builder : builders) {
                builder.set(key, value);
            }
        }
    }
    private void setParameters(SCaptureRequest.Key key, float value, SCaptureRequest.Builder... builders) {
        if (builders != null) {
            for (SCaptureRequest.Builder builder : builders) {
                builder.set(key, value);
            }
        }
    }

    /**
     * Returns required orientation that the jpeg picture needs to be rotated to be displayed upright.
     */
    private int getJpegOrientation() {
        int degrees = mLastOrientation;

        if (mCharacteristics.get(SCameraCharacteristics.LENS_FACING) == SCameraCharacteristics.LENS_FACING_FRONT) {
            degrees = -degrees;
        }

        return (mCharacteristics.get(SCameraCharacteristics.SENSOR_ORIENTATION) + degrees + 360) % 360;
    }

    /**
     * find optimal preview size for given targetRatio
     */
    private Size getOptimalPreviewSize(Size[] sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.001;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int targetHeight = Math.min(displaySize.y, displaySize.x);

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private CAMERA_STATE getState() {
        return mState;
    }

    private synchronized void setState(CAMERA_STATE state) {
        mState = state;
    }

    /**
     * Starts AF process by triggering {@link com.samsung.android.sdk.camera.SCaptureRequest#CONTROL_AF_TRIGGER_START}.
     */
    private void lockAF() {
        try {
            setState(CAMERA_STATE.WAIT_AF);
            isAFTriggered = false;

            // Set AF trigger to SCaptureRequest.Builder
            mPreviewBuilder.set(SCaptureRequest.CONTROL_AF_TRIGGER, SCaptureRequest.CONTROL_AF_TRIGGER_START);

            // App should send AF triggered request for only a single capture.
            mSCameraSession.capture(mPreviewBuilder.build(), new SCameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(SCameraCaptureSession session, SCaptureRequest request, STotalCaptureResult result) {
                    isAFTriggered = true;
                }
            }, mBackgroundHandler);
            mPreviewBuilder.set(SCaptureRequest.CONTROL_AF_TRIGGER, SCaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to trigger AF", true);
        }
    }

    /**
     * Opens a {@link com.samsung.android.sdk.camera.SCameraDevice}.
     */
    synchronized private void openCamera(int facing) {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                showAlertDialog("Time out waiting to lock camera opening.", true);
            }

            mSCameraManager = mSCamera.getSCameraManager();

            mCameraId = null;

            // Find camera device that facing to given facing parameter.
            for (String id : mSCamera.getSCameraManager().getCameraIdList()) {
                SCameraCharacteristics cameraCharacteristics = mSCamera.getSCameraManager().getCameraCharacteristics(id);
                if (cameraCharacteristics.get(SCameraCharacteristics.LENS_FACING) == facing) {
                    mCameraId = id;
                    break;
                }
            }

            if (mCameraId == null) {
                showAlertDialog("No camera exist with given facing: " + facing, true);
                return;
            }

            // acquires camera characteristics
            mCharacteristics = mSCamera.getSCameraManager().getCameraCharacteristics(mCameraId);

            StreamConfigurationMap streamConfigurationMap = mCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Acquires supported preview size list that supports SurfaceTexture
            mPreviewSize = getOptimalPreviewSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), (double) mPictureSize.getWidth() / mPictureSize.getHeight());

            Log.d(TAG, "Picture Size: " + mPictureSize.toString() + " Preview Size: " + mPreviewSize.toString());

            // Configures an ImageReader
            mJpegReader = ImageReader.newInstance(mPictureSize.getWidth(), mPictureSize.getHeight(), ImageFormat.JPEG, 1);
            mJpegReader.setOnImageAvailableListener(mImageCallback, mImageSavingHandler);

            if (contains(mCharacteristics.get(SCameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES), SCameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                List<Size> rawSizeList = new ArrayList<>();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && streamConfigurationMap.getHighResolutionOutputSizes(ImageFormat.RAW_SENSOR) != null) {
                    rawSizeList.addAll(Arrays.asList(streamConfigurationMap.getHighResolutionOutputSizes(ImageFormat.RAW_SENSOR)));
                }
                rawSizeList.addAll(Arrays.asList(streamConfigurationMap.getOutputSizes(ImageFormat.RAW_SENSOR)));

                Size rawSize = rawSizeList.get(0);

                mRawReader = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(), ImageFormat.RAW_SENSOR, 1);
                mRawReader.setOnImageAvailableListener(mImageCallback, mImageSavingHandler);

                mImageFormatList = Arrays.asList(ImageFormat.JPEG, ImageFormat.RAW_SENSOR);
            } else {
                if (mRawReader != null) {
                    mRawReader.close();
                    mRawReader = null;
                }
                mImageFormatList = Arrays.asList(ImageFormat.JPEG);
            }
            mImageFormat = ImageFormat.RAW_SENSOR;

            // Set the aspect ratio to TextureView
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                mFaceRectView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                mFaceRectView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            // calculate transform matrix for face rect view
            configureFaceRectTransform();

            // Opening the camera device here
            mSCameraManager.openCamera(mCameraId, new SCameraDevice.StateCallback() {
                @Override
                public void onDisconnected(SCameraDevice sCameraDevice) {
                    mCameraOpenCloseLock.release();
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    showAlertDialog("Camera disconnected.", true);
                }

                @Override
                public void onError(SCameraDevice sCameraDevice, int i) {
                    mCameraOpenCloseLock.release();
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    showAlertDialog("Error while camera open.", true);
                }

                public void onOpened(SCameraDevice sCameraDevice) {
                    mCameraOpenCloseLock.release();
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    mSCameraDevice = sCameraDevice;
                    createPreviewSession();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            showAlertDialog("Cannot open the camera.", true);
            Log.e(TAG, "Cannot open the camera.", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Process face information to draw face UI
     */
    private void processFace(final Face[] faces, final Rect zoomRect) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFaceRectView.setFaceRect(faces, zoomRect);
                mFaceRectView.invalidate();
            }
        });
    }

    private void setDefaultJpegSize(SCameraManager manager, int facing) {
        try {
            for (String id : manager.getCameraIdList()) {
                SCameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(SCameraCharacteristics.LENS_FACING) == facing) {
                    List<Size> jpegSizeList = new ArrayList<>();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighResolutionOutputSizes(ImageFormat.JPEG) != null) {
                        jpegSizeList.addAll(Arrays.asList(cameraCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighResolutionOutputSizes(ImageFormat.JPEG)));
                    }
                    jpegSizeList.addAll(Arrays.asList(cameraCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)));
                    mPictureSize = jpegSizeList.get(getSizeNumber());
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot access the camera.", e);
        }
    }

    /**
     * Enable/Disable an orientation listener.
     */
    private void setOrientationListener(boolean isEnable) {
        if (mOrientationListener == null) {

            mOrientationListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation == ORIENTATION_UNKNOWN) return;
                    mLastOrientation = (orientation + 45) / 90 * 90;
                }
            };
        }

        if (isEnable) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
    }

    /**
     * Shows alert dialog.
     */
    private void showAlertDialog(String message, final boolean finishActivity) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Alert")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finishActivity) finish();
                    }
                }).setCancelable(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }


    /**
     * Starts back ground thread that callback from camera will posted.
     */
    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Background Thread");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

        mImageSavingHandlerThread = new HandlerThread("Saving Thread");
        mImageSavingHandlerThread.start();
        mImageSavingHandler = new Handler(mImageSavingHandlerThread.getLooper());
    }

    /**
     * Starts a preview.
     */
    synchronized private void startPreview() {
        if (mSCameraSession == null) return;

        try {
            // Starts displaying the preview.
            mSCameraSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
            setState(CAMERA_STATE.PREVIEW);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to start preview.", true);
        }
    }

    /**
     * Stops back ground thread.
     */
    private void stopBackgroundThread() {
        if (mBackgroundHandlerThread != null) {
            mBackgroundHandlerThread.quitSafely();
            try {
                mBackgroundHandlerThread.join();
                mBackgroundHandlerThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mImageSavingHandlerThread != null) {
            mImageSavingHandlerThread.quitSafely();
            try {
                mImageSavingHandlerThread.join();
                mImageSavingHandlerThread = null;
                mImageSavingHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getSizeNumber() {
        for (int index = 0; index < TimelapseSettingsController.settings.resoluionMenu.length; index++) {
            if (TimelapseSettingsController.settings.resoluionMenu[index]) {
                switch (index) {
                    case 0:
                        return 14;
                    case 1:
                        return 11;
                    case 2:
                        return 1;
                    case 3:
                        return 0;
                }
            }
        }
        return 0;
    }


    /**
     * Take picture.
     */
    private void takePicture() {
        if (getState() == CAMERA_STATE.CLOSING)
            return;

        try {
            // Sets orientation
            mCaptureBuilder.set(SCaptureRequest.JPEG_ORIENTATION, getJpegOrientation());

            if (mImageFormat == ImageFormat.JPEG)
                mCaptureBuilder.addTarget(mJpegReader.getSurface());
            else mCaptureBuilder.addTarget(mRawReader.getSurface());

            mSCameraSession.capture(mCaptureBuilder.build(), new SCameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(SCameraCaptureSession session, SCaptureRequest request, STotalCaptureResult result) {

                    try {
                        mCaptureResultQueue.put(result);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    unlockAF();
                }

                @Override
                public void onCaptureFailed(SCameraCaptureSession session, SCaptureRequest request, SCaptureFailure failure) {
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    showAlertDialog("JPEG Capture failed.", false);
                    unlockAF();
                }
            }, mBackgroundHandler);

            if (mImageFormat == ImageFormat.JPEG)
                mCaptureBuilder.removeTarget(mJpegReader.getSurface());
            else mCaptureBuilder.removeTarget(mRawReader.getSurface());

            setState(CAMERA_STATE.TAKE_PICTURE);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to start preview.", true);
        }

    }

    /**
     * Starts AE pre-capture
     */
    private void triggerAE() {
        try {
            setState(CAMERA_STATE.WAIT_AE);
            isAETriggered = false;

            mPreviewBuilder.set(SCaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, SCaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // App should send AE triggered request for only a single capture.
            mSCameraSession.capture(mPreviewBuilder.build(), new SCameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(SCameraCaptureSession session, SCaptureRequest request, STotalCaptureResult result) {
                    isAETriggered = true;
                }
            }, mBackgroundHandler);
            mPreviewBuilder.set(SCaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, SCaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to trigger AE", true);
        }
    }

    /**
     * Unlock AF.
     */
    private void unlockAF() {
        // If we send TRIGGER_CANCEL. Lens move to its default position. This results in bad user experience.
        if (mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) == SCaptureRequest.CONTROL_AF_MODE_AUTO ||
                mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) == SCaptureRequest.CONTROL_AF_MODE_MACRO) {
            setState(CAMERA_STATE.PREVIEW);
            return;
        }

        // Triggers CONTROL_AF_TRIGGER_CANCEL to return to initial AF state.
        try {
            mPreviewBuilder.set(SCaptureRequest.CONTROL_AF_TRIGGER, SCaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            mSCameraSession.capture(mPreviewBuilder.build(), new SCameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(SCameraCaptureSession session, SCaptureRequest request, STotalCaptureResult result) {
                    if (getState() == CAMERA_STATE.CLOSING)
                        return;
                    setState(CAMERA_STATE.PREVIEW);
                }
            }, mBackgroundHandler);
            mPreviewBuilder.set(SCaptureRequest.CONTROL_AF_TRIGGER, SCaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        } catch (CameraAccessException e) {
            showAlertDialog("Fail to cancel AF", false);
        }
    }

    private enum CAMERA_STATE {
        IDLE, PREVIEW, WAIT_AF, WAIT_AE, TAKE_PICTURE, CLOSING
    }

    private class ImageSaver {
        void save(final Image image) {
/*File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/");
            if (!dir.exists()) dir.mkdirs();
            final File file = new File(dir, filename);*/


            if (image.getFormat() == ImageFormat.RAW_SENSOR) {
                SCaptureResult result = null;
                try {
                    result = mCaptureResultQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    final SDngCreator dngCreator = new SDngCreator(mCharacteristics, result);
                    dngCreator.setOrientation(DNG_ORIENTATION.get(getJpegOrientation()));

                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            FileOutputStream output = null;
                            try {
                                if(TimelapseSettingsController.settings.saveOnSD) {
                                    final DocumentFile file = TimelapseSettingsController.pickedDir.createFile("//MIME type", Integer.toString(counter) + ".dng");
                                    output = (FileOutputStream) getContentResolver().openOutputStream(file.getUri());
                                }else{
                                    File sd = new File(Environment.getExternalStorageDirectory() + "/TimelapseData/", createFileName());
                                    if (!sd.isDirectory()) {
                                        sd.mkdirs();
                                    }
                                  //  output = new FileOutputStream(sd + "/" + Integer.toString(counter) + ".jpg");
                                }
                                dngCreator.writeImage(output, image);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                image.close();
                                dngCreator.close();
                                if (null != output) {
                                    try {
                                  //      zip(output);
                                        output.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            counter++;
                            if(!TimelapseSettingsController.settings.ultraHdrMode) {
                                time += TimelapseSettingsController.settings.speed / 100;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressValue.setText(String.valueOf(counter / 30) + "s " + String.valueOf((int) (((time / 600.0f) / TimelapseSettingsController.settings.time) * 100)) + "% " + String.valueOf(time / 600) + " min");
                                    }
                                });
                            }else if(ultraHdrCounter < shutterSpeedValue[0] + TimelapseSettingsController.settings.ultraHdrRange && ultraHdrCounter <=30){
                                ultraHdrCounter++;
                                captureHdrImages();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressValue.setText(String.valueOf((int) (((ultraHdrCounter) / (shutterSpeedValue[0] + TimelapseSettingsController.settings.ultraHdrRange)) * 100)) + "% ");
                                    }
                                });
                            }else {
                                finish();
                            }

                        }
                    });
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    showAlertDialog("Fail to save DNG file.", false);
                    image.close();
                }
            }
        }
    }

    private void captureHdrImages(){
        setParameters(SCaptureRequest.CONTROL_AE_MODE, 0, mPreviewBuilder, mCaptureBuilder);
        setParameters(SCaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 0, mPreviewBuilder, mCaptureBuilder);

            setParameters(SCaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(CameraParameters.getShutterSpeedString(ultraHdrCounter)) * 1000, mPreviewBuilder, mCaptureBuilder);
            startPreview();
            if (getState() == CAMERA_STATE.PREVIEW) {
                if (mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) != SCaptureRequest.CONTROL_AF_MODE_OFF &&
                        mPreviewBuilder.get(SCaptureRequest.CONTROL_AF_MODE) != SCaptureRequest.CONTROL_AF_MODE_EDOF) {
                    lockAF();
                } else if (mPreviewBuilder.get(SCaptureRequest.CONTROL_AE_MODE) != SCaptureRequest.CONTROL_AE_MODE_OFF &&
                        mCharacteristics.get(SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != SCameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    triggerAE();
                } else {
                    takePicture();
                }
            }

    }

}
