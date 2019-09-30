package pixedar.com.superlapse;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;

public interface CameraSettings {
    public static final int AFW_MODE_ON = 1;
    public static final int ANTISHAKE_OFF = 0;
    public static final int ANTISHAKE_ON = 1;
    public static final int ANTI_FOG_LEVEL_0 = 0;
    public static final int ANTI_FOG_LEVEL_1 = 1;
    public static final int ANTI_FOG_LEVEL_2 = 2;
    public static final int ANTI_FOG_LEVEL_3 = 3;
    public static final int ANTI_FOG_LEVEL_4 = 4;
    public static final int ANTI_FOG_LEVEL_5 = 5;
    public static final int ANTI_FOG_LEVEL_6 = 6;
    public static final int ANTI_FOG_LEVEL_7 = 7;
    public static final int ANTI_FOG_LEVEL_8 = 8;
    public static final int ANTI_FOG_LEVEL_9 = 9;
    public static final int ATTACHMODE_CAMERA_NORMAL = 1;
    public static final int ATTACHMODE_NONE = 0;
    public static final int ATTACHMODE_VIDEO_NORMAL = 2;
    public static final int AUDIO_RECORDING_OFF = 0;
    public static final int AUDIO_RECORDING_ON = 1;
    public static final int AUTO_NIGHT_DETECTION_OFF = 0;
    public static final int AUTO_NIGHT_DETECTION_ON = 1;
    public static final int BEAUTYFACE_LEVEL_0 = 0;
    public static final int BEAUTYFACE_LEVEL_1 = 1;
    public static final int BEAUTYFACE_LEVEL_2 = 2;
    public static final int BEAUTYFACE_LEVEL_3 = 3;
    public static final int BEAUTYFACE_LEVEL_4 = 4;
    public static final int BEAUTYFACE_LEVEL_5 = 5;
    public static final int BEAUTYFACE_LEVEL_6 = 6;
    public static final int BEAUTYFACE_LEVEL_7 = 7;
    public static final int BEAUTYFACE_LEVEL_8 = 8;
    public static final int BEAUTY_LARGE_EYES = 1;
    public static final int BEAUTY_SHAPE_CORRECTION = 3;
    public static final int BEAUTY_SLIM = 2;
    public static final int BEAUTY_SOFTEN = 0;
    public static final int BEAUTY_SPOT_LIGHT = 4;
    public static final int CALL_STATUS_OFF = 0;
    public static final int CALL_STATUS_ON = 1;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_FACING_REAR = 0;
    public static final int COLOR_TUNE_BREEZE = 1;
    public static final int COLOR_TUNE_NONE = 0;
    public static final int COLOR_TUNE_NOSTALGIA = 3;
    public static final int COLOR_TUNE_SERENE = 5;
    public static final int COLOR_TUNE_SOFT = 4;
    public static final int COLOR_TUNE_TONE_1 = 6;
    public static final int COLOR_TUNE_TONE_2 = 7;
    public static final int COLOR_TUNE_VIVID = 2;
    public static final int COVER_CAMERA_NONE = 0;
    public static final int COVER_CAMERA_ON = 1;
    public static final String CSC_KEY_AUTONIGHTDETECTION = "csc_pref_camera_autonightdetection_key";
    public static final String CSC_KEY_CAMCORDER_RESOLUTION = "csc_pref_camcorder_resolution_key";
    public static final String CSC_KEY_CAMERA_FLASH = "csc_pref_camera_flash_key";
    public static final String CSC_KEY_FORCED_SHUTTERSOUND = "csc_pref_camera_forced_shuttersound_key";
    public static final String CSC_KEY_SETUP_STORAGE = "csc_pref_setup_storage_key";
    public static final int DEFAULT_ANTI_FOG_LEVEL = 5;
  //  public static final int DEFAULT_BACK_CAMCORDER_ANTISHAKE;
    public static final int DEFAULT_BEAUTY_MODE = 0;
    public static final int DEFAULT_CAMCORDER_AUDIORECORDING = 1;
    public static final int DEFAULT_CAMERA_AUTO_NIGHT_DETECTION = 1;
    public static final int DEFAULT_CAMERA_EFFECT = 0;
    public static final int DEFAULT_CAMERA_EXPOSURE_METER = 0;
  //  public static final int DEFAULT_CAMERA_HDR = (Feature.SUPPORT_COMPANION_CHIP ? 1 : 0);
    public static final int DEFAULT_CAMERA_ID = 0;
    public static final int DEFAULT_CAMERA_ISO = 0;
    public static final int DEFAULT_CAMERA_PICTURE_FORMAT = 0;
    public static final int DEFAULT_CAMERA_QUALITY = 0;
    public static final int DEFAULT_CAMERA_SAVE_RICHTONE = 0;
    public static final int DEFAULT_CAMERA_SHUTTER_SOUND = 1;
    public static final int DEFAULT_CAMERA_SHUTTER_SPEED = -1;
    public static final int DEFAULT_CAMERA_VOICE_COMMAND = 0;
    public static final int DEFAULT_COLOR_TUNE = 0;
 //   public static final int DEFAULT_DUAL_EFFECT;
    public static final int DEFAULT_DUAL_MODE = 0;
    public static final int DEFAULT_DUAL_TRACK_RECORDING_MODE = 0;
    public static final int DEFAULT_EFFECT = 0;
 //   public static final int DEFAULT_EFFECT_LIST_TYPE;
    public static final int DEFAULT_EFFECT_STRENGTH_LEVEL = 100;
    public static final int DEFAULT_EFFECT_VIGNETTE_LEVEL = 0;
    public static final int DEFAULT_EXPOSUREVALUE = 0;
    public static final int DEFAULT_EYEENLARGE_LEVEL = 0;
    public static final int DEFAULT_FLASH = 0;
    public static final int DEFAULT_FLOATING_CAMERA_BUTTON = 0;
    public static final int DEFAULT_FOCUS_LENGTH = -1;
    public static final int DEFAULT_FOCUS_MODE = 1;
    public static final int DEFAULT_FOOD_BLUR_TYPE = 1;
    public static final int DEFAULT_FOOD_COLOR_TUNE_VALUE = 4;
  //  public static final int DEFAULT_FRONT_CAMCORDER_ANTISHAKE;
  //  public static final int DEFAULT_FRONT_CAMERA_HDR;
    public static final int DEFAULT_FRONT_FLASH = 0;
  //  public static final int DEFAULT_FRONT_SHOOTINGMODE;
    public static final int DEFAULT_GESTURE_CONTROL_MODE = 1;
    public static final int DEFAULT_GPS = 0;
    public static final int DEFAULT_GUIDELINE = 0;
    public static final int DEFAULT_HRM_SHUTTER = 1;
    public static final int DEFAULT_INTERVAL = 0;
    public static final int DEFAULT_INTERVAL_CAPTURE_COUNT = 3;
    public static final int DEFAULT_KELVIN_VALUE = 55;
    public static final int DEFAULT_MIN_ZOOM_RATIO = 100;
    public static final int DEFAULT_MOTION_FPS = -1;
    public static final int DEFAULT_MOTION_PANORAMA_MODE = 1;
    public static final int DEFAULT_MOTION_PHOTO_VALUE = 0;
    public static final int DEFAULT_MOTION_SPEED = 0;
    public static final int DEFAULT_MOTION_WIDESELFIE_MODE = 1;
  //  public static final int DEFAULT_MULTI_AF_MODE = (Feature.SUPPORT_MULTI_AF ? 1 : 0);
    public static final int DEFAULT_OBJECT_TRACKING_AF = 0;
    public static final int DEFAULT_QRCODE_DETECTION = 1;
    public static final int DEFAULT_QUICK_LAUNCH = 1;
    public static final int DEFAULT_REAR_LENS_DISTORTION_CORRECTION = 0;
    public static final int DEFAULT_REVIEW = 0;
    public static final int DEFAULT_SELF_FLIP = 0;
    public static final int DEFAULT_SHAPE_CORRECTION_MODE = 1;
    public static final int DEFAULT_SHOOTINGMODE = 0;
    public static final int DEFAULT_SHUTTER_SOUND = 1;
    public static final int DEFAULT_SKINCOLOR_LEVEL = 0;
    public static final int DEFAULT_SLIMFACE_LEVEL = 0;
    public static final int DEFAULT_SOUND_AND_SHOT_MODE = 1;
    public static final int DEFAULT_SPOTLIGHT_LEVEL = 0;
    public static final int DEFAULT_SPOTLIGHT_POSITION = 1;
    public static final int DEFAULT_STORAGE = 0;
    public static final int DEFAULT_TAP_TO_TAKE_PICTURES = 0;
    public static final int DEFAULT_TIMER = 0;
    public static final int DEFAULT_VIDEO_COLLAGE_RECORDING_TIME = 1;
    public static final int DEFAULT_VIDEO_COLLAGE_TYPE = 12;
  //  public static final int DEFAULT_VIEW_MODE;
  //  public static final int DEFAULT_VOLUME_KEY_AS;
    public static final int DEFAULT_WATERMARK_CATEGORY = 0;
    public static final int DEFAULT_WATERMARK_ID = 9100;
    public static final int DEFAULT_WHITE_BALANCE = 0;
    public static final int DEFAULT_ZOOM_VALUE = 0;
    public static final int DUAL_MODE_OFF = 0;
    public static final int DUAL_MODE_ON = 1;
    public static final int DUAL_TRACK_RECORDING_OFF = 0;
    public static final int DUAL_TRACK_RECORDING_ON = 1;
    public static final int EFFECT_BW = 2;
    public static final int EFFECT_DUAL_CIRCLELENS = 47;
    public static final int EFFECT_DUAL_CUBISM = 41;
    public static final int EFFECT_DUAL_FLIP_PHOTO = 48;
    public static final int EFFECT_DUAL_HEART = 44;
    public static final int EFFECT_DUAL_NORMAL = 40;
    public static final int EFFECT_DUAL_OVAL_BLUR = 43;
    public static final int EFFECT_DUAL_POLAROID = 46;
    public static final int EFFECT_DUAL_POSTCARD = 42;
    public static final int EFFECT_DUAL_SPLIT_VIEW = 45;
    public static final int EFFECT_LIST_TYPE_FILTER = 0;
    public static final int EFFECT_LIST_TYPE_WATERMARK = 1;
    public static final int EFFECT_NEGATIVE = 3;
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_POINT_BLUE = 28;
    public static final int EFFECT_POINT_GREEN = 30;
    public static final int EFFECT_POINT_RED_YELLOW = 29;
    public static final int EFFECT_POSTERIZE = 27;
    public static final int EFFECT_SEPIA = 1;
    public static final int EFFECT_SINGLE_NATIVE_OFFSET = 400;
    public static final int EFFECT_SOLARIZE = 24;
    public static final int EFFECT_VINTAGE_COLD = 26;
    public static final int EFFECT_VINTAGE_WARM = 25;
    public static final int EFFECT_WASHED = 31;
    public static final int EV_0 = 0;
    public static final int EV_MINUS_0_1 = -1;
    public static final int EV_MINUS_0_2 = -2;
    public static final int EV_MINUS_0_3 = -3;
    public static final int EV_MINUS_0_4 = -4;
    public static final int EV_MINUS_0_5 = -5;
    public static final int EV_MINUS_0_6 = -6;
    public static final int EV_MINUS_0_7 = -7;
    public static final int EV_MINUS_0_8 = -8;
    public static final int EV_MINUS_0_9 = -9;
    public static final int EV_MINUS_1_0 = -10;
    public static final int EV_MINUS_1_1 = -11;
    public static final int EV_MINUS_1_2 = -12;
    public static final int EV_MINUS_1_3 = -13;
    public static final int EV_MINUS_1_4 = -14;
    public static final int EV_MINUS_1_5 = -15;
    public static final int EV_MINUS_1_6 = -16;
    public static final int EV_MINUS_1_7 = -17;
    public static final int EV_MINUS_1_8 = -18;
    public static final int EV_MINUS_1_9 = -19;
    public static final int EV_MINUS_2_0 = -20;
    public static final int EV_PLUS_0_1 = 1;
    public static final int EV_PLUS_0_2 = 2;
    public static final int EV_PLUS_0_3 = 3;
    public static final int EV_PLUS_0_4 = 4;
    public static final int EV_PLUS_0_5 = 5;
    public static final int EV_PLUS_0_6 = 6;
    public static final int EV_PLUS_0_7 = 7;
    public static final int EV_PLUS_0_8 = 8;
    public static final int EV_PLUS_0_9 = 9;
    public static final int EV_PLUS_1_0 = 10;
    public static final int EV_PLUS_1_1 = 11;
    public static final int EV_PLUS_1_2 = 12;
    public static final int EV_PLUS_1_3 = 13;
    public static final int EV_PLUS_1_4 = 14;
    public static final int EV_PLUS_1_5 = 15;
    public static final int EV_PLUS_1_6 = 16;
    public static final int EV_PLUS_1_7 = 17;
    public static final int EV_PLUS_1_8 = 18;
    public static final int EV_PLUS_1_9 = 19;
    public static final int EV_PLUS_2_0 = 20;
    public static final float EV_STEP = 0.1f;
    public static final int EXPOSURE_METER_CENTER = 0;
    public static final int EXPOSURE_METER_MATRIX = 2;
    public static final int EXPOSURE_METER_SPOT = 1;
    public static final int EXT_EFFECT_NONE = 8000;
    public static final int EYEENLARGE_LEVEL_0 = 0;
    public static final int EYEENLARGE_LEVEL_1 = 1;
    public static final int EYEENLARGE_LEVEL_2 = 2;
    public static final int EYEENLARGE_LEVEL_3 = 3;
    public static final int EYEENLARGE_LEVEL_4 = 4;
    public static final int EYEENLARGE_LEVEL_5 = 5;
    public static final int EYEENLARGE_LEVEL_6 = 6;
    public static final int EYEENLARGE_LEVEL_7 = 7;
    public static final int EYEENLARGE_LEVEL_8 = 8;
    public static final int FLASHMODE_AUTO = 1;
    public static final int FLASHMODE_OFF = 0;
    public static final int FLASHMODE_ON = 2;
    public static final int FLASHMODE_TORCH = 3;
    public static final int FLIP_OFF = 0;
    public static final int FLIP_ON = 1;
    public static final int FLOATING_CAMERA_BUTTON_OFF = 0;
    public static final int FLOATING_CAMERA_BUTTON_ON = 1;
    public static final int FOCUSMODE_AF = 1;
    public static final int FOCUSMODE_CONTINUOUS_PICTURE = 6;
    public static final int FOCUSMODE_CONTINUOUS_VIDEO = 5;
    public static final int FOCUSMODE_FACEDETECTION = 4;
    public static final int FOCUSMODE_MACRO = 9;
    public static final int FOCUSMODE_MANUAL = 3;
    public static final int FOCUSMODE_OBJECT_TRACKING_PICTURE = 7;
    public static final int FOCUSMODE_OBJECT_TRACKING_VIDEO = 8;
    public static final int FOCUSMODE_OFF = 0;
    public static final int FOCUSMODE_SINGLE_AF = 2;
    public static final int FOCUSMODE_UNSET = -1;
    public static final int FOOD_BLUR_EFFECT_OFF = 0;
    public static final int FOOD_BLUR_EFFECT_ON = 1;
    public static final int FORCED_SHUTTER_SOUND_OFF = 0;
    public static final int FORCED_SHUTTER_SOUND_ON = 1;
    public static final int FULLVIEW = 1;
    public static final int GESTURE_CONTROL_OFF = 0;
    public static final int GESTURE_CONTROL_ON = 1;
    public static final int GPS_OFF = 0;
    public static final int GPS_ON = 1;
    public static final int GUIDELINE_3BY3 = 1;
    public static final int GUIDELINE_OFF = 0;
    public static final int GUIDELINE_SQUARE = 2;
    public static final int HDR_AUTO = 1;
    public static final int HDR_OFF = 0;
    public static final int HDR_ON = 2;
    public static final int HRM_SHUTTER_OFF = 0;
    public static final int HRM_SHUTTER_ON = 1;
    public static final int INTERVAL_OFF = 0;
    public static final int INTERVAL_ON = 1;
    public static final int ISO_100 = 3;
    public static final int ISO_125 = 4;
    public static final int ISO_160 = 5;
    public static final int ISO_200 = 6;
    public static final int ISO_250 = 7;
    public static final int ISO_320 = 8;
    public static final int ISO_400 = 9;
    public static final int ISO_50 = 1;
    public static final int ISO_500 = 10;
    public static final int ISO_640 = 11;
    public static final int ISO_80 = 2;
    public static final int ISO_800 = 12;
    public static final int ISO_AUTO = 0;
    public static final String KEY_AUTO_NIGHT_DETECTION = "pref_global_auto_night_detection_key";
    public static final String KEY_BACK_CAMERA_BEAUTY_LEVEL = "pref_global_back_camera_beauty_level_key";
    public static final String KEY_BACK_CAMERA_SHOOTING_MODE_ORDER = "pref_global_back_camera_shooting_mode_order";
    public static final String KEY_BACK_EFFECT_LIST_TYPE = "pref_back_effect_list_type";
    public static final String KEY_BACK_VIDEO_COLLAGE_RECORDING_TIME = "pref_back_video_collage_recording_time";
    public static final String KEY_BACK_VIDEO_COLLAGE_TYPE = "pref_back_video_collage_type";
    public static final String KEY_CAMCORDER_ANTISHAKE = "pref_global_camcorder_antishake_key";
    public static final String KEY_CAMCORDER_DUAL_RESOLUTION = "pref_camcorder_dual_resolution_key";
    public static final String KEY_CAMCORDER_FRONT_RESOLUTION = "pref_camcorder_front_resolution_key";
    public static final String KEY_CAMCORDER_REAR_RESOLUTION = "pref_camcorder_rear_resolution_key";
    public static final String KEY_CAMERA_ANTI_FOG_LEVEL = "pref_camera_anti_fog_level_key";
    public static final String KEY_CAMERA_BEAUTY_LEVEL = "pref_global_camera_beauty_level_key";
    public static final String KEY_CAMERA_BEAUTY_MODE = "pref_camera_beauty_mode";
    public static final String KEY_CAMERA_COLOR_TUNE = "pref_camera_colortune";
    public static final String KEY_CAMERA_DUAL_FRONT_RESOLUTION = "pref_camera_dual_front_resolution_key";
    public static final String KEY_CAMERA_DUAL_REAR_RESOLUTION = "pref_camera_dual_rear_resolution_key";
    public static final String KEY_CAMERA_EXPOSURE_METER = "pref_global_camera_exposure_meter_key";
    public static final String KEY_CAMERA_EXPOSURE_VALUE = "pref_global_camera_exposure_value_key";
    public static final String KEY_CAMERA_EYEENLARGE_LEVEL = "pref_global_camera_eyeenlarge_level_key";
    public static final String KEY_CAMERA_FOCUS = "pref_camera_focus_key";
    public static final String KEY_CAMERA_FOOD_LEVEL = "pref_camera_food_level_key";
    public static final String KEY_CAMERA_FRONT_RESOLUTION = "pref_camera_front_resolution_key";
    public static final String KEY_CAMERA_GESTURE_CONTROL_MODE = "pref_global_camera_detection_mode_key";
    public static final String KEY_CAMERA_GUIDELINE = "pref_camera_guideline_key";
    public static final String KEY_CAMERA_HELP = "pref_camera_help_key";
    public static final String KEY_CAMERA_HRM_SHUTTER = "pref_camera_hrm_shutter_key";
    public static final String KEY_CAMERA_ID = "pref_global_camera_id_key";
    public static final String KEY_CAMERA_ISO = "pref_global_camera_iso_key";
    public static final String KEY_CAMERA_KELVIN = "pref_global_camera_kelvin_key";
    public static final String KEY_CAMERA_PICTURE_FORMAT = "pref_global_camera_picture_format";
    public static final String KEY_CAMERA_QRCODE_DETECTION = "pref_camera_qrcode_detection";
    public static final String KEY_CAMERA_QUICK_SHOT = "pref_global_camera_quick_shot";
    public static final String KEY_CAMERA_REAR_RESOLUTION = "pref_camera_rear_resolution_key";
    public static final String KEY_CAMERA_RESET = "pref_camera_reset";
    public static final String KEY_CAMERA_SAVE_RICHTONE = "pref_global_camera_save_richtone_key";
    public static final String KEY_CAMERA_SHPAE_CORRECTION_MODE = "pref_global_camera_shapecorrection_key";
    public static final String KEY_CAMERA_SHUTTER_SOUND = "pref_global_camera_shutter_sound_key";
    public static final String KEY_CAMERA_SHUTTER_SPEED = "pref_global_camera_shutter_speed_key";
    public static final String KEY_CAMERA_SKINCOLOR_LEVEL = "pref_global_camera_skincolor_level_key";
    public static final String KEY_CAMERA_SLIMFACE_LEVEL = "pref_global_camera_slimface_level_key";
    public static final String KEY_CAMERA_SPOTLIGHT_LEVEL = "pref_global_camera_spotlight_level_key";
    public static final String KEY_CAMERA_SPOTLIGHT_POSITION = "pref_global_camera_spotlight_position_key";
    public static final String KEY_CAMERA_TAP_TO_TAKE_PICTURES = "pref_camera_tap_to_take_pictures_key";
    public static final String KEY_CAMERA_VIEWMODE = "pref_global_camera_fullpreview_key";
    public static final String KEY_CAMERA_VOLUME_KEY_AS = "pref_global_camera_volume_key_as";
    public static final String KEY_CAMERA_WHITE_BALANCE = "pref_global_camera_white_balance_key";
    public static final String KEY_DUAL_EFFECT = "pref_global_camera_dual_effect";
    public static final String KEY_DUAL_TRACK_RECORDING = "pref_global_camera_dual_track_recording";
    public static final String KEY_FIRST_LAUNCH_CAMERA_BY_HOME_KEY = "pref_global_first_launch_camera_key_by_home_key";
    public static final String KEY_FLASH = "pref_flash_key";
    public static final String KEY_FLOATING_CAMERA_BUTTON = "pref_global_setup_floating_camera_button_key";
    public static final String KEY_FOCUS_LENGTH = "pref_global_focus_length";
    public static final String KEY_FRONT_CAMERA_SHOOTING_MODE_ORDER = "pref_global_front_camera_shooting_mode_order";
    public static final String KEY_FRONT_EFFECT_LIST_TYPE = "pref_front_effect_list_type";
    public static final String KEY_FRONT_FLASH = "pref_front_flash_key";
    public static final String KEY_FRONT_HDR = "pref_camera_front_hdr_key";
    public static final String KEY_FRONT_VIDEO_COLLAGE_RECORDING_TIME = "pref_front_video_collage_recording_time";
    public static final String KEY_FRONT_VIDEO_COLLAGE_TYPE = "pref_front_video_collage_type";
    public static final String KEY_HRM_SENSOR_CAPTURE_UNAVAILABLE_GUIDE_POPUP = "hrm_sensor_capture_unavailable_guide_popup";
    public static final String KEY_INTERVAL = "pref_camera_interval_key";
    public static final String KEY_MOTION_PANORAMA_MODE = "pref_global_camera_motion_panorama_mode";
    public static final String KEY_MOTION_PHOTO = "pref_global_motion_photo_key";
    public static final String KEY_MOTION_WIDE_SELFIE_MODE = "pref_global_camera_motion_wide_selfie_mode";
    public static final String KEY_MULTI_AF_MODE = "pref_camera_multi_af_mode";
    public static final String KEY_OVERRIDDEN_CAMCORDER_RESOLUTION = "pref_overridden_camcorder_resolution";
    public static final String KEY_REAR_HDR = "pref_camera_rear_hdr_key";
    public static final String KEY_REAR_LENS_DISTORTION_CORRECTION = "pref_global_rear_lens_distortion_correction_key";
    public static final String KEY_RECORDING_MOTION_SPEED = "pref_recording_motion_speed_key";
    public static final String KEY_SETUP_GPS = "pref_global_setup_gps_key";
    public static final String KEY_SETUP_OBJECT_TRACKING_AF = "pref_global_setup_object_trackingaf_key";
    public static final String KEY_SETUP_REVIEW = "pref_global_setup_review_key";
    public static final String KEY_SETUP_SELF_FLIP = "pref_global_setup_self_flip_key";
    public static final String KEY_SETUP_STORAGE = "pref_global_setup_storage_key";
    public static final String KEY_SETUP_VOICE_CONTROL = "pref_global_setup_voice_control_key";
    public static final String KEY_TIMER = "pref_camera_timer_key";
    public static final String KEY_WATERMARK_CATEGORY = "pref_camera_watermark_category";
    public static final int KNOX_MODE_ON = 1;
    public static final int MANUAL_FOCUS_AUTO = 0;
    public static final int MANUAL_FOCUS_OFF = 1;
    public static final int MANUAL_FOCUS_ON = 2;
    public static final int MANUAL_SETTING_OFF = 0;
    public static final int MANUAL_SETTING_ON = 1;
    public static final int MENUID_ANTI_FOG_LEVEL = 117;
    public static final int MENUID_ATTACH_MODE = 33;
    public static final int MENUID_AUTO_NIGHT_DETECTION = 87;
    public static final int MENUID_BACK = 28;
    public static final int MENUID_BACK_BEAUTYFACE_LEVEL = 180;
    public static final int MENUID_BEAUTYFACE_LEVEL = 109;
    public static final int MENUID_BEAUTY_LITE_MODE = 130;
    public static final int MENUID_BEAUTY_MODE = 129;
    public static final int MENUID_CALL_STATUS_MODE = 310;
    public static final int MENUID_CAMCORDER_ANTISHAKE = 3007;
    public static final int MENUID_CAMCORDER_AUDIORECORDING = 3004;
    public static final int MENUID_CAMCORDER_FOCUSMODE = 3002;
    public static final int MENUID_CAMCORDER_QUALITY = 3003;
    public static final int MENUID_CAMCORDER_RESOLUTION = 3001;
    public static final int MENUID_CAMERA_ID = 36;
    public static final int MENUID_CAMERA_QUALITY = 16;
    public static final int MENUID_CAMERA_RESOLUTION = 4;
    public static final int MENUID_COLOR_TUNE = 14;
    public static final int MENUID_COVER_CAMERA = 311;
    public static final int MENUID_EASYCAMERA_FLASHMODE = 108;
    public static final int MENUID_EASYCAMERA_FRONT_FLASHMODE = 106;
    public static final int MENUID_EASYCAMERA_HELP = 107;
    public static final int MENUID_EFFECT = 8;
    public static final int MENUID_EFFECT_DUAL = 90;
    public static final int MENUID_EFFECT_STRENGTH_LEVEL = 151;
    public static final int MENUID_EFFECT_VIGNETTE_LEVEL = 152;
    public static final int MENUID_EXPOSUREMETER = 11;
    public static final int MENUID_EXPOSUREVALUE = 7;
    public static final int MENUID_EXTERNAL_DOWNLOAD = 9002;
    public static final int MENUID_EXTERNAL_EFFECT = 9001;
    public static final int MENUID_EXTERNAL_NOITEM = 9005;
    public static final int MENUID_EYEENLARGE_LEVEL = 112;
    public static final int MENUID_FASTMOTION_SPEED = 5903;
    public static final int MENUID_FLASHMODE = 3;
    public static final int MENUID_FLOATING_CAMERA_BUTTON = 181;
    public static final int MENUID_FOCUSMODE = 5;
    public static final int MENUID_FOOD_BLUR_TYPE = 141;
    public static final int MENUID_FOOD_COLOR_TUNE = 118;
    public static final int MENUID_FOOD_MACRO_MODE = 143;
    public static final int MENUID_FRONT_FLASHMODE = 170;
    public static final int MENUID_GESTURE_CONTROL = 125;
    public static final int MENUID_GPS = 20;
    public static final int MENUID_GUIDELINE = 19;
    public static final int MENUID_HDR = 12;
    public static final int MENUID_HRM_SHUTTER = 116;
    public static final int MENUID_IMAGEVIEWER = 29;
    public static final int MENUID_INTERVAL = 127;
    public static final int MENUID_ISO = 10;
    public static final int MENUID_KELVIN = 35;
    public static final int MENUID_KNOXMODE = 7000;
    public static final int MENUID_MANUAL_FOCUS = 24;
    public static final int MENUID_MODE = 0;
    public static final int MENUID_MORE_SETTING = 120;
    public static final int MENUID_MOTION_PANORAMA_MODE = 135;
    public static final int MENUID_MOTION_PHOTO = 42;
    public static final int MENUID_MOTION_WIDE_SELFIE_MODE = 173;
    public static final int MENUID_MULTI_AF_MODE = 145;
    public static final int MENUID_MY_USER_ID = 318;
    public static final int MENUID_OBJECT_TRACKING_AF = 23;
    public static final int MENUID_PICTURE_FORMAT = 315;
    public static final int MENUID_QRCODE_DETECTION = 316;
    public static final int MENUID_QUICK_LAUNCH = 26;
    public static final int MENUID_REAR_LENS_DISTORTION_CORRECTION = 51;
    public static final int MENUID_RECORDING = 65;
    public static final int MENUID_RECORDING_MOTION_SPEED = 5901;
    public static final int MENUID_REMAIN_COUNT = 40;
    public static final int MENUID_REVIEW = 17;
    public static final int MENUID_RICHTONE = 46;
    public static final int MENUID_SAVE_RICHTONE = 34;
    public static final int MENUID_SCENEMODE = 2;
    public static final int MENUID_SECURE_CAMERA = 317;
    public static final int MENUID_SELF_FLIP = 38;
    public static final int MENUID_SHAPE_CORRECTION = 113;
    public static final int MENUID_SHOOTINGMODE = 1;
    public static final int MENUID_SHOOTING_MODE_DOWNLOAD = 126;
    public static final int MENUID_SHUTTER = 32;
    public static final int MENUID_SHUTTERSOUND = 21;
    public static final int MENUID_SHUTTER_SPEED = 31;
    public static final int MENUID_SIDE_QUICK_SETTING = 121;
    public static final int MENUID_SKINCOLOR_LEVEL = 122;
    public static final int MENUID_SLIMFACE_LEVEL = 110;
    public static final int MENUID_SLOWMOTION_SPEED = 5902;
    public static final int MENUID_SOUND_AND_SHOT_MODE = 77;
    public static final int MENUID_SPOTLIGHT_LEVEL = 111;
    public static final int MENUID_SPOTLIGHT_POSITION = 119;
    public static final int MENUID_STORAGE = 22;
    public static final int MENUID_SWITCH_CAMERA = 124;
    public static final int MENUID_TALKBACK = 6000;
    public static final int MENUID_TAP_TO_TAKE_PICTURES = 115;
    public static final int MENUID_THUMBNAIL_LIST = 114;
    public static final int MENUID_TIMER = 6;
    public static final int MENUID_TORCH_LIGHT_MODE = 300;
    public static final int MENUID_VIDEO_COLLAGE_RECORDING_TIME = 132;
    public static final int MENUID_VIDEO_COLLAGE_TYPE = 131;
    public static final int MENUID_VIEWMODE = 312;
    public static final int MENUID_VOICECOMMAND = 71;
    public static final int MENUID_VOICE_RECORGNITION = 72;
    public static final int MENUID_VOLUME_KEY_AS = 73;
    public static final int MENUID_WATERMARK = 160;
    public static final int MENUID_WATERMARK_CATEGORY = 161;
    public static final int MENUID_WB = 9;
    public static final int MENUID_ZOOM = 18;
    public static final int MOTION_FPS_120 = 120;
    public static final int MOTION_FPS_240 = 240;
    public static final int MOTION_FPS_60 = 60;
    public static final int MOTION_PANORAMA_MODE_OFF = 0;
    public static final int MOTION_PANORAMA_MODE_ON = 1;
    public static final int MOTION_PHOTO_OFF = 0;
    public static final int MOTION_PHOTO_ON = 1;
    public static final int MOTION_SPEED_16X_FASTER = 3;
    public static final int MOTION_SPEED_32X_FASTER = 4;
    public static final int MOTION_SPEED_4X_FASTER = 1;
    public static final int MOTION_SPEED_8X_FASTER = 2;
    public static final int MOTION_WIDE_SELFIE_MODE_OFF = 0;
    public static final int MOTION_WIDE_SELFIE_MODE_ON = 1;
    public static final int MULTIWINDOW_MODE_FREEFORM = 1;
    public static final int MULTIWINDOW_MODE_NONE = 0;
    public static final int MULTIWINDOW_MODE_SPLIT = 2;
    public static final int MULTI_AF_MODE_OFF = 0;
    public static final int MULTI_AF_MODE_ON = 1;
    public static final int NORMALVIEW = 0;
    public static final int NOT_REQUESTED = -1;
    public static final int NO_VALUE = 32767;
    public static final int OBJECT_TRACKING_AF_OFF = 0;
    public static final int OBJECT_TRACKING_AF_ON = 1;
    public static final int PICTURE_FORMAT_JPEG = 0;
    public static final int PICTURE_FORMAT_RAW = 1;
    public static final int QRCODE_DETECTION_OFF = 0;
    public static final int QRCODE_DETECTION_ON = 1;
    public static final int QUALITY_FINE = 1;
    public static final int QUALITY_FOR_FIXED_BURST = 3;
    public static final int QUALITY_NORMAL = 2;
    public static final int QUALITY_SUPERFINE = 0;
    public static final int QUICK_LAUNCH_OFF = 0;
    public static final int QUICK_LAUNCH_ONLY = 1;
    public static final int REAR_LENS_DISTORTION_CORRECTION_OFF = 0;
    public static final int REAR_LENS_DISTORTION_CORRECTION_ON = 1;
    public static final int REVIEW_OFF = 0;
    public static final int REVIEW_ON = 1;
    public static final int RICHTONE_AND_ORIGINAL = 1;
    public static final int RICHTONE_ONLY = 0;
    public static final int SCENEMODE_AQUA = 3;
    public static final int SCENEMODE_FOOD = 4;
    public static final int SCENEMODE_NIGHT = 1;
    public static final int SCENEMODE_NONE = 0;
    public static final int SCENEMODE_SPORTS = 2;
    public static final int SECURE_CAMERA_NONE = 0;
    public static final int SECURE_CAMERA_ON = 1;
    public static final int SEMCAMERA_AUTO_HDR_BEAUTY = 82;
    public static final int SEMCAMERA_AUTO_LLS_BEAUTY = 81;
    public static final int SEMCAMERA_AUTO_LLS_LITE = 22;
    public static final int SEMCAMERA_SINGLE_EFFECT = 71;
    public static final int SEMCAMERA_SUPER_RESOLUTION_ZOOM = 65;
    public static final int SHAPE_CORRECTION_OFF = 0;
    public static final int SHAPE_CORRECTION_ON = 1;
    public static final int SHOOTINGMODE_ANIMATEDGIF = 58;
    public static final int SHOOTINGMODE_ANTI_FOG = 49;
    public static final int SHOOTINGMODE_AQUA_SCENE = 42;
    public static final int SHOOTINGMODE_AUTO = 0;
    public static final int SHOOTINGMODE_BEAUTY = 7;
    public static final int SHOOTINGMODE_CONTINUOUS = 17;
    public static final int SHOOTINGMODE_CONTINUOUS_LITE = 1;
    public static final int SHOOTINGMODE_DUAL = 47;
    public static final int SHOOTINGMODE_FAST_MOTION = 64;
    public static final int SHOOTINGMODE_FOOD = 68;
    public static final int SHOOTINGMODE_HYPER_MOTION = 74;
    public static final int SHOOTINGMODE_LVB = 70;
    public static final int SHOOTINGMODE_MOTION_PANORAMA = 72;
    public static final int SHOOTINGMODE_MOTION_WIDE_SELFIE = 78;
    public static final int SHOOTINGMODE_NIGHT = 23;
    public static final int SHOOTINGMODE_NIGHT_SCENE = 39;
    public static final int SHOOTINGMODE_PANORAMA = 2;
    public static final int SHOOTINGMODE_PRO = 59;
    public static final int SHOOTINGMODE_PRODUCT_SEARCH = 79;
    public static final int SHOOTINGMODE_PRO_LITE = 75;
    public static final int SHOOTINGMODE_REAR_SELFIE = 56;
    public static final int SHOOTINGMODE_RECORDING = 3;
    public static final int SHOOTINGMODE_RICH_TONE = 14;
    public static final int SHOOTINGMODE_SELECTIVE_FOCUS = 45;
    public static final int SHOOTINGMODE_SELFIE = 55;
    public static final int SHOOTINGMODE_SEPARATED = 50;
    public static final int SHOOTINGMODE_SHOT_AND_MORE = 46;
    public static final int SHOOTINGMODE_SLOW_MOTION = 63;
    public static final int SHOOTINGMODE_SOUND_AND_SHOT = 35;
    public static final int SHOOTINGMODE_SPORTS_SCENE = 40;
    public static final int SHOOTINGMODE_TAG_SHOT = 67;
    public static final int SHOOTINGMODE_THEME = 32;
    public static final int SHOOTINGMODE_VIDEO_COLLAGE = 69;
    public static final int SHOOTINGMODE_VIRTUAL_SHOT = 62;
    public static final int SHOOTINGMODE_WIDE_SELFIE = 52;
    public static final int SHOOTINGMODE_WIDE_SELFIE_LITE = 77;
    public static final int SHUTTER_SOUND_OFF = 0;
    public static final int SHUTTER_SOUND_ON = 1;
    public static final int SHUTTER_SPEED_1000 = 9;
    public static final int SHUTTER_SPEED_100000 = 23;
    public static final int SHUTTER_SPEED_1000000 = 29;
    public static final int SHUTTER_SPEED_10000000 = 33;
    public static final int SHUTTER_SPEED_11111 = 16;
    public static final int SHUTTER_SPEED_125 = 3;
    public static final int SHUTTER_SPEED_125000 = 24;
    public static final int SHUTTER_SPEED_1333 = 10;
    public static final int SHUTTER_SPEED_166667 = 25;
    public static final int SHUTTER_SPEED_16667 = 17;
    public static final int SHUTTER_SPEED_167 = 4;
    public static final int SHUTTER_SPEED_2000 = 11;
    public static final int SHUTTER_SPEED_20000 = 18;
    public static final int SHUTTER_SPEED_2000000 = 30;
    public static final int SHUTTER_SPEED_22222 = 19;
    public static final int SHUTTER_SPEED_250 = 5;
    public static final int SHUTTER_SPEED_250000 = 26;
    public static final int SHUTTER_SPEED_2857 = 12;
    public static final int SHUTTER_SPEED_300000 = 27;
    public static final int SHUTTER_SPEED_333 = 6;
    public static final int SHUTTER_SPEED_33333 = 20;
    public static final int SHUTTER_SPEED_4000 = 13;
    public static final int SHUTTER_SPEED_4000000 = 31;
    public static final int SHUTTER_SPEED_42 = 0;
    public static final int SHUTTER_SPEED_500 = 7;
    public static final int SHUTTER_SPEED_50000 = 21;
    public static final int SHUTTER_SPEED_500000 = 28;
    public static final int SHUTTER_SPEED_5556 = 14;
    public static final int SHUTTER_SPEED_63 = 1;
    public static final int SHUTTER_SPEED_66667 = 22;
    public static final int SHUTTER_SPEED_667 = 8;
    public static final int SHUTTER_SPEED_8000 = 15;
    public static final int SHUTTER_SPEED_8000000 = 32;
    public static final int SHUTTER_SPEED_83 = 2;
    public static final int SHUTTER_SPEED_AUTO = -1;
    public static final int SLIMFACE_LEVEL_0 = 0;
    public static final int SLIMFACE_LEVEL_1 = 1;
    public static final int SLIMFACE_LEVEL_2 = 2;
    public static final int SLIMFACE_LEVEL_3 = 3;
    public static final int SLIMFACE_LEVEL_4 = 4;
    public static final int SLIMFACE_LEVEL_5 = 5;
    public static final int SLIMFACE_LEVEL_6 = 6;
    public static final int SLIMFACE_LEVEL_7 = 7;
    public static final int SLIMFACE_LEVEL_8 = 8;
    public static final int SOUND_AND_SHOT_MODE_ADD_VOICE = 1;
    public static final int SOUND_AND_SHOT_MODE_AUTO = 0;
    public static final int SPOTLIGHT_POSITION_CENTER = 1;
    public static final int SPOTLIGHT_POSITION_LEFT = 0;
    public static final int SPOTLIGHT_POSITION_RIGHT = 2;
    public static final int SPOT_LIGHT_LEVEL_0 = 0;
    public static final int SPOT_LIGHT_LEVEL_1 = 1;
    public static final int SPOT_LIGHT_LEVEL_2 = 2;
    public static final int SPOT_LIGHT_LEVEL_3 = 3;
    public static final int SPOT_LIGHT_LEVEL_4 = 4;
    public static final int SPOT_LIGHT_LEVEL_5 = 5;
    public static final int SPOT_LIGHT_LEVEL_6 = 6;
    public static final int SPOT_LIGHT_LEVEL_7 = 7;
    public static final int SPOT_LIGHT_LEVEL_8 = 8;
    public static final int STORAGE_MMC = 1;
    public static final int STORAGE_PHONE = 0;
    public static final int TALKBACK_OFF = 0;
    public static final int TALKBACK_ON = 1;
    public static final int TAP_TO_TAKE_PICTURES_OFF = 0;
    public static final int TAP_TO_TAKE_PICTURES_ON = 1;
    public static final int TIMER_10S = 3;
    public static final int TIMER_2S = 1;
    public static final int TIMER_5S = 2;
    public static final int TIMER_OFF = 0;
    public static final int TORCH_LIGHT_OFF = 0;
    public static final int TORCH_LIGHT_ON = 1;
    public static final int USER_ID_OTHERS = 1;
    public static final int USER_ID_OWNER = 0;
    public static final int VIDEO_COLLAGE_RECORDING_TIME_15S = 3;
    public static final int VIDEO_COLLAGE_RECORDING_TIME_3S = 0;
    public static final int VIDEO_COLLAGE_RECORDING_TIME_6S = 1;
    public static final int VIDEO_COLLAGE_RECORDING_TIME_9S = 2;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_1_BY_1_01 = 6;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_1_BY_1_02 = 7;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_1_BY_1_03 = 8;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_1_BY_1_04 = 9;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_4_BY_3_01 = 10;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_4_BY_3_02 = 11;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_4_BY_3_03 = 12;
    public static final int VIDEO_COLLAGE_TYPE_COLLAGE_4_BY_3_04 = 13;
    public static final int VIDEO_COLLAGE_TYPE_NONE = 0;
    public static final int VIDEO_COLLAGE_TYPE_SERIAL_1_BY_1 = 4;
    public static final int VIDEO_COLLAGE_TYPE_SERIAL_4_BY_3 = 5;
    public static final int VIDEO_COLLAGE_TYPE_SLOW_MOTION_01 = 1;
    public static final int VIDEO_COLLAGE_TYPE_SLOW_MOTION_02 = 2;
    public static final int VIDEO_COLLAGE_TYPE_SLOW_MOTION_03 = 3;
    public static final int VOICE_COMMAND_OFF = 0;
    public static final int VOICE_COMMAND_ON = 1;
    public static final int VOLUME_KEY_AS_CAMERA = 1;
    public static final int VOLUME_KEY_AS_RECORD = 2;
    public static final int VOLUME_KEY_AS_SYSTEM_VOLUME = 0;
    public static final int VOLUME_KEY_AS_ZOOM = 3;
    public static final int WATERMARK_CATEGORY_EMOJI = 0;
    public static final int WATERMARK_CATEGORY_FOOD = 4;
    public static final int WATERMARK_CATEGORY_SPORT = 5;
    public static final int WATERMARK_CATEGORY_TIME = 1;
    public static final int WATERMARK_CATEGORY_TRAVEL = 2;
    public static final int WATERMARK_CATEGORY_WEATHER = 3;
    public static final int WATERMARK_NONE = 9100;
    public static final int WB_AUTO = 0;
    public static final int WB_CLOUDY = 4;
    public static final int WB_DAYLIGHT = 3;
    public static final int WB_FLUORESCENT = 2;
    public static final int WB_INCANDESCENT = 1;
    public static final int WB_KELVIN = 5;

