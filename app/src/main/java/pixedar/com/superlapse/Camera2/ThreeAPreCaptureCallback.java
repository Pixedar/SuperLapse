package pixedar.com.superlapse.Camera2;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Log;

import pixedar.com.superlapse.Camera2.LightMeter.CameraLightMeter;

public class ThreeAPreCaptureCallback extends CameraCaptureSession.CaptureCallback {
    private int currrentIso;
    private long currentExponsureTime;
    private long currentFrameDuration;
    private Camera2Controller camera2Controller;
    private CameraLightMeter cameraLightMeter;
    private boolean enableLightMeter = false;
    ThreeAPreCaptureCallback(Camera2Controller camera2Controller,CameraLightMeter cameraLightMeter){
        this.camera2Controller = camera2Controller;
        this.cameraLightMeter = cameraLightMeter;
    }
    private void process(CaptureResult result)  {
        synchronized (camera2Controller.mCameraStateLock) {
            switch (camera2Controller.mState) {
                case Camera2Controller.STATE_PREVIEW: {
                    if(!camera2Controller.locked) {
                        currrentIso = result.get(CaptureResult.SENSOR_SENSITIVITY);
                        currentExponsureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                        currentFrameDuration = result.get(CaptureResult.SENSOR_FRAME_DURATION);
                    }
                    break;
                }
                case Camera2Controller.STATE_WAITING_FOR_3A_CONVERGENCE: {
                    boolean readyToCapture = true;
                    if (!camera2Controller.mNoAFRun) {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                            break;
                        }

                        // If auto-focus has reached locked state, we are ready to capture
                        readyToCapture =
                                (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                        afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
                    }

                    // If we are running on an non-legacy device, we should also wait until
                    // auto-exposure and auto-white-balance have converged as well before
                    // taking a picture.
                    if (!camera2Controller.isLegacyLocked()) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (aeState == null || awbState == null) {
                            break;
                        }

                        readyToCapture = readyToCapture &&
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
                                awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
                    }

                    // If we haven't finished the pre-capture sequence but have hit our maximum
                    // wait timeout, too bad! Begin capture anyway.
                    if (!readyToCapture && camera2Controller.hitTimeoutLocked()) {
                        Log.w(Camera2Controller.TAG, "Timed out waiting for pre-capture sequence to complete.");
                        readyToCapture = true;
                    }

                    if (readyToCapture && camera2Controller.mPendingUserCaptures > 0) {
                        // Capture once for each user tap of the "Picture" button.
                        while (camera2Controller.mPendingUserCaptures > 0) {
                            camera2Controller.captureStillPictureLocked();
                            camera2Controller.mPendingUserCaptures--;
                        }
                        // After this, the camera will go back to the normal state of preview.
                        camera2Controller.mState = camera2Controller.STATE_PREVIEW;
                    }
                }
            }
        }
    }
    @Override
    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureResult partialResult) {
        process(partialResult);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                   TotalCaptureResult result) {
        process(result);
        if(enableLightMeter){
            cameraLightMeter.process(result);
        }
    }
    public void enableLightMeter(boolean status){
        enableLightMeter = status;
    }
    public int getCurrrentIso() {
        return currrentIso;
    }

    public long getCurrentExponsureTime() {
        return currentExponsureTime;
    }

    public long getCurrentFrameDuration() {
        return currentFrameDuration;
    }
}
