package pixedar.com.superlapse.Camera2.LightMeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;

public class SensorMeter implements SensorEventListener2 {

    @Override
    public void onSensorChanged(SensorEvent event) {
        valueChanged(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void valueChanged(float f) {
/*        StringBuilder sb = new StringBuilder();
        sb.append("New value: ");
        sb.append(f);
        if (this.getMax) {
            if (f >= this.maxValue) {
                this.maxValue = f;
            } else {
                return;
            }
        }
        exposureChanged(Math.log(((double) f) / 2.5d) / Math.log(2.0d));*/
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }
}
