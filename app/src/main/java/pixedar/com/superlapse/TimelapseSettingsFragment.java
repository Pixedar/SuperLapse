package pixedar.com.superlapse;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

import pixedar.com.superlapse.Dslr.ptp.Camera;


public abstract class TimelapseSettingsFragment extends Fragment {
    

    public final Settings settingsT = new Settings();
    
    public RadioButton fullHd;
    public RadioButton twoK;
    public RadioButton fourK;
    public RadioButton fourKC;
    public RadioButton rawRadioButton;
    public RadioButton ultraHdrRadioButton;
    public RadioButton saveOnInternal;
    public RadioButton saveOnSD;
    public SeekBar batterySeekBar;
    public SeekBar speedSeekBar;

    public TextView speedValue;
    public TextView timeValue;
    public TextView sizeTextView;
    public TextView[] compressionValue = new TextView[1];
    public SeekBar timeSeekBar;

    public Intent batteryStatus;
    public final SeekBar[] compressionSeekBar = new SeekBar[1];

  //  private TimelapseSettingsController settingsController;
    Camera camera;
public TimelapseSettingsFragment(){


}
    private void initController(){
//        if(getArguments().getBoolean("isDslr", false)){
        //    settingsController = new DslrTimelapseSettingsController(camera);
  //      }else{
        //    settingsController = new TimelapseSettingsController(getContext(),settingsT);
  //      }
    }

