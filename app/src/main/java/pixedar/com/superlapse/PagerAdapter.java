package pixedar.com.superlapse;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import pixedar.com.superlapse.Dslr.ptp.PtpCamera;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    private PtpCamera camera;

    PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
    public void setCamera(PtpCamera camera){
        this.camera =camera;
    }
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new TimelapseSettingsController();
            case 1:
                if(camera != null){
                    DslrTimelapseSettingsController dslrTimelapseSettingsController = new DslrTimelapseSettingsController();
                    dslrTimelapseSettingsController.setCamera(camera);
                    return dslrTimelapseSettingsController;
                }else {
                    return new SliderFragment();
                }
            case 2:
                return new SliderFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}