package pixedar.com.superlapse;

import android.support.v4.os.EnvironmentCompat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpPropertyHelper;
import pixedar.com.superlapse.util.log;


public class CameraParameters {
  //  public static final int NIKON_D3400 = 0;
    public static final String ISO = "iso";
    public static final String APERTURE = "aperture";
    public static final String SHUTTER = "shutter";
    public static final String FOCUS = "focus";
    public static final int FOCUS_STEPS = -500;
    public static class Product{
        public static final int SAMSUNG_GALAXY_S7 =0;
    }
    public static int getDevideId(String model){
        switch (model){
            case "SM-G930F": return 0;
        }
        return -1;
    }
    public static class Param{
        String name;
        Number value;
        boolean isDecrete =true;
        public Param(String name,Number value){
            this.value = value;
            this.name =name;
        }
        public Param(String name,Number value,boolean isDecrete){
            this.value = value;
            this.name =name;
            this.isDecrete = isDecrete;
        }
        public String getName() {
            return name;
        }
        public boolean isDecrete(){
            return isDecrete;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Number getValue() {
            return value;
        }
        public void setValue(Number value) {
            this.value = value;
        }
    }
    public static class FocusSteps extends Param{
        public int getSoftSteps() {
            return softSteps;
        }

        public void setSoftSteps(int softSteps) {
            this.softSteps = softSteps;
        }

        public int getMediumSteps() {
            return mediumSteps;
        }

        public void setMediumSteps(int mediumSteps) {
            this.mediumSteps = mediumSteps;
        }

        public int getHardSteps() {
            return hardSteps;
        }

        public void setHardSteps(int hardSteps) {
            this.hardSteps = hardSteps;
        }

        private int softSteps;
        private int mediumSteps;
        private int hardSteps;
        public FocusSteps(int softSteps, int mediumSteps, int hardSteps){
            super( "-1",FOCUS_STEPS, true);
            this.softSteps = softSteps;
            this.mediumSteps = mediumSteps;
            this.hardSteps = hardSteps;
        }

    }


    static Map<String,Param[]> nikonD3400_params = new HashMap<>();
    static Map<String,Param[]> s7_params = new HashMap<>();
    public static Number getIsoNumber(int type,int index){
        switch(type){
            case PtpConstants.Product.NikonD3400: return Integer.valueOf(getIsoString(type,index));
            case Product.SAMSUNG_GALAXY_S7:
                if(getIsoString(type,index)!="auto"){
                    return Integer.valueOf(getIsoString(type,index));
                }
                return 0;
        }
        return null;
    }
    public static boolean isBulbSupported(int model){
        switch (model){
            case PtpConstants.Product.NikonD3400: return true;
        }
        return false;
    }
    public  static Map<String,Param[]> getParams(int productId){
        switch (productId){
            case PtpConstants.Product.NikonD3400:
                nikonD3400_params.put(ISO, getParamArray(productId,ISO));
                nikonD3400_params.put(APERTURE, getParamArray(productId,APERTURE));
                nikonD3400_params.put(SHUTTER, getParamArray(productId,SHUTTER));
                nikonD3400_params.put(FOCUS, getParamArray(productId,FOCUS));
                return nikonD3400_params;
            case  Product.SAMSUNG_GALAXY_S7:
                s7_params.put(ISO, getParamArray(productId,ISO));
                s7_params.put(SHUTTER, getParamArray(productId,SHUTTER));
                return s7_params;
        }
        return null;
    }
    private static Param[] getParamArray(int type, String type2){
        int k =0;
        ArrayList<Param> list =new ArrayList<>();
        Param param = getParam(type,type2,k);
        do{
            list.add(param);
            k++;
            param = getParam(type,type2,k);
        }while (!param.getName().equals("-1"));

        return list.toArray(new Param[list.size()]);
    }
    private static Param getParam(int type, String type2, int val){
        switch (type2){
            case ISO: return new Param(getIsoString(type,val),getIsoNumber(type,val));
            case APERTURE: return new Param(getApertureString(type,val),getApertureNumber(type,val));
            case SHUTTER: return new Param(getShutterString(type,val),getShutterNumber(type,val), !supportBulb(type));
            case FOCUS: return getFocus(type);
        }
        return null;
    }

