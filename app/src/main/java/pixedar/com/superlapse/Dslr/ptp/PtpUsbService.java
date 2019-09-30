package pixedar.com.superlapse.Dslr.ptp;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import java.util.Map;

import pixedar.com.superlapse.AppConfig;

public class PtpUsbService implements PtpService {

    private final String TAG = PtpUsbService.class.getSimpleName();

    public static WorkerNotifier getWorkerNotifier() {
        return workerNotifier;
    }

    private static WorkerNotifier workerNotifier;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver permissonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                unregisterPermissionReceiver(context);
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        connect(device);
                    } else {
                        //TODO report
                    }
                }
            }
        }
    };

    private final Handler handler = new Handler();
    private final UsbManager usbManager;
    private PtpCamera camera;
    private Camera.CamerStateListener cameraStateListener;
    //  private Camera.CameraListener cameraListener;

    private Runnable shutdownRunnable = new Runnable() {
        @Override
        public void run() {
            shutdown();
        }
    };


    public PtpUsbService(Context context) {
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        workerNotifier = new WorkerNotifier(context);
    }

    public PtpUsbService getPtpUsbService() {
        return this;
    }

    @Override
    public void setCameraListener(Camera.CamerStateListener cameraStateListener) {
        this.cameraStateListener = cameraStateListener;
        //    this.cameraListener = cameraListener;
   /*     if (camera != null) {
            camera.setListener(cameraListener);
        }*/
    }


    @Override
    public void initialize(Context context, Intent intent,boolean resume) {
        handler.removeCallbacks(shutdownRunnable);
        if (camera != null&&!resume) {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: camera available");
            }
            if (camera.getState() == PtpCamera.State.Active) {
                if (cameraStateListener != null) {
                    cameraStateListener.onCameraStarted(camera);
                }
                return;
            }
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: camera not active, satet " + camera.getState());
            }
            camera.shutdownHard();
        }
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null) {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: got device through intent");
            }
            connect(device);
        } else {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: looking for compatible camera");
            }
            device = lookupCompatibleDevice(usbManager);
            if (device != null) {
                registerPermissionReceiver(context);
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                        ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, mPermissionIntent);
            } else {
                cameraStateListener.onNoCameraFound();
            }
        }
    }

    @Override
    public void shutdown() {
        if (AppConfig.LOG) {
            Log.i(TAG, "shutdown");
        }
        if (camera != null) {
            camera.shutdown();
            camera = null;
        }
    }

    @Override
    public void lazyShutdown() {
        if (AppConfig.LOG) {
            Log.i(TAG, "lazy shutdown");
        }
        handler.postDelayed(shutdownRunnable, 4000);
    }

    private void registerPermissionReceiver(Context context) {
        if (AppConfig.LOG) {
            Log.i(TAG, "register permission receiver");
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(permissonReceiver, filter);
    }

    private void unregisterPermissionReceiver(Context context) {
        if (AppConfig.LOG) {
            Log.i(TAG, "unregister permission receiver");
        }
        context.unregisterReceiver(permissonReceiver);
    }

    private UsbDevice lookupCompatibleDevice(UsbManager manager) {
        Map<String, UsbDevice> deviceList = manager.getDeviceList();
        for (Map.Entry<String, UsbDevice> e : deviceList.entrySet()) {
            UsbDevice d = e.getValue();
            if (d.getVendorId() == PtpConstants.CanonVendorId || d.getVendorId() == PtpConstants.NikonVendorId) {
                return d;
            }
        }
        return null;
    }

    private void connect(UsbDevice device) {
/*        if (camera != null) {
            camera.shutdown();
            camera = null;
        }*/
        for (int i = 0, n = device.getInterfaceCount(); i < n; ++i) {
            UsbInterface intf = device.getInterface(i);

            if (intf.getEndpointCount() != 3) {
                continue;
            }

            UsbEndpoint in = null;
            UsbEndpoint out = null;

            for (int e = 0, en = intf.getEndpointCount(); e < en; ++e) {
                UsbEndpoint endpoint = intf.getEndpoint(e);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                        in = endpoint;
                    } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        out = endpoint;
                    }
                }
            }

            if (in == null || out == null) {
                continue;
            }

            if (AppConfig.LOG) {
                Log.i(TAG, "Found compatible USB interface");
                Log.i(TAG, "Interface class " + intf.getInterfaceClass());
                Log.i(TAG, "Interface subclass " + intf.getInterfaceSubclass());
                Log.i(TAG, "Interface protocol " + intf.getInterfaceProtocol());
                Log.i(TAG, "Bulk out max size " + out.getMaxPacketSize());
                Log.i(TAG, "Bulk in max size " + in.getMaxPacketSize());
            }

            if (device.getVendorId() == PtpConstants.CanonVendorId) {
                PtpUsbConnection connection = new PtpUsbConnection(usbManager.openDevice(device), in, out,
                        device.getVendorId(), device.getProductId());
                //       camera = new EosCamera(connection, cameraStateListener, new WorkerNotifier(context));
            } else if (device.getVendorId() == PtpConstants.NikonVendorId) {
                PtpUsbConnection connection = new PtpUsbConnection(usbManager.openDevice(device), in, out,
                        device.getVendorId(), device.getProductId());
                workerNotifier.onWorkerStarted(device.getProductId());
                if(camera == null) {
                    //    camera = new NikonCamera(connection, cameraListener, new WorkerNotifier(context));
                    camera = new NikonCamera(connection, null, cameraStateListener, workerNotifier);
                }else {
                   camera.init(connection);
               }
                //      Log.d("DEBUG","SDSWD");
            }

            return;
        }

/*        if (cameraStateListener != null) {
            cameraStateListener.onError("No compatible camera found");
        }*/

    }
}