    public interface CameraSettingChangedListener {
        void onCameraSettingChanged(int i, int i2);
    }

    public static class RequestedMediaRecorderProfileInfo {
        public int audioBitrate = -1;
        public int audioChannels = -1;
        public int audioEncoder = -1;
        public int audioSamplingRate = -1;
        public int fileSizeInterval = -1;
        public int outputFormat = -1;
        public int videoBitrate = -1;
        public int videoEncoder = -1;
        public int videoFps = -1;

        public RequestedMediaRecorderProfileInfo(int videoEncoder, int videoBitrate, int videoFps, int outputFormat, int audioEncoder, int audioBitrate, int audioChannels, int audioSamplingRate, int fileSizeInterval) {
            this.videoEncoder = videoEncoder;
            this.videoBitrate = videoBitrate;
            this.videoFps = videoFps;
            this.outputFormat = outputFormat;
            this.audioEncoder = audioEncoder;
            this.audioBitrate = audioBitrate;
            this.audioChannels = audioChannels;
            this.audioSamplingRate = audioSamplingRate;
            this.fileSizeInterval = fileSizeInterval;
        }
    }

    void clear();

    int getAntiFogLevel();

    int getAttachVideoFixedResolution();

    int getAutoNightDetectionMode();

    int getBackBeautyLevel();