    private static boolean supportBulb(int type){
       switch (type){
           case PtpConstants.Product.NikonD3400: return true;
           case Product.SAMSUNG_GALAXY_S7: return false;
           default:return false;
       }
    }
    public static Param getFocus(int model){
        switch (model){
            case PtpConstants.Product.NikonD3400: return new FocusSteps(150,11,3);
         //   case Product.SAMSUNG_GALAXY_S7: return todo
            default: return null;
        }
    }
    public static long getExponusreTimeinMillis(int model, int value){
        float val = 0;
        switch (model){
            case PtpConstants.Product.NikonD3400:
                String str = PtpPropertyHelper.mapToString(model, PtpConstants.Property.NikonShutterSpeed, getShutterNumber(model,value));
                char[] arr = str.toCharArray();
                log.log(arr);
                log.log((arr[arr.length-1]));
                log.log(str.substring(0,str.length()-1));
                if(arr[arr.length-1] == (char)'"'){
                    try{
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        symbols.setDecimalSeparator(',');
                        DecimalFormat format = new DecimalFormat("0.#");
                        format.setDecimalFormatSymbols(symbols);
                        val= format.parse(str.substring(0,str.length()-1)).floatValue();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return (long) (val*1000);
                }
        }
        return (long) (val*1000);
    }
    public static int getShutterNumber(int type,int val){
        switch (type){
            case PtpConstants.Product.NikonD3400:
                switch (val){
                    case 0: return 0xFFFFFFFF ;
                    case 1: return 0x1E0001 ;
                    case 2: return 0x190001 ;
                    case 3: return 0x140001 ;
                    case 4: return 0xF0001 ;
                    case 5: return 0xD0001 ;
                    case 6: return 0xA0001 ;
                    case 7: return 0x80001 ;
                    case 8: return 0x60001 ;
                    case 9: return 0x50001 ;
                    case 10: return 0x40001 ;
                    case 11: return 0x30001 ;
                    case 12: return 0x19000A ;
                    case 13: return 0x20001 ;
                    case 14: return 0x10000A ;
                    case 15: return 0xD000A ;
                    case 16: return 0x10001 ;
                    case 17: return 0xA000D ;
                    case 18: return 0xA0010 ;
                    case 19: return 0x10002 ;
                    case 20: return 0xA0019 ;
                    case 21: return 0x10003 ;
                    case 22: return 0x10004 ;
                    case 23: return 0x10005 ;
                    case 24: return 0x10006 ;
                    case 25: return 0x10008 ;
                    case 26: return 0x1000A ;
                    case 27: return 0x1000D ;
                    case 28: return 0x1000F ;
                    case 29: return 0x10014 ;
                    case 30: return 0x10019 ;
                    case 31: return 0x1001E ;
                    case 32: return 0x10028 ;
                    case 33: return 0x10032 ;
                    case 34: return 0x1003C ;
                    case 35: return 0x10050 ;
                    case 36: return 0x10064 ;
                    case 37: return 0x1007D ;
                    case 38: return 0x100A0 ;
                    case 39: return 0x100C8 ;
                    case 40: return 0x100FA ;
                    case 41: return 0x10140 ;
                    case 42: return 0x10190 ;
                    case 43: return 0x101F4 ;
                    case 44: return 0x10280 ;
                    case 45: return 0x10320 ;
                    case 46: return 0x103E8 ;
                    case 47: return 0x104E2 ;
                    case 48: return 0x10640 ;
                    case 49: return 0x107D0 ;
                    case 50: return 0x109C4 ;
                    case 51: return 0x10C80 ;
                    case 52: return 0x10FA0 ;
                    default: return -1;
                }
            case Product.SAMSUNG_GALAXY_S7:
                return val;
        }
        return -1;
    }
    public static String getShutterString(int type,int val){
    switch (type) {
        case PtpConstants.Product.NikonD3400:
            int num =getShutterNumber(type,val);
            if(num == -1){
                return "-1";
            }
            return  PtpPropertyHelper.mapToString(type, PtpConstants.Property.NikonShutterSpeed, num);
        case Product.SAMSUNG_GALAXY_S7:
        switch (val) {
            case 0:
                return "auto";
            case 1:
                return "1/24000";
            case 2:
                return "1/16000";
            case 3:
                return "1/12000";
            case 4:
                return "1/8000";
            case 5:
                return "1/6000";
            case 6:
                return "1/4000";
            case 7:
                return "1/3000";
            case 8:
                return "1/2000";
            case 9:
                return "1/1500";
            case 10:
                return "1/1000";
            case 11:
                return "1/750";
            case 12:
                return "1/500";
            case 13:
                return "1/350";
            case 14:
                return "1/250";
            case 15:
                return "1/180";
            case 16:
                return "1/125";
            case 17:
                return "1/90";
            case 18:
                return "1/60";
            case 19:
                return "1/50";
            case 20:
                return "1/45";
            case 21:
                return "1/30";
            case 22:
                return "1/20";
            case 23:
                return "1/15";
            case 24:
                return "1/10";
            case 25:
                return "1/8";
            case 26:
                return "1/6";
            case 27:
                return "1/4";
            case 28:
                return "0.3";
            case 29:
                return "0.5";
            case 30:
                return "1";
            case 31:
                return "2";
            case 32:
                return "4";
            case 33:
                return "8";
            case 34:
                return "10";
            default:
                return "-1";

        }
        }
        return  "-1";
    }
    public static String getIsoString(int type, int value) {
        switch (type){
            case PtpConstants.Product.NikonD3400:
                switch (value) {
                    case 0:
                        return "100";
                    case 1:
                        return "200";
                    case 2:
                        return "400";
                    case 3:
                        return "800";
                    case 4:
                        return "1600";
                    case 5:
                        return "3200";
                    case 6:
                        return "6400";
                    case 7:
                        return "128000";
                    case 8:
                        return "256000";
                    default:
                        return "-1";
                }
            case Product.SAMSUNG_GALAXY_S7:
                switch (value) {
                    case 0:
                        return "auto";
                    case 1:
                        return "50";
                    case 2:
                        return "80";
                    case 3:
                        return "100";
                    case 4:
                        return "125";
                    case 5:
                        return "160";
                    case 6:
                        return "200";
                    case 7:
                        return "250";
                    case 8:
                        return "320";
                    case 9:
                        return "400";
                    case 10:
                        return "500";
                    case 11:
                        return "640";
                    case 12:
                        return "800";
                    default:
                        return "-1";
                }
        }
       return "-1";
    }


    public static String getApertureString(int type,int value) {
        switch (type){
            case PtpConstants.Product.NikonD3400:
                switch (value) {
                    case 0:
                        return "3.5";
                    case 1:
                        return "4.0";
                    case 2:
                        return "4.5";
                    case 3:
                        return "5";
                    case 4:
                        return "5.6";
                    case 5:
                        return "6.3";
                    case 6:
                        return "7.1";
                    case 7:
                        return "8";
                }
                if(value >7&&value <=33){
                    return String.valueOf(value);
                }else{
                    return "-1";
                }
        }
        return "-1";
    }

    public static Number getApertureNumber(int type,int value) {
        switch (type){
            case PtpConstants.Product.NikonD3400:
                return (int)(Float.valueOf(getApertureString(type,value))*1000);

        }
        return -1;
    }

    public static final String EFFECT_PREVIEW_FPS_VALUE = "effect-available-fps-values";
    protected static final String TAG = "CameraParameters";
    //  private static int[] mFocusDistanceArray = new int[]{60, 63, 67, 70, 73, 77, 80, 83, 87, 90, 93, 97, 100, 103, 107, 110, 113, 117, 120, 123, 127, 130, CommandIdMap.MENUID_VIDEO_COLLAGE_SLOW_RECORDING_TIME, CommandIdMap.MENUID_HRM_SHUTTER, 140, 143, 147, CommandIdMap.MENUID_RECORDING_MOTION_SPEED, 153, 157, CameraSettings.MENUID_WATERMARK, 163, 167, 170, 173, 177, 180, 183, 187, 190, 195, 200, GLProgram.TYPE_VEC2, GLProgram.TYPE_BVEC4, GLProgram.TYPE_MAT3, 220, 225, 230, 235, CameraSettings.MOTION_FPS_240, 245, 250, DataType.SOUND_AAC, FaceAreaManager.ORIENTATION_COMPENSATAION_VERTICAL, 280, 290, 300, CameraSettings.MENUID_CALL_STATUS_MODE, 320, 330, CommandIdMap.SHOOTINGMODE_SELFIE, CommandIdMap.SHOOTINGMODE_ANTI_FOG, CommandIdMap.SHOOTINGMODE_PRODUCT_SEARCH, 370, 380, 390, 400, 410, 425, 440, 455, CameraContext.CHANGE_DEFAULT_STORAGE_DLG, CameraContext.TAG_SHOT_FIRST_LAUNCH_DLG, CommandIdMap.MENUID_BASEMENU_EASY, 520, 540, 560, 580, CommandIdMap.EASYCAMERA_FLASHMODE_OFF, 640, 670, 710, 780, 840, CommandIdMap.WB_AUTO, 980, 1200, CommandIdMap.FULLVIEW, 2100, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2800, CommandIdMap.VOLUME_KEY_AS_SYSTEM_VOLUME, 10000};

    private CameraParameters() {
    }

    public static String getCameraHDRString(int value) {
        switch (value) {
            case 0:
                return "off";
            case 1:
                return "auto";
            case 2:
                return "on";
            default:
                return "off";
        }
    }

    public static String getDualEffectString(int value) {
        switch (value) {
            case 40:
                return BuildConfig.FLAVOR;
            case 41:
                return "cubism";
            case 42:
                return "postcard";
            case 43:
                return "blur";
            case 44:
                return "heart";
            case 45:
                return "split-view";
            case 46:
                return "polaroid";
            case 47:
                return "circle-lens";
            case 48:
                return "flip";
            default:
                return "none";
        }
    }

    public static int[] getEffectFpsRange(String str) {
        int[] fpsRange = new int[]{10000, 24000};
        //  SemLog.secV(TAG, "effect fps range : " + str);
        if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
            String[] range = str.substring(1, str.length() - 1).split(",");
            if (!(Integer.parseInt(range[0]) == 0 || Integer.parseInt(range[1]) == 0)) {
                fpsRange[0] = Integer.parseInt(range[0]);
                fpsRange[1] = Integer.parseInt(range[1]);
            }
        }
        return fpsRange;
    }