    String getSizeString(Size[] sizes, int index){
        String extra ="";
        switch (index){
            case 0: extra = "small "; break;
            case 1: extra = "medium "; break;
            case 2: extra = "large "; break;
        }
        switch (sizes[index].getWidth()*sizes[index].getHeight()){
            case 1920*1080: return extra +  "(Full HD)";
            case 1140*2560: return extra +  "(2K)";
            case 4032*2268: return extra +  "(4K)";
        }
        return extra + String.valueOf(sizes[index].getWidth())+"x"+String.valueOf(sizes[index].getHeight());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        initController();
        timeSeekBar = (SeekBar) rootView.findViewById(R.id.timeSeekBar);

         speedSeekBar = (SeekBar) rootView.findViewById(R.id.speedSeekBar);
        batterySeekBar = (SeekBar) rootView.findViewById(R.id.batterySeekBar);
        final SeekBar ultraHdrRangeSeekBar = (SeekBar) rootView.findViewById(R.id.hdrRangeSeekBar);
         compressionSeekBar[0] = (SeekBar) rootView.findViewById(R.id.compressionSeekBar);
          compressionValue[0] = (TextView) rootView.findViewById(R.id.compressionValue);
        final TextView batteryValue = (TextView) rootView.findViewById(R.id.batteryValue);
        final TextView ultraHdrRangeVale = (TextView) rootView.findViewById(R.id.hdrRangeValue);

        ultraHdrRangeSeekBar.setEnabled(false);


        speedValue = (TextView) rootView.findViewById(R.id.speedValue);
        timeValue = (TextView) rootView.findViewById(R.id.timeValue);
        sizeTextView = (TextView) rootView.findViewById(R.id.sizeTextView);

        fullHd = (RadioButton) rootView.findViewById(R.id.hd);
        twoK = (RadioButton) rootView.findViewById(R.id.full_hd);
        fourK = (RadioButton) rootView.findViewById(R.id.fk);
        fourKC = (RadioButton) rootView.findViewById(R.id.fkC);
        rawRadioButton = (RadioButton) rootView.findViewById(R.id.rawRadioButton);
        ultraHdrRadioButton = (RadioButton) rootView.findViewById(R.id.hdrButton);

        saveOnInternal = (RadioButton) rootView.findViewById(R.id.saveToInternal);
        saveOnSD = (RadioButton) rootView.findViewById(R.id.saveToSD);
        Button startPreview = (Button) rootView.findViewById(R.id.startPreview);

 /*       saveOnInternal.setText(saveOnInternal.getText() + settingsController.getFreeSpace(1));
        saveOnSD.setText(saveOnSD.getText() + settingsController.getFreeSpace(0));
        settingsController.loadSettings(getContext());*/
        updateTimeBar();
        timeSeekBar.setProgress(timeSeekBar.getMax());
        if (timeSeekBar.getMax() > 60) {
            timeValue.setText(String.valueOf(timeSeekBar.getMax() / 60) + " h " + String.valueOf(timeSeekBar.getMax() - (timeSeekBar.getMax() / 60) * 60) + " min");
        } else {
            timeValue.setText(String.valueOf(timeSeekBar.getMax()) + " min");
        }


         settingsT.time = timeSeekBar.getMax();

        saveOnInternal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                     settingsT.saveOnInternal = true;
                     settingsT.saveOnSD = false;
                    saveOnSD.setChecked(false);
                    updateTimeBar();
                }
            }
        });

    //    setSaveOnSDStatus(settingsController.checkExternalMemory());
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 settingsT.time = progress;
                if (progress == 0) {
                    timeValue.setText("unlimited");
                } else if (progress > 60) {
                    timeValue.setText(String.valueOf(progress / 60) + " h " + String.valueOf(progress - (progress / 60) * 60) + " min");
                } else {
                    timeValue.setText(String.valueOf(progress) + " min");
                }
                updateTimeBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val;
                if(progress <40){
                    val = progress/4.0f;
                }else {
                    val = (float) (10+ Math.pow(progress-40,2.3f));
                }
                if(val < 60){
                    speedValue.setText(String.format("%.2f",val) + " s");
                }else if (val >=60&&val < 3600){
                    speedValue.setText(String.valueOf(Math.round(val / 60)) + " min " + String.valueOf(Math.round(val -(int)(val/60)*60)) + " s");
                }else if(val >3600) {
                    speedValue.setText(String.valueOf(Math.round(val / 3600)) + " h " + String.valueOf(Math.round((val -(int)(val/3600)*3600)/60)) + " min");
                }
           //     speedValue.setText(String.valueOf( (progress / 10.0f)) + " s");
                settingsT.speed = Math.round(val*1000);
                updateTimeBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fullHd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResolutionMenu(0);
            }
        });

        twoK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResolutionMenu(1);
            }
        });

        fourKC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResolutionMenu(2);
            }
        });

        fourK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResolutionMenu(3);
            }
        });
        rawRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResolutionMenu(4);
                if (settingsT.captureRawImages) {
                    settingsT.captureRawImages = false;
                } else {
                    settingsT.captureRawImages = true;
                }
                updateTimeBar();
            }
        });
   /*     rawRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  if (settingsController.isCaptureRawImages()) {
                if (settingsT.captureRawImages) {
                     settingsT.captureRawImages = false;
                    rawRadioButton.setChecked(false);
                    ultraHdrRangeSeekBar.setEnabled(false);
                    compressionValue[0].setTextColor(getResources().getColorStateList(R.color.black));
                    fullHd.setTextColor(getResources().getColorStateList(R.color.black));
                    twoK.setTextColor(getResources().getColorStateList(R.color.black));
                    fourK.setTextColor(getResources().getColorStateList(R.color.black));
                    fourKC.setTextColor(getResources().getColorStateList(R.color.black));
                    ultraHdrRangeVale.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    ultraHdrRadioButton.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    compressionSeekBar[0].setEnabled(true);
                    fullHd.setEnabled(true);
                    twoK.setEnabled(true);
                    fourK.setEnabled(true);
                    fourKC.setEnabled(true);
                    ultraHdrRadioButton.setEnabled(false);
                } else {
                    settingsT.captureRawImages = true;
                    rawRadioButton.setChecked(true);
                    ultraHdrRangeSeekBar.setEnabled(true);
                    compressionValue[0].setTextColor(getResources().getColorStateList(R.color.lightGray));
                    fullHd.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    twoK.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    fourK.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    fourKC.setTextColor(getResources().getColorStateList(R.color.lightGray));
                    ultraHdrRangeVale.setTextColor(getResources().getColorStateList(R.color.black));
                    ultraHdrRadioButton.setTextColor(getResources().getColorStateList(R.color.black));
                    compressionSeekBar[0].setEnabled(false);
                    fullHd.setEnabled(false);
                    twoK.setEnabled(false);
                    fourK.setEnabled(false);
                    fourKC.setEnabled(false);
                    ultraHdrRadioButton.setEnabled(true);
                }
                updateTimeBar();
            }
        });*/

        compressionSeekBar[0].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                compressionValue[0].setText(String.valueOf(progress) + "%");
                 settingsT.compression= 100 - seekBar.getProgress();
                updateTimeBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onCompressionChanged(seekBar.getProgress());
            }
        });

        batterySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryValue.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
               /* if (batteryStatus != null) {
                    seekBar.setMax(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) - 1);
                }*/
          //     settingsController.getBatteryLevel(getContext());

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 settingsT.batteryLevel = seekBar.getProgress();

            }
        });
      //  public static Uri uri;
        startPreview.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
            //    settingsController.startPreview(getActivity(),FLAG_DIR_SUPPORTS_CREATE);
                onStartPressed();
/*                if ( settingsT.saveOnSD) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.setFlags(FLAG_DIR_SUPPORTS_CREATE);
                    startActivityForResult(intent, 43);
                }else {
                    Intent intent = new Intent(getActivity(), Camera2PreviewActivity.class);
                    startActivity(intent);
                }*/
            }
        });
        ultraHdrRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! settingsT.ultraHdrMode){
                    ultraHdrRadioButton.setChecked(true);
                     settingsT.ultraHdrMode = true;
                }else{
                    ultraHdrRadioButton.setChecked(false);
                     settingsT.ultraHdrMode = false;
                }
            }
        });

        ultraHdrRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress >0) {
                    ultraHdrRangeVale.setText(String.valueOf(progress) +" pictures");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 settingsT.ultraHdrRange = seekBar.getProgress();
            }
        });

        return rootView;
    }
 //   public static DocumentFile pickedDir;

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
      //  settingsController.onActivityResult( requestCode, resultCode, resultData,getContext());