    String getBackCameraShootingModeOrder();

    int getBeautyLevel();

    int getBeautyMode();

    int getCallStatus();

    int getCamcorderAntiShake();

    int getCamcorderAudioRecording();

    int getCamcorderResolution();

    int getCamcorderVideoDurationInMS();

    int getCameraDualEffect();

    int getCameraExposureMeter();

    int getCameraFacing();

    int getCameraFocusMode();

    int getCameraHDR();

    int getCameraISO();

    int getCameraId();

    int getCameraQuality();

    int getCameraResolution();

    int getCameraResolutionByCameraId(int i);

    boolean getCameraResolutionChanged();

    int getCameraResolutionForDual();

    int getCameraShutterSound();

    int getCameraSingleEffect();

    int getCameraVoiceCommand();

    int getColorTune();

    int getCommandIdByCurrentShootingMode();

    int getCommandIdByShootingMode(int i);

    int getDefaultBackBeautyLevel();

    HashSet<Integer> getDefaultBackCameraShootingModeOrder();

    int getDefaultBeautyLevel();

    int getDefaultCamcorderResolution();

    HashSet<Integer> getDefaultFrontCameraShootingModeOrder();

    int getDefaultShootingModeByCurrentCameraId();

    int getEffectListType();

    String getEffectNameForLogging(int i);

