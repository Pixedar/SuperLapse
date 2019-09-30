package pixedar.com.superlapse;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;


public class SliderFragment extends Fragment {

    Button button;
    private TextView imagesValue;

    Button button2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.slider_fragment, container, false);
        SeekBar imagesSeekBar = (SeekBar) rootView.findViewById(R.id.imagesSeekBar);
        button2 = (Button) rootView.findViewById(R.id.button);
        imagesValue = (TextView) rootView.findViewById(R.id.imagesValue);

        button = (Button) rootView.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
                if (!myBluetooth.isEnabled()) {
                    enableBtReqest(myBluetooth);
                } else {
                    startCameraPreview();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                boolean isEnabled = bluetoothAdapter.isEnabled();
                if (!isEnabled) {
                    bluetoothAdapter.enable();
                }else {
                    bluetoothAdapter.disable();
                }
            }
        });

        imagesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TimelapseSettingsController.settings.images = (int) (progress * 7.47f);
                if (progress == 0) {
                    imagesValue.setText("unlimited");
                    TimelapseSettingsController.settings.images = 50000;
                } else {
                    imagesValue.setText(String.valueOf(progress) + " %");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return rootView;
    }

    private void enableBtReqest(BluetoothAdapter myBluetooth) {
        if (myBluetooth == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

        } else {
           Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           startActivityForResult(turnBTon, 1);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            startCameraPreview();
        }
    }

    private void startCameraPreview() {
        Intent intent = new Intent(getActivity(), CameraPreview.class);
        intent.putExtra("slider", true);
        /*intent.putExtra("delay", speed);
        intent.putExtra("time", time);
        intent.putExtra("images", images);
        intent.putExtra("hdIsChecked", hdIsChecked);
        intent.putExtra("fullHdIsChecked", fullHdIsChecked);
        intent.putExtra("fourKIsChecked", fourKIsChecked);
        intent.putExtra("fourKCIsChecked", fourKIsChecked);*/
        startActivity(intent);
    }
}
