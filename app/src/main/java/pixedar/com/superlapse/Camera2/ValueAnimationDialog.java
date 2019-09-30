package pixedar.com.superlapse.Camera2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Scatter;
import com.anychart.core.scatter.series.Line;
import com.anychart.core.scatter.series.Marker;

import java.util.ArrayList;

import pixedar.com.superlapse.CameraParameters;
import pixedar.com.superlapse.R;
import pixedar.com.superlapse.Settings;
import pixedar.com.superlapse.TimelapseSettingsController;
import pixedar.com.superlapse.util.log;

import static pixedar.com.superlapse.Camera2.Camera2PreviewActivity.fousLowerBound;

public class ValueAnimationDialog extends DialogFragment {
    private final double MAX_VAL_CEF = 1.0f/3.2f;
   private int progress;
   private boolean started = false;
    final ArrayList<DataEntry> data = new ArrayList<>();
    final ArrayList<DataEntry> data2 = new ArrayList<>();
    private SeekBar valueSlider;
    private SeekBar timeSlider;
    Line line;
    Marker marker;
public static final int MIN_INTERVAL_FOR_FOCUS_STACKING =2500;
    private int selectedIndex = 1;
    private final int markerSize = 7;
    private  AlertDialog dialogA;
    private CameraParameters.Param[] currentParams;
    private OnButtonHoldListener listener;
    private boolean isShowing = false;
    private boolean isDestroyed = false;
    private boolean isLightMeterSelected = false;
    private String type;
    public void setParams(CameraParameters.Param[] currentParams, int progress,String type){
        this.currentParams =currentParams;
        this.progress = progress;
        this.type = type;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        initKeyFrameDialog( builder ,getKeyFrameView(inflater));
        dialogA.setOnShowListener(new DialogInterface.OnShowListener() {
           @Override
           public void onShow(DialogInterface dialogInterface) {

               Button button = dialogA.getButton(AlertDialog.BUTTON_NEUTRAL);
               button.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       dialogA.dismiss();
                       initLightMeterDialog(builder, getLightMeterView(inflater));
                       isLightMeterSelected = true;

                   }
               });
           }
       });
        isShowing = true;
        return  dialogA;
    }
    public void show(){
        if(dialogA!=null){
            isShowing = true;
            dialogA.show();
        }
    }
    public boolean isDestroyed(){
        return  isDestroyed;
    }
    private void initKeyFrameDialog(AlertDialog.Builder builder  ,View view){

        builder.setView(view)
                .setNeutralButton("Use light meter", null)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(data.size() >1){
                            if(isFocusSteps()&&TimelapseSettingsController.settings.speed < MIN_INTERVAL_FOR_FOCUS_STACKING){
                                new Toast(getContext()).setText("unable to set keyframes because time period between taking photos is to small, change time peroid in settings");
                            }else {
                                TimelapseSettingsController.settings.valueAnimationParams.setKeyframes(type,getKeyframes(data, currentParams[0].isDecrete()));
                                for (OnPositiveButtonClicked listener : onPositiveButtonClickedListeners) {
                                    listener.onClicked();
                                }
                            }
                        }
                        if(isLightMeterSelected){
                            lightMeterSaveSettings();
                            TimelapseSettingsController.settings.valueAnimationParams.lightMeter.enabled = true;
                        }
                        if(ValueAnimationDialog.this.getDialog() != null){
                            ValueAnimationDialog.this.getDialog().cancel();
                            isDestroyed = true;
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(ValueAnimationDialog.this.getDialog() != null){
                            ValueAnimationDialog.this.getDialog().cancel();
                            isDestroyed = true;
                        }
                    }
                });
        dialogA =  builder.create();
    }

    private void initLightMeterDialog(final AlertDialog.Builder builder , View view){
        builder.setView(view).setNeutralButton(" ",null)
       .create().show();
    }
    private  View getLightMeterView(LayoutInflater inflater){
        View view = inflater.inflate(R.layout.light_meter_view, null);
        SeekBar maxValSlider =  view.findViewById(R.id.light_meter_max_val_slider);
        SeekBar smoothnessSlider =  view.findViewById(R.id.light_meter_smoothness_slider);
        SeekBar NdFilterSlider =  view.findViewById(R.id.light_meter_nd_filter_slider);
        SeekBar adjustSlider =  view.findViewById(R.id.light_meter_adjust_slider);
        final TextView NdFilterText =   view.findViewById(R.id.light_meter_nd_filter_text);
        final TextView adjustText =   view.findViewById(R.id.light_meter_adjust_text);
        final TextView smoothnessText =   view.findViewById(R.id.light_meter_smoothnes_Text);
        final TextView maxValText =   view.findViewById(R.id.light_meter_max_val_Text);
        RadioGroup radioGroup = view.findViewById(R.id.light_meter_radio_group);
        final RadioButton arduinButton = view.findViewById(R.id.light_meter_arduino_button);
        lightMeterLoadSettings();
        NdFilterSlider.setMax(Settings.ValueAnimationParams.LightMeter.MAX_ND_FILTER);
        NdFilterSlider.setProgress( TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue);
        NdFilterSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                NdFilterText.setText(String.valueOf(progress));
                 TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        adjustSlider.setMax(Settings.ValueAnimationParams.LightMeter.MAX_ADJUST);
        adjustSlider.setProgress( TimelapseSettingsController.settings.valueAnimationParams.lightMeter.adjustValue);
        adjustSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adjustText.setText(String.valueOf(progress));
                 TimelapseSettingsController.settings.valueAnimationParams.lightMeter.adjustValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        maxValSlider.setMax((int) Math.round(Math.pow(Settings.ValueAnimationParams.LightMeter.ABSOULTE_MAX_VAL,MAX_VAL_CEF)));
       // maxValSlider.setProgress(); //todo
        maxValSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int p = (int) Math.round(Math.pow(progress,1.0f/MAX_VAL_CEF ));
                if(p > 60000){
                    maxValText.setText(String.format("%.1f",p/60000.0f) +"min");
                }else if(p < 60000&&p > 1000){
                    maxValText.setText(String.valueOf(p/1000) +"s");
                }else{
                    maxValText.setText(String.valueOf(p) +"ms");
                }
                if(p >0){
                     TimelapseSettingsController.settings.valueAnimationParams.lightMeter.maxVal = p;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        maxValSlider.setProgress(maxValSlider.getMax()/2);

        smoothnessSlider.setMax(Math.round((1.0f - Settings.ValueAnimationParams.LightMeter.MIN_SMOTHNESS_VAL)*1000)-1);
        smoothnessSlider.setProgress((int) (( TimelapseSettingsController.settings.valueAnimationParams.lightMeter.smoothness -Settings.ValueAnimationParams.LightMeter.MIN_SMOTHNESS_VAL)*1000));
        smoothnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > 0) {
                     TimelapseSettingsController.settings.valueAnimationParams.lightMeter.smoothness = Settings.ValueAnimationParams.LightMeter.MIN_SMOTHNESS_VAL + progress / 1000.0f;
                }
                float val = Settings.ValueAnimationParams.LightMeter.MIN_SMOTHNESS_VAL*100+(progress/10.0f);

                smoothnessText.setText(String.format("%.1f",val)+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        smoothnessSlider.setProgress(smoothnessSlider.getMax()/2);
        arduinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.log(arduinButton.isChecked());
                if(!arduinButton.isChecked()){
                     TimelapseSettingsController.settings.valueAnimationParams.lightMeter.arduinoEnabled = true;
                }else{
                     TimelapseSettingsController.settings.valueAnimationParams.lightMeter.arduinoEnabled = false;
                }

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case 0:  TimelapseSettingsController.settings.valueAnimationParams.lightMeter.modeIndex= Settings.ValueAnimationParams.LightMeter.SMARPHONE_CAMERA; break;
                    case 1:  TimelapseSettingsController.settings.valueAnimationParams.lightMeter.modeIndex= Settings.ValueAnimationParams.LightMeter.LIGHT_SENSOR; break;
                }
            }
        });

        return view;
    }
    private void lightMeterLoadSettings(){
        SharedPreferences settingsPref = getContext().getSharedPreferences("SuperLapsePref", 0);
         TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue =  settingsPref.getInt("ndFilterValue", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue);
         TimelapseSettingsController.settings.valueAnimationParams.lightMeter.adjustValue =  settingsPref.getInt("adjustValue", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.adjustValue);
         TimelapseSettingsController.settings.valueAnimationParams.lightMeter.smoothness=  settingsPref.getFloat("smoothness", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.smoothness);
         TimelapseSettingsController.settings.valueAnimationParams.lightMeter.maxVal=  settingsPref.getInt("maxVal", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue);
    }
    private void lightMeterSaveSettings(){
        SharedPreferences settingsPref = getContext().getSharedPreferences("SuperLapsePref", 0);
        SharedPreferences.Editor editor = settingsPref.edit();
        editor.putInt("ndFilterValue", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.ndFilterValue);
        editor.putInt("adjustValue", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.adjustValue);
        editor.putFloat("smoothness", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.smoothness);
        editor.putInt("maxVal", TimelapseSettingsController.settings.valueAnimationParams.lightMeter.maxVal);
        editor.apply();
    }
    public interface OnButtonHoldListener{
        void onButtonHold(boolean isShowing);
    }
    public interface OnPositiveButtonClicked{
        void onClicked();
    }
     private ArrayList<OnPositiveButtonClicked> onPositiveButtonClickedListeners = new ArrayList<>();
    public void setOnPositiveButtonClickedListener(OnPositiveButtonClicked onPositiveButtonClickedListener){
        this.onPositiveButtonClickedListeners.add(onPositiveButtonClickedListener);
    }
    public void setOnAddButtonHoldListener(OnButtonHoldListener listener){
        this.listener = listener;
    }
    public boolean isShowing(){
        return isShowing;
    }
    private View getKeyFrameView(LayoutInflater inflater){
        View view = inflater.inflate(R.layout.value_animation_dialog, null);
        final Scatter lineChart = AnyChart.scatter();
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        FloatingActionButton addKeyframeButton = view.findViewById(R.id.add_keyframe_button);
        valueSlider = view.findViewById(R.id.value_slider);
        timeSlider = view.findViewById(R.id.dialog_time_slider);
        final TextView valueText = view.findViewById(R.id.value_text);
        final TextView timeText= view.findViewById(R.id.dialog_time_text);

        valueSlider.setProgress(progress);
        data.add(new ValueDataEntry(0,getValue(),0));
       // ValueDataEntry entry =new ValueDataEntry(100,progress,1);
       // data.add(entry);
        if(currentParams[0].getValue().intValue() == CameraParameters.FOCUS_STEPS){
            lineChart.yScale().maximum(((CameraParameters.FocusSteps)(currentParams[0])).getSoftSteps());
            lineChart.yScale().minimum(-((CameraParameters.FocusSteps)(currentParams[0])).getSoftSteps());
            valueSlider.setMax(((CameraParameters.FocusSteps)(currentParams[0])).getSoftSteps()*2);
        }else {
            lineChart.yScale().maximum((int)(currentParams.length*1.2f));
            lineChart.yScale().minimum(0);
            valueSlider.setMax(currentParams.length-1);
        }


        lineChart.tooltip(false);
        for(int k=0; k <100;k++){
            data2.add(new ValueDataEntry(k,0,0));
        }

        lineChart.line(data2);


        marker = lineChart.marker(data);
        marker.type("circle");
        marker.size(markerSize);

        line  =lineChart.line(data);


        marker.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value","index"}) {
            @Override
            public void onClick(Event event) {
                selectedIndex = Integer.valueOf(event.getData().get("index"));
                valueSlider.setProgress(Integer.valueOf(event.getData().get("value")));
                timeSlider.setProgress(Integer.valueOf(event.getData().get("x")));
            }
        });


        timeSlider.setMax(100);
        timeSlider.setProgress(100);
        timeText.setText("100");


        valueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(currentParams[0].getValue().intValue() == CameraParameters.FOCUS_STEPS){
                    valueText.setText(String.valueOf(getValue()));
                }else {
                    valueText.setText(currentParams[progress].getName());
                }
                setCurrentKeyFrame();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeText.setText(String.valueOf(progress)+"%");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setCurrentKeyFrame();
            }
        });

        addKeyframeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(listener !=null){
                    if(isShowing){
                        dialogA.hide();
                        isShowing = false;
                    }else{
                        dialogA.show();
                        isShowing = false;
                    }
                    listener.onButtonHold(isShowing);
                }
                return false;
            }
        });
        addKeyframeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addKeyFrame(getValue());
            }
        });

        anyChartView.setChart(lineChart);
        return  view;
    }

    public void addKeyFrame(int value){
        if(started) {
            started = false;
            if (timeSlider.getProgress() - markerSize > 0) {
                timeSlider.setProgress(timeSlider.getProgress() - markerSize);
            } else {
                timeSlider.setProgress(timeSlider.getProgress() + markerSize);
            }
        }

        //   ctn++;
        selectedIndex = data.size()-1;
        ValueAnimationDialog.ValueDataEntry entry =new ValueAnimationDialog.ValueDataEntry(timeSlider.getProgress(),value,selectedIndex);
        data.add(selectedIndex,entry);

        line.data(data);
        marker.data(data);
        started= true;
    }
    private int[] getKeyframes(ArrayList<DataEntry> data,boolean decrete){
      final int numberOfImages = (( TimelapseSettingsController.settings.time*60000)/ TimelapseSettingsController.settings.speed);
        int[] frames = new int[numberOfImages];
        log.log("numOfImages");
        log.log(TimelapseSettingsController.settings.time);
        log.log(TimelapseSettingsController.settings.time*60000);
        log.log(TimelapseSettingsController.settings.speed);
        log.log(numberOfImages);
        int dataIndex  =data.size()-1;
        float xA =1;
        float yA =1;
        float xB =1;
        float yB = 1;
        int x =0;
        boolean lock = false;
        for(int k =0; k < frames.length; k++){
            if(!lock) {
                x = Math.round((((k + 1.0f) / numberOfImages) * 100.0f));
            }
            if(dataIndex-1 >=0) {
                int datasetYA = Integer.valueOf(String.valueOf(data.get(dataIndex).getValue("value")));
                int datasetYB = Integer.valueOf(String.valueOf(data.get(dataIndex-1).getValue("value")));
                xA = Integer.valueOf(String.valueOf(data.get(dataIndex).getValue("x")));
                yA = isFocusSteps()?datasetYA:currentParams[datasetYA].getValue().floatValue();
                xB = Integer.valueOf(String.valueOf(data.get(dataIndex -1).getValue("x")));
                yB = isFocusSteps()?datasetYB:currentParams[datasetYB].getValue().floatValue();
                if(x >= Integer.valueOf(String.valueOf(data.get(dataIndex-1).getValue("x")))){
                    dataIndex--;
                }
            }else if(x > Integer.valueOf(String.valueOf(data.get(0).getValue("x")))){
                lock = true;
            }
            float y  = x*(yA - yB)/(xA -xB) + (yA - ((yA -yB)/(xA - xB))*xA);
            //log.log("x: "+String.valueOf(xB) +" y: "+String.valueOf(yB) );

            if(decrete){
                int min = Integer.MAX_VALUE;
                int index = 0;
                for(int q = 0; q < currentParams.length; q++){
                    int val = Math.round(Math.abs( y - currentParams[q].getValue().floatValue()));
                    if(val < min){
                        min = val;
                        index = q;
                    }
                }
                frames[k] = index;
                //  frames[k] = currentParams[index].getValue().intValue();
            }
            frames[k] = Math.round(y);
            log.log(Integer.valueOf(String.valueOf(data.get(0).getValue("x"))));
            log.log(x);
            log.log(frames[k]);
        }
        return frames;
    }
    private void setCurrentKeyFrame(){
        if(started) {
            ValueDataEntry entry = new ValueDataEntry(timeSlider.getProgress() , getValue(),selectedIndex);
            data.set(selectedIndex , entry);

            line.data(data);
            marker.data(data);
        }
    }
    private class ValueDataEntry extends DataEntry {

        public ValueDataEntry(Number x, Number value,int index) {
            setValue("x", x);
            setValue("value", value);
            setValue("index",index);
        }

    }
    private boolean isFocusSteps(){
        return currentParams[0].getValue().intValue() == CameraParameters.FOCUS_STEPS;
    }
    private int getValue(){
        int value;
        if(isFocusSteps()){
            value = (valueSlider.getProgress()+fousLowerBound) -((CameraParameters.FocusSteps)currentParams[0]).getSoftSteps();
        }else{
            value = valueSlider.getProgress();
        }
        return value;
    }
}