    int getEffectProcessorMode();

    int getEffectStrengthLevel();

    int getEffectVignetteLevel();

    int getExposureValue();

    int getEyeEnlargeLevel();

    String getFeatureValueByCommandIdForLogging(int i, boolean z);

    int getFlashMode();

    int getFloatingCameraButton();

    int getFocusLength();

    float getFontScale();

    int getFoodBlurType();

    int getFoodColorTuneValue();

    int getForcedShutterSound();

    String getFrontCameraShootingModeOrder();

    int getFrontFlashMode();

    int getGPS();

    int getGestureControlMode();

    int getGuideline();

    int getHRMShutter();

    int getInitialShootingMode();

    int getInterval();

    int getKelvinValue();

    boolean getLowBatteryStatus();

    int getMenuIdByPreferenceKey(String str);

    int getMotionPanoramaMode();

    int getMotionPhoto();

    int getMotionWideSelfieMode();

    int getMultiAFMode();

    int getMultiWindowMode();

    int getObjectTrackingAF();

    int getPictureFormat();

    int getQRCodeDetection();

    int getRearLensDistortionCorrection();

    int getRecordingMotionSpeed();

    RequestedMediaRecorderProfileInfo getRequestedMediaRecorderProfileInfo();

    int getRequestedRecordingDurationLimit();