/*        try {
            Uri treeUri = resultData.getData();
            SharedPreferences settingsT = getContext().getSharedPreferences("SuperLapsePref", 0);
            SharedPreferences.Editor editor = settingsT.edit();
            editor.putString("uri",treeUri.toString());
            editor.apply();

            pickedDir = DocumentFile.fromTreeUri(getContext(), treeUri).createDirectory(createDirectoryName());

            getContext().grantUriPermission(getContext().getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContext().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if(settingsController.isCaptureRawImages()){
                Intent intent = new Intent(getActivity(), SamsungCameraPreview.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(getActivity(), Camera2PreviewActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
/*    private String createDirectoryName() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        long dateTaken = calendar.getTimeInMillis();

        return DateFormat.format("yyyy-MM-dd 'at' HH꞉mm꞉ss", dateTaken).toString();
    }*/

    protected void updateResolutionMenu(int index) {
        for (int k = 0; k < 5; k++) {
            if (k == index) {
                 settingsT.resoluionMenu[k] = true;
                checkButton(true, index);
            } else {
                 settingsT.resoluionMenu[k] = false;
                checkButton(false, k);
            }
        }
        updateTimeBar();
        onResolutionMenuChanged(index);
    }

    private void checkButton(boolean value, int index) {
        switch (index) {
            case 0:
                fullHd.setChecked(value);
                break;
            case 1:
                twoK.setChecked(value);
                break;
            case 2:
                fourKC.setChecked(value);
                break;
            case 3:
                fourK.setChecked(value);
                break;
            case 4:
                rawRadioButton.setChecked(value);
                break;
            default:
                fourKC.setChecked(true);
        }
    }

/*    private String getFreeSpace(int index) {
        //stat = new StatFs("/storage/6261-3334");
        StatFs stat;
        try {
            if (index == 0) {
                stat = new StatFs(getExternalSDPath());
            } else {
                stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            }
        } catch (Exception e) {
            return " ";
        }

        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        int megaBytesAvailable = (int) (bytesAvailable / (1024 * 1024));
        if (index == 0) {
            maxSdFreeSpace = megaBytesAvailable;
        } else {
            maxInternalFreeSpace = megaBytesAvailable;
        }
        if (megaBytesAvailable > 1024) {
            return " (" + String.format("%.1f", megaBytesAvailable / 1024.0f) + " GB)";
        } else {
            return " (" + String.valueOf(megaBytesAvailable) + " mb)";
        }
    }*/


/*    public String getExternalSDPath() {
        StorageHelper.StorageVolume sv = new StorageHelper().getStorage(StorageHelper.StorageVolume.Type.EXTERNAL);
        if (sv != null) {
            return sv.file.getPath();
        } else {
            String sdpath = System.getenv("SECONDARY_STORAGE");
            if (sdpath == null || sdpath.isEmpty()) {
                sdpath = "/storage/extSdCard";
            }
            return sdpath;
        }
    }*/


    abstract void updateTimeBar();
    abstract void onResolutionMenuChanged(int index);
    abstract void onCompressionChanged(int progress);
    abstract void onStartPressed();

    public void setSaveOnSDStatus(boolean externalMemoryAvailable){
        if(!externalMemoryAvailable){
            settingsT.saveOnSD = false;
            settingsT.saveOnInternal = true;
            saveOnSD.setTextColor(ContextCompat.getColor(getContext(),R.color.lightGray));
            saveOnSD.setChecked(false);
            saveOnInternal.setChecked(true);
            saveOnSD.setEnabled(false);
            updateTimeBar();
        }else{
            saveOnSD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                         settingsT.saveOnSD = true;
                         settingsT.saveOnInternal = false;
                        saveOnInternal.setChecked(false);
                        updateTimeBar();
                    }
                }
            });
        }
    }
    public void disableSaveOnInternal(){
        if(saveOnInternal !=null&&getContext() !=null) {
            saveOnInternal.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.lightGray));
            saveOnInternal.setChecked(false);
            saveOnInternal.setEnabled(false);
            updateTimeBar();
        }
    }
/*    private void checkExternalMemory(){
        if(!externalMemoryAvailable()){
             settingsT.saveOnSD = false;
             settingsT.saveOnInternal = true;
            saveOnSD.setTextColor(ContextCompat.getColor(getContext(),R.color.lightGray));
            saveOnSD.setChecked(false);
            saveOnInternal.setChecked(true);
            saveOnSD.setEnabled(false);
            updateTimeBar();
        }else{
            saveOnSD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                         settingsT.saveOnSD = true;
                         settingsT.saveOnInternal = false;
                        saveOnInternal.setChecked(false);
                        updateTimeBar();
                    }
                }
            });
        }
    }*/


}
