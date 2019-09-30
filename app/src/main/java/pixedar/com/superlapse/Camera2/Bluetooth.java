package pixedar.com.superlapse.Camera2;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class Bluetooth extends AsyncTask<Void, Void, Void> {
    private boolean ConnectSuccess = true;
    private ProgressDialog progress;
    private BluetoothSocket btSocket;
    private WeakReference<Context> weakContext;
    private final int numOfAttempts = 3;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  //  private static final String address = "00:14:01:03:38:8B"; // stare hc-05 do slidera z drukarki prapowodpbnie teraz jest w stacji pogodowej
    private static final String address = "00:14:01:03:38:A5";
    private ConnectionListener  listener;
    public Bluetooth(){
    }
    @Override
    protected void onPreExecute() {
    }
    public void connect(){
        this.execute();
    }

    @Override
    protected Void doInBackground(Void... devices) {
        try {
            if (btSocket == null) {
                BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ConnectSuccess = false;
        }
        return null;
    }

    public void setConnectionLitener(ConnectionListener listener) {
        this.listener = listener;
    }
    public interface ConnectionListener{
        void connected();
        void connectionFailed();
    }
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (!ConnectSuccess) {
            if(listener !=null){
                listener.connectionFailed();
            }
        } else {
            if(listener!= null){
                listener.connected();
            }
            msg("Arduino connected");
        }
//        progress.dismiss();
    }
    private void msg(String s) {
        if(weakContext!=null) {
            Toast.makeText(weakContext.get(), s, Toast.LENGTH_LONG).show();
        }
    }
    public void disconnect() throws IOException {
        if (btSocket != null) {
                btSocket.close();
        }
    }
    public boolean send(String val)  {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(val.getBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                msg("cannot send settings:" + e.toString());
                return false;
            }
        }
        msg("cannot send settings: btSocket is null");
        return false;
    }
    public void send() throws IOException  {
        if (btSocket != null) {
             //   btSocket.getOutputStream().write("0".getBytes());
            btSocket.getOutputStream().write("757;8294;AC1".getBytes());
              //  textView.setText(String.valueOf(counter) + " " + String.valueOf((int) (((float) counter / Settings.images) * 100)) + "% " + String.valueOf(time / 600.0) + " min");
        }
    }
}