    long getRequestedRecordingSizeLimit();

    Uri getRequestedSaveUri();

    int getReview();

    int getSaveRichTone();

    int getSelfFlip();

    String getSeparatedShootingModeName();

    int getSettingValue(int i);

    String getSettingValuesString(int i, int i2);

    int getShapeCorrection();

    int getShootingModeIdByActivityName(String str);

    String getShootingModeNameForLogging();

    String getShootingModeResourceString();

    String getShootingModeResourceStringById(int i);

    int getShootingModeValueForISPset();

    int getShutterSpeed();

    int getSkinColorLevel();

    int getSlimFaceLevel();

    int getSoundAndShotMode();

    int getSpotlightLevel();

    int getSpotlightPosition();

    ArrayList<Integer> getStatusLoggingList();

    int getStorage();

    int getTapToTakePictures();

    int getTimer();

    int getTorchLightStatus();

    int getVideoCollageRecordingTime();

    int getVideoCollageType();

    int getViewMode();

    int getVolumeKeyAs();

    int getWatermarkCategory();

    int getWatermarkId();

    String getWatermarkInputText();

    int getWhiteBalance();

    int getZoomValue();

    void initAttachVideoFixedResolution();

    void initRequestedMediaRecorderProfileInfo();

    void initRequestedRecordingDurationLimit();

