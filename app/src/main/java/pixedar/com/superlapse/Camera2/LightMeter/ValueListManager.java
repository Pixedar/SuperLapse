package pixedar.com.superlapse.Camera2.LightMeter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ValueListManager {
    private static Double[] EVs = {Double.valueOf(-15.0d), Double.valueOf(-10.0d), Double.valueOf(-5.0d), Double.valueOf(-4.0d), Double.valueOf(-3.0d), Double.valueOf(-2.0d), Double.valueOf(-1.0d), Double.valueOf(0.0d), Double.valueOf(1.0d), Double.valueOf(2.0d), Double.valueOf(3.0d), Double.valueOf(4.0d), Double.valueOf(5.0d), Double.valueOf(6.0d), Double.valueOf(7.0d), Double.valueOf(8.0d), Double.valueOf(9.0d), Double.valueOf(10.0d), Double.valueOf(11.0d), Double.valueOf(12.0d), Double.valueOf(13.0d), Double.valueOf(14.0d), Double.valueOf(15.0d), Double.valueOf(16.0d), Double.valueOf(17.0d), Double.valueOf(18.0d), Double.valueOf(19.0d), Double.valueOf(20.0d)};
    private static Double[] F1s = {Double.valueOf(0.5d), Double.valueOf(0.7d), Double.valueOf(1.0d), Double.valueOf(1.4d), Double.valueOf(2.0d), Double.valueOf(2.8d), Double.valueOf(4.0d), Double.valueOf(5.6d), Double.valueOf(8.0d), Double.valueOf(11.0d), Double.valueOf(16.0d), Double.valueOf(22.0d), Double.valueOf(32.0d), Double.valueOf(45.0d), Double.valueOf(64.0d), Double.valueOf(90.0d), Double.valueOf(128.0d), Double.valueOf(180.0d), Double.valueOf(256.0d)};
    private static Double[] F2s = {Double.valueOf(0.7d), Double.valueOf(0.8d), Double.valueOf(1.0d), Double.valueOf(1.2d), Double.valueOf(1.4d), Double.valueOf(1.7d), Double.valueOf(2.0d), Double.valueOf(2.4d), Double.valueOf(2.8d), Double.valueOf(3.3d), Double.valueOf(4.0d), Double.valueOf(4.8d), Double.valueOf(5.6d), Double.valueOf(6.7d), Double.valueOf(8.0d), Double.valueOf(9.5d), Double.valueOf(11.0d), Double.valueOf(13.0d), Double.valueOf(16.0d), Double.valueOf(19.0d), Double.valueOf(22.0d), Double.valueOf(27.0d), Double.valueOf(32.0d), Double.valueOf(38.0d), Double.valueOf(45.0d), Double.valueOf(54.0d), Double.valueOf(64.0d), Double.valueOf(76.0d), Double.valueOf(90.0d), Double.valueOf(107.0d), Double.valueOf(128.0d)};
    private static Double[] F3s = {Double.valueOf(0.7d), Double.valueOf(0.8d), Double.valueOf(0.9d), Double.valueOf(1.0d), Double.valueOf(1.1d), Double.valueOf(1.2d), Double.valueOf(1.4d), Double.valueOf(1.6d), Double.valueOf(1.8d), Double.valueOf(2.0d), Double.valueOf(2.2d), Double.valueOf(2.5d), Double.valueOf(2.8d), Double.valueOf(3.2d), Double.valueOf(3.5d), Double.valueOf(4.0d), Double.valueOf(4.5d), Double.valueOf(5.0d), Double.valueOf(5.5d), Double.valueOf(6.3d), Double.valueOf(7.1d), Double.valueOf(8.0d), Double.valueOf(9.0d), Double.valueOf(10.0d), Double.valueOf(11.0d), Double.valueOf(13.0d), Double.valueOf(14.0d), Double.valueOf(16.0d), Double.valueOf(18.0d), Double.valueOf(20.0d), Double.valueOf(22.0d), Double.valueOf(25.0d), Double.valueOf(29.0d), Double.valueOf(32.0d), Double.valueOf(36.0d), Double.valueOf(40.0d), Double.valueOf(45.0d), Double.valueOf(51.0d), Double.valueOf(57.0d), Double.valueOf(64.0d), Double.valueOf(72.0d), Double.valueOf(80.0d), Double.valueOf(90.0d)};
    private static Integer[] FPSs = {Integer.valueOf(24), Integer.valueOf(25), Integer.valueOf(30), Integer.valueOf(48), Integer.valueOf(50), Integer.valueOf(60), Integer.valueOf(72), Integer.valueOf(120), Integer.valueOf(300)};
    private static Integer[] ISO1s = {Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(25), Integer.valueOf(50), Integer.valueOf(80), Integer.valueOf(100), Integer.valueOf(200), Integer.valueOf(400), Integer.valueOf(800), Integer.valueOf(1600), Integer.valueOf(3000), Integer.valueOf(3200), Integer.valueOf(6400), Integer.valueOf(12800), Integer.valueOf(25600), Integer.valueOf(51200), Integer.valueOf(102400), Integer.valueOf(204800), Integer.valueOf(509600)};
  //  private static Integer[] ISO3s = {Integer.valueOf(32), Integer.valueOf(40), Integer.valueOf(64), Integer.valueOf(80), Integer.valueOf(125), Integer.valueOf(160), Integer.valueOf(250), Integer.valueOf(320), Integer.valueOf(500), Integer.valueOf(640), Integer.valueOf(AdError.NETWORK_ERROR_CODE), Integer.valueOf(1250), Integer.valueOf(AdError.SERVER_ERROR_CODE), Integer.valueOf(2500), Integer.valueOf(4000), Integer.valueOf(5000), Integer.valueOf(EventsFilesManager.MAX_BYTE_SIZE_PER_FILE), Integer.valueOf(AbstractSpiCall.DEFAULT_TIMEOUT)};
   // private static Double[] NDs = {Double.valueOf(0.0d)};
    private static String[] Shutter1s = {"1/64000", "1/32000", "1/16000", "1/8000", "1/4000", "1/2000", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1", "2", "4", "8", "15", "30", "60", "120", "240", "480", "900", "1800", "2700", "3600", "4500", "5400", "6300", "7200", "10800", "14400", "18000", "21600", "25200", "28800", "32400", "36000", "39600", "43200", "86400", "115200", "129600", "144000", "172800"};
    private static String[] Shutter2s = {"1/6000", "1/3000", "1/1500", "1/750", "1/350", "1/180", "1/90", "1/45", "1/20", "1/10", "1/6", "1/3", "1/1.5", "1.5", "3", "6", "12"};
    private static String[] Shutter3s = {"1/6400", "1/5000", "1/3200", "1/2500", "1/1600", "1/1250", "1/800", "1/640", "1/400", "1/320", "1/200", "1/160", "1/100", "1/80", "1/50", "1/40", "1/25", "1/20", "1/13", "1/10", "1/6", "1/5", "1/3", "1/2.5", "1/1.6", "1/1.3", "1.3", "1.6", "2.5", "3", "5", "6", "10", "13"};
    private static ArrayList<Double> evs = new ArrayList<>(Arrays.asList(EVs));
    private static ArrayList<Integer> fpss = new ArrayList<>(Arrays.asList(FPSs));
    private static ArrayList<Double> fstops = new ArrayList<>(Arrays.asList(F1s));
    private static ArrayList<Integer> isos = new ArrayList<>(Arrays.asList(ISO1s));
 //   private static ArrayList<Double> nds = new ArrayList<>(Arrays.asList(NDs));
  //  private static SavedCustomValues savedCustomValues;
    private static Double[] shutterAngles = {Double.valueOf(8.6d), Double.valueOf(11.0d), Double.valueOf(22.5d), Double.valueOf(45.0d), Double.valueOf(72.0d), Double.valueOf(90.0d), Double.valueOf(144.0d), Double.valueOf(172.8d), Double.valueOf(178.8d), Double.valueOf(180.0d), Double.valueOf(270.0d), Double.valueOf(360.0d)};
    private static ArrayList<Double> shutterangles = new ArrayList<>(Arrays.asList(shutterAngles));
    private static ArrayList<Shutter> shutters;

/*    public static ArrayList<Shutter> getShutterList() {
        getCustomValues();
        prepareShutterList();
        return shutters;
    }

    public static ArrayList<Integer> getISOList() {
        getCustomValues();
        prepareISOList();
        return isos;
    }*/

/*    public static ArrayList<Integer> getISOListBounded() {
        ArrayList iSOList = getISOList();
        ArrayList<Integer> arrayList = new ArrayList<>();
        int value = (int) Shutter.value(LightMeterTools.getPreferences().getString("iso_max", "1000000"));
        int value2 = (int) Shutter.value(LightMeterTools.getPreferences().getString("iso_min", "1"));
        for (int i = 0; i < iSOList.size(); i++) {
            if (((Integer) iSOList.get(i)).intValue() <= value && ((Integer) iSOList.get(i)).intValue() >= value2) {
                arrayList.add(iSOList.get(i));
            }
        }
        return arrayList;
    }*/

/*    public static double roundToOneDecimal(double d) {
        return ((double) ((int) Math.round(d * 10.0d))) / 10.0d;
    }

    public static ArrayList<Double> getFStopList() {
        getCustomValues();
        prepareFStopList();
        return fstops;
    }*/

/*    private static void getCustomValues() {
        savedCustomValues = (SavedCustomValues) new Gson().fromJson(LightMeterTools.getPreferences().getString("saved_custom_values", null), SavedCustomValues.class);
        if (savedCustomValues == null) {
            savedCustomValues = new SavedCustomValues();
        }
    }*/
public static double roundToOneDecimal(double d) {
    return ((double) ((int) Math.round(d * 10.0d))) / 10.0d;
}
    public static ArrayList<Double> getEVList() {
        return evs;
    }

    public static ArrayList<Double> getShutterAngleList() {
        return shutterangles;
    }

    public static ArrayList<Integer> getFPSList() {
        return fpss;
    }

    public static Shutter getClosest(double d) {
        if (shutters == null) {
            prepareShutterList();
        }
        Shutter shutter =  shutters.get(0);
        Iterator it = shutters.iterator();
        while (it.hasNext()) {
            Shutter shutter2 = (Shutter) it.next();
            if (shutter2.getValue() > d) {
                if (Math.abs(shutter2.getValue() - d) < Math.abs(shutter.getValue() - d)) {
                    shutter = shutter2;
                }
                return shutter;
            }
            shutter = shutter2;
        }
        ArrayList<Shutter> arrayList = shutters;
        return arrayList.get(arrayList.size() - 1);
    }

    public static int getClosest(Integer num, List<Integer> list) {
        for (Integer intValue : list) {
            int intValue2 = intValue.intValue();
            if (intValue2 > num.intValue()) {
                return intValue2;
            }
        }
        return ((Integer) list.get(list.size() - 1)).intValue();
    }

    public static double getClosest(Double d, List<Double> list) {
        double d2 = Double.MIN_VALUE;
        for (Double d3 : list) {
            if (d3.doubleValue() > d.doubleValue()) {
                if (Math.abs(d3.doubleValue() - d.doubleValue()) < Math.abs(d2 - d.doubleValue())) {
                    d2 = d3.doubleValue();
                }
                return d2;
            }
            d2 = d3.doubleValue();
        }
        return ((Double) list.get(list.size() - 1)).doubleValue();
    }

    public static int getPosition(int[] iArr, int i) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (i == iArr[i2]) {
                return i2;
            }
        }
        return 0;
    }

    private static void prepareShutterList() {
        String[] strArr;
        String[] strArr2;
        String[] strArr3;
       // getCustomValues();
        shutters = new ArrayList<>();
     //   shutters.addAll(savedCustomValues.shutters);
        for (String str : Shutter1s) {
            if (!shutters.contains(new Shutter(str))) {
                shutters.add(new Shutter(str));
            }
        }
    //    if (useHalfStop()) {
            for (String str2 : Shutter2s) {
                if (!shutters.contains(new Shutter(str2))) {
                    shutters.add(new Shutter(str2));
                }
            }
    //    }
/*        if (useThirdStop()) {
            for (String str3 : Shutter3s) {
                if (!shutters.contains(new Shutter(str3))) {
                    shutters.add(new Shutter(str3));
                }
            }
        }*/
        Collections.sort(shutters);
    }

