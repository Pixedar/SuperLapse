package pixedar.com.superlapse.Camera2;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pixedar.com.superlapse.Camera2.LightMeter.CameraLightMeter;
import pixedar.com.superlapse.Camera2.Utility.CompareSizesByArea;
import pixedar.com.superlapse.Camera2.Utility.ErrorDialog;
import pixedar.com.superlapse.Camera2.Utility.RefCountedAutoCloseable;
import pixedar.com.superlapse.Camera2.Utility.TextureTransform;
import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.Dslr.ptp.WorkerNotifier;
import pixedar.com.superlapse.TimelapseSettingsController;

public class Camera2Controller implements CameraController {
    public Activity activity;
    public AutoFitTextureView mTextureView;
    private boolean fillScreen = true;
    private ThreeAPreCaptureCallback threeAPreCaptureCallback;
    public  Camera2Controller(Activity activity, pixedar.com.superlapse.Camera2.AutoFitTextureView textureView, CameraLightMeter cameraLightMeter){
        this.activity = activity;
        mTextureView = textureView;
        threeAPreCaptureCallback =new ThreeAPreCaptureCallback(this,cameraLightMeter);
        setDate();
    }

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
        /**
         * Timeout for the pre-capture sequence.
         */
        private static final long PRECAPTURE_TIMEOUT_MS = 1000;

        /**
         * Tag for the {@link Log}.
         */
        public static final String TAG = "Camera2Controller";

        /**
         * Camera state: Device is closed.
         */
        public static final int STATE_CLOSED = 0;

        /**
         * Camera state: Device is opened, but is not capturing.
         */
        private static final int STATE_OPENED = 1;

        /**
         * Camera state: Showing camera preview.
         */
        public static final int STATE_PREVIEW = 2;

        /**
         * Camera state: Waiting for 3A convergence before capturing a photo.
         */
        public static final int STATE_WAITING_FOR_3A_CONVERGENCE = 3;

        /**
         * An {@link OrientationEventListener} used to determine when device rotation has occurred.
         * This is mainly necessary for when the device is rotated by 180 degrees, in which case
         * onCreate or onConfigurationChanged is not called as the view dimensions remain the same,
         * but the orientation of the has changed, and thus the preview rotation must be updated.
         */
        private OrientationEventListener mOrientationListener;

