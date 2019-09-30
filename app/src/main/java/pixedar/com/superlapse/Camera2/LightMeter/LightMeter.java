package pixedar.com.superlapse.Camera2.LightMeter;

import pixedar.com.superlapse.Settings;

public abstract class LightMeter {
    public static final int MODE_SHUTTER = 0;
    public static final int MODE_ISO = 1;
    public static final int MODE_F_STOP = 2;
    private float iirFilterCoefficient = 0.9f;
    private int maxVal = 30;
    private int modeIndex = 0;
    private double adjust = 2.0d;
    private double adjustNd = 0.0f;
    private double lastCalculatedShutter  =0;
    private boolean shutterStart = false;  // todo resetowac na modeChange
    public OnExposureChangedListener onExposureChangedListener;
    private int mode;
    public void setMode(int mode) {
        this.mode = mode;
    }
    public LightMeter(Settings settings){
        iirFilterCoefficient = settings.valueAnimationParams.lightMeter.smoothness;
        maxVal = settings.valueAnimationParams.lightMeter.maxVal;
        adjust = settings.valueAnimationParams.lightMeter.adjustValue;
        adjustNd = settings.valueAnimationParams.lightMeter.ndFilterValue;
        mode  =settings.valueAnimationParams.lightMeter.mode;
    }

    public double calcValue(double currentEv){
/*        if (this.mode != 3) {
            this.exposureValue = d;
            PreferenceUtil.setExposureValue(getActivity(), getTag(), Double.valueOf(d));
        }*/

        double adjustEV = EV.adjustEV(currentEv, this.adjust, this.adjustNd);
        //   int i = this.mode;
        String str = "Shutter found as ";
        String str2 = "MeterFragment";
        //   if (i == 0) {
        switch (mode){
            case MODE_SHUTTER:
                Shutter shutter= Shutter.calc(adjustEV,DslrSettings.ISO, DslrSettings.F_STOP);
                checkShutterStartVal(mode,shutter.getValue());
                DslrSettings.SHUTTER = calculateIIRFilter(MODE_SHUTTER,shutter.getValue());
                return DslrSettings.SHUTTER ;
        }
        return 0;

/*            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(this.shutter);
            Log.i(str2, sb.toString());
            PreferenceUtil.setShutter(getActivity(), getTag(), this.shutter);*/
        //   }
      /*  } else if (i == 1) {
            this.iso = ISO.calc(adjustEV, this.fStop, this.shutter);
            PreferenceUtil.setISO(getActivity(), getTag(), Integer.valueOf(this.iso));
        } else if (i == 2) {
            this.fStop = FStop.calc(adjustEV, this.iso, this.shutter);
            PreferenceUtil.setFStop(getActivity(), getTag(), Double.valueOf(this.fStop));
        } else if (i == 4) {
            this.shutter = Shutter.calc(adjustEV, this.iso, this.fStop);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(this.shutter);
            Log.i(str2, sb2.toString());
            this.shutterAngle = ValueListManager.getClosest(Double.valueOf(((double) (this.fps * 360)) * this.shutter.getValue()), (List<Double>) ValueListManager.getShutterAngleList());
            PreferenceUtil.setShutterAngle(getActivity(), getTag(), Double.valueOf(this.shutterAngle));
        }*/
        // return null;
    }
    private void checkShutterStartVal(int mode,double val){
        switch(mode){
            case MODE_SHUTTER:
                if(!shutterStart){
                    lastCalculatedShutter = val;
                    shutterStart = true;
                }
                break;
        }

    }
    private double calculateIIRFilter(int mode,double val){
        switch(mode){
            case MODE_SHUTTER:
                lastCalculatedShutter = lastCalculatedShutter*iirFilterCoefficient + (1.0f-iirFilterCoefficient)*val;
                if(lastCalculatedShutter > maxVal){
                    return maxVal;
                }
                return lastCalculatedShutter;
            default: return 0;
        }
    }
    private double calcExposureValue(double d, int i, Shutter shutter) {
        return ((Math.log(d * d) / Math.log(2.0d)) - (Math.log(shutter.getValue()) / Math.log(2.0d))) - (Math.log(((double) i) / 100.0d) / Math.log(2.0d));
    }
    public void setOnExposureChanged(OnExposureChangedListener listener) {
        this.onExposureChangedListener = listener;
    }
    public interface OnExposureChangedListener{
        void exposureChanged(double val);
    }
}
