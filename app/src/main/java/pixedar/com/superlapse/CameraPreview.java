package pixedar.com.superlapse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.graphics.Bitmap.createScaledBitmap;


public class CameraPreview extends Activity implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera camera;
    String address = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean started = false;
    private Handler handler = new Handler();
    private String date;
    private int counter = 0;
    private TextView textView;
    private int delay = 1000;
    private final int maxI = 747;
    private int maxImages = maxI;
    private int maxTime = 1000000000;
    private int time = 0;
    private TextView textView2;
    private boolean hdIsChecked = false;
    private boolean fullHdIsChecked = false;
    private boolean fourKCIsChecked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        delay = intent.getIntExtra("delay", 1000);
        maxImages = intent.getIntExtra("images", maxI);
        if (maxImages == 0) {
            maxImages = maxI;
        }

        maxTime = intent.getIntExtra("time", 100000000) * 60;
        if (maxTime == 0) {
            maxTime = 1000000000;
        }

        // "00:14:01:03:38:8B"
        address = "00:14:01:03:38:8B";
        setContentView(R.layout.camera_preview);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        textView = (TextView) findViewById(R.id.textView3);
        textView2 = (TextView) findViewById(R.id.startTextView);
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters camParams = camera.getParameters();
        hdIsChecked = intent.getBooleanExtra("hdIsChecked", false);
        fullHdIsChecked = intent.getBooleanExtra("fullHdIsChecked", false);
        fourKCIsChecked = intent.getBooleanExtra("fourKCIsChecked", false);
        if (hdIsChecked || fullHdIsChecked) {
            camParams.setPictureSize(2560, 1440);
        } else {
            camParams.setPictureSize(4032, 2268);
        }


        camera.setParameters(camParams);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);


        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = (sdf.format(cal.getTime()));

        new ConnectBT().execute();

        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBtConnected) {
                    textView2.setEnabled(false);
                    textView2.setVisibility(View.INVISIBLE);
                    textView2.setOnClickListener(null);
                    start();
                }
            }
        });

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new TakePhoto().execute();

            if (started && counter < maxImages && time / 10 < maxTime) {
                start();
            } else {
                stop();
                disconnect();
            }

        }
    };

    public void start() {
        started = true;
        handler.postDelayed(runnable, delay);
    }

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private class TakePhoto extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            takePhoto();
            return null;
        }


        public void takePhoto() {
            camera.startPreview();
            camera.takePicture(null, null, mCall);
        }

        Camera.PictureCallback mCall = new Camera.PictureCallback() {

            public void onPictureTaken(final byte[] data, Camera camera) {

                FileOutputStream outStream;
                try {
                    File sd = new File(Environment.getExternalStorageDirectory() + "/TimelapseData/", date);
                    sd.mkdirs();
                    outStream = new FileOutputStream(sd + "/" + Integer.toString(counter) + ".jpg");
                    if (hdIsChecked || fullHdIsChecked || fourKCIsChecked) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        Bitmap photo;
                        if (fullHdIsChecked) {
                            photo = createScaledBitmap(bitmap, 1920, 1080, true);
                        } else {
                            photo = createScaledBitmap(bitmap, 1280, 720, true);
                        }
                        photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                        outStream.write(bytes.toByteArray());
                    } else {
                        outStream.write(data);
                    }
                    outStream.close();
                    Log.i("CAM", data.length + " byte written to:" + sd + Integer.toString(counter) + ".jpg");
                    send();

                } catch (IOException e) {
                    Log.d("CAM", e.getMessage());
                }

            }

        };


    }

    private void disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish();

    }

    private void send() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("0".getBytes());
                counter++;
                time += delay / 100;
                textView.setText(String.valueOf(counter) + " (" + String.valueOf(time / 600) + ")");
            } catch (IOException e) {
                if (started) {
                    msg("Send Error");
                }
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(CameraPreview.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        stop();
        disconnect();
    }
}