        /**
         * {@link TextureView.SurfaceTextureListener} handles several lifecycle events of a
         * {@link TextureView}.
         */
        private final TextureView.SurfaceTextureListener mSurfaceTextureListener
                = new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                TextureTransform.configureTransform(Camera2Controller.this,width, height,fillScreen);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                TextureTransform.configureTransform(Camera2Controller.this,width, height,fillScreen);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                synchronized (mCameraStateLock) {
                    mPreviewSize = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }

        };

        /**
         * An {@link pixedar.com.superlapse.util.AutoFitTextureView} for camera preview.
         */


        /**
         * An additional thread for running tasks that shouldn't block the UI.  This is used for all
         * callbacks from the {@link CameraDevice} and {@link CameraCaptureSession}s.
         */
        private HandlerThread mBackgroundThread;

        /**
         * A counter for tracking corresponding {@link CaptureRequest}s and {@link CaptureResult}s
         * across the {@link CameraCaptureSession} capture callbacks.
         */
        private final AtomicInteger mRequestCounter = new AtomicInteger();

        /**
         * A {@link Semaphore} to prevent the app from exiting before closing the camera.
         */
        private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

        /**
         * A lock protecting camera state.
         */
        public final Object mCameraStateLock = new Object();

        // *********************************************************************************************
        // State protected by mCameraStateLock.
        //
        // The following state is used across both the UI and background threads.  Methods with "Locked"
        // in the name expect mCameraStateLock to be held while calling.

        /**
         * ID of the current {@link CameraDevice}.
         */
        private String mCameraId;

        /**
         * A {@link CameraCaptureSession } for camera preview.
         */
        private CameraCaptureSession mCaptureSession;

        /**
         * A reference to the open {@link CameraDevice}.
         */
        private CameraDevice mCameraDevice;

        /**
         * The {@link Size} of camera preview.
         */
        public Size mPreviewSize;

        /**
         * The {@link CameraCharacteristics} for the currently configured camera device.
         */
        public CameraCharacteristics mCharacteristics;

        /**
         * A {@link Handler} for running tasks in the background.
         */
        private Handler mBackgroundHandler;

        /**
         * A reference counted holder wrapping the {@link ImageReader} that handles JPEG image
         * captures. This is used to allow us to clean up the {@link ImageReader} when all background
         * tasks using its {@link Image}s have completed.
         */
        private RefCountedAutoCloseable<ImageReader> mJpegImageReader;

        /**
         * A reference counted holder wrapping the {@link ImageReader} that handles RAW image captures.
         * This is used to allow us to clean up the {@link ImageReader} when all background tasks using
         * its {@link Image}s have completed.
         */
        private RefCountedAutoCloseable<ImageReader> mRawImageReader;

        /**
         * Whether or not the currently configured camera device is fixed-focus.
         */
        public boolean mNoAFRun = false;

        /**
         * Number of pending user requests to capture a photo.
         */
        public int mPendingUserCaptures = 0;

        /**
         * Request ID to {@link ImageSaver.ImageSaverBuilder} mapping for in-progress JPEG captures.
         */
        private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mJpegResultQueue = new TreeMap<>();

        /**
         * Request ID to {@link ImageSaver.ImageSaverBuilder} mapping for in-progress RAW captures.
         */
        private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mRawResultQueue = new TreeMap<>();

        /**
         * {@link CaptureRequest.Builder} for the camera preview
         */
        private CaptureRequest.Builder mPreviewRequestBuilder;


        public int mState = STATE_CLOSED;

        /**
         * Timer to use with pre-capture sequence to ensure a timely capture if 3A convergence is
         * taking too long.
         */
        private long mCaptureTimer;

        //**********************************************************************************************

        /**
         * {@link CameraDevice.StateCallback} is called when the currently active {@link CameraDevice}
         * changes its state.
         */

        private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice cameraDevice) {
                // This method is called when the camera is opened.  We start camera preview here if
                // the TextureView displaying this has been set up.
                synchronized (mCameraStateLock) {
                    mState = STATE_OPENED;
                    mCameraOpenCloseLock.release();
                    mCameraDevice = cameraDevice;

                    // Start the preview session if the TextureView has been set up already.
                    if (mPreviewSize != null && mTextureView.isAvailable()) {
                        createCameraPreviewSessionLocked();
                    }
                }
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                synchronized (mCameraStateLock) {
                    mState = STATE_CLOSED;
                    mCameraOpenCloseLock.release();
                    cameraDevice.close();
                    mCameraDevice = null;
                }
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                Log.e(TAG, "Received camera device error: " + error);
                synchronized (mCameraStateLock) {
                    mState = STATE_CLOSED;
                    mCameraOpenCloseLock.release();
                    cameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != activity) {
                    activity.finish();
                }
            }

        };

        /**
         * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
         * JPEG image is ready to be saved.
         */
        private final ImageReader.OnImageAvailableListener mOnJpegImageAvailableListener
                = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                dequeueAndSaveImage(mJpegResultQueue, mJpegImageReader);
            }

        };


        /**
         * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
         * RAW image is ready to be saved.
         */
        private final ImageReader.OnImageAvailableListener mOnRawImageAvailableListener
                = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                dequeueAndSaveImage(mRawResultQueue, mRawImageReader);
            }

        };



        /**
         * A {@link CameraCaptureSession.CaptureCallback} that handles the still JPEG and RAW capture
         * request.
         */
        private final CameraCaptureSession.CaptureCallback mCaptureCallback
                = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                         long timestamp, long frameNumber) {
                String currentDateTime = generateTimestamp();
                File rawFile = new File(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "RAW_" + currentDateTime + ".dng");
                File jpegFile = new File(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "JPEG_" + currentDateTime + ".jpg");

                // Look up the ImageSaverBuilder for this request and update it with the file name
                // based on the capture start time.
                ImageSaver.ImageSaverBuilder jpegBuilder;
                ImageSaver.ImageSaverBuilder rawBuilder;
                int requestId = (int) request.getTag();
                synchronized (mCameraStateLock) {
                    jpegBuilder = mJpegResultQueue.get(requestId);
                    rawBuilder = mRawResultQueue.get(requestId);
                }

                if (jpegBuilder != null) jpegBuilder.setFile(jpegFile);
                if (rawBuilder != null) rawBuilder.setFile(rawFile);
            }

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                           TotalCaptureResult result) {
                int requestId = (int) request.getTag();
                ImageSaver.ImageSaverBuilder jpegBuilder;
                ImageSaver.ImageSaverBuilder rawBuilder;
                StringBuilder sb = new StringBuilder();

                // Look up the ImageSaverBuilder for this request and update it with the CaptureResult
                synchronized (mCameraStateLock) {
                    jpegBuilder = mJpegResultQueue.get(requestId);
                    rawBuilder = mRawResultQueue.get(requestId);

                    if (jpegBuilder != null) {
                       jpegBuilder.setResult(result);
                   //     sb.append("Saving JPEG as: ");
                   //     sb.append(jpegBuilder.getSaveLocation());
                    }
                    if (rawBuilder != null) {
                        rawBuilder.setResult(result);
                   //     if (jpegBuilder != null) sb.append(", ");
                    //    sb.append("Saving RAW as: ");
                      //  sb.append(rawBuilder.getSaveLocation());
                    }

                    // If we have all the results necessary, save the image to a file in the background.
                    handleCompletionLocked(requestId, jpegBuilder, mJpegResultQueue);
                    handleCompletionLocked(requestId, rawBuilder, mRawResultQueue);

                    finishedCaptureLocked();
                }

               // showToast(sb.toString());
            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureFailure failure) {
                int requestId = (int) request.getTag();
                synchronized (mCameraStateLock) {
                    mJpegResultQueue.remove(requestId);
                    mRawResultQueue.remove(requestId);
                    finishedCaptureLocked();
                }
                showToast("Capture failed!");
            }

        };

        /**
         * A {@link Handler} for showing {@link Toast}s on the UI thread.
         */
        private final Handler mMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                
                if (activity != null) {
                    Toast.makeText(activity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                }
            }
        };


        private Size getJpegSize(StreamConfigurationMap map) throws NullPointerException{

            ArrayList<Size> sizes = new ArrayList<>(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)));
            Size result;
        /*    int max = 0;
            for(Size s:sizes){
                if(Math.round(100*s.getWidth()/s.getHeight())/10 ==Math.round(100*16/9)/10&&s.getWidth()*s.getHeight()>max){
                    max = s.getWidth()*s.getHeight();
                }
            }*/

            if (TimelapseSettingsController.settings.resoluionMenu[0] || TimelapseSettingsController.settings.resoluionMenu[1]) {
                result =  new Size(2560, 1440);
            } else if (TimelapseSettingsController.settings.resoluionMenu[2]) {
                result =  new Size(4032, 2268);
            } else {
                result =  new Size(4032, 3024);
            }
            if(!sizes.contains(result)){
                ErrorDialog.buildErrorDialog("This device doesn't support selected image size.").
                        show(activity.getFragmentManager(), "dialog");
                int r = result.getHeight()*result.getWidth();
                int min  =r;
                for(Size s:sizes){
                    if(Math.abs(s.getHeight()*s.getWidth() -r) <min){
                        min = Math.abs(s.getHeight()*s.getWidth() -r);
                        result = s;
                    }
                }
            }
            return result;
        }
        private void setUpJpegOutput(CameraManager manager) throws CameraAccessException {

                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                // For still image captures, we use the largest available size.
                Size jpegSize = getJpegSize(map);
                synchronized (mCameraStateLock) {
                    if (mJpegImageReader == null || mJpegImageReader.getAndRetain() == null) {
                        mJpegImageReader = new RefCountedAutoCloseable<>(
                                ImageReader.newInstance(jpegSize.getWidth(),
                                        jpegSize.getHeight(), ImageFormat.JPEG, /*maxImages*/5));
                    }
                    mJpegImageReader.get().setOnImageAvailableListener(
                            mOnJpegImageAvailableListener, mBackgroundHandler);
                    mCharacteristics = characteristics;
                    mCameraId =manager.getCameraIdList()[0];
                }
        }

    private void setUpRawOutput(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // We only use a camera that supports RAW in this sample.
            if (!contains(characteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                continue;
            }

            Size largestRaw = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.RAW_SENSOR)),
                    new CompareSizesByArea());

            synchronized (mCameraStateLock) {
                if (mRawImageReader == null || mRawImageReader.getAndRetain() == null) {
                    mRawImageReader = new RefCountedAutoCloseable<>(
                            ImageReader.newInstance(largestRaw.getWidth(),
                                    largestRaw.getHeight(), ImageFormat.RAW_SENSOR, /*maxImages*/ 5));
                }
                mRawImageReader.get().setOnImageAvailableListener(
                        mOnRawImageAvailableListener, mBackgroundHandler);
                mCharacteristics = characteristics;
                mCameraId = cameraId;
            }
            return;
        }
    }

        /**
         * Sets up state related to camera that is needed before opening a {@link CameraDevice}.
         */
        private boolean setUpCameraOutputs() {
        
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            ErrorDialog.buildErrorDialog("This device doesn't support Camera2 API.").
                    show(activity.getFragmentManager(), "dialog");
            return false;
        }
        try {
            // Find a CameraDevice that supports RAW captures, and configure state.
                if(TimelapseSettingsController.settings.captureRawImages){
                    setUpRawOutput(manager);
                }else{
                    setUpJpegOutput(manager);
                }
                return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // If we found no suitable cameras for capturing RAW, warn the user.
        ErrorDialog.buildErrorDialog("This device doesn't support capturing RAW photos").
                show(activity.getFragmentManager(), "dialog");
        return false;
    }

        /**
         * Opens the camera specified by {@link #mCameraId}.
         */
        @SuppressWarnings("MissingPermission")
        private void openCamera() {
        if (!setUpCameraOutputs()) {
            return;
        }


        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            // Wait for any previously running session to finish.
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            String cameraId;
            Handler backgroundHandler;
            synchronized (mCameraStateLock) {
                cameraId = mCameraId;
                backgroundHandler = mBackgroundHandler;
            }

            // Attempt to open the camera. mStateCallback will be called on the background handler's
            // thread when this succeeds or fails.
            manager.openCamera(cameraId, mStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

        /**
         * Closes the current {@link CameraDevice}.
         */
        private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            synchronized (mCameraStateLock) {

                // Reset state and clean up resources used by the camera.
                // Note: After calling this, the ImageReaders will be closed after any background
                // tasks saving Images from these readers have been completed.
                mPendingUserCaptures = 0;
                mState = STATE_CLOSED;
                if (null != mCaptureSession) {
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != mJpegImageReader) {
                    mJpegImageReader.close();
                    mJpegImageReader = null;
                }
                if (null != mRawImageReader) {
                    mRawImageReader.close();
                    mRawImageReader = null;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

        /**
         * Starts a background thread and its {@link Handler}.
         */
        private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        synchronized (mCameraStateLock) {
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

        /**
         * Stops the background thread and its {@link Handler}.
         */
        private void stopBackgroundThread() {
            if(mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                try {
                    mBackgroundThread.join();
                    mBackgroundThread = null;
                    synchronized (mCameraStateLock) {
                        mBackgroundHandler = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

        /**
         * Creates a new {@link CameraCaptureSession} for camera preview.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         */
        public void createCameraPreviewSessionLocked() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            ArrayList<Surface> a = new ArrayList<>();
            a.add(surface);

            if(mJpegImageReader!=  null) {
                a.add(mJpegImageReader.get().getSurface());
            }
            if(mRawImageReader !=  null) {
                a.add(mRawImageReader.get().getSurface());
            }
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(a, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            synchronized (mCameraStateLock) {
                                // The camera is already closed
                                if (null == mCameraDevice) {
                                    return;
                                }

                                try {
                                    setup3AControlsLocked(mPreviewRequestBuilder);
                                    // Finally, we start displaying the camera preview.
                                    cameraCaptureSession.setRepeatingRequest(
                                            mPreviewRequestBuilder.build(),
                                            threeAPreCaptureCallback, mBackgroundHandler);
                                    mState = STATE_PREVIEW;
                                } catch (CameraAccessException | IllegalStateException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                // When the session is ready, we start displaying the preview.
                                mCaptureSession = cameraCaptureSession;
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed to configure camera.");
                        }
                    }, mBackgroundHandler
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


        /**
         * Configure the necessary {@link android.graphics.Matrix} transformation to `mTextureView`,
         * and start/restart the preview capture session if necessary.
         * <p/>
         * This method should be called after the camera state has been initialized in
         * setUpCameraOutputs.
         *
         * @param viewWidth  The width of `mTextureView`
         * @param viewHeight The height of `mTextureView`
         */


        /**
         * Initiate a still image capture.
         * <p/>
         * This function sends a capture request that initiates a pre-capture sequence in our state
         * machine that waits for auto-focus to finish, ending in a "locked" state where the lens is no
         * longer moving, waits for auto-exposure to choose a good exposure value, and waits for
         * auto-white-balance to converge.
         */
        private void take3APicture() {
        synchronized (mCameraStateLock) {
            mPendingUserCaptures++;

            // If we already triggered a pre-capture sequence, or are in a state where we cannot
            // do this, return immediately.
            if (mState != STATE_PREVIEW) {
                return;
            }

            try {
                // Trigger an auto-focus run if camera is capable. If the camera is already focused,
                // this should do nothing.
                if (!mNoAFRun) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                            CameraMetadata.CONTROL_AF_TRIGGER_START);
                }

                // If this is not a legacy device, we can also trigger an auto-exposure metering
                // run.
                if (!isLegacyLocked()) {
                    // Tell the camera to lock focus.
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                            CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                }

                // Update state machine to wait for auto-focus, auto-exposure, and
                // auto-white-balance (aka. "3A") to converge.
                mState = STATE_WAITING_FOR_3A_CONVERGENCE;

                // Start a timer for the pre-capture sequence.
                startTimerLocked();

                // Replace the existing repeating request with one with updated 3A triggers.
                mCaptureSession.capture(mPreviewRequestBuilder.build(), threeAPreCaptureCallback,
                        mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Configure the given {@link CaptureRequest.Builder} to use auto-focus, auto-exposure, and
     * auto-white-balance controls if available.
     * <p/>
     * Call this only with {@link #mCameraStateLock} held.
     *
     * @param builder the builder to configure.
     */

    private void setup3AControlsLocked(CaptureRequest.Builder builder) {
        // Enable auto-magical 3A run by camera device
        builder.set(CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO);
        Float minFocusDist =
                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        mNoAFRun = (minFocusDist == null || minFocusDist == 0);

        if (!mNoAFRun) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (contains(mCharacteristics.get(
                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical flash control mode available, use it, otherwise default to
        // the "on" mode, which is guaranteed to always be available.
        if (contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        } else {
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON);
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
    }

 /*   private void setupControls(CaptureRequest.Builder captureBuilder){
        mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
    }*/
        /**
         * Send a capture request to the camera device that initiates a capture targeting the JPEG and
         * RAW outputs.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         */
        public void captureStillPictureLocked() {
        try {
        //    Log.d("DEBUG","G");
            
            if (null == activity || null == mCameraDevice) {
                return;
            }

            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            if(mJpegImageReader !=null){
                captureBuilder.addTarget(mJpegImageReader.get().getSurface());
            }
            if(mRawImageReader !=null) {
                captureBuilder.addTarget(mRawImageReader.get().getSurface());
            }
            // Use the same AE and AF modes as the preview.
           // setup3AControlsLocked(captureBuilder);
            setupControlsForStillImage(captureBuilder);

            // Set orientation.
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    sensorToDeviceRotation(mCharacteristics, rotation));

            // Set request tag to easily track results in callbacks.
            captureBuilder.setTag(mRequestCounter.getAndIncrement());

            CaptureRequest request = captureBuilder.build();

            // Create an ImageSaverBuilder in which to collect results, and add it to the queue
            // of active requests.
            if(mJpegImageReader !=null){
                ImageSaver.ImageSaverBuilder jpegBuilder = new ImageSaver.ImageSaverBuilder(activity,date)
                        .setCharacteristics(mCharacteristics);
                mJpegResultQueue.put((int) request.getTag(), jpegBuilder);
            }
            if(mRawImageReader !=null) {
                ImageSaver.ImageSaverBuilder rawBuilder = new ImageSaver.ImageSaverBuilder(activity,date)
                        .setCharacteristics(mCharacteristics);

                mRawResultQueue.put((int) request.getTag(), rawBuilder);
            }

            mCaptureSession.capture(request, mCaptureCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Rotation need to transform from the camera sensor orientation to the device's current
     * orientation.
     *
     * @param c                 the {@link CameraCharacteristics} to query for the camera sensor
     *                          orientation.
     * @param deviceOrientation the current device orientation relative to the native device
     *                          orientation.
     * @return the total rotation from the sensor orientation to the current device orientation.
     */
    public static int sensorToDeviceRotation(CameraCharacteristics c, int deviceOrientation) {
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Get device orientation in degrees
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);

        // Reverse device orientation for front-facing cameras
        if (c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
            deviceOrientation = -deviceOrientation;
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation - deviceOrientation + 360) % 360;
    }

        /**
         * Called after a RAW/JPEG capture has completed; resets the AF trigger state for the
         * pre-capture sequence.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         */
        private void finishedCaptureLocked() {
        try {
            // Reset the auto-focus trigger in case AF didn't run quickly enough.
            if (!mNoAFRun) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

                mCaptureSession.capture(mPreviewRequestBuilder.build(), threeAPreCaptureCallback,
                        mBackgroundHandler);

                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

        /**
         * Retrieve the next {@link Image} from a reference counted {@link ImageReader}, retaining
         * that {@link ImageReader} until that {@link Image} is no longer in use, and set this
         * {@link Image} as the result for the next request in the queue of pending requests.  If
         * all necessary information is available, begin saving the image to a file in a background
         * thread.
         *
         * @param pendingQueue the currently active requests.
         * @param reader       a reference counted wrapper containing an {@link ImageReader} from which
         *                     to acquire an image.
         */
        private void dequeueAndSaveImage(TreeMap<Integer, ImageSaver.ImageSaverBuilder> pendingQueue,
            RefCountedAutoCloseable< ImageReader > reader) {
        synchronized (mCameraStateLock) {
            Map.Entry<Integer, ImageSaver.ImageSaverBuilder> entry =
                    pendingQueue.firstEntry();
            ImageSaver.ImageSaverBuilder builder = entry.getValue();

            // Increment reference count to prevent ImageReader from being closed while we
            // are saving its Images in a background thread (otherwise their resources may
            // be freed while we are writing to a file).
            if (reader == null || reader.getAndRetain() == null) {
                Log.e(TAG, "Paused the activity before we could save the image," +
                        " ImageReader already closed.");
                pendingQueue.remove(entry.getKey());
                return;
            }

            Image image;
            try {
                image = reader.get().acquireNextImage();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Too many images queued for saving, dropping image for request: " +
                        entry.getKey());
                pendingQueue.remove(entry.getKey());
                return;
            }

            builder.setRefCountedReader(reader).setImage(image);

            handleCompletionLocked(entry.getKey(), builder, pendingQueue);
        }
    }

        // Utility classes and methods:
        // *********************************************************************************************

        /**
         * Generate a string containing a formatted timestamp with the current date and time.
         *
         * @return a {@link String} representing a time.
         */
        private static String generateTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);
        return sdf.format(new Date());
    }

        /**
         * Return true if the given array contains the given integer.
         *
         * @param modes array to check.
         * @param mode  integer to get for.
         * @return true if the array contains the given integer, otherwise false.
         */
        private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

        /**
         * Shows a {@link Toast} on the UI thread.
         *
         * @param text The message to show.
         */
        private void showToast(String text) {
        // We show a Toast by sending request message to mMessageHandler. This makes sure that the
        // Toast is shown on the UI thread.
        Message message = Message.obtain();
        message.obj = text;
        mMessageHandler.sendMessage(message);
    }

        /**
         * If the given request has been completed, remove it from the queue of active requests and
         * send an {@link ImageSaver} with the results from this request to a background thread to
         * save a file.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         *
         * @param requestId the ID of the {@link CaptureRequest} to handle.
         * @param builder   the {@link ImageSaver.ImageSaverBuilder} for this request.
         * @param queue     the queue to remove this request from, if completed.
         */
        private void handleCompletionLocked(int requestId, ImageSaver.ImageSaverBuilder builder,
        TreeMap<Integer, ImageSaver.ImageSaverBuilder> queue) {
        if (builder == null) return;
        ImageSaver saver = builder.buildIfComplete();
        if (saver != null) {
            queue.remove(requestId);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(saver);
        }
    }

        /**
         * Check if we are using a device that only supports the LEGACY hardware level.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         *
         * @return true if this is a legacy device.
         */
        public boolean isLegacyLocked() {
        return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

        /**
         * Start the timer for the pre-capture sequence.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         */
        private void startTimerLocked() {
        mCaptureTimer = SystemClock.elapsedRealtime();
    }

        /**
         * Check if the timer for the pre-capture sequence has been hit.
         * <p/>
         * Call this only with {@link #mCameraStateLock} held.
         *
         * @return true if the timeout occurred.
         */
        public boolean hitTimeoutLocked() {
        return (SystemClock.elapsedRealtime() - mCaptureTimer) > PRECAPTURE_TIMEOUT_MS;
    }

    private void setupControlsForStillImage(CaptureRequest.Builder builder){
        builder.set(CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO);
        if(exponsureTime!=-1||iso!=-1){
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            builder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
        }
        if(exponsureTime!=-1) {
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exponsureTime);
        }
        if(iso!=-1) {
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
        }
        if( sceneMode!=-1){
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,sceneMode);
        }
        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
        Float minFocusDist =
                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        mNoAFRun = (minFocusDist == null || minFocusDist == 0);

        if (!mNoAFRun) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (contains(mCharacteristics.get(
                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }

    }
    public String date;
    public String getDate(){
            return date;
    }
    private void setDate(){
        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = (sdf.format(cal.getTime()));
    }

    public boolean locked =false;
    private int iso = -1;
    private long exponsureTime = -1;
    private int sceneMode = -1;
    public void onIsoChange(int progress) {
    if(progress !=0){
        iso =progress;
        locked = true;
        Range<Integer> range2 = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        assert range2 != null;
        int max1 = range2.getUpper();//10000
        int min1 = range2.getLower();//100
         iso = ((progress * (max1 - min1)) / 34 + min1);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
        if(exponsureTime!=-1) {
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,exponsureTime );
        }else{
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,threeAPreCaptureCallback.getCurrentExponsureTime());
        }
        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
    }else{
        if(exponsureTime ==-1){
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        }else{
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,exponsureTime );
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,threeAPreCaptureCallback.getCurrrentIso());
            locked = false;
        }
            iso = -1;
        }
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.setRepeatingRequest(
                    mPreviewRequestBuilder.build(),
                    threeAPreCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            ErrorDialog.buildErrorDialog("Unable to set ISO").
                    show(activity.getFragmentManager(), "dialog");
            e.printStackTrace();
        }
    }

    public void onExponsureTimeChange(int progress) {
        if(progress !=0){
            exponsureTime =progress;
            locked = true;
            Range<Integer> range2 = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            assert range2 != null;
            int max1 = range2.getUpper();//10000
            int min1 = range2.getLower();//100
            exponsureTime = ((progress * (max1 - min1)) / 34 + min1);

            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,exponsureTime );
            if(iso!=-1) {
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,iso);
            }else{
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,threeAPreCaptureCallback.getCurrrentIso());
            }
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
        }else{
            if(exponsureTime ==-1){
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            }else{
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,exponsureTime );
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,threeAPreCaptureCallback.getCurrrentIso());
                locked = false;
            }
            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, threeAPreCaptureCallback.getCurrentFrameDuration());
            exponsureTime = -1;
        }
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.setRepeatingRequest(
                    mPreviewRequestBuilder.build(),
                    threeAPreCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            ErrorDialog.buildErrorDialog("Unable to set ISO").
                    show(activity.getFragmentManager(), "dialog");
            e.printStackTrace();
        }
    }

    public void onSceneModeChanged(int index) {
        sceneMode = index;
    }

    public void startCapture(boolean isSliderEnabled,boolean autoModeStatus) {
           // captureStillPictureLocked();
    }
    public void takePhoto(){
        captureStillPictureLocked();
    }
    public void changeAspectRatio(boolean fillScreen){
        if (mTextureView.isAvailable()) {
            TextureTransform.configureTransform(this,mTextureView.getWidth(), mTextureView.getHeight(),fillScreen);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        this.fillScreen = fillScreen;
    }
    public void stopPreview(){
            if (null != mCaptureSession) {
                try {
                    mCaptureSession.stopRepeating();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            if (mOrientationListener != null) {
                mOrientationListener.disable();
            }
    }
    public void resumePreview(){
        try {
            mCaptureSession.setRepeatingRequest(
                    mPreviewRequestBuilder.build(),
                    threeAPreCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (mOrientationListener != null && mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    public void enableLightMeter(boolean status){
        threeAPreCaptureCallback.enableLightMeter(status);
    }
    public void onResume() {
        startBackgroundThread();
        openCamera();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we should
        // configure the preview bounds here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            TextureTransform.configureTransform(this,mTextureView.getWidth(), mTextureView.getHeight(),fillScreen);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        if (mOrientationListener != null && mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }


    public void onPause() {
        endPreview();
    }
    public void endPreview(){
        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    public void onStart() {

    }

    @Override
    public int getIso() {
        return 0;
    }

    @Override
    public int getAperture() {
        return 0;
    }

    @Override
    public int getShutter() {
        return 0;
    }

    @Override
    public void startTimelapse() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void startBulb(int time) {

    }


    @Override
    public void onFChange(int progress) {

    }

    @Override
    public void onFocusChange(int value, CameraParameters.Param[] focusSteps) {

    }


    @Override
    public void setExponsureOnPhoto(boolean value) {

    }

    @Override
    public void setOnCamraInitListener(OnCameraInitListener onCamraInitListener) {

    }

    @Override
    public WorkerNotifier getNotifier() {
        return null;
    }

    @Override
    public ValueAnimationDialog onValueAnimationClicked(CameraParameters.Param[] currentParams, int progress, FragmentManager fragmentManager,String type) {
        ValueAnimationDialog newFragment = new ValueAnimationDialog();
        newFragment.setParams( currentParams,progress,type);
        newFragment.show(fragmentManager,"abc");
        return  newFragment;
    }

    @Override
    public void onExtraModesClicked(int index) {

    }

    @Override
    public String getExtraModeName(int index) {
        return null;
    }

    @Override
    public CameraParameters.FocusSteps getFocusSteps() {
        return null;
    }


    public void finishAtivity(){
        activity.finish();
    }

    public void onViewCreated() {
        mOrientationListener = new OrientationEventListener(activity,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mTextureView != null && mTextureView.isAvailable()) {
                    TextureTransform.configureTransform(Camera2Controller.this,mTextureView.getWidth(), mTextureView.getHeight(),fillScreen);
                }
            }
        };
    }


    public Camera2Controller() {
        super();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
