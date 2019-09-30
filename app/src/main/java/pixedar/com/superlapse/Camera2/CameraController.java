package pixedar.com.superlapse.Camera2;

import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.Dslr.ptp.WorkerNotifier;

public interface CameraController {
    public interface OnCameraInitListener{
        void initialised();
    }
          void onViewCreated();
          void onResume();
          void onPause();
          void changeAspectRatio(boolean fillScreen);
          void onExponsureTimeChange(int progress);
          void onIsoChange(int progress);
          void stopPreview();
          void resumePreview();
          void onSceneModeChanged(int index);
          void finishAtivity();
          void takePhoto();
          void endPreview();
          void onStart();
          int getIso();
          int getAperture();
          int getShutter();
          void startTimelapse();
          void onDestroy();
          void startBulb(int time);
          void onFChange(int progress);
          WorkerNotifier getNotifier();
          void onFocusChange(int value, CameraParameters.Param[] focusSteps);
          ValueAnimationDialog onValueAnimationClicked(CameraParameters.Param[] currentParams,int progress, android.app.FragmentManager fragmentManager,String type);
          void onExtraModesClicked(int index);
          String getExtraModeName(int index);
          CameraParameters.FocusSteps getFocusSteps();
          void setExponsureOnPhoto(boolean value);
          void setOnCamraInitListener(OnCameraInitListener onCamraInitListener);
}
