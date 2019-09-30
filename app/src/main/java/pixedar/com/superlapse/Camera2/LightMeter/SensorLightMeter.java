package pixedar.com.superlapse.Camera2.LightMeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;

import pixedar.com.superlapse.Settings;

public class SensorLightMeter  extends LightMeter implements SensorEventListener2 {
    public double getResult() {
        return result;
    }

    double result = 0;
    public SensorLightMeter(Settings settings) {
        super(settings);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
       result = calcValue(event.values[0]);
      if(onExposureChangedListener !=null){
          onExposureChangedListener.exposureChanged(result);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }
}
