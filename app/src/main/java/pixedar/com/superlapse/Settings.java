package pixedar.com.superlapse;


import java.util.ArrayList;

public class Settings {
    public boolean saveOnSD = true;
    public boolean saveOnInternal = false;
    public boolean captureRawImages = false;
    public boolean ultraHdrMode = false;
    public  boolean[] resoluionMenu = new boolean[]{false, false, true, false,false};

    public  int speed = 10000;
    public   int time = 0;
    public   int images = 747;
    public   int batteryLevel = 0;
    public   int compression = 100;
    // public   int[] maxPictureSize= new int[]{44,130,3700,4200};
    public   long[] maxPictureSize = new long[]{44, 130, 3700, 4200};
    public   long rawSize = 23839;
    public   int numberOfRecords = 1;
    public   int ultraHdrRange = 4;
     public int bulbTime = 0;
     public long exponsureTime = 0;
    public ValueAnimationParams valueAnimationParams = new ValueAnimationParams();

    public ExtraMode  extraMode= new ExtraMode();
    public class ExtraMode{
        public int index = -1;
        public int modelId;
    }
    public static class DsleHdrMode{
        public static final int RANGE = 2;
    }
    public class ValueAnimationParams{
        public ArrayList<Keyframes> keyframes =new ArrayList<>();
        public LightMeter lightMeter = new LightMeter();
        public void setKeyframes(String type,int[] keyframes){
            this.keyframes.add(new Keyframes( type, keyframes));
        }
       public class Keyframes{
           public Keyframes(String type,int[] keyframes){
               this.type = type;
               this.keyframes = keyframes;
           }
           public String type;
           public int[] keyframes;
       }
       public class LightMeter{
           public static final int MAX_ND_FILTER= 512;
           public static final int MAX_ADJUST = 20;
           public static final float MIN_SMOTHNESS_VAL = 0.5f;
           public static final float ABSOULTE_MAX_VAL = 120*60*1000;
           public static final int SMARPHONE_CAMERA = 0;
           public static final int LIGHT_SENSOR = 1;
           public boolean enabled = false;
           public boolean arduinoEnabled = false;
           public int maxVal =35000;
           public float smoothness =0.95f;
           public int modeIndex =0;
           public int adjustValue =2;
           public int ndFilterValue =0;
           public int mode = pixedar.com.superlapse.Camera2.LightMeter.LightMeter.MODE_SHUTTER;
       }

    }
}
