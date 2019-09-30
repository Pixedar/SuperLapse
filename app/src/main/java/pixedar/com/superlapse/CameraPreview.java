package pixedar.com.superlapse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
    private SurfaceHolder holder;
    private Camera camera;
    private String address = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean started = false;
    private Handler handler = new Handler();
    private String date;
    private int counter = 0;
    private TextView textView;
    private int time = 0;
    private int initialFreeSpace = 0;


    private boolean isSlider;
    private Intent batteryStatus;
    private Camera.Parameters camParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

       /* SCamera mScamera = new SCamera();
        try {
            mScamera.initialize(this);
        } catch (SsdkUnsupportedException e) {
            if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
                msg("The device is not a Samsung device. Some features will not be available");
            } else if (e.getType() == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
                msg("The device does not support Camera");
                finish();
            }
        }*/

        Intent intent = getIntent();
        isSlider = intent.getBooleanExtra("slider", false);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        initialFreeSpace = getFreeSpace();

        address = "00:14:01:03:38:8B";
        setContentView(R.layout.camera_preview);
        textView = (TextView) findViewById(R.id.progressValue);
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        final FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.startButton);
        final FloatingActionButton hdrButton = (FloatingActionButton) findViewById(R.id.hdrButton);
        final FloatingActionButton shutterSpeedButton = (FloatingActionButton) findViewById(R.id.shutterSpeedButton);
        final FloatingActionButton isoButton = (FloatingActionButton) findViewById(R.id.isoButton);
        final SeekBar camSettingsSeekBar = (SeekBar) findViewById(R.id.camSettingsSeekBar);
        final TextView camSettingsValue = (TextView) findViewById(R.id.camSettingsValue);
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.cameraPreviewMainLayout);


        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        camParams = camera.getParameters();

        if (TimelapseSettingsController.settings.resoluionMenu[0] || TimelapseSettingsController.settings.resoluionMenu[1]) {
            camParams.setPictureSize(2560, 1440);
        } else if (TimelapseSettingsController.settings.resoluionMenu[2]) {
            camParams.setPictureSize(4032, 2268);
        } else {
            camParams.setPictureSize(4032, 3024);
        }

        camera.setParameters(camParams);


        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);


        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = (sdf.format(cal.getTime()));

        if (isSlider) {
            new ConnectBT().execute();
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.INVISIBLE);
                startButton.setOnClickListener(null);

                hdrButton.setVisibility(View.INVISIBLE);
                hdrButton.setOnClickListener(null);

                isoButton.setVisibility(View.INVISIBLE);
                isoButton.setOnClickListener(null);

                shutterSpeedButton.setVisibility(View.INVISIBLE);
                shutterSpeedButton.setOnClickListener(null);

                camSettingsSeekBar.setVisibility(View.INVISIBLE);
                camSettingsSeekBar.setOnSeekBarChangeListener(null);

                camSettingsValue.setVisibility(View.INVISIBLE);
                start();
            }
        });

        final boolean[] isShutterSpeedEnabled = {false};
        final boolean[] isIsoEnabled = {false};
        final boolean[] isPreviewVisible = {false};
        final boolean[] isHdrEnabled = {false};
        final int[] isoValue = {0};
        final int[] shutterSpeedValue = {0};

        shutterSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShutterSpeedEnabled[0]) {
                    isShutterSpeedEnabled[0] = false;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsValue.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setEnabled(false);
                } else {
                    isIsoEnabled[0] = false;
                    isShutterSpeedEnabled[0] = true;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setMax(34);
                    camSettingsSeekBar.setProgress(shutterSpeedValue[0]);
               //     camSettingsValue.setText("Shutter speed " + "\n" + CameraParameters.getShutter(shutterSpeedValue[0])); ///////////////////    comment due to new method in new api //todo
                    camSettingsSeekBar.setEnabled(true);
                    camSettingsSeekBar.setVisibility(View.VISIBLE);
                    camSettingsValue.setVisibility(View.VISIBLE);
                }
            }

        });

        isoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIsoEnabled[0]) {
                    isIsoEnabled[0] = false;
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    camSettingsSeekBar.setVisibility(View.INVISIBLE);
                    camSettingsValue.setVisibility(View.INVISIBLE);
                    camSettingsSeekBar.setEnabled(false);
                } else {
                    isIsoEnabled[0] = true;
                    isShutterSpeedEnabled[0] = false;
                    shutterSpeedButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    isoButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    camSettingsSeekBar.setMax(12);
                    camSettingsSeekBar.setProgress(isoValue[0]);
                    camSettingsValue.setText("ISO " + "\n" + CameraParameters.getIsoString(isoValue[0]));
                    camSettingsSeekBar.setEnabled(true);
                    camSettingsSeekBar.setVisibility(View.VISIBLE);
                    camSettingsValue.setVisibility(View.VISIBLE);
                }

            }
        });

        camSettingsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isShutterSpeedEnabled[0]) {
                    camParams.set(CameraParameters.getModeString(31), CameraParameters.getShutterSpeedString(progress));
                    camera.setParameters(camParams);
                //    camSettingsValue.setText("Shutter speed " + "\n" + CameraParameters.getShutter(progress));  ///////////////////    comment due to new method in new api //todo
                } else {
                    camParams.set(CameraParameters.getModeString(10), CameraParameters.getIsoString(progress));
                    camera.setParameters(camParams);
                    camSettingsValue.setText("ISO " + "\n" + CameraParameters.getIsoString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isShutterSpeedEnabled[0]) {
                    shutterSpeedValue[0] = seekBar.getProgress();
                } else {
                    isoValue[0] = seekBar.getProgress();
                }
            }
        });

        hdrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHdrEnabled[0]) {
                    isHdrEnabled[0] = true;
                    hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                    hdrButton.setImageResource(R.drawable.ic_hdr_on_white_48px);
                    camParams.set(CameraParameters.getModeString(12), CameraParameters.getCameraHDRString(2));
                    camera.setParameters(camParams);
                } else {
                    isHdrEnabled[0] = false;
                    camParams.set(CameraParameters.getModeString(12), CameraParameters.getCameraHDRString(0));
                    camera.setParameters(camParams);
                    hdrButton.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                    hdrButton.setImageResource(R.drawable.ic_hdr_off_white_48px);
                }
            }
        });

        layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (started) {
                    if (!isPreviewVisible[0]) {
                        surfaceView.setVisibility(View.INVISIBLE);
                        isPreviewVisible[0] = true;
                    } else {
                        surfaceView.setVisibility(View.VISIBLE);
                        isPreviewVisible[0] = false;
                    }

                }
            }
        });

    }


   /* public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        try {
            Uri treeUri = resultData.getData();
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (isSlider) {
                new ConnectBT().execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }*/

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new TakePhoto().execute();

            if (started && time / 10 < TimelapseSettingsController.settings.time * 60 && batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) > TimelapseSettingsController.settings.batteryLevel) {
                if (isSlider) {
                    if (counter < TimelapseSettingsController.settings.images) {
                        start();
                    } else {
                        stop();
                        endPreview();
                    }
                } else {
                    start();
                }

            } else {
                stop();
                endPreview();
            }

        }
    };


    public void start() {
        started = true;
        handler.postDelayed(runnable, TimelapseSettingsController.settings.speed);
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
            camera.setPreviewDisplay(holder);

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

        void takePhoto() {
            camera.startPreview();
            camera.takePicture(null, null, mCall);
        }

        Camera.PictureCallback mCall = new Camera.PictureCallback() {
            public void onPictureTaken(final byte[] data, Camera camera) {

                FileOutputStream outStream;
                try {
                    if (TimelapseSettingsController.settings.saveOnSD) {
                        DocumentFile file = TimelapseSettingsController.pickedDir.createFile("//MIME type", Integer.toString(counter) + ".jpg");
                        outStream = (FileOutputStream) getContentResolver().openOutputStream(file.getUri());
                    } else {
                        File sd = new File(Environment.getExternalStorageDirectory() + "/TimelapseData/", date);
                        if (!sd.isDirectory()) {
                            sd.mkdirs();
                        }
                        outStream = new FileOutputStream(sd + "/" + Integer.toString(counter) + ".jpg");
                    }

                    if (TimelapseSettingsController.settings.compression== 0 && (TimelapseSettingsController.settings.resoluionMenu[2] || TimelapseSettingsController.settings.resoluionMenu[3])) {
                        outStream.write(data);
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        Bitmap photo = bitmap;
                        if (TimelapseSettingsController.settings.resoluionMenu[1]) {
                            photo = createScaledBitmap(bitmap, 1920, 1080, true);
                        } else if (TimelapseSettingsController.settings.resoluionMenu[0]) {
                            photo = createScaledBitmap(bitmap, 1280, 720, true);
                        }

                        photo.compress(Bitmap.CompressFormat.JPEG, TimelapseSettingsController.settings.compression, bytes);

                        if (outStream != null) {
                            outStream.write(bytes.toByteArray());
                            outStream.close();
                        }
                    }
                    counter++;
                    time += TimelapseSettingsController.settings.speed / 100;
                    if (isSlider) {
                        send();
                    } else {
                        textView.setText(String.valueOf(counter / 30) + "s " + String.valueOf((int) (((time / 600.0f) / TimelapseSettingsController.settings.time) * 100)) + "% " + String.valueOf(time / 600) + " min");
                    }
                } catch (IOException e) {
                    Log.d("CAMM", e.getMessage());
                }

            }

        };

    }


    private void endPreview() {
        if (isSlider) {
            disconnect();
        }
        if (counter > 100 && Integer.valueOf(camera.getParameters().get(CameraParameters.getModeString(31))) < 2000000) {
            int averangeSize = (initialFreeSpace - getFreeSpace()) / (counter + 1);  // +1 !!!!!
            int maxSize;
            if (TimelapseSettingsController.settings.compression!= 100) {
                maxSize = (int) (averangeSize / (0.953 - 0.2016 * Math.log(100 - TimelapseSettingsController.settings.compression)));
            } else {
                maxSize = (int) (averangeSize / (0.953 - 0.2016 * Math.log(100 - 99.21)));
            }

            SharedPreferences settings = getSharedPreferences("SuperLapsePref", 0);
            SharedPreferences.Editor editor = settings.edit();


            editor.putInt("numberOfRecords", TimelapseSettingsController.settings.numberOfRecords + 1);

            for (int k = 0; k < TimelapseSettingsController.settings.resoluionMenu.length; k++) {
                if (TimelapseSettingsController.settings.resoluionMenu[k]) {
                    TimelapseSettingsController.settings.maxPictureSize[k] = maxSize;
                    //  editor.putInt(String.valueOf(k), maxSize);
                    editor.putLong(String.valueOf(k), TimelapseSettingsController.settings.maxPictureSize[k] + maxSize);
                    // break;
                } else {
                    editor.putLong(String.valueOf(k), 2 * TimelapseSettingsController.settings.maxPictureSize[k]);
                }
            }
            editor.apply();
        }
        finish();
    }

    private void disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void send() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("0".getBytes());
                textView.setText(String.valueOf(counter) + " " + String.valueOf((int) (((float) counter / TimelapseSettingsController.settings.images) * 100)) + "% " + String.valueOf(time / 600.0) + " min");
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
                    BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
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
                msg("Connection Failed. Try again.");
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
        endPreview();
    }

    public String getExternalSDPath() {
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
    }

    private int getFreeSpace() {
        StatFs stat;
        if (TimelapseSettingsController.settings.saveOnSD) {
            stat = new StatFs(getExternalSDPath());
        } else {
            stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        }

        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        return (int) (bytesAvailable / (1024));
    }

}