    void initRequestedRecordingSizeLimit();

    void initRequestedSaveUri();

    void initializeCamera();

    void initializeCameraId(int i);

    void initializeCameraResolution();

    void initializeDefaultBackCameraShootingModeOrderList();

    void initializeDefaultFrontCameraShootingModeOrderList();

    int initializeShootingModeId(String str);

    int initializeShootingModeId(String str, String str2);

    int initializeShootingModeWhenSwitchCamera();

    boolean isAttachImageMode();

    boolean isAttachVideoFixedResolution();

    boolean isAttachVideoMode();

    boolean isBackCamera();

    boolean isCamcorderAntiShakeSupported(int i);

    boolean isCoverCamera();

    boolean isCurrentSeparatedShootingMode();

    boolean isDefaultFrontShootingMode(int i);

    boolean isDefaultRearShootingMode(int i);

    boolean isDualBackCamera();

    boolean isDualFrontCamera();

    boolean isEasyCamera();

    boolean isEffectRecordingRestricted();

    boolean isFirstLaunchCameraByHomeKey();

    boolean isFrontCamera();

    boolean isKeyboardCoverCamera();

    boolean isNotificationExist();

    boolean isResetRequested();

    boolean isResizableCamera();

    boolean isSecureCamera();

    boolean isSilverCamera();

