package pixedar.com.superlapse;

import android.content.Context;

public interface SettingsInterface {
    void ultraHdrRangeChanged(int range);
    void timeChanged(int progress);
    void onSpeedChanged(int progress);
    String getFreeSpace(int index);
    int getBatteryLevel(Context context);
    int getBatteryLevel();
    String getSizeString(int max);
    int calcMaxTime();
    void loadSettings(Context context);
    void init(TimelapseSettingsFragment timelapseSettingsFragment);
}
