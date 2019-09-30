package pixedar.com.superlapse.Camera2;

public interface CameraSettingsListener {
    void onIsoChange(int progress);
    void onExponsureTimeChange(int progress);
    void onSceneModeChanged(int index);
    void startCapture(boolean isSliderEnabled);
    void onResume();
    void onPause();
    void onViewCreated();

}
