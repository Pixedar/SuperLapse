package pixedar.com.superlapse.Camera2.LightMeter;

public class ISO {
/*    public static int calc(double d, double d2, Shutter shutter) {
        return ValueListManager.getClosest(Integer.valueOf((int) (Math.pow(2.0d, (((Math.log(d2 * d2) * 1.0d) / Math.log(2.0d)) - (Math.log(shutter.getValue()) / Math.log(2.0d))) - d) * 100.0d)), (List<Integer>) ValueListManager.getISOListBounded());
    }*/

    public static double EVValue(int i) {
        return Math.log(((double) i) / 100.0d) / Math.log(2.0d);
    }
}