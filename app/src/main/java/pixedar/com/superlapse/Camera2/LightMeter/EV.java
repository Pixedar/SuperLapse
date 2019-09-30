package pixedar.com.superlapse.Camera2.LightMeter;

public class EV {
    public static Double luxToFC = Double.valueOf(10.763910417d);

    public static double adjustEV(double d, double d2, double d3) {
        return d + (d2 - d3);
    }

    public static double calc(double d, int i, Shutter shutter) {
        return ValueListManager.roundToOneDecimal(((Math.log(d * d) / Math.log(2.0d)) - (Math.log(shutter.getValue()) / Math.log(2.0d))) - (Math.log(((double) i) / 100.0d) / Math.log(2.0d)));
    }

    public static int EVtoLux(double d) {
        return (int) (Math.pow(2.0d, d) * 2.5d);
    }

    public static int EVtoFC(double d) {
        return (int) (((double) EVtoLux(d)) / luxToFC.doubleValue());
    }

    public static double luxToEV(double d) {
        return Math.log(d / 2.5d) / Math.log(2.0d);
    }
}
