package pixedar.com.superlapse.Camera2;

public interface TimelapseStateListener {
    void onTimelapseStarted();
    void onTimelapseEnded();
    void onTimelapseStatusChanged(String progressString);
}
