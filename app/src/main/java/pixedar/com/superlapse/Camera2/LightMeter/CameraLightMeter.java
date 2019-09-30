package pixedar.com.superlapse.Camera2.LightMeter;

import android.hardware.camera2.CaptureResult;

public class CameraLightMeter {
    public static final int MODE_SHUTTER = 0;
    public static final int MODE_ISO = 1;
    public static final int MODE_F_STOP = 2;
    public static double IIR_FILTER_COEFFICIENT = 0.9;
    private int modeIndex = 0;
    private double adjust = 2.0d;
    private double adjustNd = 0.0f;
    private double lastCalculatedShutter  =0;
    private boolean shutterStart = false;  // todo resetowac na modeChange
    private OnExposureChangedListener onExposureChangedListener;
    public void process(CaptureResult captureResult){
        Integer num = (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Float f = (Float) captureResult.get(CaptureResult.LENS_APERTURE);
        Long l = (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME);
       /* if (num == null || f == null || l == null) { // todo

        }*/

        assert l != null;
        double longValue = ((double) l.longValue()) / 1.0E9d;
        double currentEv = calcExposureValue((double) f.floatValue(), num.intValue(), new Shutter(longValue));
        onExposureChangedListener.exposureChanged(modeIndex,calcExponsure(currentEv));
     //   calcExponsure(currentEv);
    }
    private double calcExponsure(double currentEv){
       
/*        if (this.mode != 3) {
            this.exposureValue = d;
            PreferenceUtil.setExposureValue(getActivity(), getTag(), Double.valueOf(d));
        }*/
        double exposureValue = currentEv; // zamist tego co na gorze
        double adjustEV = EV.adjustEV(exposureValue, this.adjust, this.adjustNd);
     //   int i = this.mode;
        String str = "Shutter found as ";
        String str2 = "MeterFragment";
     //   if (i == 0) {
            Shutter shutter= Shutter.calc(adjustEV,DslrSettings.ISO, DslrSettings.F_STOP);
            checkShutterStartVal(MODE_SHUTTER,shutter.getValue());

            DslrSettings.SHUTTER = calculateIIRFilter(MODE_SHUTTER,shutter.getValue());
      //      Log.d("DEBUG",String.valueOf(DslrSettings.SHUTTER));

            return DslrSettings.SHUTTER ;
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
                lastCalculatedShutter = lastCalculatedShutter*IIR_FILTER_COEFFICIENT + (1.0f-IIR_FILTER_COEFFICIENT)*val;
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
        void exposureChanged(int mode,double value);
    }
}