    boolean isSingleEffect();

    boolean isSupportedBackCamcorderResolutionFeature(int i);

    boolean isSupportedBackCameraResolutionFeature(int i);

    boolean isSupportedFrontCamcorderResolutionFeature(int i);

    boolean isSupportedFrontCameraResolutionFeature(int i);

    boolean isTemperatureHighToRecord();

    boolean isTemperatureHighToUseFlash();

    boolean isTemperatureLowToUseFlash();

    void overrideCamcorderResolution(int i);

    void overrideFocusMode(int i);

    void refreshButtonDimForCamera();

    void refreshSettingValuesFromPreferencesWhenSwitchCamera();

    void registerAllCameraSettingsChangedListener(CameraSettingChangedListener cameraSettingChangedListener);

    void registerCameraSettingChangedListener(int i, CameraSettingChangedListener cameraSettingChangedListener);

    void resetCameraEffect();

    void resetCameraSettingsInDual();

    void resetCameraSettingsToDefault();

    void resetListeners();

    void resetOverriddenCamcorderResolution();

    void resetOverriddenFocusMode();

    void resetShootingModeOrder();

    void resetZoomValue();

    void restoreDualResolution(int i);

    int restoreShootingModeAfterRecording();

    void setAntiFogLevel(int i);

    void setAttachImageMode(boolean z);

