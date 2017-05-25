package pixedar.com.superlapse;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class SliderFragment extends Fragment {

    Button button;

    private TextView speedValue;
    private TextView timeValue;
    private TextView imagesValue;

    private int speed = 1000;
    private int time = 0;
    private int images = 0;

    private RadioButton hd;
    private RadioButton fullHd;
    private RadioButton fourK;
    private RadioButton fourKC;

    private boolean hdIsChecked = false;
    private boolean fullHdIsChecked = false;
    private boolean fourKIsChecked = false;
    private boolean fourKCIsChecked = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.slider_fragment, container, false);
        SeekBar speedSeekBar = (SeekBar) rootView.findViewById(R.id.speedSeekBar);
        SeekBar imagesSeekBar = (SeekBar) rootView.findViewById(R.id.imagesSeekBar);
        SeekBar timeSeekBar = (SeekBar) rootView.findViewById(R.id.timeSeekBar);

        speedValue = (TextView) rootView.findViewById(R.id.speedValue);
        timeValue = (TextView) rootView.findViewById(R.id.timeValue);
        imagesValue = (TextView) rootView.findViewById(R.id.imagesValue);

        hd = (RadioButton) rootView.findViewById(R.id.hd);
        fullHd = (RadioButton) rootView.findViewById(R.id.full_hd);
        fourK = (RadioButton) rootView.findViewById(R.id.fk);
        fourKC = (RadioButton) rootView.findViewById(R.id.fkC);

        button = (Button) rootView.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraPreview.class);
                intent.putExtra("delay", speed);
                intent.putExtra("time", time);
                intent.putExtra("images", images);
                intent.putExtra("hdIsChecked", hdIsChecked);
                intent.putExtra("fullHdIsChecked", fullHdIsChecked);
                intent.putExtra("fourKIsChecked", fourKIsChecked);
                intent.putExtra("fourKCIsChecked", fourKIsChecked);
                startActivity(intent);
            }
        });
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValue.setText(String.valueOf((float) (progress / 10.0f)) + " s");
                speed = progress * 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        imagesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                images = progress;
                if (progress == 0) {
                    imagesValue.setText("unlimited");
                } else {
                    imagesValue.setText(String.valueOf(progress) + " images");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time = progress;
                if (progress == 0) {
                    timeValue.setText("unlimited");
                } else {
                    timeValue.setText(String.valueOf(progress) + " min");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fullHdIsChecked = false;
                    fourKIsChecked = false;
                    hdIsChecked = true;
                    fourKCIsChecked = false;
                    fourKC.setChecked(fourKCIsChecked);
                    fullHd.setChecked(fullHdIsChecked);
                    fourK.setChecked(fourKIsChecked);
                    hd.setChecked(hdIsChecked);
                }
            }
        });
        fullHd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fullHdIsChecked = true;
                    fourKIsChecked = false;
                    hdIsChecked = false;
                    fourKCIsChecked = false;
                    fourKC.setChecked(fourKCIsChecked);
                    fullHd.setChecked(fullHdIsChecked);
                    fourK.setChecked(fourKIsChecked);
                    hd.setChecked(hdIsChecked);
                }
            }
        });
        fourK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fullHdIsChecked = false;
                    fourKIsChecked = true;
                    hdIsChecked = false;
                    fourKCIsChecked = false;
                    fourKC.setChecked(fourKCIsChecked);
                    fullHd.setChecked(fullHdIsChecked);
                    fourK.setChecked(fourKIsChecked);
                    hd.setChecked(hdIsChecked);
                }
            }
        });
        fourKC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fullHdIsChecked = false;
                    fourKIsChecked = false;
                    hdIsChecked = false;
                    fourKCIsChecked = true;
                    fourKC.setChecked(fourKCIsChecked);
                    fullHd.setChecked(fullHdIsChecked);
                    fourK.setChecked(fourKIsChecked);
                    hd.setChecked(hdIsChecked);
                }
            }
        });

        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
        return rootView;
    }
}
