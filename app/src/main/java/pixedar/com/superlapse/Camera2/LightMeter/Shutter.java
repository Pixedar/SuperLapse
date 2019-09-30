package pixedar.com.superlapse.Camera2.LightMeter;


import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class Shutter implements Comparator<Shutter>, Comparable<Shutter> {
    private String humanReadable;
    private String original;
    private double value;

    public Shutter(String str) {
        this.original = str;
        this.value = value(str);
        this.humanReadable = getDisplayShutter(this.value);
    }

    public Shutter(double d) {
        StringBuilder sb = new StringBuilder();
        sb.append(d);
        sb.append("");
        this.original = sb.toString();
        this.value = d;
        this.humanReadable = this.original;
    }

    public static Shutter calc(double d, int i, double d2) {
        StringBuilder sb = new StringBuilder();
        sb.append("Input EV: ");
        sb.append(d);
        String str = "Shutter.java";
        Log.i(str, sb.toString());
        double pow = Math.pow(2.0d, ((Math.log(d2 * d2) / Math.log(2.0d)) - d) - ISO.EVValue(i));
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Raw shutter: ");
        sb2.append(pow);
        Log.i(str, sb2.toString());
        if (pow <= 15.0d) {
            return ValueListManager.getClosest(pow);
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(Math.floor(pow));
        sb3.append("");
        return new Shutter(sb3.toString());
    }


    public String getHumanReadable() {
        return this.humanReadable;
    }

    public String toString() {
        return getHumanReadable();
    }

    public double getValue() {
        return this.value;
    }

    public String getOriginal() {
        return this.original;
    }

    private String getDisplayShutter(double d) {
        if (d < 60.0d) {
            return this.original;
        }
        int i = (int) d;
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        sb.append("");
        String sb2 = sb.toString();
        if (i > 60 && i < 3600) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(i / 60);
            sb3.append("m ");
            sb3.append(i % 60);
            sb3.append("s");
            sb2 = sb3.toString();
        }
        if (i >= 3600) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(i / 3600);
            sb4.append("h ");
            sb4.append((i % 3600) / 60);
            sb4.append("m");
            sb2 = sb4.toString();
        }
        if (i >= 86400) {
            StringBuilder sb5 = new StringBuilder();
            sb5.append(i / 86400);
            sb5.append("d ");
            sb5.append((i % 86400) / 3600);
            sb5.append("h");
            sb2 = sb5.toString();
        }
        return sb2;
    }

    public static double value(String str) {
        double d;
        String str2 = "/";
        try {
            if (str.contains(str2)) {
                String[] split = str.split(str2);
                d = Double.parseDouble(split[0]) / Double.parseDouble(split[1]);
            } else {
                d = Double.parseDouble(str);
            }
            return d;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.005d;
        }
    }

    public int compare(Shutter shutter, Shutter shutter2) {
        return Double.valueOf(getValue()).compareTo(Double.valueOf(shutter.getValue()));
    }

    public int compareTo(Shutter shutter) {
        return Double.valueOf(getValue()).compareTo(Double.valueOf(shutter.getValue()));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Shutter)) {
            return false;
        }
        return ((Shutter) obj).getOriginal().equals(getOriginal());
    }
}