    public static String getEffectString(int value) {
        switch (value) {
            case 0:
                return "none";
            case 1:
                return "sepia";
            case 2:
                return "mono";
            case 3:
                return "negative";
            case 24:
                return "solarize";
            case 25:
                return "vintage-warm";
            case 26:
                return "vintage-cold";
            case 27:
                return "posterize";
            case 28:
                return "point-blue";
            case 29:
                return "point-red-yellow";
            case 30:
                return "point-green";
            case 31:
                return "washed";
            default:
                return "none";
        }
    }

    public static String getExposuremeterString(int value) {
        switch (value) {
            case 0:
                return "center";
            case 1:
                return "spot";
            case 2:
                return "matrix";
            default:
                return "center";
        }
    }

    public static String getFlashModeString(int value) {
        switch (value) {
            case 0:
                return "off";
            case 1:
                return "auto";
            case 2:
                return "on";
            case 3:
                return "torch";
            default:
                return "auto";
        }
    }

    public static String getFocusModeString(int value) {
        switch (value) {
            case 0:
                //  if (Feature.SUPPORT_INFINITY_FOCUS) {
                //      return "infinity";
                //  }
                return "fixed";
            case 1:
            case 2:
                return "auto";
            case 3:
                return "manual";
            case 4:
         /*       if (Feature.SUPPORT_FACE_PRIORITY_AF) {
                    return "face-priority";
                }
                if (Feature.SUPPORT_CONTINUOUS_AF) {
                    return "continuous-picture";
                }*/
                return "auto";
            case 5:
                return "continuous-video";
            case 6:
                return "continuous-picture";
            case 7:
                return "object-tracking-picture";
            case 8:
                return "object-tracking-video";
            case 9:
                return "macro";
            default:
                return "auto";
        }
    }