/*    private static void prepareISOList() {
        Integer[] numArr;
        Integer[] numArr2;
        getCustomValues();
        isos = new ArrayList<>();
        isos.addAll(savedCustomValues.isos);
        for (Integer num : ISO1s) {
            if (!isos.contains(num)) {
                isos.add(num);
            }
        }
        if (useThirdStop()) {
            for (Integer num2 : ISO3s) {
                if (!isos.contains(num2)) {
                    isos.add(num2);
                }
            }
        }
        Collections.sort(isos);
    }

    private static void prepareFStopList() {
        Double[] dArr;
        Double[] dArr2;
        Double[] dArr3;
        getCustomValues();
        fstops = new ArrayList<>();
        fstops.addAll(savedCustomValues.fstops);
        for (Double d : F1s) {
            if (!fstops.contains(d)) {
                fstops.add(d);
            }
        }
        if (useHalfStop()) {
            for (Double d2 : F2s) {
                if (!fstops.contains(d2)) {
                    fstops.add(d2);
                }
            }
        }
        if (useThirdStop()) {
            for (Double d3 : F3s) {
                if (!fstops.contains(d3)) {
                    fstops.add(d3);
                }
            }
        }
        Collections.sort(fstops);
    }*/

/*    private static boolean useHalfStop() {
        return LightMeterTools.getPreferences().getBoolean("halfstop", true);
    }

    private static boolean useThirdStop() {
        return LightMeterTools.getPreferences().getBoolean("thirdstop", true);
    }*/
}

