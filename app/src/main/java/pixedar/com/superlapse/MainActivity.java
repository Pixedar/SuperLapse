package pixedar.com.superlapse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pixedar.com.superlapse.Dslr.ptp.Camera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpService;


public class MainActivity extends AppCompatActivity implements Camera.CamerStateListener {
    private TabLayout tabLayout;
    private PtpService ptp;
    private PagerAdapter adapter;
    ViewPager viewPager;
    private boolean lock = false;
    private PtpCamera camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.addTab(tabLayout.newTab().setText("Slider"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = findViewById(R.id.pager);
        adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount() + 1);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        initUsbListener();
        ptp = PtpService.Singleton.getInstance(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        int hasStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> permissions = new ArrayList<String>();

        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);

        }
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
        }
        startPtp();
    }

    @Override
    protected void onDestroy() {
        if(mUsbReceiver!=null){
            unregisterReceiver(mUsbReceiver);
        }
        super.onDestroy();
    }

    private void startPtp() {
        ptp.setCameraListener(this);
        ptp.initialize(this, getIntent(),false);
    }

    private void changeTabs() {
        if (tabLayout != null && !lock) {
            @SuppressLint("InflateParams") TextView smarphoneTab = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_title_layout, null);
            smarphoneTab.setText("Samtphone");
            smarphoneTab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_smartphone_24px, 0, 0, 0);
            Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(smarphoneTab);
            @SuppressLint("InflateParams") TextView dslrTab = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_title_layout, null);
            dslrTab.setText("Dslr"); //tab label txt
            dslrTab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_photo_camera_24px, 0, 0, 0);
            Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(dslrTab);
            tabLayout.addTab(tabLayout.newTab().setText("Servo"));
            lock = true;
        }
    }

    @Override
    public void onCameraStarted(PtpCamera camera) {
        this.camera = camera;
        adapter.setCamera(camera);
        viewPager.setAdapter(adapter);
        changeTabs();
    }

    @Override
    public void onCameraStopped(Camera camera) {

    }

    @Override
    public void onNoCameraFound() {

    }
    BroadcastReceiver mUsbReceiver;
    IntentFilter filter;
    private boolean isRegistered = false;
    private void initUsbListener() {
        if(mUsbReceiver == null) {
            // BroadcastReceiver when insert/remove the device USB plug into/from a USB port
            mUsbReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    System.out.println("BroadcastReceiver Event");
                    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                        System.out.println("BroadcastReceiver USB Connected");
                        if (camera != null && (camera.getState() == PtpCamera.State.Stopped || camera.getState() == PtpCamera.State.Error)) {
                            camera.setState(PtpCamera.State.Active);
                            ptp.initialize(MainActivity.this, getIntent(), true);
                        }
                    } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                        System.out.println("BroadcastReceiver USB Disconnected");
                    }
                }
            };
            if (!isRegistered) {
                filter = new IntentFilter();
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                this.registerReceiver(mUsbReceiver, filter);
                isRegistered = true;
            }
        }
    }
}