    public static String getFrontFlashModeString(int value) {
        switch (value) {
            case 0:
                return "off";
            case 1:
                return "auto";
            case 2:
                //  return Feature.CAMERA_FRONT_FLASH ? "torch" : "on";
            case 3:
                return "torch";
            default:
                return "off";
        }
    }

    public static String getIsoString(int value) {
        switch (value) {
            case 0:
                return "auto";
            case 1:
                return "50";
            case 2:
                return "80";
            case 3:
                return "100";
            case 4:
                return "125";
            case 5:
                return "160";
            case 6:
                return "200";
            case 7:
                return "250";
            case 8:
                return "320";
            case 9:
                return "400";
            case 10:
                return "500";
            case 11:
                return "640";
            case 12:
                return "800";
            default:
                return "auto";
        }
    }



    public static String getKelvinValueString(int value) {
        return String.valueOf(value * 100);
    }

    public static int getManualFocusValue(int step) {
        // if (step < 0 || step > mFocusDistanceArray.length) {
        return 0;
        //  }
        //  return mFocusDistanceArray[step];
    }

    public static String getModeString(int key) {
        switch (key) {
            case 1:
                return "shot-mode";
            case 2:
                return "scene-mode";
            case 3:
            case 108:
            case 170:
                return "flash-mode";
            case 4:
                return "picture-size";
            case 5:
            case 3002:
                return "focus-mode";
            case 7:
                return "exposure-compensation";
            case 8:
                return "effect";
            case 9:
                return "whitebalance";
            case 10:
                return "iso";
            case 11:
                return "metering";
            case 12:
                return "rt-hdr";
            case 16:
                return "jpeg-quality";
            case 18:
                return "zoom";
            case 24:
                return "focus-distance";
            case 31:
                return "exposure-time";
            case 35:
                return "wb-k";
            case 36:
                return "camera_id";
            case 145:
                return "multi-af";
            case 3001:
                // return SemApexParameters.KEY_VIDEO_SIZE;
                return "0";
            case 3003:
                return "jpeg-quality";
            case 3007:
                return "video-stabilization";
            default:
                return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    public static String getMultiAFModeString(int value) {
        switch (value) {
            case 1:
                return "on";
            default:
                return "off";
        }
    }

    public static String getPictureFormatString(int value) {
        switch (value) {
            case 0:
                return "jpeg";
            case 1:
                return "raw+jpeg";
            default:
                return "jpeg";
        }
    }

    public static int getQualityValue(int value) {
        switch (value) {
            case 1:
                return 92;
            case 2:
                return 40;
            case 3:
                return 90;
            default:
                return 96;
        }
    }

    public static String getRecordingMotionFPS(int value) {
        switch (value) {
            case -1:
                return "-1";
            case 60:
                return "1";
            case 120:
                return "2";
            case 240://CameraSettings.MOTION_FPS_240 /*240*/:
                return "3";
            default:
                return "-1";
        }
    }

    public static String getRecordingMotionSpeed(int value) {
        switch (value) {
            case 0:
                return "-1";
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3";
            case 4:
                return "4";
            default:
                return "-1";
        }
    }

    public static String getSceneModeString(int value) {
        switch (value) {
            case 0:
                return "auto";
            case 1:
                return "night";
            case 2:
                return "sports";
            case 3:
                return "aqua_scn";
            default:
                return "auto";
        }
    }



    public static String getShutterSpeedString(int value) {
        switch (value) {
            case 0:
                return "auto";
            case 1:
                return "42"; //1/24000
            case 2:
                return "63"; // 1/16000
            case 3:
                return "83";//1/12000
            case 4:
                return "125";//1/8000
            case 5:
                return "167";//1/6000
            case 6:
                return "250";//1/4000
            case 7:
                return "333";//1/3000
            case 8:
                return "500";//1/2000
            case 9:
                return "667";//1/1500
            case 10:
                return "1000";//1/1000
            case 11:
                return "1333";//1/750
            case 12:
                return "2000";//1/500
            case 13:
                return "2857";//1/350
            case 14:
                return "4000";//1/250
            case 15:
                return "5556";//1/180
            case 16:
                return "8000";//1/125
            case 17:
                return "11111";//1/90
            case 18:
                return "16667";//1/60
            case 19:
                return "20000";//1/50
            case 20:
                return "22222";//1/45
            case 21:
                return "33333";//1/30
            case 22:
                return "50000";//1/20
            case 23:
                return "66667";//1/15
            case 24:
                return "100000";//1/10
            case 25:
                return "125000";//1/8
            case 26:
                return "166667";//1/6
            case 27:
                return "250000";//1/4
            case 28:
                return "300000";//0.3
            case 29:
                return "500000";//0.5
            case 30:
                return "1000000";//1
            case 31:
                return "2000000";//2
            case 32:
                return "4000000";//4
            case 33:
                return "8000000";//8
            case 34:
                return "10000000";//10
            default:
                return "auto";

        }
    }




    public static String getTouchMeteringModeString(int value) {
        switch (value) {
            case 0:
                return "weighted-center";
            case 1:
                return "weighted-spot";
            case 2:
                return "weighted-matrix";
            default:
                return "weighted-center";
        }
    }

    public static String getWhitebalanceString(int value) {
        switch (value) {
            case 0:
                return "auto";
            case 1:
                return "incandescent";
            case 2:
                return "fluorescent";
            case 3:
                return "daylight";
            case 4:
                return "cloudy-daylight";
            case 5:
                return "temperature";
            default:
                return "auto";
        }
    }
}