    void setAttachVideoFixedResolution(int i, int i2);

    void setAttachVideoMode(boolean z);

    void setAutoNightDetectionMode(int i);

    void setBackBeautyLevel(int i);

    void setBackCameraShootingModeOrder(String str);

    void setBeautyLevel(int i);

    void setBeautyMode(int i);

    void setCallStatus(int i, boolean z);

    void setCamcorderAntiShake(int i);

    void setCamcorderAudioRecording(int i);

    boolean setCamcorderResolution(int i);

    void setCameraDualEffect(int i);

    void setCameraEffect(int i);

    void setCameraEffect(int i, boolean z);

    void setCameraExposureMeter(int i);

    void setCameraHDR(int i);

    void setCameraISO(int i);

    void setCameraId(int i);

    void setCameraQuality(int i);

    boolean setCameraResolution(int i);

    void setCameraResolutionChanged(boolean z);

    void setCameraShutterSound(int i);

    void setCameraVoiceCommand(int i);

    void setColorTune(int i);

    void setCoverCamera(boolean z);

    void setDefaultBackCameraShootingModeOrder();

    void setEasyCamera(boolean z);

    void setEffectListType(int i);

    void setEffectStrengthLevel(int i);

    void setEffectVignetteLevel(int i);

   // void setEngine(Engine engine);

    void setExposureValue(int i);

    void setEyeEnlargeLevel(int i);

    void setFlashMode(int i);

    void setFloatingCameraButton(int i);

    void setFocusLength(int i);

    void setFocusMode(int i);

    void setFocusMode(int i, int i2);

    void setFontScale(float f);

    void setFoodBlurType(int i);

    void setFoodColorTuneValue(int i);

    void setFrontCameraShootingModeOrder(String str);

    void setFrontFlashMode(int i);

    void setGPS(int i);

    void setGestureControlMode(int i);

    void setGuideline(int i);

    void setHRMShutter(int i);

    void setInterval(int i);

    void setIsFirstLaunchCameraByHomeKey(boolean z);

    void setKelvinValue(int i);

    void setKeyboardCoverCamera(boolean z);

    void setLowBatteryStatus(boolean z);

    void setManualSettings(int i);

    void setMotionPanoramaMode(int i);

    void setMotionPhoto(int i);

    void setMotionWideSelfieMode(int i);

    void setMultiAFMode(int i);

    void setMultiWindowMode(int i);

    void setObjectTrackingAF(int i);

    void setPictureFormat(int i);

    void setQRCodeDetection(int i);

    void setRearLensDistortionCorrection(int i);

    void setRecordingMotionSpeed(int i);

    void setRequestedMediaRecorderProfileInfo(RequestedMediaRecorderProfileInfo requestedMediaRecorderProfileInfo);

    void setRequestedRecordingDurationLimit(int i);

    void setRequestedRecordingSizeLimit(long j);

    void setRequestedSaveUri(Uri uri);

    void setResizableCamera(boolean z);

    void setReview(int i);

    void setSaveRichTone(int i);

    void setSecureCamera(boolean z);

    void setSelfFlip(int i);

    void setSeparatedShootingMode(int i, String str);

    void setShapeCorrection(int i);

    void setShootingMode(int i);

    void setShutterSpeed(int i);

    void setSkinColorLevel(int i);

    void setSlimFaceLevel(int i);

    void setSoundAndShotMode(int i);

    void setSpotlightLevel(int i);

    void setSpotlightPosition(int i);

    void setStorage(int i);

    void setTapToTakePictures(int i);

    void setTemperatureHighToRecord(boolean z);

    void setTemperatureHighToUseFlash(boolean z);

    void setTemperatureLowToUseFlash(boolean z);

    void setTimer(int i);

    void setTorchLightStatus(int i);

    void setVideoCollageRecordingTime(int i);

    void setVideoCollageResolution();

    void setVideoCollageType(int i, boolean z);

    void setViewMode(int i);

    void setVolumeKeyAs(int i);

    void setWatermarkCategory(int i);

    void setWatermarkId(int i);

    void setWatermarkInputText(String str);

    void setWhiteBalance(int i);

    void setZoomValue(int i);

    void storeShootingModeBeforeRecording();

    void unregisterAllCameraSettingsChangedListener(CameraSettingChangedListener cameraSettingChangedListener);

    void unregisterCameraSettingChangedListener(int i, CameraSettingChangedListener cameraSettingChangedListener);

    void updateCameraResolutionForDual();


}
